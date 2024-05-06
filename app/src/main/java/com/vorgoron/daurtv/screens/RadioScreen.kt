package com.vorgoron.daurtv.screens

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.google.common.primitives.Ints
import com.idapgroup.snowfall.snowfall
import com.vorgoron.daurtv.R
import com.vorgoron.daurtv.isNewYear

@ExperimentalAnimationGraphicsApi
@UnstableApi
object RadioScreen {
    @Composable
    fun RadioScreen(isTv: Boolean, radioViewModel: RadioScreenViewModel = viewModel()) {
        val uiState by radioViewModel.uiState.collectAsState()
        val isNewYear = isNewYear()
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.tertiary
        ) {
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
                            Box(modifier = Modifier.weight(1f), Alignment.Center) {
                                if (uiState.isPlaying) {
                                    RippleAnimation(
                                        color = MaterialTheme.colorScheme.primary,
                                        maxRadius = configuration.smallestScreenWidthDp / 2
                                    )
                                }
                                FmIcon(configuration, isNewYear, isTv)
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioScreenPlayerControl(radioViewModel)
                            }
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = Modifier.weight(1f), Alignment.Center) {
                                if (uiState.isPlaying) {
                                    RippleAnimation(
                                        color = MaterialTheme.colorScheme.primary,
                                        maxRadius = configuration.smallestScreenWidthDp / 2
                                    )
                                }
                                FmIcon(configuration, isNewYear, isTv)
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioScreenPlayerControl(radioViewModel)
                            }
                        }


                    }
                }
            }
        }
    }

    @Composable
    private fun FmIcon(configuration: Configuration, isNewYear: Boolean, isTv: Boolean) {
        val minSize = Ints.min(
            configuration.smallestScreenWidthDp,
            Ints.max(configuration.screenWidthDp, configuration.screenHeightDp) / 2
        )
        Box {
            Image(
                painter = painterResource(id = R.drawable.logo_fm),
                contentDescription = stringResource(R.string.daur_fm),
                modifier = Modifier.size(minSize.dp)
            )
            if (isNewYear) {
                Image(
                    painter = painterResource(id = R.drawable.tol_babay),
                    contentDescription = "izy",
                    modifier = Modifier.absoluteOffset(
                        x = (minSize * 0.59).dp,
                        y = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                            && !isTv)
                            (minSize * 0.19).dp
                        else
                            (minSize * 0.25).dp
                    )
                )
            }
        }
    }

    @Composable
    private fun RippleAnimation(
        animDuration: Int = 5000,
        rippleCount: Int = 5,
        color: Color = Color.Red,
        maxRadius: Int = 500
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "Ripple")
        val radiuses = mutableListOf<Dp>()
        val alphas = mutableListOf<Float>()
        for (i in 0..rippleCount) {
            val initialStartOffset =
                StartOffset((i * animDuration.toFloat() / rippleCount.toFloat()).toInt())
            val radius by infiniteTransition.animateValue(
                label = "radius",
                initialValue = 0.dp,
                targetValue = maxRadius.dp,
                typeConverter = Dp.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = LinearEasing),
                    initialStartOffset = initialStartOffset
                ),
            )
            radiuses.add(radius)

            val alpha by infiniteTransition.animateFloat(
                label = "alpha",
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration), initialStartOffset = initialStartOffset
                ),
            )
            alphas.add(alpha)
        }

        Canvas(modifier = Modifier.size(100.dp), onDraw = {
            for (i in 0..rippleCount) {
                drawCircle(color = color, radius = radiuses[i].toPx(), alpha = alphas[i])
            }
        })
    }

    @Composable
    private fun RadioScreenPlayerControl(radioViewModel: RadioScreenViewModel = viewModel()) {
        val uiState by radioViewModel.uiState.collectAsState()
        val image = AnimatedImageVector.animatedVectorResource(R.drawable.play_to_pause)

        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = uiState.title ?: "",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .weight(0.3f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
            FloatingActionButton(
                shape = CircleShape,
                modifier = Modifier.size(90.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                onClick = {
                    if (!uiState.isLoading) {
                        if (!uiState.isPlaying) radioViewModel.play()
                        else radioViewModel.pause()
                    }
                }) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(42.dp),
                        color = Color.White,
                    )
                } else {
                    Image(
                        painter = rememberAnimatedVectorPainter(
                            animatedImageVector = image,
                            atEnd = uiState.isPlaying,
                        ), contentDescription = "play",
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Text(
                text = uiState.error ?: "",
                style = TextStyle(
                    fontSize = 16.sp, color = Color.Red, textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .weight(0.3f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}