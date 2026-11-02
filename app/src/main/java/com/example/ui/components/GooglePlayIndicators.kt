package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AccentVioletLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * -----------------------------------------------------------------------------------------
 * VIDEO 1: GOOGLE PLAY STYLE MORPHING SHAPE PULL-TO-REFRESH INDICATOR
 * -----------------------------------------------------------------------------------------
 * Continuously and smoothly morphs between:
 * 1. Rounded Pentagon (5 vertices)
 * 2. Scalloped Daisy / Gear (12 rounded lobes)
 * 3. 4-lobed Squircle / Clover (4 rounded corners)
 * 4. 10-pointed wavy flower / star
 * 5. Smooth Oval / Pebble (2 lobes)
 * 6. 8-lobed Flower / Asterisk
 *
 * Designed with transparent background, centered in the middle of the screen like Google Play.
 */
@Composable
fun GooglePlayMorphingShape(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = Color.White,
    gradientColors: List<Color>? = null,
    isAnimating: Boolean = true,
    manualFraction: Float? = null // For manual pull progress (0f..1f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "morphing_shape_transition")

    // Continuous 0..6 morph cycle across the 6 keyframe shapes
    val shapeCycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shape_morph_cycle"
    )

    // Smooth continuous rotation
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_rot"
    )

    // Subtle breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val currentCycle = if (manualFraction != null) {
        manualFraction * 6f
    } else {
        shapeCycleProgress
    }

    val currentRotation = if (manualFraction != null) {
        manualFraction * 270f
    } else {
        continuousRotation
    }

    val scaleFactor = if (manualFraction != null) {
        manualFraction.coerceIn(0.2f, 1f)
    } else {
        if (isAnimating) breathingScale else 1.0f
    }

    Canvas(
        modifier = modifier
            .size(size)
            .scale(scaleFactor)
            .rotate(currentRotation)
    ) {
        val centerX = size.toPx() / 2f
        val centerY = size.toPx() / 2f
        val baseRadius = (size.toPx() / 2f) * 0.72f

        val path = buildMorphingPath(
            centerX = centerX,
            centerY = centerY,
            baseRadius = baseRadius,
            cycle = currentCycle
        )

        if (gradientColors != null && gradientColors.size >= 2) {
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(size.toPx(), size.toPx())
                ),
                style = Fill
            )
        } else {
            drawPath(
                path = path,
                color = color,
                style = Fill
            )
        }
    }
}

/**
 * Builds the mathematical morphing polygon/flower path by interpolating
 * adjacent harmonic shape profiles.
 */
private fun buildMorphingPath(
    centerX: Float,
    centerY: Float,
    baseRadius: Float,
    cycle: Float
): Path {
    val totalKeyframes = 6
    val normalizedCycle = (cycle % totalKeyframes).let { if (it < 0f) it + totalKeyframes else it }
    val index1 = normalizedCycle.toInt() % totalKeyframes
    val index2 = (index1 + 1) % totalKeyframes
    val fraction = normalizedCycle - normalizedCycle.toInt()

    // Smooth S-curve easing between keyframes for organic fluid morphing
    val smoothAlpha = (1f - cos(fraction * PI.toFloat())) / 2f

    val path = Path()
    val steps = 120 // High resolution for silky rounded lobes
    val stepAngle = (2 * PI / steps).toFloat()

    for (i in 0 until steps) {
        val theta = i * stepAngle

        val r1 = calculateShapeRadius(index1, theta, baseRadius)
        val r2 = calculateShapeRadius(index2, theta, baseRadius)
        val r = (1f - smoothAlpha) * r1 + smoothAlpha * r2

        val x = centerX + r * cos(theta.toDouble()).toFloat()
        val y = centerY + r * sin(theta.toDouble()).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

/**
 * Harmonic radius formulas corresponding to the 6 shapes from Video 1:
 * 0: Rounded Pentagon (5 vertices)
 * 1: Scalloped Daisy / Gear (12 lobes)
 * 2: 4-lobed Squircle / Clover (4 corners)
 * 3: 10-pointed Flower / Star (10 lobes)
 * 4: Smooth Oval / Pebble (2 lobes)
 * 5: 8-lobed Flower (8 lobes)
 */
private fun calculateShapeRadius(shapeIndex: Int, theta: Float, baseRadius: Float): Float {
    return when (shapeIndex) {
        0 -> { // Rounded Pentagon
            baseRadius * (1f + 0.18f * cos(5f * theta.toDouble()).toFloat() - 0.04f * cos(10f * theta.toDouble()).toFloat())
        }
        1 -> { // 12-lobed Daisy / Gear
            baseRadius * (1f + 0.12f * cos(12f * theta.toDouble()).toFloat())
        }
        2 -> { // 4-lobed Clover / Squircle
            baseRadius * (1f + 0.22f * cos(4f * theta.toDouble()).toFloat() - 0.05f * cos(8f * theta.toDouble()).toFloat())
        }
        3 -> { // 10-pointed Flower
            baseRadius * (1f + 0.14f * cos(10f * theta.toDouble()).toFloat())
        }
        4 -> { // Smooth Oval / Pebble
            baseRadius * (1f + 0.28f * cos(2f * theta.toDouble()).toFloat())
        }
        5 -> { // 8-lobed Flower
            baseRadius * (1f + 0.16f * cos(8f * theta.toDouble()).toFloat())
        }
        else -> baseRadius
    }
}

/**
 * Pull-To-Refresh Layout using the Video 1 Google Play Morphing Shape.
 * Has a transparent background and sits in the middle / upper-middle of the screen.
 */
@Composable
fun GooglePlayPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val pullThresholdPx = with(density) { 90.dp.toPx() }

    var pullOffsetPx by remember { mutableFloatStateOf(0f) }

    // When refreshing ends, reset pull offset
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullOffsetPx = 0f
        }
    }

    val animatedOffsetPx by animateFloatAsState(
        targetValue = if (isRefreshing) with(density) { 60.dp.toPx() } else pullOffsetPx,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "pull_offset_spring"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                state = rememberDraggableState { delta ->
                    if (!isRefreshing) {
                        val newOffset = (pullOffsetPx + delta * 0.5f).coerceIn(0f, pullThresholdPx * 1.5f)
                        pullOffsetPx = newOffset
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = {
                    if (pullOffsetPx >= pullThresholdPx && !isRefreshing) {
                        onRefresh()
                    } else {
                        pullOffsetPx = 0f
                    }
                }
            )
    ) {
        // Child content
        content()

        // Pull to refresh indicator: transparent background, middle of the screen (small like Google Play)
        val pullFraction = (pullOffsetPx / pullThresholdPx).coerceIn(0f, 1f)
        val isVisible = isRefreshing || pullOffsetPx > 10f

        if (isVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, animatedOffsetPx.roundToInt()) }
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Transparent background floating morphing indicator (small like Google Play)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = AccentViolet.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(Color(0xCC131A2A)) // Ultra-subtle translucent dark disc
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GooglePlayMorphingShape(
                        size = 34.dp,
                        color = Color.White,
                        gradientColors = listOf(Color.White, Color(0xFFE2E8F0)),
                        isAnimating = isRefreshing,
                        manualFraction = if (isRefreshing) null else pullFraction
                    )
                }
            }
        }
    }
}

/**
 * -----------------------------------------------------------------------------------------
 * VIDEO 2: GOOGLE PLAY STYLE SQUIGGLY CIRCULAR ARC LOADER
 * -----------------------------------------------------------------------------------------
 * Small, transparent background, placed in the middle of the screen like Google Play.
 * A sinusoidal squiggly ripple undulates and rotates around a circular arc,
 * expanding and contracting smoothly.
 */
@Composable
fun GooglePlaySquigglyLoader(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    strokeColor: Color = Color(0xFF93C5FD), // Soft celestial blue as shown in Video 2
    accentColor: Color = AccentVioletLight,
    strokeWidth: Dp = 2.8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "squiggly_loader_transition")

    // Continuous 360 degree rotation of the arc
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "squiggly_rot"
    )

    // Indeterminate sweep contraction and expansion (from 40 deg to 280 deg)
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 45f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "squiggly_sweep"
    )

    // Wave ripple travelling phase along the arc
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotationAngle)
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val centerX = size.toPx() / 2f
        val centerY = size.toPx() / 2f
        val baseRadius = (size.toPx() / 2f) - strokeWidthPx - 3.dp.toPx()
        val waveAmplitude = 2.2.dp.toPx()
        val waveFrequency = 18f // 18 ripples around 360 deg

        val path = Path()
        val startRad = 0.0
        val sweepRad = (sweepAngle * PI / 180.0)
        val steps = (sweepAngle * 0.9f).toInt().coerceAtLeast(15)

        for (i in 0..steps) {
            val fraction = i.toDouble() / steps
            val currentAngleRad = startRad + fraction * sweepRad

            // Calculate sinusoidal ripple along the radius
            val ripple = waveAmplitude * sin(waveFrequency * currentAngleRad + wavePhase.toDouble()).toFloat()
            val r = baseRadius + ripple

            val x = centerX + r * cos(currentAngleRad).toFloat()
            val y = centerY + r * sin(currentAngleRad).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                colors = listOf(
                    strokeColor.copy(alpha = 0.2f),
                    strokeColor,
                    accentColor,
                    strokeColor
                ),
                center = Offset(centerX, centerY)
            ),
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Centered Loading Overlay (Google Play Style):
 * - Transparent background
 * - In the middle of the screen
 * - Small like Google Play style
 * - Seamlessly displays the squiggly loader from Video 2
 */
@Composable
fun GooglePlayLoadingOverlay(
    isLoading: Boolean,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.85f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.85f),
        modifier = modifier.fillMaxSize()
    ) {
        // Transparent background, centered in the middle of the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.6f),
                        spotColor = AccentViolet.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xDD0F1422)) // Subtly translucent Google Play style card
                    .padding(horizontal = 22.dp, vertical = 18.dp)
                    .testTag("google_play_loading_box")
            ) {
                // Video 2 Squiggly Loader
                GooglePlaySquigglyLoader(
                    size = 44.dp,
                    strokeColor = Color(0xFF93C5FD),
                    accentColor = AccentVioletLight,
                    strokeWidth = 3.dp
                )

                if (!message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
