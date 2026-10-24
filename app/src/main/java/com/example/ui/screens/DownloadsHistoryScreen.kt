package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadJobState
import com.example.data.model.MediaFormat
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SemanticWarning
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.io.File

@Composable
fun DownloadsHistoryScreen(
    downloads: List<DownloadTaskEntity>,
    onOpenModal: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearCompleted: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "COMPLETED", "FAILED"

    val filteredList = remember(downloads, selectedFilter) {
        when (selectedFilter) {
            "ACTIVE" -> downloads.filter {
                it.state == DownloadJobState.DOWNLOADING.name ||
                    it.state == DownloadJobState.PREPARING.name ||
                    it.state == DownloadJobState.QUEUED.name ||
                    it.state == DownloadJobState.PROCESSING.name ||
                    it.state == DownloadJobState.PAUSED.name
            }
            "COMPLETED" -> downloads.filter { it.state == DownloadJobState.COMPLETED.name }
            "FAILED" -> downloads.filter { it.state == DownloadJobState.FAILED.name }
            else -> downloads
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Screen Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Downloads",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${downloads.size} items in queue & history",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (downloads.any { it.state == DownloadJobState.COMPLETED.name }) {
                IconButton(
                    onClick = onClearCompleted,
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear Completed",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlateSurface)
                .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filterOptions = listOf(
                "ALL" to "All",
                "ACTIVE" to "Active",
                "COMPLETED" to "Completed",
                "FAILED" to "Failed"
            )

            filterOptions.forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AccentIndigo else Color.Transparent)
                        .clickable { selectedFilter = key }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SlateSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = when (selectedFilter) {
                            "ACTIVE" -> "No active transfers"
                            "COMPLETED" -> "No completed downloads yet"
                            "FAILED" -> "No failed downloads"
                            else -> "Download queue is empty"
                        },
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste a supported URL on the Home tab to start",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.jobId }) { task ->
                    DownloadItemCard(
                        task = task,
                        onClick = { onOpenModal(task.jobId) },
                        onPause = { onPause(task.jobId) },
                        onResume = { onResume(task.jobId) },
                        onDelete = { onDelete(task.jobId) },
                        onShare = {
                            task.localFilePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = if (task.format == MediaFormat.MP3.name || task.format == MediaFormat.M4A.name) "audio/*" else "video/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share media file"))
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    task: DownloadTaskEntity,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val isCompleted = task.state == DownloadJobState.COMPLETED.name
    val isFailed = task.state == DownloadJobState.FAILED.name
    val isPaused = task.state == DownloadJobState.PAUSED.name
    val isVideo = task.format == MediaFormat.MP4.name || task.format == MediaFormat.WEBM.name

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("download_card_${task.jobId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Icon Container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isVideo) AccentIndigo.copy(alpha = 0.15f) else AccentSky.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = if (isVideo) AccentIndigo else AccentSky,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${task.providerName} • ${task.qualityLabel}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isCompleted -> "Completed"
                                isFailed -> "Failed"
                                isPaused -> "Paused"
                                else -> "${task.progressPercent}%"
                            },
                            color = when {
                                isCompleted -> SemanticSuccess
                                isFailed -> SemanticError
                                isPaused -> SemanticWarning
                                else -> AccentSky
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Actions
                Row {
                    if (isCompleted) {
                        IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    } else if (!isFailed) {
                        IconButton(onClick = if (isPaused) onResume else onPause, modifier = Modifier.size(36.dp)) {
                            Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Real-time progress bar if running or paused
            if (!isCompleted && !isFailed) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (task.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AccentIndigo,
                    trackColor = SlateSurfaceElevated
                )
            }
        }
    }
}
