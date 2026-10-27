package com.example.data.repository

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.data.dao.AdminLogDao
import com.example.data.dao.VideoDao
import com.example.data.model.AdminLog
import com.example.data.model.VideoEntity
import com.example.data.util.VideoFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao,
    private val adminLogDao: AdminLogDao
) {
    private val TAG = "VideoRepository"

    val activeVideos: Flow<List<VideoEntity>> = videoDao.getAllActiveVideos()
    val featuredVideos: Flow<List<VideoEntity>> = videoDao.getFeaturedVideos()
    val favoriteVideos: Flow<List<VideoEntity>> = videoDao.getFavoriteVideos()
    val continueWatching: Flow<List<VideoEntity>> = videoDao.getContinueWatching()
    val trashedVideos: Flow<List<VideoEntity>> = videoDao.getTrashedVideos()
    val activeCount: Flow<Int> = videoDao.getActiveVideoCount()
    val trashedCount: Flow<Int> = videoDao.getTrashedVideoCount()
    val totalStorageBytes: Flow<Long?> = videoDao.getTotalActiveStorageBytes()
    val adminLogs: Flow<List<AdminLog>> = adminLogDao.getAllLogs()

    fun getVideo(id: Long): Flow<VideoEntity?> = videoDao.getVideoById(id)

    private fun extractRawVideo(rawResId: Int, fileName: String): File {
        val videosDir = File(context.filesDir, "offline_videos")
        if (!videosDir.exists()) videosDir.mkdirs()
        val destFile = File(videosDir, fileName)
        if (!destFile.exists() || destFile.length() < 1000) {
            context.resources.openRawResource(rawResId).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return destFile
    }

    /**
     * Initializes default offline video library on first start.
     */
    suspend fun initializeDefaultLibrary() = withContext(Dispatchers.IO) {
        val existing = activeVideos.firstOrNull() ?: emptyList()
        val trashed = trashedVideos.firstOrNull() ?: emptyList()
        if (existing.isNotEmpty() || trashed.isNotEmpty()) {
            return@withContext
        }

        Log.d(TAG, "Initializing default offline video library with demo clips...")

        // Extract pre-packaged authentic offline demo video clips
        val demo1 = extractRawVideo(R.raw.demo_scifi, "stellar_odyssey.mp4")
        val demo2 = extractRawVideo(R.raw.demo_nature, "wild_himalaya.mp4")
        val demo3 = extractRawVideo(R.raw.demo_cyberpunk, "neon_cyberpunk.mp4")
        val demo4 = extractRawVideo(R.raw.demo_tutorial, "jetpack_compose_masterclass.mp4")
        val demo5 = extractRawVideo(R.raw.demo_lofi, "ambient_night_lofi.mp4")

        val sampleVideos = listOf(
            VideoEntity(
                title = "Stellar Odyssey: Nebula Core",
                description = "Journey through uncharted star clusters and deep-space gravitational anomalies. Encoded in crisp offline 1080p.",
                category = "Sci-Fi & Action",
                videoUri = demo1?.absolutePath?.let { "file://$it" } ?: "",
                thumbnailUri = "hero_scifi",
                durationMs = 14000L,
                fileSizeBytes = demo1?.length() ?: (24L * 1024 * 1024),
                resolution = "1080p FHD",
                isFeatured = true,
                isFavorite = true,
                tags = "Sci-Fi, Space, Offline 1080p"
            ),
            VideoEntity(
                title = "Wild Himalaya: Snow Leopard's Realm",
                description = "An intimate wildlife documentary observing snow leopards in the high altitude peaks of the Karakoram range.",
                category = "Documentary & Nature",
                videoUri = demo2?.absolutePath?.let { "file://$it" } ?: "",
                thumbnailUri = "hero_nature",
                durationMs = 12000L,
                fileSizeBytes = demo2?.length() ?: (18L * 1024 * 1024),
                resolution = "4K UHD",
                isFeatured = true,
                isFavorite = false,
                tags = "Nature, 4K, Wildlife, Animals"
            ),
            VideoEntity(
                title = "Cyberpunk 2099: Neon Syndicate",
                description = "Night drive through the holographic canyons of Neo-Tokyo. High-speed cinematic chase with synthesizer pulse soundtrack.",
                category = "Sci-Fi & Action",
                videoUri = demo3?.absolutePath?.let { "file://$it" } ?: "",
                thumbnailUri = "hero_scifi",
                durationMs = 15000L,
                fileSizeBytes = demo3?.length() ?: (28L * 1024 * 1024),
                resolution = "1080p FHD",
                isFeatured = false,
                isFavorite = true,
                tags = "Cyberpunk, Neon, Action"
            ),
            VideoEntity(
                title = "Android Jetpack Compose Architecture",
                description = "Deep dive into clean architecture, unidirectional data flow, StateFlow, and offline-first Room persistence in Kotlin.",
                category = "Tech & Tutorials",
                videoUri = demo4?.absolutePath?.let { "file://$it" } ?: "",
                thumbnailUri = "hero_nature",
                durationMs = 10000L,
                fileSizeBytes = demo4?.length() ?: (15L * 1024 * 1024),
                resolution = "1080p FHD",
                isFeatured = false,
                isFavorite = false,
                tags = "Tech, Coding, Kotlin, Compose"
            ),
            VideoEntity(
                title = "Midnight Tokyo Lo-Fi Beats",
                description = "Relaxing lofi chill hop and rain sounds for late night studying, coding, or relaxation. 100% offline playback.",
                category = "Music & Chill",
                videoUri = demo5?.absolutePath?.let { "file://$it" } ?: "",
                thumbnailUri = "hero_scifi",
                durationMs = 16000L,
                fileSizeBytes = demo5?.length() ?: (20L * 1024 * 1024),
                resolution = "1080p FHD",
                isFeatured = false,
                isFavorite = true,
                tags = "Lo-Fi, Music, Chill, Relax"
            )
        )

        videoDao.insertVideos(sampleVideos)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "INITIALIZE_LIBRARY",
                videoTitle = "System Default Catalog",
                details = "Initialized 5 offline streaming videos with full metadata"
            )
        )
    }

    /**
     * Imports a video from user's device into internal offline storage.
     */
    suspend fun importVideo(
        sourceUri: Uri,
        title: String,
        description: String,
        category: String,
        tags: String
    ): VideoEntity? = withContext(Dispatchers.IO) {
        val destFile = VideoFileUtils.copyVideoToInternalStorage(context, sourceUri) ?: return@withContext null
        val info = VideoFileUtils.extractMetadata(context, destFile)

        val entity = VideoEntity(
            title = title.ifBlank { destFile.nameWithoutExtension },
            description = description.ifBlank { "Locally imported offline video stream." },
            category = category.ifBlank { "Custom Imports" },
            videoUri = "file://${destFile.absolutePath}",
            thumbnailUri = info.thumbnailPath ?: "hero_scifi",
            durationMs = info.durationMs,
            fileSizeBytes = info.fileSizeBytes,
            resolution = info.resolution,
            isFeatured = false,
            isFavorite = false,
            tags = tags.ifBlank { "Local, Imported" }
        )

        val id = videoDao.insertVideo(entity)
        val inserted = entity.copy(id = id)

        adminLogDao.insertLog(
            AdminLog(
                actionType = "IMPORT_VIDEO",
                videoTitle = inserted.title,
                details = "Imported video (${inserted.getFormattedSize()}, ${inserted.resolution}) into local offline storage"
            )
        )

        inserted
    }

    /**
     * Imports a downloaded video file (from device Downloads folder or file manager).
     */
    suspend fun importDownloadedVideo(
        sourceUri: Uri,
        title: String,
        description: String,
        category: String,
        tags: String,
        isFeatured: Boolean = false
    ): VideoEntity? = withContext(Dispatchers.IO) {
        val originalFileName = VideoFileUtils.getFileName(context, sourceUri)
        val destFile = VideoFileUtils.copyVideoToInternalStorage(context, sourceUri, originalFileName) ?: return@withContext null
        val info = VideoFileUtils.extractMetadata(context, destFile)

        val cleanTitle = title.ifBlank {
            originalFileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        }

        val entity = VideoEntity(
            title = cleanTitle,
            description = description.ifBlank { "Downloaded video file ($originalFileName) imported into offline library." },
            category = category.ifBlank { "Downloads" },
            videoUri = "file://${destFile.absolutePath}",
            thumbnailUri = info.thumbnailPath ?: "hero_scifi",
            durationMs = info.durationMs,
            fileSizeBytes = info.fileSizeBytes,
            resolution = info.resolution,
            isFeatured = isFeatured,
            isFavorite = false,
            tags = tags.ifBlank { "Downloaded, Offline, Local" }
        )

        val id = videoDao.insertVideo(entity)
        val inserted = entity.copy(id = id)

        adminLogDao.insertLog(
            AdminLog(
                actionType = "IMPORT_DOWNLOAD",
                videoTitle = inserted.title,
                details = "Imported downloaded file ($originalFileName, ${inserted.getFormattedSize()}, ${inserted.resolution}) into offline catalog"
            )
        )

        inserted
    }

    /**
     * Creates an on-device synthetic demo video directly from the Admin Panel.
     */
    suspend fun createQuickDemoVideo(
        title: String,
        category: String,
        themeHex: Int
    ): VideoEntity? = withContext(Dispatchers.IO) {
        val safeName = title.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' } + "_${System.currentTimeMillis()}.mp4"
        val rawRes = when (category) {
            "Sci-Fi & Action" -> R.raw.demo_scifi
            "Documentary & Nature" -> R.raw.demo_nature
            "Tech & Tutorials" -> R.raw.demo_tutorial
            "Music & Chill" -> R.raw.demo_lofi
            else -> R.raw.demo_cyberpunk
        }
        val file = extractRawVideo(rawRes, safeName)
        val info = VideoFileUtils.extractMetadata(context, file)

        val entity = VideoEntity(
            title = title,
            description = "Synthesized offline streaming demo stream.",
            category = category,
            videoUri = "file://${file.absolutePath}",
            thumbnailUri = info.thumbnailPath ?: "hero_scifi",
            durationMs = info.durationMs,
            fileSizeBytes = info.fileSizeBytes,
            resolution = "1080p FHD",
            isFeatured = false,
            isFavorite = false,
            tags = "Demo, Stream, Offline"
        )
        val id = videoDao.insertVideo(entity)
        val inserted = entity.copy(id = id)

        adminLogDao.insertLog(
            AdminLog(
                actionType = "CREATE_DEMO",
                videoTitle = inserted.title,
                details = "Admin created offline demo stream ${inserted.getFormattedSize()}"
            )
        )
        inserted
    }

    /**
     * Soft deletes video (moves to trash).
     */
    suspend fun softDeleteVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        videoDao.softDeleteVideo(video.id)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "DELETE_VIDEO",
                videoTitle = video.title,
                details = "Moved video to trash (id=${video.id})"
            )
        )
    }

    /**
     * Soft deletes multiple videos.
     */
    suspend fun softDeleteBatch(videos: List<VideoEntity>) = withContext(Dispatchers.IO) {
        val ids = videos.map { it.id }
        videoDao.softDeleteBatch(ids)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "BATCH_DELETE",
                videoTitle = "${videos.size} videos",
                details = "Batch deleted: ${videos.joinToString { it.title }}"
            )
        )
    }

    /**
     * Restores a video from trash.
     */
    suspend fun restoreVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        videoDao.restoreVideo(video.id)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "RESTORE_VIDEO",
                videoTitle = video.title,
                details = "Restored video from trash"
            )
        )
    }

    /**
     * Restores all trashed videos.
     */
    suspend fun restoreAllTrashed() = withContext(Dispatchers.IO) {
        videoDao.restoreAllTrashed()
        adminLogDao.insertLog(
            AdminLog(
                actionType = "RESTORE_ALL",
                videoTitle = "All Trashed Videos",
                details = "Restored all videos in trash"
            )
        )
    }

    /**
     * Permanently deletes a video, removing its database entry AND local video/thumbnail files.
     */
    suspend fun permanentDeleteVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        VideoFileUtils.deletePhysicalFiles(video.videoUri, video.thumbnailUri)
        videoDao.permanentDeleteVideo(video.id)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "PERMANENT_DELETE",
                videoTitle = video.title,
                details = "Permanently erased video and storage file (${video.getFormattedSize()} freed)"
            )
        )
    }

    /**
     * Permanently deletes multiple videos and removes physical files.
     */
    suspend fun permanentDeleteBatch(videos: List<VideoEntity>) = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        videos.forEach { video ->
            VideoFileUtils.deletePhysicalFiles(video.videoUri, video.thumbnailUri)
            totalFreed += video.fileSizeBytes
        }
        val ids = videos.map { it.id }
        videoDao.permanentDeleteBatch(ids)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "BATCH_PERMANENT_DELETE",
                videoTitle = "${videos.size} videos",
                details = "Permanently deleted ${videos.size} videos, freed ${totalFreed / (1024 * 1024)} MB"
            )
        )
    }

    /**
     * Empties entire trash bin permanently.
     */
    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        val trashed = videoDao.getTrashedVideosSync()
        var totalFreed = 0L
        trashed.forEach { video ->
            VideoFileUtils.deletePhysicalFiles(video.videoUri, video.thumbnailUri)
            totalFreed += video.fileSizeBytes
        }
        videoDao.permanentDeleteAllTrashed()
        adminLogDao.insertLog(
            AdminLog(
                actionType = "EMPTY_TRASH",
                videoTitle = "Trash Bin",
                details = "Emptied trash: permanently removed ${trashed.size} videos, freed ${totalFreed / (1024 * 1024)} MB"
            )
        )
    }

    suspend fun updateWatchProgress(id: Long, progressMs: Long) = withContext(Dispatchers.IO) {
        videoDao.updateWatchProgress(id, progressMs)
    }

    suspend fun incrementViews(id: Long) = withContext(Dispatchers.IO) {
        videoDao.incrementViews(id)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setFavorite(id, isFavorite)
    }

    suspend fun updateVideo(video: VideoEntity) = withContext(Dispatchers.IO) {
        videoDao.updateVideo(video)
        adminLogDao.insertLog(
            AdminLog(
                actionType = "EDIT_VIDEO",
                videoTitle = video.title,
                details = "Updated video metadata (category=${video.category}, featured=${video.isFeatured})"
            )
        )
    }

    suspend fun clearAdminLogs() = withContext(Dispatchers.IO) {
        adminLogDao.clearLogs()
    }

    suspend fun resetLibraryToDefault() = withContext(Dispatchers.IO) {
        // Delete all files in offline_videos
        val vDir = File(context.filesDir, "offline_videos")
        vDir.deleteRecursively()
        val tDir = File(context.filesDir, "thumbnails")
        tDir.deleteRecursively()

        videoDao.deleteAllVideos()
        adminLogDao.clearLogs()
        initializeDefaultLibrary()
    }
}
