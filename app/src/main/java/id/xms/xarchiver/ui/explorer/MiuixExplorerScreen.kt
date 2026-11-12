package id.xms.xarchiver.ui.explorer

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.xms.xarchiver.R
import id.xms.xarchiver.core.*
import id.xms.xarchiver.core.archive.*
import id.xms.xarchiver.core.install.ApkInstaller
import id.xms.xarchiver.ui.components.LocalNotificationHost
import id.xms.xarchiver.ui.explorer.miuix.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.xms.xarchiver.ui.explorer.utils.DirectoryWatcher
import id.xms.xarchiver.ui.explorer.utils.FileActionHandler
import id.xms.xarchiver.ui.explorer.utils.FileTypeDetector
import id.xms.xarchiver.ui.theme.ThemeMode
import id.xms.xarchiver.ui.theme.ThemePreferences
import id.xms.xarchiver.ui.theme.isAppInDarkTheme
import id.xms.xarchiver.ui.explorer.search.*
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Main MIUIX / HyperOS file explorer screen.
 * Orchestrates navigation, selection, file operations, top bars, docks, and dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiuixExplorerScreen(path: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val archiveManager = remember { ArchiveManager(context) }
    val selectionManager = remember { SelectionManager() }
    val bookmarksManager = remember { BookmarksManager(context) }
    val snackbarHostState = LocalNotificationHost.current
    val backdrop = rememberLayerBackdrop()

    val themePreferences = remember { ThemePreferences(context) }
    val currentThemeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isDark = isAppInDarkTheme(context, currentThemeMode)

    // MIUIX / HyperOS Design Tokens
    val pageBgColor = if (isDark) Color.Black else Color(0xFFF2F4F7)
    val cardBgColor = if (isDark) Color(0xFF141416) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB)
    val dividerColor = if (isDark) Color(0xFF222225) else Color(0xFFF0F0F2)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF1F2937)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)
    val inputBgColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFE9EBEF)
    val miuixBlue = Color(0xFF0070F0)
    val miuixRed = Color(0xFFEF4444)

    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFabMenu by remember { mutableStateOf(false) }

    // Dialog states
    var pendingApk by remember { mutableStateOf<File?>(null) }
    var showPropertiesDialog by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<List<String>?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showFileExistsDialog by remember { mutableStateOf<String?>(null) }
    var showCreateArchiveDialog by remember { mutableStateOf(false) }
    var showQuickExtractDialog by remember { mutableStateOf<FileItem?>(null) }
    var showCustomPathDialogFor by remember { mutableStateOf<FileItem?>(null) }
    var pendingFileOperation by remember { mutableStateOf<Pair<String, List<String>>?>(null) }
    var extractionProgress by remember { mutableStateOf<ExtractionProgress?>(null) }
    var archiveCreationProgress by remember { mutableStateOf<ArchiveCreationProgress?>(null) }
    var fileOperationProgress by remember { mutableStateOf<FileOperationProgress?>(null) }
    var multiArchiveExtractList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentExtractingIndex by remember { mutableStateOf(0) }
    var pendingPasswordExtraction by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var passwordExtractionError by remember { mutableStateOf<String?>(null) }

    // Clipboard state
    val hasClipboard = FileOperationsManager.hasClipboardContent()
    val clipboardCount = FileOperationsManager.getClipboardCount()
    val clipboardOp = FileOperationsManager.getClipboardOperation()

    // Selection mode state
    val isSelecting = selectionManager.isSelecting
    val selectedCount = selectionManager.selectedCount
    val selectedPathsList = selectionManager.selectedPaths.toList()
    val singleFile = if (selectedPathsList.size == 1) files.find { it.path == selectedPathsList.first() } else null
    val hasArchiveSingle = singleFile != null && !singleFile.isDirectory && FileTypeDetector.isArchiveExtension(singleFile.name)

    // Search state
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchCategory by remember { mutableStateOf(ExplorerSearchCategory.ALL) }
    var searchResults by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isSearchLoading by remember { mutableStateOf(false) }

    // Scoped recursive search within active explorer directory
    LaunchedEffect(isSearching, searchQuery, searchCategory, path) {
        if (!isSearching) {
            searchResults = emptyList()
            isSearchLoading = false
            return@LaunchedEffect
        }
        val clean = searchQuery.trim()
        if (clean.isEmpty() && searchCategory == ExplorerSearchCategory.ALL) {
            searchResults = emptyList()
            isSearchLoading = false
            return@LaunchedEffect
        }
        isSearchLoading = true
        delay(200)
        val results = ExplorerSearchEngine.searchScopedDirectory(
            scopePath = path,
            query = clean,
            category = searchCategory
        )
        searchResults = results
        isSearchLoading = false
    }

    val isRootScope = remember(path) {
        val clean = path.trimEnd('/')
        clean.isEmpty() || clean == "/" || clean == "/sdcard" || clean == "/storage/emulated/0"
    }

    val onTriggerExpandSearch: () -> Unit = {
        scope.launch {
            isSearchLoading = true
            val targetScope = if (!isRootScope) "/storage/emulated/0" else path
            val results = ExplorerSearchEngine.searchScopedDirectory(
                scopePath = targetScope,
                query = searchQuery,
                category = searchCategory,
                isDeepSearch = true
            )
            searchResults = results
            isSearchLoading = false
        }
    }

    val filteredFiles = remember(files) {
        files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    val listState = rememberLazyListState()

    fun refreshFiles(silent: Boolean = false) {
        scope.launch {
            if (!silent || files.isEmpty()) {
                isLoading = true
            }
            val newFiles = FileService.listDirectory(path)
            if (files != newFiles) {
                files = newFiles
            }
            isLoading = false
        }
    }

    val executeExtraction: (String, String, String, String?) -> Unit = { archivePath, archiveName, outputDir, pwd ->
        scope.launch {
            extractionProgress = ExtractionProgress(0, "Starting...", ExtractionState.STARTED)
            archiveManager.extractArchive(
                archiveFilePath = archivePath,
                outputDir = outputDir,
                password = pwd
            ).collect { progress ->
                extractionProgress = progress
                if (progress.state == ExtractionState.COMPLETED) {
                    extractionProgress = null
                    refreshFiles()
                    snackbarHostState.showSnackbar("Extracted to ${File(outputDir).name}")
                } else if (progress.state == ExtractionState.ERROR) {
                    extractionProgress = null
                    val err = progress.error ?: progress.currentFile
                    if (err.contains("password", ignoreCase = true) || err.contains("kata sandi", ignoreCase = true)) {
                        passwordExtractionError = if (pwd != null) "Incorrect password. Please try again." else null
                        pendingPasswordExtraction = Triple(archivePath, archiveName, outputDir)
                    } else {
                        snackbarHostState.showSnackbar("Extraction failed: $err")
                    }
                }
            }
        }
    }

    val requestExtraction: (String, String, String) -> Unit = { archivePath, archiveName, outputDir ->
        scope.launch {
            val isEncrypted = withContext(Dispatchers.IO) {
                archiveManager.isArchiveEncrypted(archivePath)
            }
            if (isEncrypted) {
                passwordExtractionError = null
                pendingPasswordExtraction = Triple(archivePath, archiveName, outputDir)
            } else {
                executeExtraction(archivePath, archiveName, outputDir, null)
            }
        }
    }

    LaunchedEffect(path) {
        refreshFiles()
        selectionManager.clearSelection()
        isSearching = false
        searchQuery = ""
        searchCategory = ExplorerSearchCategory.ALL
        searchResults = emptyList()
    }

    // Observe file system changes within current directory in realtime
    DisposableEffect(path) {
        val watcher = DirectoryWatcher(
            path = path,
            scope = scope,
            debounceMs = 400L
        ) {
            refreshFiles(silent = true)
        }
        watcher.start()
        onDispose {
            watcher.stop()
        }
    }

    // Auto-refresh when app returns to foreground from background or other apps
    DisposableEffect(lifecycleOwner, path) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshFiles(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle batch extraction of multiple selected archives
    LaunchedEffect(multiArchiveExtractList, currentExtractingIndex) {
        if (multiArchiveExtractList.isNotEmpty() && currentExtractingIndex < multiArchiveExtractList.size) {
            val archivePath = multiArchiveExtractList[currentExtractingIndex]
            val archiveFile = File(archivePath)
            val outputDir = "$path/${archiveFile.nameWithoutExtension}"

            extractionProgress = ExtractionProgress(
                0, "Extracting ${archiveFile.name} (${currentExtractingIndex + 1}/${multiArchiveExtractList.size})...", ExtractionState.STARTED
            )

            try {
                archiveManager.extractArchive(archivePath, outputDir).collect { progress ->
                    extractionProgress = ExtractionProgress(
                        progress.percentage,
                        "${archiveFile.name}: ${progress.currentFile}",
                        progress.state
                    )

                    if (progress.state == ExtractionState.COMPLETED) {
                        if (currentExtractingIndex + 1 < multiArchiveExtractList.size) {
                            currentExtractingIndex++
                        } else {
                            extractionProgress = null
                            multiArchiveExtractList = emptyList()
                            refreshFiles()
                            snackbarHostState.showSnackbar("Extracted archives successfully")
                        }
                    } else if (progress.state == ExtractionState.ERROR) {
                        snackbarHostState.showSnackbar("Error extracting ${archiveFile.name}")
                        if (currentExtractingIndex + 1 < multiArchiveExtractList.size) {
                            currentExtractingIndex++
                        } else {
                            extractionProgress = null
                            multiArchiveExtractList = emptyList()
                            refreshFiles()
                        }
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message}")
                extractionProgress = null
                multiArchiveExtractList = emptyList()
            }
        }
    }

    val currentFolderDisplayName = remember(path) {
        when {
            path == "/" -> "Root"
            path == "/storage/emulated/0" || path == "/sdcard" -> "Internal Storage"
            path.endsWith("/0") && path.startsWith("/storage/emulated") -> "Internal Storage"
            else -> File(path).name.ifEmpty { "Files" }
        }
    }

    Scaffold(
        containerColor = pageBgColor,
        topBar = {
            if (isSelecting) {
                MiuixSelectionTopBar(
                    selectedCount = selectedCount,
                    primaryTextColor = primaryTextColor,
                    pageBgColor = pageBgColor,
                    onClearSelection = { selectionManager.clearSelection() },
                    onSelectAll = { selectionManager.selectAll(files.map { it.path }) },
                    onReverseSelection = { selectionManager.reverseSelection(files.map { it.path }) },
                    onSelectSameType = if (selectedCount == 1) {
                        { selectionManager.selectSameType(files.map { it.path }, selectionManager.selectedPaths.first()) }
                    } else null
                )
            } else if (isSearching) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(pageBgColor)
                ) {
                    MiuixSearchTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        pageBgColor = pageBgColor,
                        inputBgColor = inputBgColor,
                        cardBorderColor = cardBorderColor,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        miuixBlue = miuixBlue,
                        onCloseSearch = {
                            isSearching = false
                            searchQuery = ""
                            searchCategory = ExplorerSearchCategory.ALL
                            searchResults = emptyList()
                        }
                    )
                    ExplorerSearchCategoryChips(
                        selectedCategory = searchCategory,
                        onCategorySelected = { searchCategory = it },
                        activeBgColor = miuixBlue,
                        activeTextColor = Color.White,
                        inactiveBgColor = cardBgColor,
                        inactiveTextColor = secondaryTextColor
                    )
                }
            } else {
                MiuixNormalTopBar(
                    currentFolderDisplayName = currentFolderDisplayName,
                    path = path,
                    isDark = isDark,
                    pageBgColor = pageBgColor,
                    cardBgColor = cardBgColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    miuixBlue = miuixBlue,
                    onBack = { navController.navigateUp() },
                    onStartSearch = { isSearching = true },
                    onToggleSelectMode = {
                        if (files.isNotEmpty()) {
                            selectionManager.toggleSelection(files.first().path)
                        }
                    },
                    onRefresh = { refreshFiles() },
                    onNavigatePath = { newPath ->
                        navController.navigate("explorer/${Uri.encode(newPath)}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            MiuixExplorerFab(
                visible = !isSelecting && !hasClipboard,
                showFabMenu = showFabMenu,
                isDark = isDark,
                cardBgColor = cardBgColor,
                primaryTextColor = primaryTextColor,
                miuixBlue = miuixBlue,
                onToggleFabMenu = { showFabMenu = !showFabMenu },
                onNewFolder = {
                    showFabMenu = false
                    showNewFolderDialog = true
                },
                onNewFile = {
                    showFabMenu = false
                    showNewFileDialog = true
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (isSearching) {
                val defaultScopeName = stringResource(R.string.search_scope_this_device)
                val scopeDisplayName = remember(path, defaultScopeName) { getSearchScopeDisplayName(path, defaultScopeName) }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isSearchLoading) {
                        item {
                            ExplorerSearchHeader(
                                totalItems = 0,
                                scopeDisplayName = scopeDisplayName,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                        items(7) {
                            MiuixFileSkeleton(isDark = isDark, cardBgColor = cardBgColor, cardBorderColor = cardBorderColor)
                        }
                    } else if (searchQuery.trim().isEmpty() && searchCategory == ExplorerSearchCategory.ALL) {
                        item {
                            ExplorerSearchEmptyView(
                                isQueryEmpty = true,
                                searchQuery = searchQuery,
                                scopeDisplayName = scopeDisplayName,
                                isRootScope = isRootScope,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = miuixBlue,
                                onExpandSearchClick = onTriggerExpandSearch
                            )
                        }
                    } else if (searchResults.isEmpty()) {
                        item {
                            ExplorerSearchEmptyView(
                                isQueryEmpty = false,
                                searchQuery = searchQuery,
                                scopeDisplayName = scopeDisplayName,
                                isRootScope = isRootScope,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = miuixBlue,
                                onExpandSearchClick = onTriggerExpandSearch
                            )
                        }
                    } else {
                        item {
                            ExplorerSearchHeader(
                                totalItems = searchResults.size,
                                scopeDisplayName = scopeDisplayName,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                        itemsIndexed(
                            items = searchResults,
                            key = { _, file -> file.path },
                            contentType = { _, file -> if (file.isDirectory) "folder" else "file" }
                        ) { _, file ->
                            MiuixFileItemCard(
                                file = file,
                                isSelected = selectionManager.isSelected(file.path),
                                isSelectionMode = isSelecting,
                                isDark = isDark,
                                cardBgColor = cardBgColor,
                                cardBorderColor = cardBorderColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                primaryAccentColor = miuixBlue,
                                highlightQuery = searchQuery,
                                currentScopePath = path,
                                onClick = {
                                    if (isSelecting) {
                                        selectionManager.toggleSelection(file.path)
                                    } else {
                                        FileActionHandler.handleFileClick(
                                            context = context,
                                            file = file,
                                            navController = navController,
                                            archiveManager = archiveManager,
                                            scope = scope,
                                            notificationHostState = snackbarHostState,
                                            onApkClick = { pendingApk = it }
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!isSelecting) {
                                        selectionManager.toggleSelection(file.path)
                                    }
                                }
                            )
                        }
                        item {
                            ExplorerSearchExpandFooter(
                                isRootScope = isRootScope,
                                secondaryTextColor = secondaryTextColor,
                                accentColor = miuixBlue,
                                onActionClick = onTriggerExpandSearch
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding())
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        items(7) {
                            MiuixFileSkeleton(isDark = isDark, cardBgColor = cardBgColor, cardBorderColor = cardBorderColor)
                        }
                    } else if (filteredFiles.isEmpty()) {
                        item {
                            MiuixEmptyFolderState(
                                isSearch = false,
                                searchQuery = "",
                                isDark = isDark,
                                primaryColor = miuixBlue,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = filteredFiles,
                            key = { _, file -> file.path },
                            contentType = { _, file -> if (file.isDirectory) "folder" else "file" }
                        ) { _, file ->
                            MiuixFileItemCard(
                                file = file,
                                isSelected = selectionManager.isSelected(file.path),
                                isSelectionMode = isSelecting,
                                isDark = isDark,
                                cardBgColor = cardBgColor,
                                cardBorderColor = cardBorderColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                primaryAccentColor = miuixBlue,
                                onClick = {
                                    if (isSelecting) {
                                        selectionManager.toggleSelection(file.path)
                                    } else {
                                        FileActionHandler.handleFileClick(
                                            context = context,
                                            file = file,
                                            navController = navController,
                                            archiveManager = archiveManager,
                                            scope = scope,
                                            notificationHostState = snackbarHostState,
                                            onApkClick = { pendingApk = it }
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!isSelecting) {
                                        selectionManager.toggleSelection(file.path)
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp).navigationBarsPadding())
                    }
                }
            }

            // Floating bottom docks (Clipboard & Selection)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clipboard paste dock
                if (hasClipboard && !isSelecting) {
                    MiuixClipboardDock(
                        clipboardCount = clipboardCount,
                        isCut = clipboardOp == ClipboardOperation.CUT,
                        isDark = isDark,
                        backdrop = backdrop,
                        primaryColor = miuixBlue,
                        onPaste = {
                            scope.launch {
                                val itemCount = clipboardCount
                                fileOperationProgress = FileOperationProgress("Preparing...", 0, 0, 0, 0, itemCount)
                                FileOperationsManager.pasteFiles(path).collect { progress ->
                                    fileOperationProgress = progress
                                }
                                fileOperationProgress = null
                                refreshFiles()
                                val action = if (clipboardOp == ClipboardOperation.CUT) "moved" else "copied"
                                snackbarHostState.showSnackbar("$itemCount items $action successfully")
                                FileOperationsManager.clearClipboard()
                            }
                        },
                        onCancel = {
                            FileOperationsManager.clearClipboard()
                            refreshFiles()
                        }
                    )
                }

                // Selection mode action dock
                AnimatedVisibility(
                    visible = isSelecting,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val selectedArchives = selectionManager.selectedPaths.filter {
                        FileTypeDetector.isArchiveExtension(File(it).name)
                    }
                    val hasArchives = selectedArchives.isNotEmpty()

                    MiuixSelectionDock(
                        isDark = isDark,
                        backdrop = backdrop,
                        hasArchives = hasArchives,
                        selectedCount = selectionManager.selectedPaths.size,
                        onCopy = {
                            pendingFileOperation = Pair("COPY", selectionManager.selectedPaths.toList())
                            selectionManager.clearSelection()
                        },
                        onCut = {
                            pendingFileOperation = Pair("CUT", selectionManager.selectedPaths.toList())
                            selectionManager.clearSelection()
                        },
                        onDelete = {
                            showDeleteDialog = selectionManager.selectedPaths.toList()
                        },
                        onExtractOrShare = {
                            if (hasArchives) {
                                if (selectedArchives.size == 1) {
                                    val archiveFile = files.find { it.path == selectedArchives.first() }
                                    if (archiveFile != null) {
                                        selectionManager.clearSelection()
                                        showQuickExtractDialog = archiveFile
                                    }
                                } else {
                                    val archivesToExtract = selectedArchives.toList()
                                    selectionManager.clearSelection()
                                    multiArchiveExtractList = archivesToExtract
                                    currentExtractingIndex = 0
                                }
                            } else {
                                ShareUtils.shareMultipleFiles(context, selectionManager.selectedPaths)
                                selectionManager.clearSelection()
                            }
                        },
                        onCompress = {
                            showCreateArchiveDialog = true
                        },
                        onRename = {
                            singleFile?.let {
                                showRenameDialog = it
                                selectionManager.clearSelection()
                            }
                        },
                        onProperties = {
                            singleFile?.let {
                                showPropertiesDialog = it.path
                                selectionManager.clearSelection()
                            }
                        },
                        onBookmark = {
                            singleFile?.let { file ->
                                scope.launch {
                                    val isNowBookmarked = bookmarksManager.toggleBookmark(file.path)
                                    snackbarHostState.showSnackbar(
                                        if (isNowBookmarked) "Added to bookmarks" else "Removed from bookmarks"
                                    )
                                }
                                selectionManager.clearSelection()
                            }
                        },
                        onShare = {
                            if (selectedPathsList.size == 1) {
                                ShareUtils.shareFile(context, selectedPathsList.first())
                            } else {
                                ShareUtils.shareMultipleFiles(context, selectedPathsList)
                            }
                            selectionManager.clearSelection()
                        },
                        onOpenArchive = if (hasArchiveSingle) {
                            {
                                singleFile?.let {
                                    showQuickExtractDialog = it
                                    selectionManager.clearSelection()
                                }
                            }
                        } else null
                    )
                }
            }
        }
    }

    // Modal Dialogs, Bottom Sheets & Progress Overlays Host
    MiuixExplorerDialogHost(
        currentPath = path,
        showSelectionBottomSheet = false,
        selectedPaths = selectedPathsList,
        singleFile = singleFile,
        showPropertiesDialog = showPropertiesDialog,
        showRenameDialog = showRenameDialog,
        showDeleteDialog = showDeleteDialog,
        showNewFolderDialog = showNewFolderDialog,
        showNewFileDialog = showNewFileDialog,
        showFileExistsDialog = showFileExistsDialog,
        pendingApk = pendingApk,
        showCreateArchiveDialog = showCreateArchiveDialog,
        showQuickExtractDialog = showQuickExtractDialog,
        showCustomPathDialogFor = showCustomPathDialogFor,
        pendingPasswordExtraction = pendingPasswordExtraction,
        passwordExtractionError = passwordExtractionError,
        pendingFileOperation = pendingFileOperation,
        extractionProgress = extractionProgress,
        fileOperationProgress = fileOperationProgress,
        archiveCreationProgress = archiveCreationProgress,
        isDark = isDark,
        cardBgColor = cardBgColor,
        cardBorderColor = cardBorderColor,
        dividerColor = dividerColor,
        inputBgColor = inputBgColor,
        primaryTextColor = primaryTextColor,
        secondaryTextColor = secondaryTextColor,
        miuixBlue = miuixBlue,
        miuixRed = miuixRed,
        onDismissSelectionBottomSheet = {},
        onRenameSingleFile = { showRenameDialog = it },
        onShareSelected = { paths ->
            if (paths.size == 1) {
                ShareUtils.shareFile(context, paths.first())
            } else {
                ShareUtils.shareMultipleFiles(context, paths)
            }
        },
        onPropertiesSingleFile = { showPropertiesDialog = it },
        onBookmarkSingleFile = { file ->
            scope.launch {
                val isNowBookmarked = bookmarksManager.toggleBookmark(file.path)
                snackbarHostState.showSnackbar(
                    if (isNowBookmarked) "Added to bookmarks" else "Removed from bookmarks"
                )
            }
        },
        onExtractSingleFile = { file ->
            showQuickExtractDialog = file
        },
        onDismissPropertiesDialog = { showPropertiesDialog = null },
        onConfirmRename = { file, newName ->
            scope.launch {
                val result = FileOperationsManager.renameFile(file.path, newName)
                when (result) {
                    is FileOperationResult.Success -> {
                        refreshFiles()
                        snackbarHostState.showSnackbar("Renamed successfully")
                    }
                    is FileOperationResult.Error -> {
                        snackbarHostState.showSnackbar(result.message)
                    }
                }
            }
            showRenameDialog = null
        },
        onDismissRename = { showRenameDialog = null },
        onConfirmDelete = { paths ->
            scope.launch {
                val result = FileOperationsManager.deleteFiles(paths)
                selectionManager.clearSelection()
                refreshFiles()
                when (result) {
                    is FileOperationResult.Success -> snackbarHostState.showSnackbar(result.message)
                    is FileOperationResult.Error -> snackbarHostState.showSnackbar(result.message)
                }
            }
            showDeleteDialog = null
        },
        onDismissDelete = { showDeleteDialog = null },
        onConfirmNewFolder = { name ->
            scope.launch {
                val result = FileOperationsManager.createFolder(path, name)
                refreshFiles()
                when (result) {
                    is FileOperationResult.Success -> snackbarHostState.showSnackbar("Folder created")
                    is FileOperationResult.Error -> snackbarHostState.showSnackbar(result.message)
                }
            }
            showNewFolderDialog = false
        },
        onDismissNewFolder = { showNewFolderDialog = false },
        onConfirmNewFile = { name ->
            if (files.any { it.name == name }) {
                showFileExistsDialog = name
                showNewFileDialog = false
            } else {
                scope.launch {
                    val result = FileOperationsManager.createFile(path, name)
                    refreshFiles()
                    when (result) {
                        is FileOperationResult.Success -> snackbarHostState.showSnackbar("File created")
                        is FileOperationResult.Error -> snackbarHostState.showSnackbar(result.message)
                    }
                }
                showNewFileDialog = false
            }
        },
        onDismissNewFile = { showNewFileDialog = false },
        onOverwriteExists = { name ->
            scope.launch {
                FileOperationsManager.deleteFiles(listOf("$path/$name"))
                val result = FileOperationsManager.createFile(path, name)
                refreshFiles()
                when (result) {
                    is FileOperationResult.Success -> snackbarHostState.showSnackbar("File overwritten")
                    is FileOperationResult.Error -> snackbarHostState.showSnackbar(result.message)
                }
            }
            showFileExistsDialog = null
        },
        onDuplicateExists = { name ->
            val nameWithoutExt = name.substringBeforeLast(".", name)
            val ext = if (name.contains(".")) ".${name.substringAfterLast(".")}" else ""
            var index = 1
            var newName = "$nameWithoutExt($index)$ext"
            while (files.any { it.name == newName }) {
                index++
                newName = "$nameWithoutExt($index)$ext"
            }
            scope.launch {
                val result = FileOperationsManager.createFile(path, newName)
                refreshFiles()
                when (result) {
                    is FileOperationResult.Success -> snackbarHostState.showSnackbar("File created as $newName")
                    is FileOperationResult.Error -> snackbarHostState.showSnackbar(result.message)
                }
            }
            showFileExistsDialog = null
        },
        onDismissFileExists = { showFileExistsDialog = null },
        onInstallApk = { apkFile ->
            ApkInstaller.installApk(context, apkFile)
            pendingApk = null
        },
        onDismissApk = { pendingApk = null },
        onCreateArchive = { archiveName, format, compressionLevel, password, encryptionType ->
            showCreateArchiveDialog = false
            val selectedFiles = selectionManager.selectedPaths.toList()
            val fullPath = "$path/$archiveName.${format.extension}"

            scope.launch {
                try {
                    archiveCreationProgress = ArchiveCreationProgress("Preparing...", 0, 0, selectedFiles.size, 0L, 0L)
                    ArchiveCreator.createArchive(
                        outputPath = fullPath,
                        files = selectedFiles,
                        basePath = if (selectedFiles.size == 1) {
                            File(selectedFiles.first()).parentFile?.absolutePath ?: ""
                        } else {
                            findCommonParentMiuix(selectedFiles)
                        },
                        compressionLevel = compressionLevel,
                        password = password,
                        encryptionType = encryptionType
                    ).collect { prog ->
                        archiveCreationProgress = prog
                    }
                    archiveCreationProgress = null
                    selectionManager.clearSelection()
                    refreshFiles()
                    snackbarHostState.showSnackbar("Archive created successfully")
                } catch (e: Exception) {
                    archiveCreationProgress = null
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        },
        onDismissCreateArchive = { showCreateArchiveDialog = false },
        onExtractHere = { file ->
            showQuickExtractDialog = null
            val outputDir = "$path/${File(file.path).nameWithoutExtension}"
            requestExtraction(file.path, file.name, outputDir)
        },
        onOpenArchive = { file ->
            showQuickExtractDialog = null
            navController.navigate("archive_explorer/${Uri.encode(file.path)}")
        },
        onCustomPathExtractRequest = { file ->
            showQuickExtractDialog = null
            showCustomPathDialogFor = file
        },
        onDismissQuickExtract = { showQuickExtractDialog = null },
        onCustomPathFolderSelected = { file, selectedPath ->
            showCustomPathDialogFor = null
            val targetFolder = "$selectedPath/${File(file.path).nameWithoutExtension}"
            requestExtraction(file.path, file.name, targetFolder)
        },
        onDismissCustomPath = { showCustomPathDialogFor = null },
        onConfirmPassword = { archivePath, archiveName, outputDir, enteredPassword ->
            pendingPasswordExtraction = null
            passwordExtractionError = null
            executeExtraction(archivePath, archiveName, outputDir, enteredPassword)
        },
        onDismissPassword = {
            pendingPasswordExtraction = null
            passwordExtractionError = null
        },
        onFileOperationDestinationSelected = { operation, paths, selectedPath ->
            val isCut = operation == "CUT"
            val itemCount = paths.size
            pendingFileOperation = null
            scope.launch {
                if (isCut) {
                    FileOperationsManager.cutToClipboard(paths)
                } else {
                    FileOperationsManager.copyToClipboard(paths)
                }

                fileOperationProgress = FileOperationProgress("Preparing...", 0, 0, 0, 0, itemCount)
                FileOperationsManager.pasteFiles(selectedPath).collect { progress ->
                    fileOperationProgress = progress
                }
                fileOperationProgress = null

                refreshFiles()
                val action = if (isCut) "moved" else "copied"
                snackbarHostState.showSnackbar("$itemCount items $action successfully")
                FileOperationsManager.clearClipboard()
            }
        },
        onDismissFileOperation = { pendingFileOperation = null }
    )
}
