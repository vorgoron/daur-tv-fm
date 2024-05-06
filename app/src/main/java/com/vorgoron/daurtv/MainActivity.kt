package com.vorgoron.daurtv

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vorgoron.daurtv.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : FragmentActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var player: ExoPlayer? = null
    private var playWhenReady = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    @UnstableApi
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) {
            initPlayer()
        }
    }

    @UnstableApi
    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Build.VERSION.SDK_INT < 24 || player == null) {
            initPlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    @UnstableApi
    private fun initPlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(30000, 50000,  2500, 5000)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(5000, true)
            .build()
        val liveTargetOffsetMs = DefaultMediaSourceFactory(this)
            .setLiveMinOffsetMs(0)
            .setLiveMaxOffsetMs(50000)
            .setLiveMinSpeed(0.5f)
            .setLiveMaxSpeed(1.5f)
        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(liveTargetOffsetMs)
            .build()
            .also { exoPlayer ->
                viewBinding.playerView.player = exoPlayer
                // Create a data source factory.
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                // Create a HLS media source pointing to a playlist uri.
                val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(false)
                    .createMediaSource(MediaItem.fromUri(getString(R.string.daur_tv_hls_url)))
                exoPlayer.setMediaSource(hlsMediaSource)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(object: Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        Timber.e("error=$error")
                        FirebaseCrashlytics.getInstance().recordException(error)
                        if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                            // Re-initialize player at the current live window default position.
                            player?.seekToDefaultPosition()
                            player?.prepare()
                        } else {
                            // Handle other errors.
                        }
                    }
                })
                exoPlayer.prepare()
            }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun releasePlayer() {
        player?.run {
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return viewBinding.playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}