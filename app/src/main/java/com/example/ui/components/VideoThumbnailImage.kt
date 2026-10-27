package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.StreamPrimary
import java.io.File

@Composable
fun VideoThumbnailImage(
    thumbnailUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(CinemaSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            thumbnailUri == "hero_scifi" -> {
                Image(
                    painter = painterResource(id = R.drawable.hero_scifi),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            thumbnailUri == "hero_nature" -> {
                Image(
                    painter = painterResource(id = R.drawable.hero_nature),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            !thumbnailUri.isNullOrEmpty() && (thumbnailUri.startsWith("/") || thumbnailUri.startsWith("file://")) -> {
                val cleanPath = thumbnailUri.removePrefix("file://")
                val file = File(cleanPath)
                if (file.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(file)
                            .crossfade(true)
                            .build(),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FallbackThumbnail()
                }
            }
            else -> {
                FallbackThumbnail()
            }
        }
    }
}

@Composable
private fun FallbackThumbnail() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = StreamPrimary.copy(alpha = 0.6f)
        )
    }
}
