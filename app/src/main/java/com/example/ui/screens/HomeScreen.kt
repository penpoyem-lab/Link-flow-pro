package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedShimmerProgressIndicator
import com.example.ui.components.AnimatedWaveformVisualizer
import com.example.ui.components.GooglePlayLoadingOverlay
import com.example.ui.components.GooglePlayPullToRefreshBox
import com.example.ui.components.GooglePlaySquigglyLoader
import com.example.ui.components.rememberAnimatedGradientBrush
import coil.compose.AsyncImage
import com.example.data.local.DownloadTaskEntity
import com.example.data.model.DownloadJobState
import com.example.data.model.MediaAnalysisResult
import com.example.data.model.MediaFormat
import com.example.data.model.QualityOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.LinkFlowUiState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AccentVioletDark
import com.example.ui.theme.AccentVioletLight
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
    onSelectPreset: (String, MediaFormat) -> Unit = { _, _ -> },
    onNavigateToPhotos: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "home_infinite_anim")

    val readyDotPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ready_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        // Atmospheric Ambient Top Glow Mesh
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentIndigo.copy(alpha = 0.28f),
                        AccentSky.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, -20.dp.toPx()),
                    radius = size.width * 0.85f
                )
            )
        }

        // Google Play Style Morphing Shape Pull-To-Refresh Container (Video 1)
        GooglePlayPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    delay(1200)
                    isRefreshing = false
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Top Bar with User Avatar, Title, and Pro Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, AccentViolet.copy(alpha = 0.6f), CircleShape)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&q=80",
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Video Downloader",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                            Text(
                                text = "Download from any platform instantly",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // Pro Crown Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color.Black)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B2E))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape)
                            .clickable { /* Pro details */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 17.sp
                        )
                    }
                }
            }

            // Pill-Shaped URL Input Bar with Embedded Circular Download Action
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(8.dp, RoundedCornerShape(29.dp), ambientColor = AccentViolet.copy(alpha = 0.25f))
                            .clip(RoundedCornerShape(29.dp))
                            .background(SlateSurface)
                            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(29.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = AccentVioletLight,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.inputUrl.isEmpty()) {
                                    Text(
                                        text = "Paste your link here or auto-detect",
                                        color = TextTertiary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedTextField(
                                    value = uiState.inputUrl,
                                    onValueChange = onUrlChanged,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        cursorColor = AccentViolet
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("url_input_field")
                                )
                            }

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
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // Circular Purple Download Action Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(6.dp, CircleShape, ambientColor = AccentViolet.copy(alpha = 0.6f))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AccentViolet, AccentVioletDark)
                                        )
                                    )
                                    .clickable {
                                        if (uiState.inputUrl.isNotBlank()) {
                                            onAnalyze()
                                        } else {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val item = clipboard?.primaryClip?.getItemAt(0)
                                            val text = item?.text?.toString()?.trim() ?: ""
                                            if (text.isNotBlank()) {
                                                onUrlChanged(text)
                                                onAnalyze()
                                            }
                                        }
                                    }
                                    .testTag("pill_download_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isAnalyzing) {
                                    GooglePlaySquigglyLoader(
                                        size = 22.dp,
                                        strokeColor = Color.White,
                                        accentColor = Color(0xFFE2E8F0),
                                        strokeWidth = 2.4.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Reminder: Respect creators' work and intellectual property rights.",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // "Open Social App to Copy Link" Section with Brand Icons
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SlateBorderSubtle)
                        )
                        Text(
                            text = "   Open Social App to Copy Link   ",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(SlateBorderSubtle)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialPlatformBadge(
                            name = "TikTok",
                            iconText = "🎵",
                            bgColor = Color(0xFF111111),
                            onClick = {
                                onSelectPreset(
                                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                    MediaFormat.MP4
                                )
                            }
                        )

                        SocialPlatformBadge(
                            name = "Instagram",
                            iconText = "📷",
                            bgBrush = Brush.linearGradient(
                                colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                            ),
                            onClick = {
                                onNavigateToPhotos()
                            }
                        )

                        SocialPlatformBadge(
                            name = "Facebook",
                            iconText = "f",
                            bgColor = Color(0xFF1877F2),
                            isLetter = true,
                            onClick = {
                                onUrlChanged("https://www.facebook.com/watch/?v=1080p_sample_stream")
                                onAnalyze()
                            }
                        )

                        SocialPlatformBadge(
                            name = "X",
                            iconText = "𝕏",
                            bgColor = Color(0xFF111111),
                            isLetter = true,
                            onClick = {
                                onUrlChanged("https://x.com/creator/status/1080p_stream")
                                onAnalyze()
                            }
                        )

                        SocialPlatformBadge(
                            name = "Pinterest",
                            iconText = "P",
                            bgColor = Color(0xFFE60023),
                            isLetter = true,
                            onClick = {
                                onUrlChanged("https://www.pinterest.com/pin/sample_aesthetic_photo")
                                onAnalyze()
                            }
                        )

                        SocialPlatformBadge(
                            name = "Spotify",
                            iconText = "🎧",
                            bgColor = Color(0xFF1DB954),
                            onClick = {
                                onSelectPreset(
                                    "https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT",
                                    MediaFormat.MP3
                                )
                            }
                        )

                        SocialPlatformBadge(
                            name = "YouTube",
                            iconText = "▶",
                            bgColor = Color(0xFFFF0000),
                            isLetter = true,
                            onClick = {
                                onSelectPreset(
                                    "https://www.youtube.com/watch?v=sample1080p",
                                    MediaFormat.MP4
                                )
                            }
                        )
                    }
                }
            }

            // Dedicated High-Quality Instagram Photos Downloader Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(AccentViolet.copy(alpha = 0.5f), Color(0xFFE1306C).copy(alpha = 0.5f))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onNavigateToPhotos() }
                        .testTag("banner_instagram_photos")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFF77737))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Instagram Photo Downloader",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentViolet.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "1080p HD",
                                        color = AccentVioletLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Save uncompressed 1080x1350 Original HD & Carousels in PNG / JPG",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            tint = AccentVioletLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // "Recently Download" Section from Screenshot
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Download",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View all >",
                            color = AccentVioletLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { onNavigateToDownloads() }
                                .padding(4.dp)
                                .testTag("btn_view_all_downloads")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Creator Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80",
                                    contentDescription = "Reya Ramani",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Reya Ramani",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "@reyaramani458",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onSelectPreset(
                                            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                            MediaFormat.MP4
                                        )
                                    },
                                    modifier = Modifier.size(32.dp).testTag("btn_recent_download")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { /* Share recent link */ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Video Thumbnail with Center Play Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SlateSurfaceElevated)
                                    .clickable {
                                        onSelectPreset(
                                            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                            MediaFormat.MP4
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80",
                                    contentDescription = "Recent video thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Centered Glass Play Button
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // "Premium Features" Card from Screenshot
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Premium Features",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Remove watermarks, download audio, and unlock exclusive features",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Bottom Full-Width Prominent "Download" Button
            item {
                val isAnalyzeReady = uiState.inputUrl.isNotBlank() && !uiState.isAnalyzing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = AccentViolet.copy(alpha = 0.6f))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentViolet, AccentVioletDark)
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = {
                                if (uiState.currentAnalysis != null) {
                                    onStartDownload()
                                } else if (uiState.inputUrl.isNotBlank()) {
                                    onAnalyze()
                                } else {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val item = clipboard?.primaryClip?.getItemAt(0)
                                    val text = item?.text?.toString()?.trim() ?: ""
                                    if (text.isNotBlank()) {
                                        onUrlChanged(text)
                                        onAnalyze()
                                    }
                                }
                            }
                        )
                        .testTag("btn_primary_download"),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyzing Media…",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = if (uiState.currentAnalysis != null) "Download Now" else "Download",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

        // Format Selection Segmented Switch with Animated Sliding Capsule
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Download Format",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (uiState.quickFormatSelection == MediaFormat.MP4) "Direct MP4" else "Hi-Fi MP3",
                            color = AccentSky,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateSurfaceElevated)
                            .padding(3.dp)
                    ) {
                        val itemWidth = (maxWidth - 6.dp) / 2
                        val isVideo = uiState.quickFormatSelection == MediaFormat.MP4
                        val animatedPillOffset by animateDpAsState(
                            targetValue = if (isVideo) 0.dp else itemWidth,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f),
                            label = "format_pill_spring"
                        )

                        // Animated Sliding Pill
                        Box(
                            modifier = Modifier
                                .offset(x = animatedPillOffset)
                                .width(itemWidth)
                                .height(40.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    ambientColor = AccentIndigo.copy(alpha = 0.5f),
                                    spotColor = AccentSky.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(AccentIndigo, AccentSky)
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = { onQuickFormatSelected(MediaFormat.MP4) }
                                    )
                                    .testTag("format_tab_mp4"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = if (isVideo) Color.White else TextSecondary,
                                        modifier = Modifier.size(16.dp).scale(if (isVideo) 1.08f else 1.0f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Video (MP4)",
                                        color = if (isVideo) Color.White else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isVideo) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = { onQuickFormatSelected(MediaFormat.MP3) }
                                    )
                                    .testTag("format_tab_mp3"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = if (!isVideo) Color.White else TextSecondary,
                                        modifier = Modifier.size(16.dp).scale(if (!isVideo) 1.08f else 1.0f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio (MP3)",
                                        color = if (!isVideo) Color.White else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (!isVideo) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
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
                                    if (isAudioOnly) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            AnimatedWaveformVisualizer(
                                                isActive = true,
                                                barCount = 11,
                                                modifier = Modifier
                                                    .height(50.dp)
                                                    .width(140.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Studio Audio Stream Ready",
                                                color = AccentSky,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = TextTertiary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
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

                            // Primary Glowing Animated Download Action Button
                            val downloadGradientBrush = rememberAnimatedGradientBrush(
                                colors = listOf(AccentIndigo, AccentSky, Color(0xFF7C3AED), AccentIndigo),
                                durationMs = 2800
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(
                                        elevation = 10.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = AccentIndigo.copy(alpha = 0.5f),
                                        spotColor = AccentSky.copy(alpha = 0.4f)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(downloadGradientBrush)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White),
                                        onClick = onStartDownload
                                    )
                                    .testTag("btn_start_download"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start High-Quality Download",
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = AccentSky,
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
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AnimatedShimmerProgressIndicator(
                                        progress = task.progressPercent / 100f,
                                        height = 6.dp,
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

        // Google Play Style Centered Squiggly Loader Overlay (Video 2) with transparent background
        GooglePlayLoadingOverlay(
            isLoading = uiState.isAnalyzing,
            message = "Analyzing media link…"
        )
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

@Composable
private fun SocialPlatformBadge(
    name: String,
    iconText: String,
    bgColor: Color = Color.Transparent,
    bgBrush: Brush? = null,
    isLetter: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
            .testTag("social_platform_${name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f))
                .clip(CircleShape)
                .then(
                    if (bgBrush != null) Modifier.background(bgBrush)
                    else Modifier.background(bgColor)
                )
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = Color.White,
                fontSize = if (isLetter) 18.sp else 16.sp,
                fontWeight = if (isLetter) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = name,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
