package id.xms.xarchiver.ui.explorer.miuix

import android.util.LruCache
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.xarchiver.R
import id.xms.xarchiver.core.FileItem
import id.xms.xarchiver.core.humanReadable
import id.xms.xarchiver.ui.explorer.utils.FileTypeDetector
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import id.xms.xarchiver.ui.explorer.search.buildHighlightedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal val miuixApkIconCache = LruCache<String, android.graphics.drawable.Drawable>(64)
internal val miuixDateFormatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

@Composable
fun MiuixFileItemCard(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isDark: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    primaryAccentColor: Color,
    highlightQuery: String? = null,
    currentScopePath: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val fileIcon = remember(file.name, file.isDirectory) { getFileIconMiuix(file) }
    val badgeColor = remember(file) { getFileBadgeColorMiuix(file) }

    val relativeLocation = remember(file.path, currentScopePath) {
        if (currentScopePath.isNullOrEmpty()) null
        else {
            val parent = File(file.path).parent ?: ""
            if (parent == currentScopePath || parent.isEmpty()) null
            else parent.removePrefix(currentScopePath).trimStart('/', '\\')
        }
    }

    val formattedSubtitle = remember(file.isDirectory, file.size, file.lastModified, file.itemCount, relativeLocation) {
        val dateStr = miuixDateFormatter.format(Date(file.lastModified))
        val base = if (file.isDirectory) {
            val count = file.itemCount
            if (count != null) "$count item • $dateStr" else "Folder • $dateStr"
        } else {
            "${file.size.humanReadable()} • $dateStr"
        }
        if (!relativeLocation.isNullOrEmpty()) "$base • $relativeLocation" else base
    }

    val isImage = remember(file.name) { FileTypeDetector.isImageExtension(file.name.substringAfterLast('.', "").lowercase()) }
    val isVideo = remember(file.name) { FileTypeDetector.isVideoExtension(file.name.substringAfterLast('.', "").lowercase()) }
    val isApk = remember(file.name) { file.name.substringAfterLast('.', "").lowercase() == "apk" }

    var apkIconDrawable by remember(file.path) {
        mutableStateOf(if (isApk) miuixApkIconCache.get(file.path) else null)
    }
    val context = LocalContext.current

    if (isApk && apkIconDrawable == null) {
        LaunchedEffect(file.path) {
            withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val pi = pm.getPackageArchiveInfo(file.path, 0)
                    pi?.applicationInfo?.let { appInfo ->
                        appInfo.sourceDir = file.path
                        appInfo.publicSourceDir = file.path
                        val icon = appInfo.loadIcon(pm)
                        if (icon != null) {
                            miuixApkIconCache.put(file.path, icon)
                            apkIconDrawable = icon
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) primaryAccentColor else cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) primaryAccentColor.copy(alpha = if (isDark) 0.16f else 0.08f) else cardBgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox (when selecting)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) primaryAccentColor else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) primaryAccentColor else secondaryTextColor.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
            }

            // Thumbnail / Icon Badge
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp)),
                color = badgeColor.copy(alpha = if (isDark) 0.2f else 0.12f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if ((isImage || isVideo)) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(File(file.path))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isApk && apkIconDrawable != null) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(apkIconDrawable)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(6.dp)
                        )
                    } else {
                        Icon(
                            imageVector = fileIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // File Name & Details
            Column(modifier = Modifier.weight(1f)) {
                if (highlightQuery.isNullOrBlank()) {
                    Text(
                        text = file.name,
                        color = primaryTextColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = buildHighlightedText(
                            text = file.name,
                            query = highlightQuery,
                            normalColor = primaryTextColor,
                            highlightColor = primaryAccentColor
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = formattedSubtitle,
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isSelectionMode && file.isDirectory) {
                Icon(
                    imageVector = MiuixIcons.ChevronForward,
                    contentDescription = null,
                    tint = secondaryTextColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MiuixFileSkeleton(
    isDark: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color
) {
    val transition = rememberInfiniteTransition(label = "miuix_skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(0.5.dp, cardBorderColor, RoundedCornerShape(16.dp)),
        color = cardBgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = alpha * 0.4f))
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = alpha * 0.4f))
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Gray.copy(alpha = alpha * 0.25f))
                )
            }
        }
    }
}

@Composable
fun MiuixEmptyFolderState(
    isSearch: Boolean,
    searchQuery: String,
    isDark: Boolean,
    primaryColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val emptyColor = if (isSearch) primaryColor else Color(0xFFF59E0B)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(emptyColor.copy(alpha = if (isDark) 0.15f else 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearch) Icons.Default.SearchOff else Icons.Default.FolderOpen,
                contentDescription = null,
                tint = emptyColor,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = if (isSearch) "Tidak ada hasil untuk \"$searchQuery\"" else stringResource(R.string.explorer_empty_folder),
            color = primaryTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Belum ada file atau folder di direktori ini.",
            color = secondaryTextColor,
            fontSize = 13.sp
        )
    }
}

fun findCommonParentMiuix(paths: List<String>): String {
    if (paths.isEmpty()) return ""
    if (paths.size == 1) return File(paths.first()).parentFile?.absolutePath ?: ""

    val splitPaths = paths.map { it.split("/", "\\") }
    val minLength = splitPaths.minOf { it.size }
    val commonParts = mutableListOf<String>()
    for (i in 0 until minLength) {
        val part = splitPaths[0][i]
        if (splitPaths.all { it[i] == part }) commonParts.add(part) else break
    }
    return commonParts.joinToString("/")
}

fun formatFileSizeMiuix(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun getFileIconMiuix(file: FileItem): ImageVector {
    if (file.isDirectory) return Icons.Default.Folder
    val ext = file.name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "zip", "rar", "7z", "tar", "gz", "tgz", "jar", "aar", "xapk", "bz2", "xz" -> Icons.Default.Archive
        "apk" -> Icons.Default.Android
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "opus" -> MiuixIcons.Music
        "mp4", "avi", "mkv", "mov", "wmv", "webm", "3gp" -> MiuixIcons.Play
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic" -> MiuixIcons.Image
        "pdf" -> Icons.Default.PictureAsPdf
        "txt", "md", "log", "doc", "docx" -> MiuixIcons.Notes
        "xls", "xlsx" -> Icons.Default.TableChart
        "ppt", "pptx" -> Icons.Default.Slideshow
        else -> MiuixIcons.File
    }
}

fun getFileBadgeColorMiuix(file: FileItem): Color {
    if (file.isDirectory) return Color(0xFFF59E0B)
    val ext = file.name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "zip", "rar", "7z", "tar", "gz", "tgz", "jar", "aar", "xapk", "bz2", "xz" -> Color(0xFFFF9800)
        "apk" -> Color(0xFF4CAF50)
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "opus" -> Color(0xFF9C27B0)
        "mp4", "avi", "mkv", "mov", "wmv", "webm", "3gp" -> Color(0xFFE91E63)
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic" -> Color(0xFF03A9F4)
        "pdf" -> Color(0xFFF44336)
        "txt", "md", "log", "doc", "docx" -> Color(0xFF009688)
        "xls", "xlsx" -> Color(0xFF4CAF50)
        "ppt", "pptx" -> Color(0xFFFF5722)
        else -> Color(0xFF757575)
    }
}
