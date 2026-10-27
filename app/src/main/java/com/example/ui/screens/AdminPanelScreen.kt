package com.example.ui.screens

import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.platform.LocalContext
import com.example.data.util.VideoFileUtils
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminLog
import com.example.data.model.VideoEntity
import com.example.ui.components.VideoThumbnailImage
import com.example.ui.theme.CinemaBlack
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.StreamAccent
import com.example.ui.theme.StreamError
import com.example.ui.theme.StreamPrimary
import com.example.ui.theme.StreamSuccess
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AdminTab

@Composable
fun AdminPanelScreen(
    isAdminUnlocked: Boolean,
    activeVideos: List<VideoEntity>,
    trashedVideos: List<VideoEntity>,
    adminLogs: List<AdminLog>,
    totalStorageBytes: Long,
    onUnlockAdmin: (String) -> Boolean,
    onLockAdmin: () -> Unit,
    onChangePin: (String) -> Unit,
    onPlayVideo: (VideoEntity) -> Unit,
    onDeleteVideoRequest: (video: VideoEntity, permanent: Boolean) -> Unit,
    onRestoreVideo: (VideoEntity) -> Unit,
    onRestoreAllTrashed: () -> Unit,
    onEmptyTrash: () -> Unit,
    onBatchDeleteRequest: (videos: List<VideoEntity>, permanent: Boolean) -> Unit,
    onImportVideo: (uri: Uri, title: String, desc: String, category: String, tags: String) -> Unit,
    onImportDownloadedVideo: (uri: Uri, title: String, desc: String, category: String, tags: String, isFeatured: Boolean) -> Unit,
    onCreateDemoVideo: (title: String, category: String, colorHex: Int) -> Unit,
    onUpdateVideo: (VideoEntity) -> Unit,
    onResetToDefaults: () -> Unit,
    onClearLogs: () -> Unit
) {
    if (!isAdminUnlocked) {
        AdminLockScreen(onUnlock = onUnlockAdmin)
        return
    }

    var selectedTab by remember { mutableStateOf(AdminTab.VIDEOS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isBatchSelectionMode by remember { mutableStateOf(false) }

    // Dialog states
    var showImportDialog by remember { mutableStateOf(false) }
    var isDownloadedFileImport by remember { mutableStateOf(false) }
    var showCreateDemoDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<VideoEntity?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showEmptyTrashConfirmDialog by remember { mutableStateOf(false) }
    var pickedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery / Media Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedVideoUri = uri
            isDownloadedFileImport = false
            showImportDialog = true
        }
    }

    // Document / Downloads file picker launcher (targets downloaded files)
    val downloadFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickedVideoUri = uri
            isDownloadedFileImport = true
            showImportDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("admin_panel_screen")
    ) {
        // Top Admin Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StreamError.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Admin",
                        tint = StreamError,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ADMIN CONSOLE",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Authorized Access • Video Management & Purge",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Lock Button
            OutlinedButton(
                onClick = onLockAdmin,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StreamError),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("admin_lock_button")
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lock", fontSize = 12.sp)
            }
        }

        // 4 KPI Metric Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(
                title = "Active Videos",
                value = "${activeVideos.size}",
                color = StreamPrimary,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "In Trash",
                value = "${trashedVideos.size}",
                color = if (trashedVideos.isNotEmpty()) StreamError else TextMuted,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Storage",
                value = formatBytes(totalStorageBytes),
                color = StreamSuccess,
                modifier = Modifier.weight(1f)
            )
            AdminMetricCard(
                title = "Total Views",
                value = "${activeVideos.sumOf { it.viewsCount }}",
                color = StreamAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Admin Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Option: Import Downloaded Video File (From Downloads / File Manager)
                Button(
                    onClick = {
                        downloadFilePickerLauncher.launch(
                            arrayOf("video/*", "video/mp4", "video/mkv", "video/webm", "video/3gpp", "video/x-matroska", "application/octet-stream")
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("admin_import_download_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Download File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Media Gallery Picker
                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("admin_import_video_button")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery", fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Create Demo Video
                OutlinedButton(
                    onClick = { showCreateDemoDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_create_demo_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Demo Stream", fontSize = 12.sp)
                }

                // Batch selection toggle
                OutlinedButton(
                    onClick = {
                        isBatchSelectionMode = !isBatchSelectionMode
                        if (!isBatchSelectionMode) selectedBatchIds = emptySet()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isBatchSelectionMode) StreamAccent else TextWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_batch_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isBatchSelectionMode) Icons.Default.CheckCircle else Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBatchSelectionMode) "Done Selecting" else "Batch Delete", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Admin Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = CinemaBlack,
            contentColor = TextWhite,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = StreamPrimary
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == AdminTab.VIDEOS,
                onClick = { selectedTab = AdminTab.VIDEOS },
                text = { Text("Active Videos (${activeVideos.size})", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == AdminTab.TRASH,
                onClick = { selectedTab = AdminTab.TRASH },
                text = {
                    Text(
                        "Trash Bin (${trashedVideos.size})",
                        color = if (trashedVideos.isNotEmpty()) StreamError else TextMuted,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == AdminTab.STORAGE,
                onClick = { selectedTab = AdminTab.STORAGE },
                text = { Text("Storage & Tools", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == AdminTab.LOGS,
                onClick = { selectedTab = AdminTab.LOGS },
                text = { Text("Audit Logs (${adminLogs.size})", fontSize = 13.sp) }
            )
        }

        HorizontalDivider(color = CinemaSurfaceElevated)

        // Main Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                AdminTab.VIDEOS -> {
                    AdminVideosTab(
                        videos = activeVideos,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        isBatchMode = isBatchSelectionMode,
                        selectedBatchIds = selectedBatchIds,
                        onToggleBatch = { id ->
                            selectedBatchIds = if (selectedBatchIds.contains(id)) {
                                selectedBatchIds - id
                            } else {
                                selectedBatchIds + id
                            }
                        },
                        onSelectAll = {
                            selectedBatchIds = activeVideos.map { it.id }.toSet()
                        },
                        onPlay = onPlayVideo,
                        onEdit = { showEditDialog = it },
                        onDeleteSingle = { video -> onDeleteVideoRequest(video, false) },
                        onDeleteBatch = {
                            val targets = activeVideos.filter { selectedBatchIds.contains(it.id) }
                            onBatchDeleteRequest(targets, false)
                            selectedBatchIds = emptySet()
                            isBatchSelectionMode = false
                        }
                    )
                }

                AdminTab.TRASH -> {
                    AdminTrashTab(
                        trashedVideos = trashedVideos,
                        onRestoreSingle = onRestoreVideo,
                        onPermanentDeleteSingle = { video -> onDeleteVideoRequest(video, true) },
                        onRestoreAll = onRestoreAllTrashed,
                        onEmptyTrashRequest = { showEmptyTrashConfirmDialog = true }
                    )
                }

                AdminTab.STORAGE -> {
                    AdminStorageTab(
                        activeVideos = activeVideos,
                        trashedVideos = trashedVideos,
                        totalStorageBytes = totalStorageBytes,
                        onImportDownloadClick = {
                            downloadFilePickerLauncher.launch(
                                arrayOf("video/*", "video/mp4", "video/mkv", "video/webm", "video/3gpp", "video/x-matroska", "application/octet-stream")
                            )
                        },
                        onCreateDemoStream = { title, cat, hex ->
                            onCreateDemoVideo(title, cat, hex)
                        },
                        onChangePinClick = { showChangePinDialog = true },
                        onResetClick = { showResetConfirmDialog = true }
                    )
                }

                AdminTab.LOGS -> {
                    AdminLogsTab(
                        logs = adminLogs,
                        onClearLogs = onClearLogs
                    )
                }
            }
        }
    }

    // Dialogs
    // 1. Import Video Dialog
    if (showImportDialog && pickedVideoUri != null) {
        ImportVideoMetadataDialog(
            uri = pickedVideoUri!!,
            isDownloadedFile = isDownloadedFileImport,
            onConfirm = { title, desc, category, tags, isFeatured ->
                if (isDownloadedFileImport) {
                    onImportDownloadedVideo(pickedVideoUri!!, title, desc, category, tags, isFeatured)
                } else {
                    onImportVideo(pickedVideoUri!!, title, desc, category, tags)
                }
                showImportDialog = false
                pickedVideoUri = null
            },
            onDismiss = {
                showImportDialog = false
                pickedVideoUri = null
            }
        )
    }

    // 2. Create Demo Stream Dialog
    if (showCreateDemoDialog) {
        CreateDemoStreamDialog(
            onConfirm = { title, category, hex ->
                onCreateDemoVideo(title, category, hex)
                showCreateDemoDialog = false
            },
            onDismiss = { showCreateDemoDialog = false }
        )
    }

    // 3. Edit Video Metadata Dialog
    if (showEditDialog != null) {
        EditVideoDialog(
            video = showEditDialog!!,
            onConfirm = { updated ->
                onUpdateVideo(updated)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    // 4. Change PIN Dialog
    if (showChangePinDialog) {
        ChangePinDialog(
            onConfirm = { newPin ->
                onChangePin(newPin)
                showChangePinDialog = false
            },
            onDismiss = { showChangePinDialog = false }
        )
    }

    // 5. Reset Factory Defaults Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = CinemaSurface,
            title = { Text("Reset Catalog to Defaults?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete all current offline streams and re-synthesize factory demo videos. This cannot be undone.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToDefaults()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreamError)
                ) {
                    Text("Reset All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // 6. Empty Trash Confirmation Dialog
    if (showEmptyTrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirmDialog = false },
            containerColor = CinemaSurface,
            title = { Text("Permanently Empty Trash?", color = StreamError, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All ${trashedVideos.size} videos in the trash bin will be permanently erased from device storage. This operation is permanent.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyTrash()
                        showEmptyTrashConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreamError),
                    modifier = Modifier.testTag("confirm_empty_trash_button")
                ) {
                    Text("Purge & Reclaim Storage", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmptyTrashConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, color = TextMuted, fontSize = 10.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminVideosTab(
    videos: List<VideoEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isBatchMode: Boolean,
    selectedBatchIds: Set<Long>,
    onToggleBatch: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onPlay: (VideoEntity) -> Unit,
    onEdit: (VideoEntity) -> Unit,
    onDeleteSingle: (VideoEntity) -> Unit,
    onDeleteBatch: () -> Unit
) {
    val filtered = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) videos
        else {
            val q = searchQuery.trim().lowercase()
            videos.filter { it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Batch info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter active videos...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CinemaSurfaceVariant,
                    unfocusedContainerColor = CinemaSurfaceVariant,
                    focusedBorderColor = StreamPrimary,
                    unfocusedBorderColor = CinemaSurfaceElevated,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            if (isBatchMode) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onSelectAll,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Select All", fontSize = 12.sp)
                }
            }
        }

        // Batch Action Banner
        AnimatedVisibility(visible = isBatchMode && selectedBatchIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StreamError.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedBatchIds.size} videos selected",
                    color = StreamError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Button(
                    onClick = onDeleteBatch,
                    colors = ButtonDefaults.buttonColors(containerColor = StreamError),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("admin_delete_selected_batch_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Selected", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Video Items List
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching videos in catalog", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { video ->
                    AdminVideoRowItem(
                        video = video,
                        isBatchMode = isBatchMode,
                        isSelected = selectedBatchIds.contains(video.id),
                        onToggleSelect = { onToggleBatch(video.id) },
                        onPlay = { onPlay(video) },
                        onEdit = { onEdit(video) },
                        onDelete = { onDeleteSingle(video) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminVideoRowItem(
    video: VideoEntity,
    isBatchMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_video_row_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Batch checkbox
            if (isBatchMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = StreamPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Thumbnail
            Box(
                modifier = Modifier
                    .size(72.dp, 48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onPlay() }
            ) {
                VideoThumbnailImage(
                    thumbnailUri = video.thumbnailUri,
                    contentDescription = video.title
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = "${video.category} • ${video.getFormattedSize()} • ${video.getFormattedDuration()}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${video.resolution} • ${video.viewsCount} views",
                        color = TextDarkMuted,
                        fontSize = 10.sp
                    )
                    if (video.isFeatured) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Featured",
                            tint = StreamAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = StreamPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Specs",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button (Prominent Red)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("admin_delete_video_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Video",
                        tint = StreamError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminTrashTab(
    trashedVideos: List<VideoEntity>,
    onRestoreSingle: (VideoEntity) -> Unit,
    onPermanentDeleteSingle: (VideoEntity) -> Unit,
    onRestoreAll: () -> Unit,
    onEmptyTrashRequest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Trash Header Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recycle & Trash Bin",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${trashedVideos.size} items in trash",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            if (trashedVideos.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestoreAll,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore All", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onEmptyTrashRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = StreamError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_empty_trash_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Empty Trash", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (trashedVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = TextDarkMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Trash Bin is Empty", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Deleted videos will appear here before being permanently purged.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trashedVideos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trash_item_${video.id}"),
                        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp, 44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                VideoThumbnailImage(
                                    thumbnailUri = video.thumbnailUri,
                                    contentDescription = video.title
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

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
                                    fontSize = 11.sp
                                )
                            }

                            // Restore
                            IconButton(
                                onClick = { onRestoreSingle(video) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore",
                                    tint = StreamSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Permanent Purge
                            IconButton(
                                onClick = { onPermanentDeleteSingle(video) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("trash_permanent_delete_${video.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Permanent Delete",
                                    tint = StreamError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStorageTab(
    activeVideos: List<VideoEntity>,
    trashedVideos: List<VideoEntity>,
    totalStorageBytes: Long,
    onImportDownloadClick: () -> Unit,
    onCreateDemoStream: (title: String, category: String, hex: Int) -> Unit,
    onChangePinClick: () -> Unit,
    onResetClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Download & File Import Hub Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StreamPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = StreamPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Import & Download Manager",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Index downloaded video files (.mp4, .mkv) for offline streaming",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onImportDownloadClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_storage_import_download_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import from Device Downloads Folder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Instant 1-Tap Offline Presets:",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onCreateDemoStream(
                                    "Cosmic Void 4K",
                                    "Sci-Fi & Action",
                                    AndroidColor.rgb(99, 102, 241)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Sci-Fi Clip", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                onCreateDemoStream(
                                    "Alpine Peak HDR",
                                    "Documentary & Nature",
                                    AndroidColor.rgb(16, 185, 129)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Nature Clip", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                onCreateDemoStream(
                                    "Tokyo Synth Groove",
                                    "Music & Chill",
                                    AndroidColor.rgb(245, 158, 11)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Lofi Clip", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Device Storage Footprint",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total Media Size: ${formatBytes(totalStorageBytes)}",
                        color = StreamSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Active Videos Storage: ${formatBytes(activeVideos.sumOf { it.fileSizeBytes })} (${activeVideos.size} files)\n" +
                                "• Trashed Storage: ${formatBytes(trashedVideos.sumOf { it.fileSizeBytes })} (${trashedVideos.size} files)\n" +
                                "• Storage Location: Internal App Data (100% Private, Offline)",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Admin Security & PIN",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Change the passcode required to access administrator features and deletion controls.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onChangePinClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Admin PIN", fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Danger Zone: Factory Reset",
                        color = StreamError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deletes all downloaded/imported video files and regenerates factory default offline demo videos.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onResetClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StreamError),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Catalog to Defaults", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLogsTab(
    logs: List<AdminLog>,
    onClearLogs: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Administrative Audit Trail",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (logs.isNotEmpty()) {
                TextButton(onClick = onClearLogs) {
                    Text("Clear Logs", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No admin actions logged yet", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.actionType,
                                    color = when {
                                        log.actionType.contains("DELETE") -> StreamError
                                        log.actionType.contains("IMPORT") -> StreamPrimary
                                        log.actionType.contains("RESTORE") -> StreamSuccess
                                        else -> StreamAccent
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(text = log.getFormattedTime(), color = TextDarkMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = log.videoTitle, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = log.details, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLockScreen(onUnlock: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaBlack)
            .statusBarsPadding()
            .testTag("admin_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StreamPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Lock",
                        tint = StreamPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Administrator Access",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Text(
                    text = "Enter 4-digit PIN to manage and delete offline videos. Default PIN is 1234.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6) {
                            pin = it
                            error = false
                        }
                    },
                    label = { Text("Enter PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated,
                        focusedLabelColor = StreamPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                if (error) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Incorrect PIN. Please try again.", color = StreamError, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val success = onUnlock(pin)
                        if (!success) {
                            error = true
                            pin = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_unlock_submit")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock Admin Console", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        onUnlock("1234")
                    }
                ) {
                    Text("Quick Test Unlock (1234)", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

// Dialog for Import Video Metadata
@Composable
private fun ImportVideoMetadataDialog(
    uri: Uri,
    isDownloadedFile: Boolean,
    onConfirm: (title: String, desc: String, category: String, tags: String, isFeatured: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val detectedFileName = remember(uri) { VideoFileUtils.getFileName(context, uri) }
    val detectedFileSize = remember(uri) { VideoFileUtils.getFileSize(context, uri) }

    val formattedSize = remember(detectedFileSize) {
        if (detectedFileSize > 0) {
            val mb = detectedFileSize.toDouble() / (1024 * 1024)
            if (mb >= 1024) String.format("%.2f GB", mb / 1024) else String.format("%.1f MB", mb)
        } else "Detected upon indexing"
    }

    val defaultTitle = remember(detectedFileName) {
        detectedFileName.substringBeforeLast(".")
            .replace("_", " ")
            .replace("-", " ")
            .trim()
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
            .ifBlank { if (isDownloadedFile) "Downloaded Video" else "Imported Video" }
    }

    var title by remember { mutableStateOf(defaultTitle) }
    var description by remember {
        mutableStateOf(
            if (isDownloadedFile) "Downloaded video file ($detectedFileName) imported for offline streaming."
            else "Locally imported offline video stream."
        )
    }
    var category by remember { mutableStateOf(if (isDownloadedFile) "Downloads" else "Custom Imports") }
    var tags by remember { mutableStateOf(if (isDownloadedFile) "Downloaded, Offline, 1080p" else "Local, Offline") }
    var isFeatured by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDownloadedFile) Icons.Default.FileDownload else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = StreamPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDownloadedFile) "Import Download Video File" else "Import Video to Library",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // File specs banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isDownloadedFile) StreamSuccess.copy(alpha = 0.2f) else StreamPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isDownloadedFile) "DOWNLOADS FOLDER" else "DEVICE MEDIA",
                                    color = if (isDownloadedFile) StreamSuccess else StreamPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = formattedSize,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detectedFileName,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title") },
                    placeholder = { Text("e.g. Vacation Highlights") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated,
                        focusedLabelColor = StreamPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated,
                        focusedLabelColor = StreamPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated,
                        focusedLabelColor = StreamPrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin to Featured Banner", color = TextWhite, fontSize = 12.sp)
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StreamPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, category, tags, isFeatured) },
                colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary),
                modifier = Modifier.testTag("admin_confirm_import_button")
            ) {
                Text(
                    text = if (isDownloadedFile) "Import Download File" else "Save & Index Video",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog for Creating Demo Stream
@Composable
private fun CreateDemoStreamDialog(
    onConfirm: (title: String, category: String, colorHex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Hyperdrive Genesis") }
    var category by remember { mutableStateOf("Sci-Fi & Action") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val colors = listOf(
        Pair("Indigo", AndroidColor.rgb(99, 102, 241)),
        Pair("Cyan", AndroidColor.rgb(6, 182, 212)),
        Pair("Amber", AndroidColor.rgb(245, 158, 11)),
        Pair("Rose", AndroidColor.rgb(244, 63, 94)),
        Pair("Emerald", AndroidColor.rgb(16, 185, 129))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaSurface,
        title = { Text("Synthesize Demo Stream", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Generates a fully valid offline playable MP4 video on-device with motion graphics and test timer.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Theme Color Accent:", color = TextWhite, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEachIndexed { index, pair ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(pair.second))
                                .border(
                                    width = if (selectedColorIndex == index) 3.dp else 0.dp,
                                    color = if (selectedColorIndex == index) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIndex == index) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, category, colors[selectedColorIndex].second) },
                colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary)
            ) {
                Text("Generate Video", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog for Editing Video Metadata
@Composable
private fun EditVideoDialog(
    video: VideoEntity,
    onConfirm: (VideoEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(video.title) }
    var description by remember { mutableStateOf(video.description) }
    var category by remember { mutableStateOf(video.category) }
    var isFeatured by remember { mutableStateOf(video.isFeatured) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaSurface,
        title = { Text("Edit Video Metadata", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pin as Featured Video", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StreamPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        video.copy(
                            title = title,
                            description = description,
                            category = category,
                            isFeatured = isFeatured
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// Dialog for Changing Admin PIN
@Composable
private fun ChangePinDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaSurface,
        title = { Text("Change Admin Passcode", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter a 4-6 digit numeric passcode:", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6) newPin = it },
                    label = { Text("New PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StreamPrimary,
                        unfocusedBorderColor = CinemaSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg!!, color = StreamError, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.length < 4) {
                        errorMsg = "PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        errorMsg = "PINs do not match"
                    } else {
                        onConfirm(newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StreamPrimary)
            ) {
                Text("Save PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes.toDouble() / (1024 * 1024)
    return if (mb >= 1024) String.format("%.2f GB", mb / 1024) else String.format("%.1f MB", mb)
}
