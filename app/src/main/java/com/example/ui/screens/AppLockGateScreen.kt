package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.LockType
import com.example.ui.components.DigitalPinKeypad
import com.example.ui.components.PatternLockView
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockGateScreen(
    currentLockType: LockType,
    hasPinConfigured: Boolean,
    hasPatternConfigured: Boolean,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onVerifyPin: (String) -> Boolean,
    onVerifyPattern: (List<Int>) -> Boolean,
    onTriggerBiometric: () -> Unit,
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeMode by remember(currentLockType) { mutableStateOf(currentLockType) }
    var currentPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isErrorActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Auto-prompt biometric if enabled on screen entry
    LaunchedEffect(Unit) {
        if (isBiometricAvailable && isBiometricEnabled) {
            delay(300)
            onTriggerBiometric()
        }
    }

    fun triggerError(msg: String) {
        errorMessage = msg
        isErrorActive = true
        scope.launch {
            delay(1200)
            isErrorActive = false
            currentPinInput = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_lock_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Clean Security Shield
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SlateSurfaceElevated)
                        .border(1.dp, SlateBorderCrisp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App Locked",
                        tint = AccentSky,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LinkFlow Locked",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = errorMessage != null && isErrorActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = SemanticError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (errorMessage == null || !isErrorActive) {
                    Text(
                        text = if (activeMode == LockType.PIN) "Enter your security PIN" else "Draw your pattern to unlock",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Center: Active Lock UI (PIN Keypad or Pattern Canvas)
            AnimatedContent(
                targetState = activeMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "lock_mode_switch"
            ) { mode ->
                when (mode) {
                    LockType.PIN -> {
                        DigitalPinKeypad(
                            currentPin = currentPinInput,
                            pinLength = 4,
                            isError = isErrorActive,
                            showBiometricButton = isBiometricAvailable && isBiometricEnabled,
                            onDigitClick = { digit ->
                                if (currentPinInput.length < 4 && !isErrorActive) {
                                    val updated = currentPinInput + digit
                                    currentPinInput = updated
                                    if (updated.length == 4) {
                                        val valid = onVerifyPin(updated)
                                        if (valid) {
                                            onUnlockSuccess()
                                        } else {
                                            triggerError("Incorrect PIN")
                                        }
                                    }
                                }
                            },
                            onBackspaceClick = {
                                if (currentPinInput.isNotEmpty() && !isErrorActive) {
                                    currentPinInput = currentPinInput.dropLast(1)
                                }
                            },
                            onBiometricClick = onTriggerBiometric
                        )
                    }
                    LockType.PATTERN -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PatternLockView(
                                isError = isErrorActive,
                                enabled = !isErrorActive,
                                onPatternEntered = { nodes ->
                                    if (nodes.size < 4) {
                                        triggerError("Connect at least 4 dots")
                                    } else {
                                        val valid = onVerifyPattern(nodes)
                                        if (valid) {
                                            onUnlockSuccess()
                                        } else {
                                            triggerError("Incorrect pattern")
                                        }
                                    }
                                }
                            )

                            if (isBiometricAvailable && isBiometricEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SlateSurfaceElevated)
                                        .border(1.dp, SlateBorderSubtle, RoundedCornerShape(20.dp))
                                        .clickable { onTriggerBiometric() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Use Biometric",
                                        tint = AccentSky,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Use Fingerprint",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Switcher: Clean Segmented Pill
            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SlateSurface)
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // PIN button
                val isPinSelected = activeMode == LockType.PIN
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isPinSelected) AccentIndigo else Color.Transparent)
                        .clickable {
                            activeMode = LockType.PIN
                            currentPinInput = ""
                            isErrorActive = false
                            errorMessage = null
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pin,
                        contentDescription = "PIN Mode",
                        tint = if (isPinSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PIN",
                        color = if (isPinSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isPinSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Pattern button
                val isPatternSelected = activeMode == LockType.PATTERN
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isPatternSelected) AccentIndigo else Color.Transparent)
                        .clickable {
                            activeMode = LockType.PATTERN
                            currentPinInput = ""
                            isErrorActive = false
                            errorMessage = null
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pattern,
                        contentDescription = "Pattern Mode",
                        tint = if (isPatternSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pattern",
                        color = if (isPatternSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isPatternSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
