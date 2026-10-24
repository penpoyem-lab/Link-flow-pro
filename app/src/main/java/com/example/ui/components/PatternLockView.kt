package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlin.math.hypot

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    onPatternEntered: (List<Int>) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    fun performDotHaptic() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        }
    }

    val activeLineColor = if (isError) SemanticError else AccentIndigo

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(20.dp)
            .testTag("pattern_lock_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedNodes.clear()
                            currentTouchPoint = offset
                            val dotIndex = findDotIndexAtOffset(offset, size.width.toFloat(), size.height.toFloat())
                            if (dotIndex != null) {
                                selectedNodes.add(dotIndex)
                                performDotHaptic()
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentTouchPoint = change.position
                            val dotIndex = findDotIndexAtOffset(change.position, size.width.toFloat(), size.height.toFloat())
                            if (dotIndex != null && !selectedNodes.contains(dotIndex)) {
                                selectedNodes.add(dotIndex)
                                performDotHaptic()
                            }
                        },
                        onDragEnd = {
                            currentTouchPoint = null
                            if (selectedNodes.isNotEmpty()) {
                                onPatternEntered(selectedNodes.toList())
                            }
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedNodes.clear()
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val cellW = w / 3f
            val cellH = h / 3f

            fun getNodeCenter(index: Int): Offset {
                val row = index / 3
                val col = index % 3
                return Offset(
                    x = col * cellW + cellW / 2f,
                    y = row * cellH + cellH / 2f
                )
            }

            val strokeWidth = 4.dp.toPx()

            // Draw connecting lines between already visited nodes
            if (selectedNodes.size > 1) {
                for (i in 0 until selectedNodes.size - 1) {
                    val p1 = getNodeCenter(selectedNodes[i])
                    val p2 = getNodeCenter(selectedNodes[i + 1])
                    drawLine(
                        color = activeLineColor,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw trailing line to current finger
            if (selectedNodes.isNotEmpty() && currentTouchPoint != null && !isError) {
                val lastNodeCenter = getNodeCenter(selectedNodes.last())
                drawLine(
                    color = AccentIndigo.copy(alpha = 0.7f),
                    start = lastNodeCenter,
                    end = currentTouchPoint!!,
                    strokeWidth = strokeWidth * 0.85f,
                    cap = StrokeCap.Round
                )
            }

            // Draw 3x3 dots
            for (i in 0 until 9) {
                val center = getNodeCenter(i)
                val isSelected = selectedNodes.contains(i)

                val baseDotRadius = 6.dp.toPx()
                val selectedDotRadius = 8.dp.toPx()
                val activeRingRadius = 22.dp.toPx()

                if (isSelected) {
                    val ringColor = if (isError) SemanticError else AccentIndigo

                    // Active outer guide ring
                    drawCircle(
                        color = ringColor.copy(alpha = 0.25f),
                        radius = activeRingRadius,
                        center = center
                    )
                    drawCircle(
                        color = ringColor,
                        radius = activeRingRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Solid inner core
                    drawCircle(
                        color = if (isError) SemanticError else Color.White,
                        radius = selectedDotRadius,
                        center = center
                    )
                } else {
                    // Inactive dot
                    drawCircle(
                        color = TextTertiary.copy(alpha = 0.35f),
                        radius = activeRingRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = TextTertiary,
                        radius = baseDotRadius,
                        center = center
                    )
                }
            }
        }
    }

    LaunchedEffect(isError) {
        if (isError) {
            delay(800)
            selectedNodes.clear()
            currentTouchPoint = null
        }
    }
}

private fun findDotIndexAtOffset(offset: Offset, width: Float, height: Float): Int? {
    val cellW = width / 3f
    val cellH = height / 3f
    val hitRadius = cellW * 0.36f

    for (row in 0..2) {
        for (col in 0..2) {
            val centerX = col * cellW + cellW / 2f
            val centerY = row * cellH + cellH / 2f
            val dist = hypot(offset.x - centerX, offset.y - centerY)
            if (dist <= hitRadius) {
                return row * 3 + col
            }
        }
    }
    return null
}
