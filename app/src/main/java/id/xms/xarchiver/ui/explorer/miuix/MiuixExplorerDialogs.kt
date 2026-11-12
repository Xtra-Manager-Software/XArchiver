package id.xms.xarchiver.ui.explorer.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.xms.xarchiver.R
import id.xms.xarchiver.core.FileItem
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

@Composable
fun MiuixTextInputDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    confirmText: String,
    isDark: Boolean,
    cardBg: Color,
    inputBg: Color,
    primaryColor: Color,
    textColor: Color,
    secondaryColor: Color,
    icon: ImageVector? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(inputBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                        cursorBrush = SolidColor(primaryColor),
                        decorationBox = { inner ->
                            if (text.isEmpty()) {
                                Text(placeholder, color = secondaryColor, fontSize = 15.sp)
                            }
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            color = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB),
                            contentColor = textColor
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }

                    Button(
                        onClick = { onConfirm(text.trim()) },
                        enabled = text.isNotBlank(),
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixDeleteConfirmDialog(
    count: Int,
    isDark: Boolean,
    cardBg: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    deleteColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(deleteColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = deleteColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.dialog_delete_title, count, if (count > 1) "s" else ""),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                        Text(
                            text = "$count item dipilih",
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.dialog_delete_desc),
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            color = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB),
                            contentColor = primaryTextColor
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(color = deleteColor, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixFileExistsDialog(
    name: String,
    isDark: Boolean,
    cardBg: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    miuixRed: Color,
    miuixBlue: Color,
    onOverwrite: () -> Unit,
    onDuplicate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(stringResource(R.string.dialog_file_exists_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                        Text(name, fontSize = 12.sp, color = secondaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.dialog_file_exists_desc, name), fontSize = 14.sp, color = secondaryTextColor)
                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOverwrite,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(color = miuixRed, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.action_overwrite))
                    }
                    Button(
                        onClick = onDuplicate,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text(stringResource(R.string.action_duplicate))
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(color = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB), contentColor = primaryTextColor)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixApkInstallDialog(
    apkFile: File,
    isDark: Boolean,
    cardBg: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    primaryColor: Color,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(stringResource(R.string.dialog_install_apk_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                        Text(apkFile.name, fontSize = 12.sp, color = secondaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.dialog_install_apk_desc, apkFile.name), fontSize = 14.sp, color = secondaryTextColor)
                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(color = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB), contentColor = primaryTextColor)
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text(stringResource(R.string.action_install))
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixQuickExtractDialog(
    file: FileItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    primaryColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onDismiss: () -> Unit,
    onExtractHere: () -> Unit,
    onOpenArchive: () -> Unit,
    onCustomPath: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(stringResource(R.string.archive_extract_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                        Text(file.name, fontSize = 12.sp, color = secondaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(Modifier.height(18.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiuixExtractOption(
                        title = stringResource(R.string.archive_extract_here),
                        subtitle = File(file.path).nameWithoutExtension + "/",
                        icon = Icons.Default.Unarchive,
                        isDark = isDark,
                        primaryColor = primaryColor,
                        cardBorder = cardBorder,
                        primaryText = primaryTextColor,
                        secondaryText = secondaryTextColor,
                        onClick = onExtractHere
                    )

                    MiuixExtractOption(
                        title = "Buka Arsip",
                        subtitle = "Jelajahi isi file terlebih dahulu",
                        icon = Icons.Default.FolderOpen,
                        isDark = isDark,
                        primaryColor = primaryColor,
                        cardBorder = cardBorder,
                        primaryText = primaryTextColor,
                        secondaryText = secondaryTextColor,
                        onClick = onOpenArchive
                    )

                    MiuixExtractOption(
                        title = stringResource(R.string.archive_extract_custom),
                        subtitle = stringResource(R.string.archive_extract_custom_desc),
                        icon = Icons.Default.CreateNewFolder,
                        isDark = isDark,
                        primaryColor = primaryColor,
                        cardBorder = cardBorder,
                        primaryText = primaryTextColor,
                        secondaryText = secondaryTextColor,
                        onClick = onCustomPath
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(color = if (isDark) Color(0xFF222225) else Color(0xFFE5E7EB), contentColor = primaryTextColor)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
fun MiuixExtractOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDark: Boolean,
    primaryColor: Color,
    cardBorder: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (isDark) Color(0xFF1B1B1E) else Color(0xFFF9FAFB),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = primaryText)
                Text(subtitle, fontSize = 12.sp, color = secondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun MiuixProgressDialog(
    title: String,
    currentFile: String,
    percentage: Int,
    detailText: String,
    icon: ImageVector,
    isDark: Boolean,
    cardBg: Color,
    primaryColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            color = cardBg,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                        Text("$percentage% selesai", fontSize = 12.sp, color = secondaryTextColor)
                    }
                }

                Spacer(Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = primaryColor
                )

                Spacer(Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF1B1B1E) else Color(0xFFF3F4F6)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(currentFile, fontSize = 12.sp, color = primaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(detailText, fontSize = 11.sp, color = secondaryTextColor)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiuixSelectionMoreBottomSheet(
    selectedCount: Int,
    isDark: Boolean,
    sheetBgColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    dividerColor: Color,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onProperties: () -> Unit,
    onBookmark: () -> Unit,
    onExtract: (() -> Unit)?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                text = stringResource(R.string.clipboard_items_selected, selectedCount),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = primaryTextColor,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            if (selectedCount == 1) {
                MiuixBottomSheetRow(Icons.Default.Edit, stringResource(R.string.action_rename), primaryTextColor, onRename)
                MiuixBottomSheetRow(Icons.Default.Info, stringResource(R.string.action_details), primaryTextColor, onProperties)
                MiuixBottomSheetRow(Icons.Default.Bookmark, stringResource(R.string.action_favorite), primaryTextColor, onBookmark)
            }

            MiuixBottomSheetRow(Icons.Default.Share, stringResource(R.string.action_share), primaryTextColor, onShare)

            onExtract?.let {
                MiuixBottomSheetRow(Icons.Default.FolderZip, stringResource(R.string.action_open_archive), primaryTextColor, it)
            }
        }
    }
}

@Composable
fun MiuixBottomSheetRow(
    icon: ImageVector,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, color = textColor)
    }
}
