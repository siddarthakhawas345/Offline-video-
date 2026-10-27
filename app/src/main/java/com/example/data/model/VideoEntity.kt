package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val videoUri: String,
    val thumbnailUri: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val resolution: String = "1080p FHD",
    val codec: String = "H.264 / AAC",
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val watchProgressMs: Long = 0L,
    val viewsCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val tags: String = "Offline, 1080p"
) {
    fun getFormattedDuration(): String {
        val totalSecs = durationMs / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val hours = mins / 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, mins % 60, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    fun getFormattedSize(): String {
        val mb = fileSizeBytes.toDouble() / (1024 * 1024)
        return if (mb >= 1024) {
            String.format("%.2f GB", mb / 1024)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
