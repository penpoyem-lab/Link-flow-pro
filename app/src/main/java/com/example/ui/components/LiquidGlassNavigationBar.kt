package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentSky
import com.example.ui.theme.SlateBorderCrisp
import com.example.ui.theme.SlateBorderSubtle
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0
)

@Composable
fun LiquidGlassNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    activeDownloadsCount: Int,
    libraryCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = remember(activeDownloadsCount, libraryCount) {
        listOf(
            NavTabItem("Home", Icons.Default.Home, "nav_tab_home"),
            NavTabItem("Downloads", Icons.Default.Download, "nav_tab_downloads", badgeCount = activeDownloadsCount),
            NavTabItem("Library", Icons.Default.VideoLibrary, "nav_tab_library", badgeCount = libraryCount),
            NavTabItem("Settings", Icons.Default.Settings, "nav_tab_settings")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF5171F33),
                            Color(0xFA0E1524)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(4.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabCount = tabs.size
                val itemWidth = maxWidth / tabCount
                val capsuleWidth = itemWidth - 6.dp

                // Smooth Sliding Capsule Transition
                val animatedOffsetX by animateDpAsState(
                    targetValue = (itemWidth * selectedTab) + 3.dp,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 420f
                    ),
                    label = "capsule_translation_x"
                )

                // Active Capsule Pill
                Box(
                    modifier = Modifier
                        .offset(x = animatedOffsetX)
                        .width(capsuleWidth)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AccentIndigo,
                                    Color(0xFF4F46E5)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(28.dp)
                        )
                )

                // Interactive Tab Items
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        val interactionSource = remember { MutableInteractionSource() }

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.08f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.7f),
                            label = "tab_icon_scale"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(bounded = true, color = Color.White),
                                    onClick = { onTabSelected(index) }
                                )
                                .testTag(item.testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .scale(iconScale)
                                    )

                                    if (item.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 8.dp, y = (-4).dp)
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else AccentSky),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                color = if (isSelected) AccentIndigo else Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.title,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
