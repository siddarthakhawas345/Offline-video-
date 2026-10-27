package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.VStreamDatabase
import com.example.data.model.AdminLog
import com.example.data.model.VideoEntity
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    BROWSE,
    LIBRARY,
    ADMIN
}

enum class AdminTab {
    VIDEOS,
    TRASH,
    LOGS,
    STORAGE
}

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository
    private val prefs = application.getSharedPreferences("vstream_prefs", Context.MODE_PRIVATE)

    val currentScreen = MutableStateFlow(AppScreen.HOME)
    val playingVideo = MutableStateFlow<VideoEntity?>(null)
    val detailsVideo = MutableStateFlow<VideoEntity?>(null)

    // Search and filter in Browse
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    // Admin state
    val isAdminUnlocked = MutableStateFlow(false)
    val adminTab = MutableStateFlow(AdminTab.VIDEOS)
    val adminSearch = MutableStateFlow("")
    val selectedBatchIds = MutableStateFlow<Set<Long>>(emptySet())
    val isBatchMode = MutableStateFlow(false)

    // Delete dialog states
    val videoPendingDelete = MutableStateFlow<VideoEntity?>(null)
    val isPermanentDeleteChoice = MutableStateFlow(false)
    val showBatchDeleteDialog = MutableStateFlow(false)

    // Notification / Toast
    val snackbarMessage = MutableStateFlow<String?>(null)

    val activeVideos: StateFlow<List<VideoEntity>>
    val featuredVideos: StateFlow<List<VideoEntity>>
    val favoriteVideos: StateFlow<List<VideoEntity>>
    val continueWatching: StateFlow<List<VideoEntity>>
    val trashedVideos: StateFlow<List<VideoEntity>>
    val activeCount: StateFlow<Int>
    val trashedCount: StateFlow<Int>
    val totalStorageBytes: StateFlow<Long>
    val adminLogs: StateFlow<List<AdminLog>>

    init {
        val db = VStreamDatabase.getDatabase(application)
        repository = VideoRepository(application, db.videoDao(), db.adminLogDao())

        activeVideos = repository.activeVideos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        featuredVideos = repository.featuredVideos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        favoriteVideos = repository.favoriteVideos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        continueWatching = repository.continueWatching
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        trashedVideos = repository.trashedVideos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeCount = repository.activeCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        trashedCount = repository.trashedCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        totalStorageBytes = repository.totalStorageBytes
            .combine(activeVideos) { bytes, list ->
                bytes ?: list.sumOf { it.fileSizeBytes }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

        adminLogs = repository.adminLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Preload default offline library if empty
        viewModelScope.launch {
            repository.initializeDefaultLibrary()
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen.value = screen
    }

    fun playVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.incrementViews(video.id)
            playingVideo.value = video
        }
    }

    fun closePlayer() {
        playingVideo.value = null
    }

    fun openDetails(video: VideoEntity) {
        detailsVideo.value = video
    }

    fun closeDetails() {
        detailsVideo.value = null
    }

    fun toggleFavorite(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, !video.isFavorite)
        }
    }

    fun updateProgress(video: VideoEntity, progressMs: Long) {
        viewModelScope.launch {
            repository.updateWatchProgress(video.id, progressMs)
        }
    }

    fun unlockAdmin(pin: String): Boolean {
        val storedPin = prefs.getString("admin_pin", "1234") ?: "1234"
        return if (pin == storedPin) {
            isAdminUnlocked.value = true
            showSnackbar("Admin mode unlocked")
            true
        } else {
            showSnackbar("Incorrect Admin PIN")
            false
        }
    }

    fun lockAdmin() {
        isAdminUnlocked.value = false
        selectedBatchIds.value = emptySet()
        isBatchMode.value = false
        showSnackbar("Admin mode locked")
    }

    fun changeAdminPin(newPin: String) {
        if (newPin.length >= 4) {
            prefs.edit().putString("admin_pin", newPin).apply()
            showSnackbar("Admin PIN updated successfully")
        }
    }

    // Video Delete Functions
    fun requestDeleteVideo(video: VideoEntity, permanent: Boolean) {
        videoPendingDelete.value = video
        isPermanentDeleteChoice.value = permanent
    }

    fun confirmDeleteVideo() {
        val video = videoPendingDelete.value ?: return
        val isPermanent = isPermanentDeleteChoice.value
        viewModelScope.launch {
            if (isPermanent) {
                repository.permanentDeleteVideo(video)
                showSnackbar("Permanently erased '${video.title}' and freed disk space")
            } else {
                repository.softDeleteVideo(video)
                showSnackbar("Moved '${video.title}' to Trash")
            }
            if (playingVideo.value?.id == video.id) {
                playingVideo.value = null
            }
            if (detailsVideo.value?.id == video.id) {
                detailsVideo.value = null
            }
            videoPendingDelete.value = null
        }
    }

    fun cancelDeleteVideo() {
        videoPendingDelete.value = null
    }

    fun restoreVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.restoreVideo(video)
            showSnackbar("Restored '${video.title}' to active catalog")
        }
    }

    fun restoreAllTrashed() {
        viewModelScope.launch {
            repository.restoreAllTrashed()
            showSnackbar("Restored all trashed videos")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            showSnackbar("Trash bin emptied. Storage permanently reclaimed!")
        }
    }

    // Batch Selection & Deletion
    fun toggleBatchSelection(videoId: Long) {
        val current = selectedBatchIds.value.toMutableSet()
        if (current.contains(videoId)) {
            current.remove(videoId)
        } else {
            current.add(videoId)
        }
        selectedBatchIds.value = current
    }

    fun selectAllActive(list: List<VideoEntity>) {
        selectedBatchIds.value = list.map { it.id }.toSet()
    }

    fun clearBatchSelection() {
        selectedBatchIds.value = emptySet()
        isBatchMode.value = false
    }

    fun confirmBatchDelete(permanent: Boolean) {
        val ids = selectedBatchIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            val list = if (permanent) {
                trashedVideos.value.filter { ids.contains(it.id) }
                    .ifEmpty { activeVideos.value.filter { ids.contains(it.id) } }
            } else {
                activeVideos.value.filter { ids.contains(it.id) }
            }

            if (permanent) {
                repository.permanentDeleteBatch(list)
                showSnackbar("Permanently erased ${list.size} videos from storage")
            } else {
                repository.softDeleteBatch(list)
                showSnackbar("Moved ${list.size} videos to Trash")
            }
            clearBatchSelection()
            showBatchDeleteDialog.value = false
        }
    }

    // Import from device
    fun importVideoFromDevice(
        uri: Uri,
        title: String,
        description: String,
        category: String,
        tags: String
    ) {
        viewModelScope.launch {
            showSnackbar("Importing and indexing video for offline playback...")
            val result = repository.importVideo(uri, title, description, category, tags)
            if (result != null) {
                showSnackbar("Imported '${result.title}' (${result.getFormattedSize()}) successfully!")
            } else {
                showSnackbar("Failed to import video. Unsupported format.")
            }
        }
    }

    // Import downloaded video file from Downloads or File Picker
    fun importDownloadedVideoFile(
        uri: Uri,
        title: String,
        description: String,
        category: String,
        tags: String,
        isFeatured: Boolean = false
    ) {
        viewModelScope.launch {
            showSnackbar("Importing downloaded video into offline catalog...")
            val result = repository.importDownloadedVideo(uri, title, description, category, tags, isFeatured)
            if (result != null) {
                showSnackbar("Downloaded file '${result.title}' (${result.getFormattedSize()}) imported!")
            } else {
                showSnackbar("Failed to import downloaded video file.")
            }
        }
    }

    // Quick demo video creation from admin
    fun createDemoVideo(title: String, category: String, colorHex: Int) {
        viewModelScope.launch {
            showSnackbar("Synthesizing offline test stream...")
            val created = repository.createQuickDemoVideo(title, category, colorHex)
            if (created != null) {
                showSnackbar("Created demo video '${created.title}'")
            } else {
                showSnackbar("Could not create demo stream")
            }
        }
    }

    // Update video metadata
    fun updateVideoMetadata(video: VideoEntity) {
        viewModelScope.launch {
            repository.updateVideo(video)
            showSnackbar("Updated video metadata")
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetLibraryToDefault()
            showSnackbar("Reset offline catalog to factory defaults")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAdminLogs()
            showSnackbar("Admin logs cleared")
        }
    }

    fun showSnackbar(message: String) {
        snackbarMessage.value = message
    }

    fun dismissSnackbar() {
        snackbarMessage.value = null
    }
}
