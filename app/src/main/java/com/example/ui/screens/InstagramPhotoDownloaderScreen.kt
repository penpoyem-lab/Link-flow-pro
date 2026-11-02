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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaAnalysisResult
import com.example.data.model.MediaFormat
import com.example.data.model.QualityOption
import com.example.ui.LinkFlowUiState
import com.example.ui.components.rememberAnimatedGradientBrush
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.AccentVioletDark
import com.example.ui.theme.SemanticSuccess
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SocialInstagram
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun InstagramPhotoDownloaderScreen(
    uiState: LinkFlowUiState,
    onUrlChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    onDownloadPhoto: (QualityOption) -> Unit,
    onSelectPresetUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }
    var selectedPhotoFormat by remember { mutableStateOf("JPG") } // "JPG" or "PNG"

    val analysis = uiState.currentAnalysis
    val isPhotoReady = analysis != null && (analysis.isPhotoPost || analysis.photoUrls.isNotEmpty())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Screen Header matching Instagram High-Quality Photo Downloader
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
                            .size(42.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = SocialInstagram.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFFCB045)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Instagram Photos",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = AccentSky,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Download 1080p Ultra-HD Photos & Carousels",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // HD Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentViolet.copy(alpha = 0.18f))
                        .border(1.dp, AccentViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "1080p HD",
                        color = AccentViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Link Input Card
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
                        text = "Instagram Post Link",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste link to single photo or multi-photo carousel post",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.inputUrl,
                        onValueChange = onUrlChanged,
                        placeholder = {
                            Text(
                                text = "https://www.instagram.com/p/...",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = SocialInstagram,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.inputUrl.isNotBlank()) {
                                IconButton(
                                    onClick = { onUrlChanged("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceElevated,
                            unfocusedContainerColor = SlateSurfaceElevated,
                            focusedBorderColor = AccentViolet,
                            unfocusedBorderColor = SlateBorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AccentViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ig_photo_input_field")
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
                                val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                                if (clipText.isNotBlank()) onUrlChanged(clipText)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SlateBorderCrisp)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("ig_btn_paste")
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentSky)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        val isReady = uiState.inputUrl.isNotBlank() && !uiState.isAnalyzing
                        Button(
                            onClick = onAnalyze,
                            enabled = isReady,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentViolet,
                                disabledContainerColor = SlateSurfaceVariant,
                                contentColor = Color.White,
                                disabledContentColor = TextDisabled
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .testTag("ig_btn_fetch_photo")
                        ) {
                            if (uiState.isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extracting…", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fetch Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Preset Photo Samples
        if (!isPhotoReady) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SlateBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Sample Instagram HD Photos",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap any sample to test ultra-high quality photo extraction",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SamplePhotoRow(
                            title = "Vogue Fashion Portrait (1080x1350 HD)",
                            creator = "@vogue_magazine",
                            badge = "1080p Ultra-HD",
                            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=1080",
                            onClick = {
                                onSelectPresetUrl("https://www.instagram.com/p/C3FashionVogueHD/")
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SamplePhotoRow(
                            title = "Tokyo Neon Streetscape (Full Resolution)",
                            creator = "@travel_japan",
                            badge = "Carousel (4 Photos)",
                            imageUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?q=80&w=1080",
                            onClick = {
                                onSelectPresetUrl("https://www.instagram.com/p/C3TokyoNeonNight/")
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SamplePhotoRow(
                            title = "Swiss Alps Mountain Sunrise (Master Quality)",
                            creator = "@nature_wanderlust",
                            badge = "Uncompressed",
                            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=1080",
                            onClick = {
                                onSelectPresetUrl("https://www.instagram.com/p/C3SwissAlpsSunrise/")
                            }
                        )
                    }
                }
            }
        }

        // Photo Preview & High-Quality Download Card
        if (isPhotoReady && analysis != null) {
            item {
                val photoUrls = if (analysis.photoUrls.isNotEmpty()) analysis.photoUrls else listOfNotNull(analysis.thumbnailUrl)
                val activePhotoUrl = photoUrls.getOrNull(selectedPhotoIndex) ?: photoUrls.firstOrNull() ?: ""

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentViolet.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .testTag("ig_photo_preview_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SocialInstagram.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SocialInstagram, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = analysis.author, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Instagram Post", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SemanticSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "HD Ready", color = SemanticSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // High Resolution Photo Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SlateSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activePhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = activePhotoUrl,
                                    contentDescription = "Instagram Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(54.dp))
                            }

                            // Resolution badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "1080 x 1350 Original HD", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Carousel Thumbnails Row (if multiple photos)
                        if (photoUrls.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Carousel Photos (${photoUrls.size} items)", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(photoUrls) { index, url ->
                                    val isSelected = selectedPhotoIndex == index
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) AccentViolet else SlateBorderSubtle,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedPhotoIndex = index }
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Format Selection (JPG vs PNG)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlateSurfaceElevated)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPhotoFormat == "JPG") AccentViolet else Color.Transparent)
                                    .clickable { selectedPhotoFormat = "JPG" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "JPEG (Ultra-HD 1080p)",
                                    color = if (selectedPhotoFormat == "JPG") Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedPhotoFormat == "PNG") AccentViolet else Color.Transparent)
                                    .clickable { selectedPhotoFormat = "PNG" },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PNG (Lossless)",
                                    color = if (selectedPhotoFormat == "PNG") Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Download Photo Button
                        val downloadPhotoOption = QualityOption(
                            id = "ig_dl_photo_${selectedPhotoIndex}_$selectedPhotoFormat",
                            format = if (selectedPhotoFormat == "PNG") MediaFormat.PNG else MediaFormat.JPG,
                            label = "Instagram HD Photo ($selectedPhotoFormat)",
                            subLabel = "Original 1080p Resolution",
                            badge = "High Quality",
                            resolutionOrBitrate = "1080x1350",
                            estimatedSizeBytes = if (selectedPhotoFormat == "PNG") 4_200_000L else 2_500_000L,
                            downloadUrl = activePhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=1080" },
                            containerExtension = selectedPhotoFormat.lowercase()
                        )

                        val downloadGradient = rememberAnimatedGradientBrush(
                            colors = listOf(AccentViolet, Color(0xFFE1306C), AccentViolet),
                            durationMs = 2800
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(10.dp, RoundedCornerShape(12.dp), ambientColor = AccentViolet.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(12.dp))
                                .background(downloadGradient)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = { onDownloadPhoto(downloadPhotoOption) }
                                )
                                .testTag("btn_download_ig_photo"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Download High-Quality Photo (${selectedPhotoFormat})",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
private fun SamplePhotoRow(
    title: String,
    creator: String,
    badge: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurfaceElevated)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateSurfaceVariant)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = creator, color = TextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AccentViolet.copy(alpha = 0.15f))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(text = badge, color = AccentViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
