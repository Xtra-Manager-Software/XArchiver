package id.xms.xarchiver.ui.explorer.miuix

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
            MiuixDockAction(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.action_copy),
                onClick = onCopy
            )
            MiuixDockAction(
                icon = Icons.Default.ContentCut,
                label = stringResource(R.string.action_cut),
                onClick = onCut
            )
            MiuixDockAction(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.action_delete),
                tint = Color(0xFFEF4444),
                onClick = onDelete
            )
            if (hasArchives) {
                MiuixDockAction(
                    icon = Icons.Default.Unarchive,
                    label = stringResource(R.string.action_extract),
                    onClick = onExtractOrShare
                )
            }
            MiuixDockAction(
                icon = Icons.Default.FolderZip,
                label = stringResource(R.string.action_compress),
                onClick = onCompress
            )

            val renameLabel = stringResource(R.string.action_rename)
            val detailsLabel = stringResource(R.string.action_details)
            val favoriteLabel = stringResource(R.string.action_favorite)
            val shareLabel = stringResource(R.string.action_share)
            val openArchiveLabel = stringResource(R.string.action_open_archive)

            val moreItems = remember(
                selectedCount, onOpenArchive, onRename, onProperties, onBookmark, onShare,
                renameLabel, detailsLabel, favoriteLabel, shareLabel, openArchiveLabel
            ) {
                buildList {
                    if (selectedCount == 1) {
                        add(
                            DropdownItem(
                                text = renameLabel,
                                onClick = onRename
                            )
                        )
                        add(
                            DropdownItem(
                                text = detailsLabel,
                                onClick = onProperties
                            )
                        )
                        add(
                            DropdownItem(
                                text = favoriteLabel,
                                onClick = onBookmark
                            )
                        )
                    }
                    add(
                        DropdownItem(
                            text = shareLabel,
                            onClick = onShare
                        )
                    )
                    if (onOpenArchive != null) {
                        add(
                            DropdownItem(
                                text = openArchiveLabel,
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
                val moreActionLabel = stringResource(R.string.explorer_action_more)
                WindowIconDropdownMenu(
                    entry = DropdownEntry(items = moreItems),
                    backgroundColor = Color.Transparent,
                    minWidth = 0.dp,
                    minHeight = 0.dp,
                    cornerRadius = 12.dp
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "moreScale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = moreActionLabel,
                                tint = MiuixTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = moreActionLabel,
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "miuixDockActionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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
                    text = stringResource(R.string.clipboard_items_selected, clipboardCount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isCut) {
                        stringResource(R.string.clipboard_ready_to_move)
                    } else {
                        stringResource(R.string.clipboard_ready_to_copy)
                    },
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
                    Text(stringResource(R.string.action_cancel))
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
                    Text(stringResource(R.string.action_paste))
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
