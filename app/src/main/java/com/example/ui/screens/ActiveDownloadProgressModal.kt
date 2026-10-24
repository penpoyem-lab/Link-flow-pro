package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadJobState
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActiveDownloadProgressModal(
    task: DownloadTaskEntity?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (task == null) return

    val stateEnum = runCatching { DownloadJobState.valueOf(task.state) }.getOrDefault(DownloadJobState.DOWNLOADING)
    val isCompleted = stateEnum == DownloadJobState.COMPLETED
    val isFailed = stateEnum == DownloadJobState.FAILED
    val isPaused = stateEnum == DownloadJobState.PAUSED

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorderCrisp, RoundedCornerShape(20.dp))
                .padding(4.dp)
                .testTag("download_progress_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
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
                                .background(
                                    when {
                                        isCompleted -> SemanticSuccess.copy(alpha = 0.15f)
                                        isFailed -> SemanticError.copy(alpha = 0.15f)
                                        else -> AccentIndigo.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(20.dp))
                                isFailed -> Icon(Icons.Default.Error, contentDescription = null, tint = SemanticError, modifier = Modifier.size(20.dp))
                                else -> CircularProgressIndicator(
                                    progress = { task.progressPercent / 100f },
                                    modifier = Modifier.size(20.dp),
                                    color = AccentSky,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stateEnum.displayName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = task.qualityLabel,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_modal_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = task.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (task.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) SemanticSuccess else if (isFailed) SemanticError else AccentIndigo,
                    trackColor = SlateSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val downloadedMb = task.downloadedBytes / (1024 * 1024.0)
                    val totalMb = task.totalBytes / (1024 * 1024.0)
                    Text(
                        text = if (totalMb > 0) String.format(java.util.Locale.US, "%.1f / %.1f MB", downloadedMb, totalMb) else String.format(java.util.Locale.US, "%.1f MB", downloadedMb),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "${task.progressPercent}%",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isCompleted && !isFailed && task.speedBytesPerSec > 0) {
                    val speedMb = task.speedBytesPerSec / (1024 * 1024.0)
                    val etaStr = if (task.etaSeconds > 0) "${task.etaSeconds}s remaining" else "Calculating..."
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f MB/s", speedMb),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = etaStr,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isFailed && task.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.errorMessage,
                        color = SemanticError,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isCompleted && !isFailed) {
                        OutlinedButton(
                            onClick = onCancel,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("cancel_download_btn")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = if (isPaused) onResume else onPause,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("pause_resume_btn")
                        ) {
                            Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPaused) "Resume" else "Pause")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("modal_done_btn")
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}
