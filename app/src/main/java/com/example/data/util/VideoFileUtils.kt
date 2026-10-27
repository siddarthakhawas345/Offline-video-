package com.example.data.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object VideoFileUtils {

    private const val TAG = "VideoFileUtils"

    data class ExtractedVideoInfo(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val resolution: String,
        val fileSizeBytes: Long,
        val thumbnailPath: String?
    )

    /**
     * Resolves display file name from content or file URI (e.g. from Downloads).
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "downloaded_video_${System.currentTimeMillis()}.mp4"
        try {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)?.let { name = it }
                    }
                }
            } else if (uri.scheme == "file") {
                uri.path?.let { p ->
                    val f = File(p)
                    if (f.name.isNotBlank()) name = f.name
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve file name: ${e.message}")
        }
        return name
    }

    /**
     * Resolves file size in bytes from URI.
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        try {
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            } else if (uri.scheme == "file") {
                uri.path?.let { size = File(it).length() }
            }
        } catch (_: Exception) {}
        return size
    }

    /**
     * Copies a video from content:// URI into the app's internal offline_videos folder.
     */
    suspend fun copyVideoToInternalStorage(
        context: Context,
        sourceUri: Uri,
        preferredFileName: String? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            val videosDir = File(context.filesDir, "offline_videos")
            if (!videosDir.exists()) videosDir.mkdirs()

            val rawName = preferredFileName ?: getFileName(context, sourceUri)
            val safeName = rawName.filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
                .ifBlank { "download_${System.currentTimeMillis()}.mp4" }

            val destFile = File(videosDir, "dl_${System.currentTimeMillis()}_$safeName")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy video to internal storage: ${e.message}", e)
            null
        }
    }

    /**
     * Extracts duration, resolution, size, and generates a thumbnail frame.
     */
    suspend fun extractMetadata(context: Context, videoFile: File): ExtractedVideoInfo = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var durationMs = 15000L
        var width = 1920
        var height = 1080
        var thumbnailPath: String? = null

        try {
            retriever.setDataSource(videoFile.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!durationStr.isNullOrEmpty()) {
                durationMs = durationStr.toLongOrNull() ?: 15000L
            }

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            if (!widthStr.isNullOrEmpty()) width = widthStr.toIntOrNull() ?: 1920
            if (!heightStr.isNullOrEmpty()) height = heightStr.toIntOrNull() ?: 1080

            // Extract frame at 1s for thumbnail
            val frameBitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime

            if (frameBitmap != null) {
                val thumbDir = File(context.filesDir, "thumbnails")
                if (!thumbDir.exists()) thumbDir.mkdirs()

                val thumbFile = File(thumbDir, "thumb_${videoFile.nameWithoutExtension}.jpg")
                FileOutputStream(thumbFile).use { out ->
                    frameBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                thumbnailPath = thumbFile.absolutePath
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting metadata: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing retriever", e)
            }
        }

        val resolution = when {
            width >= 3840 || height >= 2160 -> "4K UHD"
            width >= 1920 || height >= 1080 -> "1080p FHD"
            width >= 1280 || height >= 720 -> "720p HD"
            else -> "${width}x${height}"
        }

        ExtractedVideoInfo(
            durationMs = durationMs,
            width = width,
            height = height,
            resolution = resolution,
            fileSizeBytes = videoFile.length(),
            thumbnailPath = thumbnailPath
        )
    }

    /**
     * Permanently deletes physical files from device storage when video is permanently deleted.
     */
    suspend fun deletePhysicalFiles(videoUri: String, thumbnailUri: String?): Boolean = withContext(Dispatchers.IO) {
        var success = true
        try {
            // Delete video file
            val videoPath = when {
                videoUri.startsWith("file://") -> videoUri.removePrefix("file://")
                videoUri.startsWith("/") -> videoUri
                else -> null
            }
            if (videoPath != null) {
                val vFile = File(videoPath)
                if (vFile.exists()) {
                    val deleted = vFile.delete()
                    if (!deleted) success = false
                    Log.d(TAG, "Deleted video file: $videoPath (success=$deleted)")
                }
            }

            // Delete thumbnail file
            if (!thumbnailUri.isNullOrEmpty() && (thumbnailUri.startsWith("/") || thumbnailUri.startsWith("file://"))) {
                val thumbPath = thumbnailUri.removePrefix("file://")
                val tFile = File(thumbPath)
                if (tFile.exists()) {
                    tFile.delete()
                    Log.d(TAG, "Deleted thumbnail file: $thumbPath")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete physical files: ${e.message}", e)
            success = false
        }
        success
    }

    /**
     * Calculates storage size used by offline video directory.
     */
    suspend fun getAppVideoStorageBytes(context: Context): Long = withContext(Dispatchers.IO) {
        var total = 0L
        val dir = File(context.filesDir, "offline_videos")
        if (dir.exists()) {
            dir.walkTopDown().forEach { file ->
                if (file.isFile) total += file.length()
            }
        }
        val thumbDir = File(context.filesDir, "thumbnails")
        if (thumbDir.exists()) {
            thumbDir.walkTopDown().forEach { file ->
                if (file.isFile) total += file.length()
            }
        }
        total
    }
}
