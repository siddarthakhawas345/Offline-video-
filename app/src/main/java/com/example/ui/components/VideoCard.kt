package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.StreamError
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun VideoCard(
    video: VideoEntity,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    onDeleteRequest: () -> Unit,
    onToggleFavorite: () -> Unit,
    cardWidth: Dp = 220.dp,
    aspectRatio: Float = 16f / 9f,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onPlay() }
            .testTag("video_card_${video.id}")
    ) {
        // Thumbnail Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(10.dp))
                .background(CinemaSurfaceVariant)
        ) {
            VideoThumbnailImage(
                thumbnailUri = video.thumbnailUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.getFormattedDuration(),
                    color = TextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Offline Tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StreamSuccess.copy(alpha = 0.9f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "OFFLINE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Watch progress bar
            if (video.watchProgressMs > 0 && video.durationMs > 0) {
                LinearProgressIndicator(
                    progress = { (video.watchProgressMs.toFloat() / video.durationMs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(3.dp),
                    color = StreamPrimary,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.category} • ${video.getFormattedSize()}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            // Options menu button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("video_options_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CinemaBlack)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Video", color = TextWhite) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = StreamPrimary) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details & Specs", color = TextWhite) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = TextMuted) },
                        onClick = {
                            showMenu = false
                            onDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (video.isFavorite) "Remove Favorite" else "Add to Favorites",
                                color = TextWhite
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                tint = if (video.isFavorite) StreamError else TextMuted
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleFavorite()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Video", color = StreamError, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = StreamError) },
                        onClick = {
                            showMenu = false
                            onDeleteRequest()
                        }
                    )
                }
            }
        }
    }
}
