package com.vorgoron.daurtv

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import com.google.common.primitives.Ints.max
import com.google.common.primitives.Ints.min
import com.idapgroup.snowfall.snowfall
import com.vorgoron.daurtv.screens.RadioScreen
import com.vorgoron.daurtv.ui.theme.DaurTVTheme
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

@OptIn(ExperimentalAnimationGraphicsApi::class)
@UnstableApi
class StartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            DaurTVTheme {
                NavHost(navController = navController, startDestination = "start") {
                    composable("start") { StartScreen(navController) }
                    composable("tv") { TvScreen(navController) }
                    composable("radio") { RadioScreen.RadioScreen(uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) }
                }
            }
        }
    }

    @Composable
    private fun StartScreen(navController: NavHostController) {
        Surface(
            modifier = Modifier
                .fillMaxSize(), color = MaterialTheme.colorScheme.tertiary
        ) {
            val isNewYear = isNewYear()
            Box(
                modifier = if (isNewYear) Modifier.snowfall(density = 0.02) else Modifier
            ) {
                val configuration = LocalConfiguration.current
                when (configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MainIcon(isNewYear)
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                StartContent(navController)
                            }
                        }
                    }

                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                MainIcon(isNewYear)
                                StartContent(navController)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MainIcon(isNewYear: Boolean) {
        val configuration = LocalConfiguration.current
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        val minSize = min(configuration.smallestScreenWidthDp,
            max(configuration.screenWidthDp, configuration.screenHeightDp) / 2)
        Box {
            Image(
                painter = painterResource(id = R.drawable.logo_main),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(minSize.dp)
            )
            if (isNewYear) {
                Image(
                    painter = painterResource(id = R.drawable.tol_babay),
                    contentDescription = "izy",
                    modifier = Modifier.absoluteOffset(
                        x = (minSize * 0.59).dp,
                        y = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                            && uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION)
                            (minSize * 0.19).dp
                        else
                            (minSize * 0.25).dp
                    )
                )
            }
        }
    }

    @Composable
    private fun StartContent(
        navController: NavHostController) {
        StartButton(R.drawable.logo_tv_button, R.string.watch) {
            navController.navigate("tv")
            showSystemUi(false)
        }
        Spacer(modifier = Modifier.size(12.dp))
        StartButton(R.drawable.logo_fm_button, R.string.listen) { navController.navigate("radio") }
    }

    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    private fun StartButton(
        @DrawableRes logo: Int,
        @StringRes text: Int,
        onClick: () -> Unit
    ) {
        val content: @Composable() (RowScope.() -> Unit) = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = logo),
                    contentDescription = stringResource(R.string.daur_fm),
                    modifier = Modifier.size(35.dp)
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = text),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                )
            }
        }
        val modifier = Modifier.width(155.dp)
        val contentPadding = PaddingValues(5.dp)
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            androidx.tv.material3.Button(
                onClick = onClick,
                modifier = modifier,
                contentPadding = contentPadding,
                glow = androidx.tv.material3.ButtonDefaults.glow(
                    focusedGlow = Glow(
                        elevation = 5.dp,
                        elevationColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ),
                colors = androidx.tv.material3.ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                ),
                content = content
            )
        } else {
            Button(
                onClick = onClick,
                modifier = modifier,
                contentPadding = contentPadding,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),
                content = content
            )
        }
    }


    private var webView: WebView? = null
    private var pageLoadingCompleteTimer: Timer? = null

    @Composable
    fun TvScreen(navController: NavHostController) {
        val tvUrl = "https://vk.com/video_ext.php?oid=-63121683&id=456241224&hd=4"
        AndroidView(factory = {
            webView = WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        pageLoadingCompleteTimer = Timer()
                        pageLoadingCompleteTimer?.schedule(object : TimerTask() {
                            override fun run() {
                                // hide vk video
                                runOnUiThread { evaluateJavascript("""document.getElementsByClassName("videoplayer_btn_vk_video")[0].style.display = "none";""") {} }
                            }
                        }, 50)
                    }
                }
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.pluginState = WebSettings.PluginState.ON
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                isClickable = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requestPointerCapture()
                }

                loadUrl(tvUrl)
            }
            webView!!
        }, update = {
            it.loadUrl(tvUrl)
        })

        BackHandler {
            pageLoadingCompleteTimer?.cancel()
            pageLoadingCompleteTimer = null
            webView?.destroy()
            webView = null
            navController.popBackStack()
            showSystemUi(true)
        }
    }

    private val mOverrideKeyCodes = mapOf(
        KeyEvent.KEYCODE_DPAD_CENTER to 32,
        KeyEvent.KEYCODE_DPAD_UP to 38,
        KeyEvent.KEYCODE_DPAD_LEFT to 37,
        KeyEvent.KEYCODE_DPAD_DOWN to 40,
        KeyEvent.KEYCODE_DPAD_RIGHT to 39
    )

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        webView?.let { webView ->
            val eventKeyCode: Int = event.keyCode
            mOverrideKeyCodes[eventKeyCode]?.let { keyboardKeyCode ->
                if (event.action == KeyEvent.ACTION_UP) {
                    webView.evaluateJavascript(
                        """
                        var keydown = new KeyboardEvent("keydown", {
                            "view": window,
                            "bubbles": true,
                            "keyCode": $keyboardKeyCode
                        });
                        document.getElementsByClassName("videoplayer")[0].dispatchEvent(keydown)
                    """.trimIndent()
                    ) {
                        Timber.d("res=$it")
                    }
                }
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    @SuppressLint("InlinedApi")
    private fun showSystemUi(show: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, show)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            if (show) {
                controller.show(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

}