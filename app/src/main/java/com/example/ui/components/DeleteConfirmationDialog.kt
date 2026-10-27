package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.StreamError
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun DeleteConfirmationDialog(
    video: VideoEntity,
    defaultPermanent: Boolean = false,
    onConfirm: (permanent: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var permanentDelete by remember { mutableStateOf(defaultPermanent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaSurface,
        titleContentColor = TextWhite,
        textContentColor = TextMuted,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(StreamError.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (permanentDelete) Icons.Default.DeleteForever else Icons.Default.Delete,
                    contentDescription = "Delete Icon",
                    tint = StreamError,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = if (permanentDelete) "Permanently Delete Video?" else "Delete Video?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Video preview chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaSurfaceElevated)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp, 36.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        VideoThumbnailImage(
                            thumbnailUri = video.thumbnailUri,
                            contentDescription = video.title
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${video.category} • ${video.getFormattedSize()}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete options
                Text(
                    text = "Select Deletion Type:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Soft delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { permanentDelete = false }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !permanentDelete,
                        onClick = { permanentDelete = false },
                        colors = RadioButtonDefaults.colors(selectedColor = StreamPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Move to Trash", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Removes from catalog; can be restored later", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Permanent delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { permanentDelete = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = permanentDelete,
                        onClick = { permanentDelete = true },
                        colors = RadioButtonDefaults.colors(selectedColor = StreamError)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Permanently Erase File", color = StreamError, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Deletes physical file and reclaims ${video.getFormattedSize()}", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(permanentDelete) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (permanentDelete) StreamError else StreamPrimary
                ),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(
                    text = if (permanentDelete) "Erase Permanently" else "Move to Trash",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
