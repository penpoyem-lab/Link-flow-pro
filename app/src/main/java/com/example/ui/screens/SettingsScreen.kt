package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.LockType
import com.example.ui.LinkFlowUiState
import com.example.ui.components.AppLockSetupDialog
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    uiState: LinkFlowUiState,
    onToggleWifiOnly: (Boolean) -> Unit,
    onToggleAutoRetry: (Boolean) -> Unit,
    onSetMaxConcurrent: (Int) -> Unit,
    onCheckAppUpdates: () -> Unit,
    // AppLock Security Callbacks
    onToggleAppLock: (Boolean) -> Unit,
    onSetPrimaryLockType: (LockType) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSetLockTimeout: (Long) -> Unit,
    onLockAppNow: () -> Unit,
    onOpenAppLockSetup: (String) -> Unit,
    onCloseAppLockSetup: () -> Unit,
    onSavePin: (String) -> Unit,
    onSavePattern: (List<Int>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "System preferences, storage & app security",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // App Version Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
                    .testTag("app_version_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = AccentIndigo, modifier = Modifier.size(22.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LinkFlow Media Downloader",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Version 1.0.0 • Production Build",
                            color = AccentIndigo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "On-device universal media pipeline and secure storage",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = SemanticSuccess, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Preferences Group
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Download Engine Options",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Wi-Fi Only Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = AccentSky, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Wi-Fi Only Downloads", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Restrict media downloads to unmetered Wi-Fi connections", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = uiState.wifiOnlyDownload,
                            onCheckedChange = onToggleWifiOnly,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentIndigo
                            ),
                            modifier = Modifier.testTag("switch_wifi_only")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Retry Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = AccentSky, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Resume on Interruption", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Automatically resume interrupted transfers on reconnect", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = uiState.autoRetryFailed,
                            onCheckedChange = onToggleAutoRetry,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentIndigo
                            ),
                            modifier = Modifier.testTag("switch_auto_retry")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Concurrent Download Queue Limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Concurrent Transfer Slots", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Maximum parallel download workers", color = TextSecondary, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3, 5).forEach { count ->
                                val isSelected = uiState.maxConcurrentDownloads == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentIndigo else SlateSurfaceElevated)
                                        .border(1.dp, if (isSelected) AccentIndigo else SlateBorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { onSetMaxConcurrent(count) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Storage Metrics Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurfaceElevated)
                            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = AccentSky, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val freeGb = String.format("%.1f", uiState.freeStorageBytes / 1_000_000_000.0)
                            val totalGb = String.format("%.1f", uiState.totalStorageBytes / 1_000_000_000.0)
                            Text("Local Storage Capacity", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("$freeGb GB available out of $totalGb GB total storage", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECURITY & NATIVE APPLOCK CARD
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (uiState.isAppLockEnabled) AccentIndigo.copy(alpha = 0.5f) else SlateBorderSubtle,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("security_applock_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isAppLockEnabled) AccentIndigo.copy(alpha = 0.15f) else SlateSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (uiState.isAppLockEnabled) AccentIndigo else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Security & AppLock",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.isAppLockEnabled) {
                                        "Protected • ${if (uiState.primaryLockType == LockType.PIN) "Digital PIN" else "Pattern Lock"}${if (uiState.isBiometricEnabled && uiState.isBiometricAvailable) " + Biometrics" else ""}"
                                    } else {
                                        "Lock app with PIN, Pattern & Fingerprint"
                                    },
                                    color = if (uiState.isAppLockEnabled) AccentSky else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = uiState.isAppLockEnabled,
                            onCheckedChange = onToggleAppLock,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentIndigo
                            ),
                            modifier = Modifier.testTag("switch_app_lock")
                        )
                    }

                    // Expanded Controls when AppLock is active
                    AnimatedVisibility(
                        visible = uiState.isAppLockEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            // Primary Lock Method Selector
                            Text(
                                text = "Primary Lock Method",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SlateSurfaceVariant)
                                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isPin = uiState.primaryLockType == LockType.PIN
                                Button(
                                    onClick = { onSetPrimaryLockType(LockType.PIN) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPin) AccentIndigo else Color.Transparent,
                                        contentColor = if (isPin) Color.White else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_select_pin_mode")
                                ) {
                                    Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Digital PIN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                val isPattern = uiState.primaryLockType == LockType.PATTERN
                                Button(
                                    onClick = { onSetPrimaryLockType(LockType.PATTERN) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPattern) AccentIndigo else Color.Transparent,
                                        contentColor = if (isPattern) Color.White else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_select_pattern_mode")
                                ) {
                                    Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pattern Lock", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons to change credentials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onOpenAppLockSetup("PIN") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = SolidColor(SlateBorderCrisp)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_change_pin")
                                ) {
                                    Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (uiState.hasPinConfigured) "Change PIN" else "Set PIN", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { onOpenAppLockSetup("PATTERN") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = SolidColor(SlateBorderCrisp)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_change_pattern")
                                ) {
                                    Icon(Icons.Default.Pattern, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (uiState.hasPatternConfigured) "Change Pattern" else "Set Pattern", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Fingerprint / Biometric Unlock Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AccentSky, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Fingerprint / Biometrics", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = if (uiState.isBiometricAvailable) "Sensor ready • Quick authentication" else "Hardware unavailable or not enrolled",
                                            color = if (uiState.isBiometricAvailable) SemanticSuccess else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.isBiometricEnabled && uiState.isBiometricAvailable,
                                    enabled = uiState.isBiometricAvailable,
                                    onCheckedChange = onToggleBiometric,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AccentIndigo
                                    ),
                                    modifier = Modifier.testTag("switch_biometric")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Auto-Lock Timeout Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Lock Timeout", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Re-locks when leaving the application", color = TextSecondary, fontSize = 11.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val timeouts = listOf(0L to "Instant", 60L to "1 min", 300L to "5 min")
                                    timeouts.forEach { (sec, label) ->
                                        val isSelected = uiState.lockTimeoutSeconds == sec
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) AccentIndigo.copy(alpha = 0.2f) else SlateSurfaceElevated)
                                                .border(1.dp, if (isSelected) AccentIndigo else SlateBorderSubtle, RoundedCornerShape(8.dp))
                                                .clickable { onSetLockTimeout(sec) }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) TextPrimary else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Lock App Now action
                            Button(
                                onClick = onLockAppNow,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_lock_app_now")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lock Application Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Software Updates Check Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = AccentSky, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Software Updates", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onCheckAppUpdates,
                            enabled = !uiState.isCheckingAppUpdate,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isCheckingAppUpdate) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking...")
                            } else {
                                Text("Check for Updates")
                            }
                        }
                    }
                    if (uiState.appUpdateMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.appUpdateMessage,
                            color = SemanticSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (uiState.showAppLockSetupDialog) {
        AppLockSetupDialog(
            initialMode = uiState.appLockSetupMode ?: "PIN",
            onSavePin = onSavePin,
            onSavePattern = onSavePattern,
            onDismiss = onCloseAppLockSetup
        )
    }
}
