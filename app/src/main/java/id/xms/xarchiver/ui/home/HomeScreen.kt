package id.xms.xarchiver.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.items
import id.xms.xarchiver.R
import id.xms.xarchiver.core.Bookmark
import id.xms.xarchiver.core.BookmarksManager
import id.xms.xarchiver.core.FileItem
import id.xms.xarchiver.core.humanReadable
import id.xms.xarchiver.core.ShareUtils
import id.xms.xarchiver.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class HomeTab {
    Recent, Files
}

@SuppressLint("SdCardPath")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val isRootAccessEnabled by themePreferences.isRootAccessEnabled.collectAsState(initial = false)
    val isMiuixUiEnabled by themePreferences.isMiuixUiEnabled.collectAsState(initial = ThemePreferences.isMiuiOrHyperOsDevice)

    if (isMiuixUiEnabled) {
        MiuixHomeScreen(navController, viewModel)
        return
    }

    val scope = rememberCoroutineScope()
    val bookmarksManager = remember { BookmarksManager(context) }
    val bookmarks by bookmarksManager.bookmarks.collectAsState(initial = emptyList())
    var showBookmarksSheet by remember { mutableStateOf(false) }
    
    var currentTab by remember { mutableStateOf(HomeTab.Files) }
    
    LaunchedEffect(currentTab) {
        if (currentTab == HomeTab.Recent) {
            viewModel.loadRecentFiles()
        }
    }
    
    val listState = rememberLazyListState()
    val recentListState = rememberLazyListState()
    
    // Staggered entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // Parallax effect for header based on scroll
    val scrollOffset by remember {
        derivedStateOf {
            if (currentTab == HomeTab.Files && listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else if (currentTab == HomeTab.Recent && recentListState.firstVisibleItemIndex == 0) {
                recentListState.firstVisibleItemScrollOffset.toFloat()
            } else {
                300f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            topBar = {
                ModernTopBar(
                    onChangelogClick = { navController.navigate("changelog") },
                    onSettingsClick = { navController.navigate("settings") },
                    scrollOffset = scrollOffset
                )
            },
            floatingActionButton = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        val isRecent = currentTab == HomeTab.Recent
                        Surface(
                            shape = CircleShape,
                            color = if (isRecent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { currentTab = HomeTab.Recent }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecent) Icons.Filled.AccessTime else Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = if (isRecent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.fab_recent),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isRecent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isRecent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isFiles = currentTab == HomeTab.Files
                        Surface(
                            shape = CircleShape,
                            color = if (isFiles) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { currentTab = HomeTab.Files }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFiles) Icons.Filled.Folder else Icons.Outlined.Folder,
                                    contentDescription = null,
                                    tint = if (isFiles) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.fab_files),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isFiles) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFiles) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color.Transparent
        ) { padding ->
            if (currentTab == HomeTab.Files) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Storage Section
                    item(key = "storage") {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = tween(600, 100, EaseOutCubic)
                            )
                        ) {
                            StorageSection(
                                viewModel = viewModel,
                                navController = navController,
                                isRootAccessEnabled = isRootAccessEnabled
                            )
                        }
                    }

                    // Quick Categories - Grid layout & Bookmarks
                    item(key = "categories") {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = tween(600, 200, EaseOutCubic)
                            )
                        ) {
                            CategoriesSection(
                                categories = viewModel.categories.value,
                                bookmarksCount = bookmarks.size,
                                firstBookmarkName = bookmarks.firstOrNull()?.name,
                                onCategoryClick = { category ->
                                    navController.navigate("category_explorer/${Uri.encode(category.name)}")
                                },
                                onBookmarkClick = {
                                    when {
                                        bookmarks.isEmpty() -> {
                                            navController.navigate("explorer/${Uri.encode("/sdcard")}")
                                        }
                                        bookmarks.size == 1 -> {
                                            navController.navigate("explorer/${Uri.encode(bookmarks.first().path)}")
                                        }
                                        else -> {
                                            showBookmarksSheet = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Quick Access Folders
                    item(key = "shortcuts") {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(
                                initialOffsetY = { 50 },
                                animationSpec = tween(600, 300, EaseOutCubic)
                            )
                        ) {
                            QuickAccessSection(
                                shortcuts = viewModel.shortcuts,
                                onShortcutClick = { shortcut ->
                                    navController.navigate("explorer/${Uri.encode(shortcut.path)}")
                                }
                            )
                        }
                    }
                }
            } else {
                // Recent Files Screen
                val groupedFiles = viewModel.recentFilesGrouped.value
                val isLoading = viewModel.isLoadingRecent.value
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (groupedFiles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.recent_empty),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.recent_empty_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = recentListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(bottom = 88.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.recent_today),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        
                        groupedFiles.forEach { (bucketName, files) ->
                            item(key = bucketName) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Folder Header
                                        val parentDir = files.firstOrNull()?.let { java.io.File(it.path).parent }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    if (parentDir != null) {
                                                        navController.navigate("explorer/${Uri.encode(parentDir)}")
                                                    }
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            val headerIcon = when {
                                                bucketName.contains("tangkapan layar", ignoreCase = true) || bucketName.contains("screenshot", ignoreCase = true) -> Icons.Filled.PhotoLibrary
                                                bucketName.contains("kamera", ignoreCase = true) || bucketName.contains("camera", ignoreCase = true) -> Icons.Filled.CameraAlt
                                                bucketName.contains("pictures", ignoreCase = true) || bucketName.contains("gambar", ignoreCase = true) -> Icons.Filled.Image
                                                bucketName.contains("download", ignoreCase = true) || bucketName.contains("unduhan", ignoreCase = true) -> Icons.Filled.Download
                                                bucketName.contains("whatsapp", ignoreCase = true) -> Icons.AutoMirrored.Filled.Chat
                                                else -> Icons.Filled.Folder
                                            }
                                            Icon(
                                                imageVector = headerIcon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = bucketName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        val allMedia = files.all { f ->
                                             val lower = f.name.lowercase()
                                            lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".mp4")
                                        }
                                        
                                        if (allMedia) {
                                            // Horizontal thumbnail preview row
                                            androidx.compose.foundation.lazy.LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                items(files.size) { index ->
                                                    val file = files[index]
                                                    val isVideo = file.name.lowercase().endsWith(".mp4")
                                                    Box(
                                                        modifier = Modifier
                                                            .size(104.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .clickable {
                                                                openRecentItem(context, file, navController)
                                                            }
                                                    ) {
                                                        androidx.compose.foundation.Image(
                                                            painter = coil.compose.rememberAsyncImagePainter(java.io.File(file.path)),
                                                            contentDescription = file.name,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                        if (isVideo) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Surface(
                                                                    shape = CircleShape,
                                                                    color = Color.Black.copy(alpha = 0.5f),
                                                                    modifier = Modifier.size(28.dp)
                                                                ) {
                                                                    Box(contentAlignment = Alignment.Center) {
                                                                        Icon(
                                                                            Icons.Filled.PlayArrow,
                                                                            contentDescription = null,
                                                                            tint = Color.White,
                                                                            modifier = Modifier.size(18.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // List item style (like Download card in Origin OS)
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                files.take(4).forEach { file ->
                                                    val isDir = file.isDirectory
                                                    val isApk = file.name.endsWith(".apk", ignoreCase = true)
                                                    val isArchive = file.name.lowercase().let { it.endsWith(".zip") || it.endsWith(".rar") || it.endsWith(".7z") || it.endsWith(".tar") || it.endsWith(".gz") }
                                                    val isImage = file.name.lowercase().let { it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg") || it.endsWith(".webp") || it.endsWith(".gif") }
                                                    
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                            .clickable {
                                                                openRecentItem(context, file, navController)
                                                            }
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Icon Box
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    when {
                                                                        isDir -> MaterialTheme.colorScheme.primaryContainer
                                                                        isApk -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                                        isArchive -> MaterialTheme.colorScheme.secondaryContainer
                                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                                    }
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            when {
                                                                isImage -> {
                                                                    androidx.compose.foundation.Image(
                                                                        painter = coil.compose.rememberAsyncImagePainter(java.io.File(file.path)),
                                                                        contentDescription = null,
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                                    )
                                                                }
                                                                isDir -> Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                                isApk -> Icon(Icons.Filled.Android, contentDescription = null, tint = Color(0xFF4CAF50))
                                                                isArchive -> Icon(Icons.Filled.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                                                else -> Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                            }
                                                        }
                                                        
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = file.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                modifier = Modifier.basicMarquee()
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = if (isDir) "Folder" else file.size.humanReadable(),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBookmarksSheet && bookmarks.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = { showBookmarksSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.home_favorites),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = stringResource(R.string.home_favorites_count, bookmarks.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(bookmarks, key = { it.path }) { bookmark ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBookmarksSheet = false
                                        navController.navigate("explorer/${Uri.encode(bookmark.path)}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bookmark.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = bookmark.path,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                bookmarksManager.removeBookmark(bookmark.path)
                                                if (bookmarks.size <= 1) {
                                                    showBookmarksSheet = false
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Hapus bookmark",
                                            tint = MaterialTheme.colorScheme.error,
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
    }
}

// Removed AnimatedBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(
    onChangelogClick: () -> Unit,
    onSettingsClick: () -> Unit,
    scrollOffset: Float
) {
    val elevation = (scrollOffset / 50f).coerceIn(0f, 1f)
    
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f + (elevation * 0.15f)),
        shadowElevation = (elevation * 4).dp
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // App Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "XArchiver Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            "XArchiver",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            actions = {
                // Changelog button
                IconButton(onClick = onChangelogClick) {
                    Icon(
                        Icons.Outlined.NewReleases,
                        contentDescription = stringResource(R.string.about_changelog),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Settings button
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (id.xms.xarchiver.core.update.UpdateManager.availableUpdate != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

// WelcomeHeader removed

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun StorageSection(
    viewModel: HomeViewModel,
    navController: NavController,
    isRootAccessEnabled: Boolean
) {
    val storages = viewModel.storages.value
    val isRefreshing = viewModel.isRefreshing.value
    val rootEnabled = isRootAccessEnabled
    val pageCount = storages.size + (if (rootEnabled) 1 else 0)

    // Gunakan teknik polling karena beberapa custom ROM (HyperOS/MIUI) 
    // seringkali menggunakan intent broadcast non-standar untuk USB OTG.
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.refreshStorage()
            kotlinx.coroutines.delay(2000) // Cek setiap 2 detik
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pageCount })

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(
                title = stringResource(R.string.home_storage),
                icon = Icons.AutoMirrored.Outlined.List,
                trailing = {
                    if (isRefreshing) {
                        @OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
                        androidx.compose.material3.ContainedLoadingIndicator()
                    } else if (pageCount > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                stringResource(R.string.home_storage_page_format, pagerState.currentPage + 1, pageCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            )
        }
        
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            if (isRefreshing) {
                StoragePlaceholderCard(title = stringResource(R.string.home_detecting_storage))
            } else if (page < storages.size) {
                val storage = storages[page]
                val icon = when {
                    storage.label.contains("USB", ignoreCase = true) -> Icons.Outlined.Usb
                    storage.label.contains("SD", ignoreCase = true) -> Icons.Outlined.Save
                    else -> Icons.Outlined.Smartphone
                }
                
                StorageCard(
                    info = storage,
                    icon = icon,
                    onClick = {
                        navController.navigate("explorer/${Uri.encode(storage.path)}")
                    }
                )
            } else if (rootEnabled) {
                RootStorageCard(
                    info = viewModel.rootStorageInfo,
                    onOpenRoot = { rootPath ->
                        navController.navigate("root_explorer/${Uri.encode(rootPath)}")
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<id.xms.xarchiver.core.Category>,
    bookmarksCount: Int,
    firstBookmarkName: String?,
    onCategoryClick: (id.xms.xarchiver.core.Category) -> Unit,
    onBookmarkClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.home_categories),
            icon = Icons.Outlined.GridView
        )
        
        CategoryGrid(
            categories = categories,
            onCategoryClick = onCategoryClick
        )

        BookmarkLongItem(
            bookmarksCount = bookmarksCount,
            firstBookmarkName = firstBookmarkName,
            onClick = onBookmarkClick
        )
    }
}

@Composable
private fun BookmarkLongItem(
    bookmarksCount: Int,
    firstBookmarkName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bookmark_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = stringResource(R.string.home_favorites),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_favorites),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (bookmarksCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$bookmarksCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                val subtitle = if (bookmarksCount > 0) {
                    if (bookmarksCount == 1 && firstBookmarkName != null) {
                        firstBookmarkName
                    } else {
                        stringResource(R.string.home_favorites_count, bookmarksCount)
                    }
                } else {
                    stringResource(R.string.home_favorites_empty)
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickAccessSection(
    shortcuts: List<id.xms.xarchiver.core.Shortcut>,
    onShortcutClick: (id.xms.xarchiver.core.Shortcut) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = stringResource(R.string.home_quick_access),
            icon = Icons.Outlined.Folder
        )
        
        ShortcutList(
            shortcuts = shortcuts,
            onShortcutClick = onShortcutClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        trailing()
    }
}

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

private fun openRecentItem(context: Context, file: FileItem, navController: NavController) {
    val actualFile = java.io.File(file.path)
    if (!actualFile.exists()) return
    
    if (file.isDirectory) {
        navController.navigate("explorer/${Uri.encode(file.path)}")
        return
    }
    
    val ext = file.name.substringAfterLast('.', "").lowercase()
    when {
        file.name.endsWith(".apk", ignoreCase = true) -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        actualFile
                    )
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                ShareUtils.openFile(context, file.path)
            }
        }
        ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp") -> {
            navController.navigate("image_viewer/${Uri.encode(file.path)}")
        }
        ext in listOf("mp4", "mkv", "webm", "avi", "3gp") -> {
            navController.navigate("video_player/${Uri.encode(file.path)}")
        }
        ext in listOf("mp3", "m4a", "wav", "flac", "ogg", "aac") -> {
            navController.navigate("audio_player/${Uri.encode(file.path)}")
        }
        ext in listOf("zip", "rar", "7z", "tar", "gz", "xz", "bz2") -> {
            navController.navigate("archive_explorer/${Uri.encode(file.path)}")
        }
        ext in listOf("txt", "log", "json", "xml", "html", "css", "js", "kt", "java", "md", "sh") -> {
            navController.navigate("text_editor/${Uri.encode(file.path)}")
        }
        else -> {
            ShareUtils.openFile(context, file.path)
        }
    }
}

