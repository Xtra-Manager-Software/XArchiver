package id.xms.xarchiver.ui.explorer

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import id.xms.xarchiver.ui.explorer.material.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Redesigned Material File Explorer Screen.
 * Features:
 * - Dynamic view mode switching: Tampilan daftar (default), Air terjun, and Tampilan kisi
 * - Modern TopBar with circular action buttons: [+], [Search], [:]
 * - Minimalist breadcrumb navigation bar ("Semua file > ...")
 * - Enhanced list item, grid card, and waterfall layout styling
 * - Seamless sorting (Name, Date, Size, Type; Ascending/Descending)
 * - Retains full archive, extraction, and file management capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(path: String, navController: NavController) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val isMiuixUiEnabled by themePreferences.isMiuixUiEnabled.collectAsState(
        initial = ThemePreferences.isMiuiOrHyperOsDevice
    )

    // Route to dedicated MIUIX / HyperOS File Explorer if enabled
    if (isMiuixUiEnabled) {
        MiuixExplorerScreen(path = path, navController = navController)
        return
    }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val archiveManager = remember { ArchiveManager(context) }
    val selectionManager = remember { SelectionManager() }
    val bookmarksManager = remember { BookmarksManager(context) }
    val snackbarHostState = LocalNotificationHost.current

    val currentThemeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isDark = isAppInDarkTheme(context, currentThemeMode)

    // Material 3 Monet Theme Palette
    val pageBgColor = MaterialTheme.colorScheme.background
    val cardBgColor = MaterialTheme.colorScheme.surfaceContainer
    val buttonBgColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val inputBgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // View mode and sorting preferences
    val explorerPreferences = remember { ExplorerPreferences(context) }
    val viewMode by explorerPreferences.viewMode.collectAsState(initial = ExplorerViewMode.LIST)
    val sortBy by explorerPreferences.sortBy.collectAsState(initial = ExplorerSortBy.NAME)
    val sortOrder by explorerPreferences.sortOrder.collectAsState(initial = ExplorerSortOrder.DESCENDING)

    var showSortBottomSheet by remember { mutableStateOf(false) }

    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog & overlay states
    var pendingApk by remember { mutableStateOf<File?>(null) }
    var showPropertiesDialog by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<List<String>?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showFileExistsDialog by remember { mutableStateOf<String?>(null) }
    var showSelectionBottomSheet by remember { mutableStateOf(false) }
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

    // Selection mode state
    var isManualSelectionMode by remember { mutableStateOf(false) }
    val isSelecting = isManualSelectionMode || selectionManager.isSelecting
    val selectedCount = selectionManager.selectedCount

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

    // Sort files based on user preference (keeping directories on top)
    val sortedFiles = remember(files, sortBy, sortOrder) {
        val comparator = when (sortBy) {
            ExplorerSortBy.NAME -> compareBy<FileItem> { it.name.lowercase(Locale.ROOT) }
            ExplorerSortBy.DATE -> compareBy<FileItem> { it.lastModified }
            ExplorerSortBy.SIZE -> compareBy<FileItem> { it.size }
            ExplorerSortBy.TYPE -> compareBy<FileItem> { it.name.substringAfterLast('.', "").lowercase(Locale.ROOT) }
        }
        val dirComparator = compareByDescending<FileItem> { it.isDirectory }
        val fullComparator = if (sortOrder == ExplorerSortOrder.DESCENDING) {
            dirComparator.then(comparator.reversed())
        } else {
            dirComparator.then(comparator)
        }
        files.sortedWith(fullComparator)
    }

    val filteredFiles = sortedFiles

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

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
        isManualSelectionMode = false
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

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(pageBgColor)
            ) {
                if (isSelecting) {
                    MaterialSelectionTopBar(
                        selectedCount = selectedCount,
                        primaryTextColor = primaryTextColor,
                        buttonBgColor = buttonBgColor,
                        onClearSelection = {
                            isManualSelectionMode = false
                            selectionManager.clearSelection()
                        },
                        onSelectAll = { selectionManager.selectAll(files.map { it.path }) },
                        onReverseSelection = { selectionManager.reverseSelection(files.map { it.path }) }
                    )
                } else if (isSearching) {
                    MaterialSearchTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onCloseSearch = {
                            isSearching = false
                            searchQuery = ""
                            searchCategory = ExplorerSearchCategory.ALL
                            searchResults = emptyList()
                        },
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        inputBgColor = inputBgColor
                    )
                    ExplorerSearchCategoryChips(
                        selectedCategory = searchCategory,
                        onCategorySelected = { searchCategory = it },
                        activeBgColor = MaterialTheme.colorScheme.primary,
                        activeTextColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveBgColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    MaterialNormalTopBar(
                        path = path,
                        totalItemCount = files.size,
                        currentViewMode = viewMode,
                        currentSortOrder = sortOrder,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        buttonBgColor = buttonBgColor,
                        onBack = { navController.navigateUp() },
                        onStartSearch = { isSearching = true },
                        onSelectViewMode = { mode ->
                            scope.launch { explorerPreferences.setViewMode(mode) }
                        },
                        onStartEdit = { isManualSelectionMode = true },
                        onOpenSort = { showSortBottomSheet = true },
                        onNewFolder = { showNewFolderDialog = true },
                        onNewFile = { showNewFileDialog = true }
                    )
                }

                // Breadcrumb navigation bar (shown in non-search mode)
                if (!isSearching) {
                    MaterialBreadcrumbBar(
                        currentPath = path,
                        onNavigate = { newPath ->
                            navController.navigate("explorer/${Uri.encode(newPath)}") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSelecting,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                val selectedArchives = selectionManager.selectedPaths.filter {
                    FileTypeDetector.isArchiveExtension(File(it).name)
                }
                val hasArchives = selectedArchives.isNotEmpty()

                MaterialSelectionDock(
                    hasArchives = hasArchives,
                    onCopy = {
                        pendingFileOperation = Pair("COPY", selectionManager.selectedPaths.toList())
                        isManualSelectionMode = false
                        selectionManager.clearSelection()
                    },
                    onCut = {
                        pendingFileOperation = Pair("CUT", selectionManager.selectedPaths.toList())
                        isManualSelectionMode = false
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
                                    isManualSelectionMode = false
                                    selectionManager.clearSelection()
                                    showQuickExtractDialog = archiveFile
                                }
                            } else {
                                val archivesToExtract = selectedArchives.toList()
                                isManualSelectionMode = false
                                selectionManager.clearSelection()
                                multiArchiveExtractList = archivesToExtract
                                currentExtractingIndex = 0
                            }
                        } else {
                            ShareUtils.shareMultipleFiles(context, selectionManager.selectedPaths)
                            isManualSelectionMode = false
                            selectionManager.clearSelection()
                        }
                    },
                    onCompress = {
                        showCreateArchiveDialog = true
                    },
                    onMore = {
                        showSelectionBottomSheet = true
                    }
                )
            }
        },
        containerColor = pageBgColor,
        contentColor = primaryTextColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isSearching) {
                val defaultScopeName = stringResource(R.string.search_scope_this_device)
                val scopeDisplayName = remember(path, defaultScopeName) { getSearchScopeDisplayName(path, defaultScopeName) }
                when {
                    isSearchLoading -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                ExplorerSearchHeader(
                                    totalItems = 0,
                                    scopeDisplayName = scopeDisplayName,
                                    primaryTextColor = primaryTextColor,
                                    secondaryTextColor = secondaryTextColor
                                )
                            }
                            items(6) { MaterialFileItemSkeleton() }
                        }
                    }
                    searchQuery.trim().isEmpty() && searchCategory == ExplorerSearchCategory.ALL -> {
                        ExplorerSearchEmptyView(
                            isQueryEmpty = true,
                            searchQuery = searchQuery,
                            scopeDisplayName = scopeDisplayName,
                            isRootScope = isRootScope,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onExpandSearchClick = onTriggerExpandSearch
                        )
                    }
                    searchResults.isEmpty() -> {
                        ExplorerSearchEmptyView(
                            isQueryEmpty = false,
                            searchQuery = searchQuery,
                            scopeDisplayName = scopeDisplayName,
                            isRootScope = isRootScope,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onExpandSearchClick = onTriggerExpandSearch
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
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
                                key = { _, file -> file.path }
                            ) { _, file ->
                                MaterialFileListItem(
                                    file = file,
                                    isSelected = selectionManager.isSelected(file.path),
                                    isSelectionMode = isSelecting,
                                    primaryTextColor = primaryTextColor,
                                    secondaryTextColor = secondaryTextColor,
                                    dividerColor = dividerColor,
                                    highlightQuery = searchQuery,
                                    highlightColor = MaterialTheme.colorScheme.primary,
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
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    onActionClick = onTriggerExpandSearch
                                )
                            }
                        }
                    }
                }
            } else {
                when {
                    isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(6) { MaterialFileItemSkeleton() }
                    }
                }
                filteredFiles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialEmptyFolderView(searchQuery = searchQuery)
                    }
                }
                viewMode == ExplorerViewMode.LIST -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(
                            items = filteredFiles,
                            key = { _, file -> file.path },
                            contentType = { _, file -> if (file.isDirectory) "folder" else "file" }
                        ) { _, file ->
                            MaterialFileListItem(
                                file = file,
                                isSelected = selectionManager.isSelected(file.path),
                                isSelectionMode = isSelecting,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
                                dividerColor = dividerColor,
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
                }
                viewMode == ExplorerViewMode.GRID -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = filteredFiles,
                            key = { _, file -> file.path },
                            contentType = { _, file -> if (file.isDirectory) "folder" else "file" }
                        ) { _, file ->
                            MaterialFileGridCard(
                                file = file,
                                isSelected = selectionManager.isSelected(file.path),
                                isSelectionMode = isSelecting,
                                cardBgColor = cardBgColor,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
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
                }
                viewMode == ExplorerViewMode.WATERFALL -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        itemsIndexed(
                            items = filteredFiles,
                            key = { _, file -> file.path },
                            contentType = { _, file -> if (file.isDirectory) "folder" else "file" }
                        ) { _, file ->
                            MaterialFileWaterfallItem(
                                file = file,
                                isSelected = selectionManager.isSelected(file.path),
                                isSelectionMode = isSelecting,
                                primaryTextColor = primaryTextColor,
                                secondaryTextColor = secondaryTextColor,
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
                }
            }
        }
    }
    }

    // Sort Bottom Sheet Modal
    if (showSortBottomSheet) {
        MaterialSortBottomSheet(
            currentSortBy = sortBy,
            currentSortOrder = sortOrder,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            dividerColor = dividerColor,
            onSortByChange = { newSortBy ->
                scope.launch { explorerPreferences.setSortBy(newSortBy) }
            },
            onSortOrderChange = { newOrder ->
                scope.launch { explorerPreferences.setSortOrder(newOrder) }
            },
            onDismiss = { showSortBottomSheet = false }
        )
    }

    // File Operations Dialogs & Progress Overlays Host
    val selectedPathsList = selectionManager.selectedPaths.toList()
    val singleFile = if (selectedPathsList.size == 1) files.find { it.path == selectedPathsList.first() } else null

    MaterialExplorerDialogHost(
        currentPath = path,
        showSelectionBottomSheet = showSelectionBottomSheet,
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
        onDismissSelectionBottomSheet = { showSelectionBottomSheet = false },
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
        onExtractSingleFile = { file -> showQuickExtractDialog = file },
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
                isManualSelectionMode = false
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
                            findCommonParent(selectedFiles)
                        },
                        compressionLevel = compressionLevel,
                        password = password,
                        encryptionType = encryptionType
                    ).collect { prog ->
                        archiveCreationProgress = prog
                    }
                    archiveCreationProgress = null
                    isManualSelectionMode = false
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
