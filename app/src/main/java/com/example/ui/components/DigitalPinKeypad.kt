package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlin.math.roundToInt

@Composable
fun DigitalPinKeypad(
    currentPin: String,
    pinLength: Int = 4,
    isError: Boolean = false,
    showBiometricButton: Boolean = true,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
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

    fun performHaptic(durationMs: Long = 15) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        }
    }

    // Shake animation when error occurs
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            performHaptic(40)
            shakeOffset.animateElevationKeyframes()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_pin_keypad"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN dot indicators with shake effect
        Row(
            modifier = Modifier
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until pinLength) {
                val isFilled = i < currentPin.length
                val dotColor by animateColorAsState(
                    targetValue = when {
                        isError -> SemanticError
                        isFilled -> AccentIndigo
                        else -> Color.Transparent
                    },
                    animationSpec = tween(150),
                    label = "pin_dot_color"
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = 1.5.dp,
                            color = when {
                                isError -> SemanticError
                                isFilled -> AccentIndigo
                                else -> TextTertiary.copy(alpha = 0.5f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3x4 Keypad Layout
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("BIO", "0", "DEL")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (key in row) {
                        when (key) {
                            "BIO" -> {
                                if (showBiometricButton) {
                                    KeypadIconButton(
                                        icon = Icons.Default.Fingerprint,
                                        contentDesc = "Biometric Unlock",
                                        testTag = "keypad_btn_biometric",
                                        accentColor = AccentSky,
                                        onClick = {
                                            performHaptic()
                                            onBiometricClick()
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.size(68.dp))
                                }
                            }
                            "DEL" -> {
                                KeypadIconButton(
                                    icon = Icons.Default.Backspace,
                                    contentDesc = "Backspace",
                                    testTag = "keypad_btn_delete",
                                    accentColor = TextSecondary,
                                    onClick = {
                                        performHaptic()
                                        onBackspaceClick()
                                    }
                                )
                            }
                            else -> {
                                KeypadNumberButton(
                                    number = key,
                                    letters = getSubLetters(key),
                                    onClick = {
                                        performHaptic()
                                        onDigitClick(key)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun Animatable<Float, *>.animateElevationKeyframes() {
    animateTo(
        targetValue = 0f,
        animationSpec = keyframes {
            durationMillis = 350
            0f at 0
            -22f at 50
            22f at 100
            -15f at 160
            15f at 220
            -6f at 280
            0f at 350
        }
    )
}

@Composable
private fun KeypadNumberButton(
    number: String,
    letters: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(SlateSurfaceElevated)
            .border(1.dp, SlateBorderSubtle, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White)
            ) {
                onClick()
            }
            .testTag("keypad_btn_$number"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = TextTertiary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    testTag: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(SlateSurface)
            .border(1.dp, SlateBorderSubtle, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor)
            ) {
                onClick()
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun getSubLetters(digit: String): String = when (digit) {
    "2" -> "ABC"
    "3" -> "DEF"
    "4" -> "GHI"
    "5" -> "JKL"
    "6" -> "MNO"
    "7" -> "PQRS"
    "8" -> "TUV"
    "9" -> "WXYZ"
    else -> ""
}
