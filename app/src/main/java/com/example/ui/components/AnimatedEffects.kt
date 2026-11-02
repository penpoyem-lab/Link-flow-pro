package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SlateSurfaceElevated

/**
 * Animated real-time frequency spectrum visualizer bars.
 * Dances smoothly during analyzing, downloading, or playback preview.
 */
@Composable
fun AnimatedWaveformVisualizer(
    isActive: Boolean = true,
    barCount: Int = 9,
    modifier: Modifier = Modifier,
    activeColor: Color = AccentCyan,
    inactiveColor: Color = SlateSurfaceElevated
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    // Provide staggered phase animations for natural organic movement
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val multipliers = listOf(0.4f, 0.7f, 1.0f, 0.6f, 0.9f, 0.5f, 0.85f, 0.45f, 0.65f)

        for (i in 0 until barCount) {
            val weight = multipliers[i % multipliers.size]
            val heightFraction = if (!isActive) 0.25f else {
                when (i % 3) {
                    0 -> (phase1 * weight).coerceIn(0.18f, 1.0f)
                    1 -> (phase2 * weight).coerceIn(0.18f, 1.0f)
                    else -> (phase3 * weight).coerceIn(0.18f, 1.0f)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive) {
                            Brush.verticalGradient(
                                colors = listOf(AccentCyan, AccentSky, AccentIndigo)
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(inactiveColor, inactiveColor)
                            )
                        }
                    )
            )
        }
    }
}

/**
 * Fluid Shimmer Progress Bar with illuminated specular wave across the fill.
 */
@Composable
fun AnimatedShimmerProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    trackColor: Color = Color(0xFF1E283E),
    gradientColors: List<Color> = listOf(AccentIndigo, AccentSky, AccentCyan)
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_anim")

    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
    ) {
        val totalWidth = size.width
        val corner = CornerRadius(size.height / 2, size.height / 2)

        // Draw track background
        drawRoundRect(
            color = trackColor,
            size = size,
            cornerRadius = corner
        )

        // Draw filled progress
        if (clampedProgress > 0f) {
            val fillWidth = totalWidth * clampedProgress

            // Base progress gradient
            val baseGradient = Brush.horizontalGradient(
                colors = gradientColors,
                startX = 0f,
                endX = fillWidth
            )
            drawRoundRect(
                brush = baseGradient,
                size = Size(fillWidth, size.height),
                cornerRadius = corner
            )

            // Specular sheen sweep over the filled portion
            val shimmerStart = (shimmerTranslate * fillWidth)
            val shimmerWidth = fillWidth * 0.45f
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.0f),
                    Color.White.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.0f)
                ),
                start = Offset(shimmerStart, 0f),
                end = Offset(shimmerStart + shimmerWidth, 0f)
            )

            drawRoundRect(
                brush = shimmerBrush,
                size = Size(fillWidth, size.height),
                cornerRadius = corner
            )
        }
    }
}

/**
 * Animated Ambient Gradient Brush for buttons, cards, and header banners.
 */
@Composable
fun rememberAnimatedGradientBrush(
    colors: List<Color> = listOf(AccentIndigo, AccentSky, Color(0xFF7C3AED), AccentIndigo),
    durationMs: Int = 3000
): Brush {
    val transition = rememberInfiniteTransition(label = "gradient_brush_anim")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(offset, 0f),
        end = Offset(offset + 600f, 600f)
    )
}
