package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos WHERE isDeleted = 0 ORDER BY dateAdded DESC")
    fun getAllActiveVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDeleted = 0 AND isFeatured = 1 ORDER BY dateAdded DESC")
    fun getFeaturedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDeleted = 0 AND watchProgressMs > 0 ORDER BY dateAdded DESC")
    fun getContinueWatching(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDeleted = 1 ORDER BY dateAdded DESC")
    fun getTrashedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isDeleted = 0 AND category = :category ORDER BY dateAdded DESC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT DISTINCT category FROM videos WHERE isDeleted = 0")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    fun getVideoById(id: Long): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoByIdSync(id: Long): VideoEntity?

    @Query("SELECT * FROM videos WHERE isDeleted = 1")
    suspend fun getTrashedVideosSync(): List<VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteVideo(id: Long)

    @Query("UPDATE videos SET isDeleted = 1 WHERE id IN (:ids)")
    suspend fun softDeleteBatch(ids: List<Long>)

    @Query("UPDATE videos SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreVideo(id: Long)

    @Query("UPDATE videos SET isDeleted = 0 WHERE isDeleted = 1")
    suspend fun restoreAllTrashed()

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun permanentDeleteVideo(id: Long)

    @Query("DELETE FROM videos WHERE id IN (:ids)")
    suspend fun permanentDeleteBatch(ids: List<Long>)

    @Query("DELETE FROM videos WHERE isDeleted = 1")
    suspend fun permanentDeleteAllTrashed()

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("SELECT COUNT(*) FROM videos WHERE isDeleted = 0")
    fun getActiveVideoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM videos WHERE isDeleted = 1")
    fun getTrashedVideoCount(): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM videos WHERE isDeleted = 0")
    fun getTotalActiveStorageBytes(): Flow<Long?>

    @Query("UPDATE videos SET watchProgressMs = :progressMs WHERE id = :id")
    suspend fun updateWatchProgress(id: Long, progressMs: Long)

    @Query("UPDATE videos SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: Long)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
