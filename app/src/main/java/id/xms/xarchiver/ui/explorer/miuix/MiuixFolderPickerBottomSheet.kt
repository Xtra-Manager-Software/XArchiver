package id.xms.xarchiver.ui.explorer.miuix

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xarchiver.R
import id.xms.xarchiver.core.FileItem
import id.xms.xarchiver.core.FileOperationsManager
import id.xms.xarchiver.core.FileOperationResult
import id.xms.xarchiver.core.StorageInfo
import id.xms.xarchiver.core.StorageUtils
import id.xms.xarchiver.core.humanReadable
import id.xms.xarchiver.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * HyperOS / MIUIX styled ModalBottomSheet for browsing and selecting destination folders
 * for Copy, Move (Cut), and Extract actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiuixFolderPickerBottomSheet(
    title: String? = null,
    operation: String = "SELECT", // "COPY", "CUT", "EXTRACT", "SELECT"
    itemCount: Int = 1,
    initialPath: String? = null,
    isDark: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    dividerColor: Color,
    inputBgColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    miuixBlue: Color,
    onDismiss: () -> Unit,
    onFolderSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePreferences = remember { ThemePreferences(context) }
    val isRootAccessEnabled by themePreferences.isRootAccessEnabled.collectAsState(initial = false)

    var currentPath by remember { mutableStateOf<String?>(null) }
    var partitions by remember { mutableStateOf<List<StorageInfo>>(emptyList()) }
    var folders by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    // Intercept back gesture to navigate up folder hierarchy within sheet
    BackHandler(enabled = currentPath != null) {
        val path = currentPath
        if (path != null) {
            val parent = File(path).parent
            if (parent == null || partitions.any { it.path == path }) {
                currentPath = null
            } else {
                currentPath = parent
            }
        }
    }

    // Load partitions on launch
    LaunchedEffect(isRootAccessEnabled) {
        withContext(Dispatchers.IO) {
            val storages = StorageUtils.getAllStorage(context).toMutableList()
            if (isRootAccessEnabled) {
                storages.add(StorageUtils.getRootStorageInfo())
            }
            partitions = storages
        }
    }

    // Load subfolders when currentPath changes
    LaunchedEffect(currentPath) {
        val target = currentPath
        if (target != null) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val allItems = id.xms.xarchiver.core.FileService.listDirectory(target)
                    folders = allItems.filter { it.isDirectory && !it.name.startsWith(".") }
                        .sortedBy { it.name.lowercase() }
                } catch (e: Exception) {
                    folders = emptyList()
                }
            }
            isLoading = false
        }
    }

    val sheetTitle = title ?: when (operation) {
        "COPY" -> stringResource(R.string.folder_picker_copy_title)
        "CUT" -> stringResource(R.string.folder_picker_move_title)
        "EXTRACT" -> stringResource(R.string.folder_picker_extract_title)
        else -> stringResource(R.string.folder_picker_select_title)
    }

    val actionButtonText = when (operation) {
        "COPY" -> stringResource(R.string.folder_picker_copy_action)
        "CUT" -> stringResource(R.string.folder_picker_move_action)
        "EXTRACT" -> stringResource(R.string.folder_picker_extract_action)
        else -> stringResource(R.string.folder_picker_select_action)
    }

    val sheetBgColor = if (isDark) Color(0xFF1C1C1E) else Color.White
    val navSurfaceColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    val buttonCancelBg = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBgColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        if (isDark) Color(0xFF38383A) else Color(0xFFE5E5EA),
                        CircleShape
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header: Title + Item Count Subtitle + Close Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sheetTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        text = if (itemCount > 0) {
                            stringResource(R.string.folder_picker_items_count, itemCount, if (itemCount > 1) "s" else "")
                        } else {
                            stringResource(R.string.folder_picker_select_storage)
                        },
                        fontSize = 13.sp,
                        color = secondaryTextColor
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = navSurfaceColor,
                    modifier = Modifier.size(32.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MIUIX Breadcrumb & Navigation Card
            Surface(
                color = navSurfaceColor,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPath != null) {
                        IconButton(
                            onClick = {
                                val path = currentPath
                                if (path != null) {
                                    val parent = File(path).parent
                                    if (parent == null || partitions.any { it.path == path }) {
                                        currentPath = null
                                    } else {
                                        currentPath = parent
                                    }
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = miuixBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = File(currentPath!!).name.ifEmpty { currentPath!! },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentPath!!,
                                fontSize = 11.sp,
                                color = secondaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { showNewFolderDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreateNewFolder,
                                contentDescription = "New folder",
                                tint = miuixBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = miuixBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.folder_picker_select_storage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Storage List or Folder List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = miuixBlue,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                } else if (currentPath == null) {
                    // Partitions List
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Quick Jump to Current Directory
                        if (!initialPath.isNullOrBlank() && File(initialPath).exists()) {
                            item {
                                Surface(
                                    color = cardBgColor,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { currentPath = initialPath }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = miuixBlue.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.FolderSpecial,
                                                    contentDescription = null,
                                                    tint = miuixBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.folder_picker_current_folder),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryTextColor
                                            )
                                            Text(
                                                text = initialPath,
                                                fontSize = 12.sp,
                                                color = secondaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = secondaryTextColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        items(partitions, key = { it.path }) { partition ->
                            val isRoot = partition.path == "/"
                            val freeSpace = (partition.total - partition.used).humanReadable()
                            val totalSpace = partition.total.humanReadable()

                            Surface(
                                color = cardBgColor,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { currentPath = partition.path }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (isRoot) Color(0xFFEF4444).copy(alpha = 0.15f) else miuixBlue.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isRoot) Icons.Default.Security else Icons.Default.Storage,
                                                contentDescription = null,
                                                tint = if (isRoot) Color(0xFFEF4444) else miuixBlue,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = partition.label,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryTextColor
                                        )
                                        Text(
                                            text = if (partition.total > 0) "$freeSpace bebas dari $totalSpace" else partition.path,
                                            fontSize = 12.sp,
                                            color = secondaryTextColor
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = secondaryTextColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Folders List
                    if (folders.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = secondaryTextColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.folder_picker_empty),
                                fontSize = 14.sp,
                                color = secondaryTextColor
                            )
                        }
                    } else {
                        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(folders, key = { it.path }) { folder ->
                                Surface(
                                    color = cardBgColor,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { currentPath = folder.path }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Folder,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = primaryTextColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val subtext = if (folder.itemCount != null && folder.itemCount > 0) {
                                                "${folder.itemCount} item"
                                            } else {
                                                dateFormat.format(Date(folder.lastModified))
                                            }
                                            Text(
                                                text = subtext,
                                                fontSize = 12.sp,
                                                color = secondaryTextColor,
                                                maxLines = 1
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = secondaryTextColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Dock
            Surface(
                color = sheetBgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonCancelBg,
                            contentColor = primaryTextColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { currentPath?.let(onFolderSelected) },
                        enabled = currentPath != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = miuixBlue,
                            disabledContainerColor = miuixBlue.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = actionButtonText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Modal Create New Folder Dialog
    if (showNewFolderDialog && currentPath != null) {
        MiuixTextInputDialog(
            title = stringResource(R.string.dialog_new_folder),
            initialValue = "",
            placeholder = stringResource(R.string.dialog_name),
            confirmText = stringResource(R.string.action_create),
            isDark = isDark,
            cardBg = cardBgColor,
            inputBg = inputBgColor,
            primaryColor = miuixBlue,
            textColor = primaryTextColor,
            secondaryColor = secondaryTextColor,
            icon = Icons.Default.CreateNewFolder,
            onConfirm = { name ->
                showNewFolderDialog = false
                val trimmed = name.trim()
                if (trimmed.isNotEmpty() && currentPath != null) {
                    val targetParent = currentPath!!
                    scope.launch {
                        val result = FileOperationsManager.createFolder(targetParent, trimmed)
                        if (result is FileOperationResult.Success) {
                            currentPath = "$targetParent/$trimmed"
                        }
                    }
                }
            },
            onDismiss = { showNewFolderDialog = false }
        )
    }
}
