package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.security.LockType
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockSetupDialog(
    initialMode: String = "PIN", // "PIN" or "PATTERN"
    onSavePin: (String) -> Unit,
    onSavePattern: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentType by remember { mutableStateOf(if (initialMode == "PATTERN") LockType.PATTERN else LockType.PIN) }
    var step by remember { mutableStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    var firstPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isErrorActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun triggerError(msg: String) {
        errorMessage = msg
        isErrorActive = true
        scope.launch {
            delay(1200)
            isErrorActive = false
            currentPinInput = ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(SlateSurface)
                .border(
                    width = 1.dp,
                    color = SlateBorderCrisp,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
                .testTag("app_lock_setup_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Bar with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AccentIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Set Up AppLock",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_setup_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector: PIN vs Pattern (if on step 1)
                if (step == 1) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurfaceVariant)
                            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        val isPin = currentType == LockType.PIN
                        Button(
                            onClick = {
                                currentType = LockType.PIN
                                currentPinInput = ""
                                firstPin = ""
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPin) AccentIndigo else Color.Transparent,
                                contentColor = if (isPin) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("setup_tab_pin")
                        ) {
                            Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Digital PIN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        val isPattern = currentType == LockType.PATTERN
                        Button(
                            onClick = {
                                currentType = LockType.PATTERN
                                firstPattern = emptyList()
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPattern) AccentIndigo else Color.Transparent,
                                contentColor = if (isPattern) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("setup_tab_pattern")
                        ) {
                            Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pattern Lock", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Step Instructions & Errors
                val titleText = when {
                    currentType == LockType.PIN && step == 1 -> "Enter 4-Digit PIN"
                    currentType == LockType.PIN && step == 2 -> "Re-enter PIN to Confirm"
                    currentType == LockType.PATTERN && step == 1 -> "Draw Pattern (at least 4 dots)"
                    else -> "Draw Pattern Again to Confirm"
                }

                Text(
                    text = titleText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                AnimatedVisibility(
                    visible = errorMessage != null && isErrorActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = SemanticError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Component (PIN or Pattern)
                AnimatedContent(
                    targetState = currentType,
                    label = "setup_input_switch"
                ) { type ->
                    when (type) {
                        LockType.PIN -> {
                            DigitalPinKeypad(
                                currentPin = currentPinInput,
                                pinLength = 4,
                                isError = isErrorActive,
                                showBiometricButton = false,
                                onDigitClick = { digit ->
                                    if (currentPinInput.length < 4 && !isErrorActive) {
                                        val updated = currentPinInput + digit
                                        currentPinInput = updated
                                        if (updated.length == 4) {
                                            if (step == 1) {
                                                firstPin = updated
                                                currentPinInput = ""
                                                step = 2
                                            } else {
                                                if (updated == firstPin) {
                                                    onSavePin(updated)
                                                    onDismiss()
                                                } else {
                                                    triggerError("PINs do not match. Try again.")
                                                    step = 1
                                                    firstPin = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                onBackspaceClick = {
                                    if (currentPinInput.isNotEmpty() && !isErrorActive) {
                                        currentPinInput = currentPinInput.dropLast(1)
                                    }
                                },
                                onBiometricClick = {}
                            )
                        }
                        LockType.PATTERN -> {
                            PatternLockView(
                                isError = isErrorActive,
                                enabled = !isErrorActive,
                                onPatternEntered = { nodes ->
                                    if (nodes.size < 4) {
                                        triggerError("Connect at least 4 dots")
                                    } else {
                                        if (step == 1) {
                                            firstPattern = nodes
                                            step = 2
                                        } else {
                                            if (nodes == firstPattern) {
                                                onSavePattern(nodes)
                                                onDismiss()
                                            } else {
                                                triggerError("Patterns do not match. Try again.")
                                                step = 1
                                                firstPattern = emptyList()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Reset / Back Button if on step 2
                if (step == 2) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            step = 1
                            currentPinInput = ""
                            firstPin = ""
                            firstPattern = emptyList()
                            errorMessage = null
                            isErrorActive = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SlateBorderCrisp)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Over", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
