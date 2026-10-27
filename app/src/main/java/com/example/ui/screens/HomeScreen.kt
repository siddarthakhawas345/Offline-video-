package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoThumbnailImage
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.StreamAccent
import com.example.ui.theme.StreamError
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun HomeScreen(
    featuredVideos: List<VideoEntity>,
    activeVideos: List<VideoEntity>,
    continueWatching: List<VideoEntity>,
    isAdminUnlocked: Boolean,
    onPlayVideo: (VideoEntity) -> Unit,
    onVideoDetails: (VideoEntity) -> Unit,
    onDeleteVideoRequest: (VideoEntity) -> Unit,
    onToggleFavorite: (VideoEntity) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val heroVideo = featuredVideos.firstOrNull() ?: activeVideos.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(StreamPrimary, Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "VSTREAM",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = StreamSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% OFFLINE",
                                color = StreamSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Admin Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdminUnlocked) StreamPrimary.copy(alpha = 0.2f) else CinemaSurfaceElevated,
                    modifier = Modifier.clickable { onNavigateToAdmin() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = if (isAdminUnlocked) StreamPrimary else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAdminUnlocked) "Admin Active" else "Admin",
                            color = if (isAdminUnlocked) StreamPrimary else TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Hero Featured Banner
        if (heroVideo != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(16f / 9.5f)
                        .clickable { onPlayVideo(heroVideo) }
                        .testTag("hero_banner")
                ) {
                    VideoThumbnailImage(
                        thumbnailUri = heroVideo.thumbnailUri,
                        contentDescription = heroVideo.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Hero Content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StreamAccent)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FEATURED OFFLINE",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${heroVideo.category} • ${heroVideo.resolution}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = heroVideo.title,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onPlayVideo(heroVideo) },
                                colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("hero_play_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Stream", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { onVideoDetails(heroVideo) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("hero_info_button")
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Details", color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Continue Watching Shelf
        if (continueWatching.isNotEmpty()) {
            item {
                ShelfHeader(title = "Continue Watching", count = continueWatching.size)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueWatching, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onPlay = { onPlayVideo(video) },
                            onDetails = { onVideoDetails(video) },
                            onDeleteRequest = { onDeleteVideoRequest(video) },
                            onToggleFavorite = { onToggleFavorite(video) },
                            cardWidth = 240.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Offline Stream Catalog
        item {
            ShelfHeader(title = "All Offline Videos", count = activeVideos.size)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(activeVideos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onPlay = { onPlayVideo(video) },
                        onDetails = { onVideoDetails(video) },
                        onDeleteRequest = { onDeleteVideoRequest(video) },
                        onToggleFavorite = { onToggleFavorite(video) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Category Shelves
        val categories = listOf("Sci-Fi & Action", "Documentary & Nature", "Tech & Tutorials", "Music & Chill", "Custom Imports")
        items(categories) { category ->
            val catVideos = activeVideos.filter { it.category == category }
            if (catVideos.isNotEmpty()) {
                ShelfHeader(title = category, count = catVideos.size)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(catVideos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onPlay = { onPlayVideo(video) },
                            onDetails = { onVideoDetails(video) },
                            onDeleteRequest = { onDeleteVideoRequest(video) },
                            onToggleFavorite = { onToggleFavorite(video) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Offline Guarantee Banner at footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CinemaSurfaceElevated)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = StreamSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Offline Mode Active",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "All streams, metadata, and files run purely on-device without internet access.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShelfHeader(title: String, count: Int? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (count != null) {
            Text(
                text = "$count videos",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
