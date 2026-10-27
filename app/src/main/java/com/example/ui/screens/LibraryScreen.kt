package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.components.VideoCard
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun LibraryScreen(
    activeVideos: List<VideoEntity>,
    favoriteVideos: List<VideoEntity>,
    continueWatching: List<VideoEntity>,
    totalStorageBytes: Long,
    onPlayVideo: (VideoEntity) -> Unit,
    onVideoDetails: (VideoEntity) -> Unit,
    onDeleteVideoRequest: (VideoEntity) -> Unit,
    onToggleFavorite: (VideoEntity) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Favorites", "Watch History", "Custom Imports", "All Streams")

    val currentList = when (selectedTabIndex) {
        0 -> favoriteVideos
        1 -> continueWatching
        2 -> activeVideos.filter { it.category == "Custom Imports" || it.videoUri.contains("imported_") }
        else -> activeVideos
    }

    val formattedStorage = remember(totalStorageBytes) {
        val mb = totalStorageBytes.toDouble() / (1024 * 1024)
        if (mb >= 1024) String.format("%.2f GB", mb / 1024) else String.format("%.1f MB", mb)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("library_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Library",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Stored on this device for offline playback",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Storage pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CinemaSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = StreamSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedStorage,
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CinemaBlack,
            contentColor = TextWhite,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = StreamPrimary
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, label ->
                val isSelected = selectedTabIndex == index
                val count = when (index) {
                    0 -> favoriteVideos.size
                    1 -> continueWatching.size
                    2 -> activeVideos.count { it.category == "Custom Imports" || it.videoUri.contains("imported_") }
                    else -> activeVideos.size
                }
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = "$label ($count)",
                            color = if (isSelected) TextWhite else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Content
        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (selectedTabIndex) {
                            0 -> Icons.Default.Favorite
                            1 -> Icons.Default.History
                            2 -> Icons.Default.VideoLibrary
                            else -> Icons.Default.PlayCircle
                        },
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "No favorite videos yet"
                            1 -> "No watch history recorded"
                            2 -> "No imported videos found"
                            else -> "No videos in library"
                        },
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "Tap the heart icon on any video to pin it here."
                            1 -> "Start streaming any offline video to track progress."
                            2 -> "Use the Admin Panel to import videos from device storage."
                            else -> "Your offline video library is empty."
                        },
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onPlay = { onPlayVideo(video) },
                        onDetails = { onVideoDetails(video) },
                        onDeleteRequest = { onDeleteVideoRequest(video) },
                        onToggleFavorite = { onToggleFavorite(video) },
                        cardWidth = 180.dp
                    )
                }
            }
        }
    }
}
