package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadJobState
import com.example.data.model.MediaFormat
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SemanticError
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    downloads: List<DownloadTaskEntity>,
    selectedFilter: String,
    searchQuery: String,
    renamingTask: DownloadTaskEntity?,
    playbackPreviewTask: DownloadTaskEntity?,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenRename: (DownloadTaskEntity) -> Unit,
    onDismissRename: () -> Unit,
    onConfirmRename: (String, String) -> Unit,
    onOpenPreview: (DownloadTaskEntity) -> Unit,
    onClosePreview: () -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val completedItems = downloads.filter { it.state == DownloadJobState.COMPLETED.name }

    val filteredItems = completedItems.filter { task ->
        val matchesCategory = when (selectedFilter) {
            "VIDEO" -> task.format.equals(MediaFormat.MP4.name, ignoreCase = true) || task.format.equals(MediaFormat.WEBM.name, ignoreCase = true)
            "AUDIO" -> task.format.equals(MediaFormat.MP3.name, ignoreCase = true) || task.format.equals(MediaFormat.M4A.name, ignoreCase = true)
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
            task.title.contains(searchQuery, ignoreCase = true) ||
            task.fileName.contains(searchQuery, ignoreCase = true) ||
            task.providerName.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
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
                    text = "Library",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${completedItems.size} saved files",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceElevated)
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${filteredItems.size} items",
                    color = AccentSky,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search files by title or provider…", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentIndigo,
                unfocusedBorderColor = SlateBorderSubtle,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "All (${completedItems.size})",
                "VIDEO" to "Videos (${completedItems.count { it.format.equals("MP4", true) || it.format.equals("WEBM", true) }})",
                "AUDIO" to "Audio (${completedItems.count { it.format.equals("MP3", true) || it.format.equals("M4A", true) }})"
            )
            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AccentIndigo else SlateSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AccentIndigo else SlateBorderSubtle,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onFilterChange(key) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_chip_$key")
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

        // File List or Empty State
        if (filteredItems.isEmpty()) {
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
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\"" else "No saved files in this category",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completed downloads are saved here offline",
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
                items(filteredItems, key = { it.jobId }) { task ->
                    LibraryMediaCard(
                        task = task,
                        onPlayPreview = { onOpenPreview(task) },
                        onRename = { onOpenRename(task) },
                        onShare = { shareMediaFile(context, task) },
                        onOpenExternal = { openWithExternalApp(context, task) },
                        onDelete = { onDelete(task.jobId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Rename Dialog
    if (renamingTask != null) {
        RenameFileDialog(
            task = renamingTask,
            onDismiss = onDismissRename,
            onConfirm = { newName -> onConfirmRename(renamingTask.jobId, newName) }
        )
    }

    // In-App Preview Dialog
    if (playbackPreviewTask != null) {
        InAppMediaPreviewDialog(
            task = playbackPreviewTask,
            onDismiss = onClosePreview,
            onOpenExternal = { openWithExternalApp(context, playbackPreviewTask) }
        )
    }
}

@Composable
fun LibraryMediaCard(
    task: DownloadTaskEntity,
    onPlayPreview: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit,
    onDelete: () -> Unit
) {
    val isVideo = task.format.equals(MediaFormat.MP4.name, ignoreCase = true) || task.format.equals(MediaFormat.WEBM.name, ignoreCase = true)

    val dateFormatted = remember(task.createdAt) {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(task.createdAt))
    }
    val mbSize = remember(task.downloadedBytes, task.totalBytes) {
        val bytes = if (task.downloadedBytes > 0) task.downloadedBytes else task.totalBytes
        String.format(Locale.US, "%.1f MB", bytes / (1024 * 1024.0))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(14.dp))
            .testTag("library_card_${task.jobId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isVideo) AccentIndigo.copy(alpha = 0.15f) else AccentSky.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isVideo) Icons.Default.Videocam else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = if (isVideo) AccentIndigo else AccentSky,
                        modifier = Modifier.size(22.dp)
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
                    Text(
                        text = task.fileName,
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = mbSize,
                            color = AccentSky,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = " • ", color = TextTertiary, fontSize = 11.sp)
                        Text(
                            text = task.providerName,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onPlayPreview, modifier = Modifier.size(36.dp).testTag("preview_btn_${task.jobId}")) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Preview", tint = AccentSky)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Toolbar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    color = TextTertiary,
                    fontSize = 10.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onOpenExternal, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open in player", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SemanticError.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RenameFileDialog(
    task: DownloadTaskEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(task.fileName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                Text(
                    text = "Specify a new file name:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentIndigo,
                        unfocusedBorderColor = SlateBorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                shape = RoundedCornerShape(8.dp),
                enabled = newName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SlateSurface
    )
}

@Composable
fun InAppMediaPreviewDialog(
    task: DownloadTaskEntity,
    onDismiss: () -> Unit,
    onOpenExternal: () -> Unit
) {
    val isVideo = task.format.equals(MediaFormat.MP4.name, ignoreCase = true) || task.format.equals(MediaFormat.WEBM.name, ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Media Preview",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AccentIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isVideo) Icons.Default.Videocam else Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = AccentSky,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = task.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.fileName,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${task.providerName} • ${task.qualityLabel}",
                    color = AccentSky,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onOpenExternal,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in System Player", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun shareMediaFile(context: Context, task: DownloadTaskEntity) {
    task.localFilePath?.let { path ->
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (task.format.equals("MP3", ignoreCase = true) || task.format.equals("M4A", ignoreCase = true)) "audio/*" else "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${task.fileName}"))
        }
    }
}

private fun openWithExternalApp(context: Context, task: DownloadTaskEntity) {
    task.localFilePath?.let { path ->
        val file = File(path)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = if (task.format.equals("MP3", ignoreCase = true) || task.format.equals("M4A", ignoreCase = true)) "audio/*" else "video/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching {
                context.startActivity(intent)
            }
        }
    }
}
