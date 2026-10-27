package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.VideoDetailsBottomSheet
import com.example.ui.components.VideoPlayerView
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VideoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: VideoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playingVideo by viewModel.playingVideo.collectAsStateWithLifecycle()
    val detailsVideo by viewModel.detailsVideo.collectAsStateWithLifecycle()
    val videoPendingDelete by viewModel.videoPendingDelete.collectAsStateWithLifecycle()
    val isPermanentDeleteChoice by viewModel.isPermanentDeleteChoice.collectAsStateWithLifecycle()

    val activeVideos by viewModel.activeVideos.collectAsStateWithLifecycle()
    val featuredVideos by viewModel.featuredVideos.collectAsStateWithLifecycle()
    val favoriteVideos by viewModel.favoriteVideos.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val trashedVideos by viewModel.trashedVideos.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()
    val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    // Back handling
    BackHandler(enabled = currentScreen != AppScreen.HOME && playingVideo == null && detailsVideo == null) {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CinemaBlack,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CinemaSurface,
                    contentColor = TextWhite
                )
            }
        },
        bottomBar = {
            if (playingVideo == null) {
                NavigationBar(
                    containerColor = CinemaSurface,
                    contentColor = TextWhite,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Stream", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StreamPrimary,
                            selectedTextColor = StreamPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = StreamPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.BROWSE,
                        onClick = { viewModel.navigateTo(AppScreen.BROWSE) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.BROWSE) Icons.Filled.Explore else Icons.Outlined.Explore,
                                contentDescription = "Browse"
                            )
                        },
                        label = { Text("Browse", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StreamPrimary,
                            selectedTextColor = StreamPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = StreamPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_browse")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.LIBRARY,
                        onClick = { viewModel.navigateTo(AppScreen.LIBRARY) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.LIBRARY) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StreamPrimary,
                            selectedTextColor = StreamPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = StreamPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_library")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.ADMIN,
                        onClick = { viewModel.navigateTo(AppScreen.ADMIN) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Admin"
                            )
                        },
                        label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StreamPrimary,
                            selectedTextColor = StreamPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = StreamPrimary.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (playingVideo == null) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    featuredVideos = featuredVideos,
                    activeVideos = activeVideos,
                    continueWatching = continueWatching,
                    isAdminUnlocked = isAdminUnlocked,
                    onPlayVideo = { video -> viewModel.playVideo(video) },
                    onVideoDetails = { video -> viewModel.openDetails(video) },
                    onDeleteVideoRequest = { video -> viewModel.requestDeleteVideo(video, false) },
                    onToggleFavorite = { video -> viewModel.toggleFavorite(video) },
                    onNavigateToAdmin = { viewModel.navigateTo(AppScreen.ADMIN) }
                )

                AppScreen.BROWSE -> BrowseScreen(
                    videos = activeVideos,
                    onPlayVideo = { video -> viewModel.playVideo(video) },
                    onVideoDetails = { video -> viewModel.openDetails(video) },
                    onDeleteVideoRequest = { video -> viewModel.requestDeleteVideo(video, false) },
                    onToggleFavorite = { video -> viewModel.toggleFavorite(video) }
                )

                AppScreen.LIBRARY -> LibraryScreen(
                    activeVideos = activeVideos,
                    favoriteVideos = favoriteVideos,
                    continueWatching = continueWatching,
                    totalStorageBytes = totalStorageBytes,
                    onPlayVideo = { video -> viewModel.playVideo(video) },
                    onVideoDetails = { video -> viewModel.openDetails(video) },
                    onDeleteVideoRequest = { video -> viewModel.requestDeleteVideo(video, false) },
                    onToggleFavorite = { video -> viewModel.toggleFavorite(video) }
                )

                AppScreen.ADMIN -> AdminPanelScreen(
                    isAdminUnlocked = isAdminUnlocked,
                    activeVideos = activeVideos,
                    trashedVideos = trashedVideos,
                    adminLogs = adminLogs,
                    totalStorageBytes = totalStorageBytes,
                    onUnlockAdmin = { pin -> viewModel.unlockAdmin(pin) },
                    onLockAdmin = { viewModel.lockAdmin() },
                    onChangePin = { newPin -> viewModel.changeAdminPin(newPin) },
                    onPlayVideo = { video -> viewModel.playVideo(video) },
                    onDeleteVideoRequest = { video, permanent -> viewModel.requestDeleteVideo(video, permanent) },
                    onRestoreVideo = { video -> viewModel.restoreVideo(video) },
                    onRestoreAllTrashed = { viewModel.restoreAllTrashed() },
                    onEmptyTrash = { viewModel.emptyTrash() },
                    onBatchDeleteRequest = { videos, permanent ->
                        videos.forEach { viewModel.toggleBatchSelection(it.id) }
                        viewModel.confirmBatchDelete(permanent)
                    },
                    onImportVideo = { uri, title, desc, cat, tags ->
                        viewModel.importVideoFromDevice(uri, title, desc, cat, tags)
                    },
                    onImportDownloadedVideo = { uri, title, desc, cat, tags, isFeatured ->
                        viewModel.importDownloadedVideoFile(uri, title, desc, cat, tags, isFeatured)
                    },
                    onCreateDemoVideo = { title, cat, hex ->
                        viewModel.createDemoVideo(title, cat, hex)
                    },
                    onUpdateVideo = { video -> viewModel.updateVideoMetadata(video) },
                    onResetToDefaults = { viewModel.resetToDefaults() },
                    onClearLogs = { viewModel.clearLogs() }
                )
            }

            // Fullscreen Player Overlay
            AnimatedVisibility(
                visible = playingVideo != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                playingVideo?.let { video ->
                    VideoPlayerView(
                        video = video,
                        onClose = { viewModel.closePlayer() },
                        onProgressUpdate = { pos -> viewModel.updateProgress(video, pos) },
                        onToggleFavorite = { v -> viewModel.toggleFavorite(v) },
                        onDeleteRequest = { v -> viewModel.requestDeleteVideo(v, false) }
                    )
                }
            }

            // Video Details Modal Sheet
            if (detailsVideo != null) {
                VideoDetailsBottomSheet(
                    video = detailsVideo!!,
                    onPlay = {
                        val v = detailsVideo!!
                        viewModel.closeDetails()
                        viewModel.playVideo(v)
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(detailsVideo!!) },
                    onDeleteRequest = {
                        val v = detailsVideo!!
                        viewModel.closeDetails()
                        viewModel.requestDeleteVideo(v, false)
                    },
                    onDismiss = { viewModel.closeDetails() }
                )
            }

            // Video Delete Confirmation Dialog
            if (videoPendingDelete != null) {
                DeleteConfirmationDialog(
                    video = videoPendingDelete!!,
                    defaultPermanent = isPermanentDeleteChoice,
                    onConfirm = { permanent ->
                        viewModel.confirmDeleteVideo()
                    },
                    onDismiss = { viewModel.cancelDeleteVideo() }
                )
            }
        }
    }
}
