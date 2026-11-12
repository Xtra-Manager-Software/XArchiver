package id.xms.xarchiver.ui.home

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import id.xms.xarchiver.R
import id.xms.xarchiver.core.*
import id.xms.xarchiver.ui.theme.ThemeMode
import id.xms.xarchiver.ui.theme.ThemePreferences
import id.xms.xarchiver.ui.theme.isAppInDarkTheme
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.blur.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

/**
 * Redesigned MIUIX / HyperOS Home Screen.
 * Adopts the authentic Xiaomi File Manager visual hierarchy (Category pills grid,
 * Quick Access group, Modern Storage card, Sources group, and dual-capsule floating navigation)
 * while enriching it with modern XArchiver touches.
 */
@Composable
fun MiuixHomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    val isRootAccessEnabled by themePreferences.isRootAccessEnabled.collectAsState(initial = false)
    val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val isDark = isAppInDarkTheme(context, themeMode)
    var currentTab by remember { mutableStateOf(HomeTab.Files) }
    val backdrop = rememberLayerBackdrop()

    LaunchedEffect(currentTab) {
        if (currentTab == HomeTab.Recent) {
            viewModel.loadRecentFiles()
        }
    }

    val pageBgColor = if (isDark) Color.Black else Color(0xFFF3F4F6)
    val screenTitle = if (currentTab == HomeTab.Files) {
        stringResource(R.string.home_browse)
    } else {
        stringResource(R.string.home_recent_tab)
    }

    Scaffold(
        containerColor = pageBgColor,
        topBar = {
            TopAppBar(
                title = screenTitle,
                largeTitle = screenTitle,
                color = pageBgColor,
                actions = {
                    IconButton(onClick = { navController.navigate("changelog") }) {
                        Icon(MiuixIcons.Notes, contentDescription = stringResource(R.string.about_changelog))
                    }
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(MiuixIcons.Settings, contentDescription = "Settings")
                        }
                        if (id.xms.xarchiver.core.update.UpdateManager.availableUpdate != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dual Pill Navigation Capsule
                Surface(
                    modifier = Modifier
                        .shadow(
                            elevation = if (isDark) 12.dp else 8.dp,
                            shape = RoundedCornerShape(100),
                            spotColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f),
                            ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.06f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color(0xFF2C2C2F) else Color(0xFFE2E4E9),
                            shape = RoundedCornerShape(100)
                        )
                        .textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(100),
                            blurRadiusX = 25f,
                            blurRadiusY = 25f
                        ),
                    color = if (isDark) Color(0xFF1A1A1C).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(100)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isRecent = currentTab == HomeTab.Recent
                        Button(
                            onClick = { currentTab = HomeTab.Recent },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = if (isRecent) {
                                ButtonDefaults.buttonColorsPrimary()
                            } else {
                                ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.onSurface)
                            }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = MiuixIcons.Recent,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(17.dp),
                                tint = if (isRecent) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.home_recent_tab),
                                fontWeight = if (isRecent) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }

                        val isFiles = currentTab == HomeTab.Files
                        Button(
                            onClick = { currentTab = HomeTab.Files },
                            modifier = Modifier.padding(start = 4.dp),
                            colors = if (isFiles) {
                                ButtonDefaults.buttonColorsPrimary()
                            } else {
                                ButtonDefaults.buttonColors(color = Color.Transparent, contentColor = MiuixTheme.colorScheme.onSurface)
                            }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = MiuixIcons.Folder,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(17.dp),
                                tint = if (isFiles) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.home_browse),
                                fontWeight = if (isFiles) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Floating Circular Quick Search Button
                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            elevation = if (isDark) 12.dp else 8.dp,
                            shape = CircleShape,
                            spotColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f),
                            ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.06f)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color(0xFF2C2C2F) else Color(0xFFE2E4E9),
                            shape = CircleShape
                        )
                        .textureBlur(
                            backdrop = backdrop,
                            shape = CircleShape,
                            blurRadiusX = 25f,
                            blurRadiusY = 25f
                        )
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate("explorer/${Uri.encode("/sdcard")}")
                        },
                    color = if (isDark) Color(0xFF1A1A1C).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.88f),
                    shape = CircleShape
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.explorer_search_files),
                            tint = if (isDark) Color.White else Color(0xFF1F2937),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .layerBackdrop(backdrop),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                if (currentTab == HomeTab.Files) {
                    MiuixFilesContent(viewModel, navController, isRootAccessEnabled, isDark)
                } else {
                    MiuixRecentContent(viewModel, navController, isDark)
                }
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

/**
 * Main Files / Telusuri content adhering to HyperOS File Manager hierarchy:
 * 1. Category 2-Column Grid Pills
 * 2. Quick Access Group (Downloads, Favorites)
 * 3. Modern Storage Card (Phone storage with gauge + Root privileged storage)
 * 4. Sources Section (Camera, Screenshots, Messaging shortcuts)
 */
@Composable
fun MiuixFilesContent(
    viewModel: HomeViewModel,
    navController: NavController,
    isRootAccessEnabled: Boolean,
    isDark: Boolean
) {
    val context = LocalContext.current
    val bookmarksManager = remember { BookmarksManager(context) }
    val bookmarks by bookmarksManager.bookmarks.collectAsState(initial = emptyList())

    val cardColor = if (isDark) Color(0xFF161618) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF242428) else Color(0xFFE5E7EB)
    val dividerColor = if (isDark) Color(0xFF222226) else Color(0xFFF0F1F4)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)
    val miuixBlue = Color(0xFF0070F0)

    val cardModifier = Modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isDark) 0.dp else 1.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.04f)
        )
        .border(
            width = 0.5.dp,
            color = cardBorderColor,
            shape = RoundedCornerShape(20.dp)
        )

    val externalStorage = Environment.getExternalStorageDirectory().absolutePath
    val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    val cameraPath = "$externalStorage/DCIM/Camera"
    val screenshotsPath = "$externalStorage/Pictures/Screenshots"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Category Pills: 2 Columns x 3 Rows
        val categories = viewModel.categories.value
        val catDoc = categories.find { it.name.equals("Documents", true) }
        val catImage = categories.find { it.name.equals("Images", true) }
        val catVideo = categories.find { it.name.equals("Videos", true) }
        val catAudio = categories.find { it.name.equals("Audio", true) }
        val catArchive = categories.find { it.name.equals("Archives", true) }
        val catApk = categories.find { it.name.equals("APK", true) || it.name.equals("Apps", true) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Dok & Gambar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_doc),
                    count = catDoc?.count ?: 0,
                    icon = MiuixIcons.Notes,
                    iconBgColor = Color(0xFFF59E0B), // Warm Amber
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("Documents")}") }
                )
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_image),
                    count = catImage?.count ?: 0,
                    icon = MiuixIcons.Image,
                    iconBgColor = Color(0xFF3B82F6), // Royal Blue
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("Images")}") }
                )
            }

            // Row 2: Video & Musik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_video),
                    count = catVideo?.count ?: 0,
                    icon = MiuixIcons.Play,
                    iconBgColor = Color(0xFF8B5CF6), // Purple / Indigo
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("Videos")}") }
                )
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_music),
                    count = catAudio?.count ?: 0,
                    icon = MiuixIcons.Music,
                    iconBgColor = Color(0xFFEF4444), // Coral Red
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("Audio")}") }
                )
            }

            // Row 3: Arsip & APK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_archive),
                    count = catArchive?.count ?: 0,
                    icon = Icons.Outlined.FolderZip,
                    iconBgColor = Color(0xFF06B6D4), // Cyan / Sky Blue
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("Archives")}") }
                )
                MiuixCategoryPill(
                    title = stringResource(R.string.home_cat_apk),
                    count = catApk?.count ?: 0,
                    icon = Icons.Outlined.Android,
                    iconBgColor = Color(0xFF10B981), // Emerald Green
                    cardBgColor = cardColor,
                    cardBorderColor = cardBorderColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("category_explorer/${Uri.encode("APK")}") }
                )
            }
        }

        // 2. Quick Access Group: Unduhan & Favorit
        Surface(
            modifier = cardModifier,
            color = cardColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Downloads Item
                MiuixActionRow(
                    title = stringResource(R.string.home_downloads),
                    subtitle = downloadsPath,
                    icon = Icons.Outlined.Download,
                    iconTint = miuixBlue,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    onClick = { navController.navigate("explorer/${Uri.encode(downloadsPath)}") }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = dividerColor,
                    thickness = 0.5.dp
                )

                // Favorites / Bookmarks Item
                val favSubtitle = if (bookmarks.isNotEmpty()) {
                    stringResource(R.string.home_favorites_count, bookmarks.size)
                } else {
                    stringResource(R.string.home_favorites_empty)
                }
                MiuixActionRow(
                    title = stringResource(R.string.home_favorites),
                    subtitle = favSubtitle,
                    icon = Icons.Outlined.FavoriteBorder,
                    iconTint = Color(0xFFF43F5E), // Rose
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    onClick = {
                        if (bookmarks.isNotEmpty()) {
                            navController.navigate("explorer/${Uri.encode(bookmarks.first().path)}")
                        } else {
                            navController.navigate("explorer/${Uri.encode(externalStorage)}")
                        }
                    }
                )
            }
        }

        // 3. Modern Storage Section: "Ponsel saya" & "Penyimpanan Sistem (Root)"
        val storages = viewModel.storages.value
        val isRefreshing = viewModel.isRefreshing.value

        Surface(
            modifier = cardModifier,
            color = cardColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.home_detecting_storage),
                        color = secondaryTextColor,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Internal primary storage ("Ponsel saya")
                    storages.forEachIndexed { index, storage ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = dividerColor,
                                thickness = 0.5.dp
                            )
                        }
                        val isInternal = !storage.label.contains("USB", ignoreCase = true) && !storage.label.contains("SD", ignoreCase = true)
                        val icon = if (isInternal) MiuixIcons.Phone else MiuixIcons.Folder
                        val title = if (isInternal) stringResource(R.string.home_my_phone) else storage.label
                        val freeBytes = (storage.total - storage.used).coerceAtLeast(0L)
                        val usageFraction = if (storage.total > 0L) (storage.used.toFloat() / storage.total.toFloat()).coerceIn(0f, 1f) else 0f

                        MiuixStorageRow(
                            title = title,
                            freeBytes = freeBytes,
                            totalBytes = storage.total,
                            usageFraction = usageFraction,
                            badge = storage.fsType.ifEmpty { if (isInternal) "Internal" else "" },
                            icon = icon,
                            iconTint = miuixBlue,
                            progressBarColor = miuixBlue,
                            isDark = isDark,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { navController.navigate("explorer/${Uri.encode(storage.path)}") }
                        )
                    }

                    // Root Storage (XArchiver's signature feature)
                    if (isRootAccessEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = dividerColor,
                            thickness = 0.5.dp
                        )

                        val rootInfo = viewModel.rootStorageInfo
                        val rootFreeBytes = (rootInfo.total - rootInfo.used).coerceAtLeast(0L)
                        val rootUsageFraction = if (rootInfo.total > 0L) (rootInfo.used.toFloat() / rootInfo.total.toFloat()).coerceIn(0f, 1f) else 0f
                        val amberColor = Color(0xFFF59E0B)

                        MiuixStorageRow(
                            title = stringResource(R.string.home_root_storage),
                            freeBytes = rootFreeBytes,
                            totalBytes = rootInfo.total,
                            usageFraction = rootUsageFraction,
                            badge = "Root",
                            icon = Icons.Outlined.Security,
                            iconTint = amberColor,
                            progressBarColor = amberColor,
                            isDark = isDark,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { navController.navigate("root_explorer/${Uri.encode("/")}") }
                        )
                    }
                }
            }
        }

        // 4. Section "Sumber" (Sources)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.home_sources),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )

            Surface(
                modifier = cardModifier,
                color = cardColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Kamera
                    MiuixActionRow(
                        title = stringResource(R.string.home_camera),
                        subtitle = stringResource(R.string.home_camera_desc),
                        icon = Icons.Outlined.PhotoCamera,
                        iconTint = Color(0xFF0EA5E9), // Light Sky Blue
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = {
                            val targetPath = if (File(cameraPath).exists()) cameraPath else "$externalStorage/DCIM"
                            navController.navigate("explorer/${Uri.encode(targetPath)}")
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = dividerColor,
                        thickness = 0.5.dp
                    )

                    // Screenshot & rekaman layar
                    MiuixActionRow(
                        title = stringResource(R.string.home_screenshots),
                        subtitle = stringResource(R.string.home_screenshots_desc),
                        icon = Icons.Outlined.Screenshot,
                        iconTint = Color(0xFF6366F1), // Indigo
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = {
                            val targetPath = if (File(screenshotsPath).exists()) screenshotsPath else "$externalStorage/Pictures"
                            navController.navigate("explorer/${Uri.encode(targetPath)}")
                        }
                    )

                    // Additional Messaging Shortcuts (WhatsApp / Telegram / Bluetooth)
                    val shortcuts = viewModel.shortcuts
                    shortcuts.filter { it.name != "Downloads" }.forEach { shortcut ->
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = dividerColor,
                            thickness = 0.5.dp
                        )
                        val icon = when (shortcut.name.lowercase()) {
                            "whatsapp" -> Icons.Outlined.Forum
                            "telegram" -> Icons.AutoMirrored.Outlined.Send
                            "bluetooth" -> Icons.Outlined.Bluetooth
                            else -> Icons.Outlined.Folder
                        }
                        val tint = when (shortcut.name.lowercase()) {
                            "whatsapp" -> Color(0xFF22C55E)
                            "telegram" -> Color(0xFF0284C7)
                            "bluetooth" -> Color(0xFF3B82F6)
                            else -> miuixBlue
                        }
                        MiuixActionRow(
                            title = shortcut.name,
                            subtitle = shortcut.path,
                            icon = icon,
                            iconTint = tint,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { navController.navigate("explorer/${Uri.encode(shortcut.path)}") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual squircle pill card representing a category in the 2-column grid.
 */
@Composable
fun MiuixCategoryPill(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    cardBgColor: Color,
    cardBorderColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBgColor)
            .border(0.5.dp, cardBorderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconBgColor,
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "$count item",
                color = secondaryTextColor,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Action row with squircle icon, title, subtitle, and ChevronForward.
 */
@Composable
fun MiuixActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconTint.copy(alpha = 0.12f),
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = primaryTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = secondaryTextColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        androidx.compose.material3.Icon(
            imageVector = MiuixIcons.ChevronForward,
            contentDescription = null,
            tint = secondaryTextColor.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Storage item with gauge, filesystem badge, and slim linear progress bar.
 */
@Composable
fun MiuixStorageRow(
    title: String,
    freeBytes: Long,
    totalBytes: Long,
    usageFraction: Float,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    progressBarColor: Color,
    isDark: Boolean,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = iconTint.copy(alpha = 0.12f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = primaryTextColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (badge.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = iconTint.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = iconTint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${freeBytes.humanReadable()} bebas / ${totalBytes.humanReadable()}",
                color = secondaryTextColor,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            // Modern Slim Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color(0xFF26262A) else Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = usageFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(progressBarColor)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        androidx.compose.material3.Icon(
            imageVector = MiuixIcons.ChevronForward,
            contentDescription = null,
            tint = secondaryTextColor.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Recent tab content showing recent files grouped by timeline.
 */
@Composable
fun MiuixRecentContent(viewModel: HomeViewModel, navController: NavController, isDark: Boolean) {
    val recentFiles = viewModel.recentFilesGrouped.value
    val cardColor = if (isDark) Color(0xFF161618) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF242428) else Color(0xFFE5E7EB)
    val dividerColor = if (isDark) Color(0xFF222226) else Color(0xFFF0F1F4)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)
    val miuixBlue = Color(0xFF0070F0)

    val cardModifier = Modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isDark) 0.dp else 1.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color.Black.copy(alpha = 0.04f)
        )
        .border(
            width = 0.5.dp,
            color = cardBorderColor,
            shape = RoundedCornerShape(20.dp)
        )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (recentFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = MiuixIcons.Recent,
                        contentDescription = null,
                        tint = secondaryTextColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tidak ada berkas terbaru",
                        color = secondaryTextColor,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            recentFiles.forEach { (group, items) ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = group,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                    Surface(
                        modifier = cardModifier,
                        color = cardColor,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            items.forEachIndexed { index, file ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = dividerColor,
                                        thickness = 0.5.dp
                                    )
                                }
                                MiuixActionRow(
                                    title = file.name,
                                    subtitle = file.path,
                                    icon = if (file.isDirectory) MiuixIcons.Folder else MiuixIcons.File,
                                    iconTint = miuixBlue,
                                    primaryTextColor = primaryTextColor,
                                    secondaryTextColor = secondaryTextColor,
                                    onClick = {
                                        navController.navigate("explorer/${Uri.encode(File(file.path).parent ?: "/sdcard")}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
