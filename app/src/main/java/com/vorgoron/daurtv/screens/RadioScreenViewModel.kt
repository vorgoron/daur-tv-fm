package com.vorgoron.daurtv.screens

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.vorgoron.daurtv.PlayerUiState
import com.vorgoron.daurtv.R
import com.vorgoron.daurtv.service.MediaRadioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.concurrent.Executors

private const val RADIO_STATION_URL_KEY = "radio_station_url"

@UnstableApi
class RadioScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            isPlaying = false,
            isLoading = false,
            title = null,
            error = null
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var radioStationUrl: String
    private var mediaServiceController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>
    private var executorService = Executors.newCachedThreadPool()

    init {
        radioStationUrl = Firebase.remoteConfig.getString(RADIO_STATION_URL_KEY)
        Firebase.remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener(ContextCompat.getMainExecutor(application)) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    val newRadioStationUrl = Firebase.remoteConfig.getString(RADIO_STATION_URL_KEY)
                    if (newRadioStationUrl != radioStationUrl) {
                        setMediaItem(application.packageName, newRadioStationUrl)
                    }
                    Timber.d("Config params updated: $updated, radio_station_url: $newRadioStationUrl")
                } else {
                    Timber.d("Remote config fetch failed.")
                }
            }

        val sessionToken =
            SessionToken(application, ComponentName(application, MediaRadioService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            // MediaController is available here with controllerFuture.get()
            mediaServiceController = controllerFuture.get()

            mediaServiceController?.let { mediaController ->
                _uiState.update {
                    it.copy(
                        isPlaying = mediaController.isPlaying,
                        title = mediaController.mediaMetadata.title?.toString()
                    )
                }

                if (!mediaController.isPlaying) {
                    setMediaItem(application.packageName, radioStationUrl)
                }

                mediaController.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _uiState.update {
                            it.copy(isPlaying = isPlaying)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_IDLE, Player.STATE_READY -> {
                                _uiState.update {
                                    it.copy(isLoading = false)
                                }
                            }

                            Player.STATE_BUFFERING -> {
                                _uiState.update {
                                    it.copy(isLoading = true)
                                }
                            }
                        }
                    }

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        _uiState.update {
                            it.copy(isLoading = isLoading)
                        }
                    }

                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                        _uiState.update {
                            it.copy(title = mediaMetadata.title?.toString() ?: "")
                        }
                    }

                    override fun onPlayerErrorChanged(error: PlaybackException?) {
                        _uiState.update {
                            var playing = it.isPlaying
                            if (error != null)
                                playing = false
                            it.copy(
                                error = error?.localizedMessage ?: error?.message ?: error?.errorCodeName ?: "",
                                isPlaying = playing,
                                isLoading = false
                            )
                        }
                    }
                })
            }
        }, MoreExecutors.directExecutor())
    }

    @Synchronized
    private fun setMediaItem(packageName: String?, stationUrl: String) {
        radioStationUrl = stationUrl
        Timber.d("radioStationUrl=$stationUrl")
        val artworkUri =
            Uri.parse("android.resource://" + packageName + "/" + R.drawable.artwork)
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(stationUrl))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtworkUri(artworkUri)
                    .build()
            )
            .build()
        mediaServiceController?.apply {
            setMediaItem(mediaItem)
            prepare()
        }
    }

    override fun onCleared() {
        super.onCleared()
        executorService.shutdown()
        controllerFuture.let { MediaController.releaseFuture(it) }
        mediaServiceController?.release()
    }

    fun play() {
        mediaServiceController?.play()
    }

    fun pause() {
        mediaServiceController?.pause()
    }
}