package id.xms.xarchiver.ui.explorer.miuix

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xarchiver.R
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixSelectionDock(
    isDark: Boolean,
    backdrop: LayerBackdrop,
    hasArchives: Boolean,
    selectedCount: Int,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onExtractOrShare: () -> Unit,
    onCompress: () -> Unit,
    onRename: () -> Unit,
    onProperties: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onOpenArchive: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = if (isDark) 16.dp else 10.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .border(
                width = 0.5.dp,
                color = if (isDark) Color(0xFF2C2C2F) else Color(0xFFE2E4E9),
                shape = RoundedCornerShape(26.dp)
            )
            .textureBlur(backdrop = backdrop, shape = RoundedCornerShape(26.dp), blurRadiusX = 25f, blurRadiusY = 25f),
        color = if (isDark) Color(0xFF1A1A1C).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiuixDockAction(icon = Icons.Default.ContentCopy, label = "Salin", onClick = onCopy)
            MiuixDockAction(icon = Icons.Default.ContentCut, label = "Potong", onClick = onCut)
            MiuixDockAction(icon = Icons.Default.Delete, label = "Hapus", tint = Color(0xFFEF4444), onClick = onDelete)
            if (hasArchives) {
                MiuixDockAction(icon = Icons.Default.Unarchive, label = "Ekstrak", onClick = onExtractOrShare)
            } else {
                MiuixDockAction(icon = Icons.Default.Share, label = "Bagikan", onClick = onExtractOrShare)
            }
            MiuixDockAction(icon = Icons.Default.FolderZip, label = "Kompres", onClick = onCompress)

            val moreItems = remember(selectedCount, onOpenArchive, onRename, onProperties, onBookmark, onShare) {
                buildList {
                    if (selectedCount == 1) {
                        add(
                            DropdownItem(
                                text = "Ganti Nama",
                                icon = { modifier ->
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = modifier,
                                        tint = MiuixTheme.colorScheme.primary
                                    )
                                },
                                onClick = onRename
                            )
                        )
                        add(
                            DropdownItem(
                                text = "Properti",
                                icon = { modifier ->
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = modifier,
                                        tint = MiuixTheme.colorScheme.primary
                                    )
                                },
                                onClick = onProperties
                            )
                        )
                        add(
                            DropdownItem(
                                text = "Tambah Bookmark",
                                icon = { modifier ->
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        modifier = modifier,
                                        tint = MiuixTheme.colorScheme.primary
                                    )
                                },
                                onClick = onBookmark
                            )
                        )
                    }
                    add(
                        DropdownItem(
                            text = "Bagikan",
                            icon = { modifier ->
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = modifier,
                                    tint = MiuixTheme.colorScheme.primary
                                )
                            },
                            onClick = onShare
                        )
                    )
                    if (onOpenArchive != null) {
                        add(
                            DropdownItem(
                                text = "Buka Arsip",
                                icon = { modifier ->
                                    Icon(
                                        imageVector = Icons.Default.FolderZip,
                                        contentDescription = null,
                                        modifier = modifier,
                                        tint = MiuixTheme.colorScheme.primary
                                    )
                                },
                                onClick = onOpenArchive
                            )
                        )
                    }
                }
            }

            val currentDispatcherOwner = LocalNavigationEventDispatcherOwner.current
            val fallbackDispatcherOwner = remember {
                object : NavigationEventDispatcherOwner {
                    override val navigationEventDispatcher = NavigationEventDispatcher()
                }
            }
            val dispatcherOwner = currentDispatcherOwner ?: fallbackDispatcherOwner

            CompositionLocalProvider(
                LocalNavigationEventDispatcherOwner provides dispatcherOwner
            ) {
                WindowIconDropdownMenu(
                    entry = DropdownEntry(items = moreItems),
                    backgroundColor = Color.Transparent,
                    minWidth = 0.dp,
                    minHeight = 0.dp,
                    cornerRadius = 12.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Lainnya",
                            tint = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Lainnya",
                            color = MiuixTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixDockAction(
    icon: ImageVector,
    label: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val defaultColor = MiuixTheme.colorScheme.onSurface
    val finalTint = if (tint != Color.Unspecified) tint else defaultColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = finalTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = finalTint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MiuixClipboardDock(
    clipboardCount: Int,
    isCut: Boolean,
    isDark: Boolean,
    backdrop: LayerBackdrop,
    primaryColor: Color,
    onPaste: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = primaryColor.copy(alpha = 0.25f)
            )
            .border(
                width = 0.5.dp,
                color = if (isDark) Color(0xFF2C2C2F) else Color(0xFFE2E4E9),
                shape = RoundedCornerShape(24.dp)
            )
            .textureBlur(backdrop = backdrop, shape = RoundedCornerShape(24.dp), blurRadiusX = 20f, blurRadiusY = 20f),
        color = if (isDark) Color(0xFF1A1A1C).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$clipboardCount item dipilih",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isCut) "Siap dipindahkan ke sini" else "Siap disalin ke sini",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        color = Color.Transparent,
                        contentColor = if (isDark) Color.White else Color(0xFF1F2937)
                    )
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = onPaste,
                    colors = ButtonDefaults.buttonColorsPrimary()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                        tint = Color.White
                    )
                    Text("Tempel")
                }
            }
        }
    }
}

@Composable
fun MiuixFabMenuItem(
    icon: ImageVector,
    label: String,
    isDark: Boolean,
    cardBg: Color,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(100))
            .clip(RoundedCornerShape(100))
            .clickable(onClick = onClick),
        color = cardBg,
        shape = RoundedCornerShape(100)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Floating action button speed-dial for MIUIX Explorer with animated expand/collapse menu.
 */
@Composable
fun MiuixExplorerFab(
    visible: Boolean,
    showFabMenu: Boolean,
    isDark: Boolean,
    cardBgColor: Color,
    primaryTextColor: Color,
    miuixBlue: Color,
    onToggleFabMenu: () -> Unit,
    onNewFolder: () -> Unit,
    onNewFile: () -> Unit
) {
    if (!visible) return

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = showFabMenu,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                MiuixFabMenuItem(
                    icon = Icons.Default.CreateNewFolder,
                    label = stringResource(R.string.dialog_new_folder),
                    isDark = isDark,
                    cardBg = cardBgColor,
                    primaryColor = miuixBlue,
                    textColor = primaryTextColor,
                    onClick = onNewFolder
                )
                MiuixFabMenuItem(
                    icon = Icons.AutoMirrored.Filled.NoteAdd,
                    label = stringResource(R.string.dialog_new_file),
                    isDark = isDark,
                    cardBg = cardBgColor,
                    primaryColor = miuixBlue,
                    textColor = primaryTextColor,
                    onClick = onNewFile
                )
            }
        }

        // Main MIUI FAB button
        Surface(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isDark) 8.dp else 6.dp,
                    shape = CircleShape,
                    spotColor = miuixBlue.copy(alpha = 0.4f)
                )
                .clip(CircleShape)
                .clickable(onClick = onToggleFabMenu),
            color = miuixBlue,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
