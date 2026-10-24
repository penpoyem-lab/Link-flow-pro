package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadJobState
import com.example.data.model.MediaAnalysisResult
import com.example.data.model.MediaFormat
import com.example.data.model.QualityOption
import com.example.ui.LinkFlowUiState
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

@Composable
fun HomeScreen(
    uiState: LinkFlowUiState,
    activeDownloads: List<DownloadTaskEntity>,
    onUrlChanged: (String) -> Unit,
    onClearInput: () -> Unit,
    onAnalyze: () -> Unit,
    onQuickFormatSelected: (MediaFormat) -> Unit,
    onOpenQualitySelector: () -> Unit,
    onStartDownload: () -> Unit,
    onOpenDownloadModal: (String) -> Unit,
    onSelectPreset: (String, MediaFormat) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Professional App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "LinkFlow",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            text = "Media Downloader",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateSurfaceVariant)
                        .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SemanticSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ready",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // URL Input & Actions Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Source Link",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.inputUrl,
                        onValueChange = onUrlChanged,
                        placeholder = {
                            Text(
                                text = "Paste video, audio, or direct media link…",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = AccentSky,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.inputUrl.isNotBlank()) {
                                IconButton(
                                    onClick = onClearInput,
                                    modifier = Modifier.size(28.dp).testTag("btn_clear_url")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceElevated,
                            unfocusedContainerColor = SlateSurfaceElevated,
                            focusedBorderColor = AccentIndigo,
                            unfocusedBorderColor = SlateBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentSky
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val item = clipboard?.primaryClip?.getItemAt(0)
                                val text = item?.text?.toString()?.trim() ?: ""
                                if (text.isNotBlank()) {
                                    onUrlChanged(text)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorderCrisp)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_paste_clipboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AccentSky
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = onAnalyze,
                            enabled = uiState.inputUrl.isNotBlank() && !uiState.isAnalyzing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentIndigo,
                                disabledContainerColor = SlateSurfaceVariant,
                                contentColor = Color.White,
                                disabledContentColor = TextDisabled
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .testTag("btn_analyze_link")
                        ) {
                            if (uiState.isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing…", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze Link", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Format Selection Segmented Switch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Download Format",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurfaceElevated)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isVideo = uiState.quickFormatSelection == MediaFormat.MP4
                        Button(
                            onClick = { onQuickFormatSelected(MediaFormat.MP4) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVideo) AccentIndigo else Color.Transparent,
                                contentColor = if (isVideo) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("format_tab_mp4")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Video (MP4)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        val isAudio = uiState.quickFormatSelection == MediaFormat.MP3
                        Button(
                            onClick = { onQuickFormatSelected(MediaFormat.MP3) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAudio) AccentIndigo else Color.Transparent,
                                contentColor = if (isAudio) Color.White else TextSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("format_tab_mp3")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Audio (MP3)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // High-Quality Media Presets (Quick-Tap for 1080p Video & 320kbps Music)
        if (uiState.currentAnalysis == null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
                        .testTag("high_quality_presets_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "High-Quality Media Streams",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentIndigo.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "1080p & 320k",
                                    color = AccentIndigo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to test instant high-bitrate video or music download",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Preset 1: 1080p 60fps HD Video
                        PresetItemRow(
                            title = "Big Buck Bunny (1080p 60fps HD Video)",
                            subtitle = "Full-Length 1080p Cinema Video Stream • 60fps MP4",
                            badge = "1080p HD",
                            badgeColor = AccentSky,
                            icon = Icons.Default.Videocam,
                            iconTint = AccentSky,
                            onClick = {
                                onSelectPreset(
                                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                    MediaFormat.MP4
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset 2: 320kbps Hi-Fi Studio Music
                        PresetItemRow(
                            title = "SoundHelix Studio Symphony (320k Music)",
                            subtitle = "Direct 320kbps MP3 Audio • Master Fidelity",
                            badge = "Hi-Fi 320k",
                            badgeColor = AccentIndigo,
                            icon = Icons.Default.Audiotrack,
                            iconTint = AccentIndigo,
                            onClick = {
                                onSelectPreset(
                                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                                    MediaFormat.MP3
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset 3: Cinematic 1080p Landscape Video
                        PresetItemRow(
                            title = "ForBiggerBlazes Cinematic (1080p MP4)",
                            subtitle = "Direct 1080p High-Bitrate Landscape Video",
                            badge = "1080p HD",
                            badgeColor = AccentSky,
                            icon = Icons.Default.Videocam,
                            iconTint = AccentSky,
                            onClick = {
                                onSelectPreset(
                                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                    MediaFormat.MP4
                                )
                            }
                        )
                    }
                }
            }
        }

        // Media Stream Analysis Inspection Card
        item {
            AnimatedVisibility(
                visible = uiState.currentAnalysis != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                uiState.currentAnalysis?.let { analysis ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentIndigo.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                            .testTag("media_preview_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SlateSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (analysis.thumbnailUrl != null) {
                                    AsyncImage(
                                        model = analysis.thumbnailUrl,
                                        contentDescription = analysis.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val isAudioOnly = analysis.videoOptions.isEmpty() && analysis.audioOptions.isNotEmpty()
                                    Icon(
                                        imageVector = if (isAudioOnly) Icons.Default.Audiotrack else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }

                                // Duration pill overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = analysis.durationFormatted,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Provider pill overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentIndigo)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = analysis.providerName,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = analysis.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Metadata Badges Row
                            val formattedSize = uiState.selectedQualityOption?.let { opt ->
                                if (opt.estimatedSizeBytes > 0) {
                                    String.format(java.util.Locale.US, "%.1f MB", opt.estimatedSizeBytes / (1024 * 1024.0))
                                } else "Auto"
                            } ?: "Auto"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetadataChip(
                                    label = "Quality",
                                    value = uiState.selectedQualityOption?.resolutionOrBitrate ?: "Default"
                                )
                                MetadataChip(
                                    label = "Size",
                                    value = formattedSize
                                )
                                MetadataChip(
                                    label = "Format",
                                    value = uiState.selectedQualityOption?.format?.name ?: "MP4"
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Quality Selector Trigger Button
                            OutlinedButton(
                                onClick = onOpenQualitySelector,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorderCrisp)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("btn_select_quality")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentSky)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quality: ${uiState.selectedQualityOption?.label ?: "Select Stream Quality"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Primary Download Action Button
                            Button(
                                onClick = onStartDownload,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_start_download")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start Download",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Tasks Section (if any tasks are currently running)
        if (activeDownloads.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Transfers (${activeDownloads.size})",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeDownloads.take(3).forEach { task ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable { onOpenDownloadModal(task.jobId) }
                                .testTag("active_task_${task.jobId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = AccentSky,
                                        modifier = Modifier.size(18.dp)
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { task.progressPercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = AccentIndigo,
                                        trackColor = SlateSurfaceElevated
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${task.progressPercent}% • ${formatSpeed(task.speedBytesPerSec)}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = task.durationFormatted,
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Supported Services Info
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Supported Services",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ServiceItem("YouTube")
                        ServiceItem("Instagram")
                        ServiceItem("TikTok")
                        ServiceItem("Direct Link")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MetadataChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, color = TextTertiary, fontSize = 10.sp)
            Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ServiceItem(name: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(AccentSky)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "Connecting…"
    val mb = bytesPerSec / 1_000_000.0
    return if (mb >= 1.0) {
        String.format("%.1f MB/s", mb)
    } else {
        val kb = bytesPerSec / 1_000.0
        String.format("%.0f KB/s", kb)
    }
}

@Composable
private fun PresetItemRow(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurfaceElevated)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
