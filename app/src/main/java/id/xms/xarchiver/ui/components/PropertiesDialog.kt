package id.xms.xarchiver.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import id.xms.xarchiver.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.xms.xarchiver.core.humanReadable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PropertiesDialog(
    filePath: String,
    onDismiss: () -> Unit
) {
    val file = remember { File(filePath) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    
    // Calculate directory size if needed
    var directorySize by remember { mutableStateOf<Long?>(null) }
    var fileCount by remember { mutableStateOf(0) }
    var folderCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(filePath) {
        if (file.isDirectory) {
            var size = 0L
            var files = 0
            var folders = 0
            
            file.walkTopDown().forEach { f ->
                if (f.isFile) {
                    size += f.length()
                    files++
                } else if (f.isDirectory && f != file) {
                    folders++
                }
            }
            
            directorySize = size
            fileCount = files
            folderCount = folders
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.dialog_properties_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Name
                    PropertyRow(
                        icon = Icons.AutoMirrored.Filled.Label,
                        label = stringResource(R.string.property_name),
                        value = file.name
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Path
                    PropertyRow(
                        icon = Icons.Default.FolderOpen,
                        label = stringResource(R.string.property_location),
                        value = file.parent ?: "/"
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Size
                    val displaySize = if (file.isDirectory) {
                        directorySize?.humanReadable() ?: stringResource(R.string.property_calculating)
                    } else {
                        file.length().humanReadable()
                    }
                    PropertyRow(
                        icon = Icons.Default.Storage,
                        label = stringResource(R.string.property_size),
                        value = displaySize
                    )
                    
                    // For directories, show file/folder count
                    if (file.isDirectory) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        PropertyRow(
                            icon = Icons.Default.Folder,
                            label = stringResource(R.string.property_contents),
                            value = stringResource(R.string.property_contents_format, fileCount, folderCount)
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Last Modified
                    PropertyRow(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.property_modified),
                        value = dateFormatter.format(Date(file.lastModified()))
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    
                    // Permissions
                    PropertyRow(
                        icon = Icons.Default.Security,
                        label = stringResource(R.string.property_permissions),
                        value = id.xms.xarchiver.ui.explorer.utils.FileTypeDetector.formatFilePermissions(file)
                    )
                    
                    // File extension (for files only)
                    if (file.isFile && file.extension.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        PropertyRow(
                            icon = Icons.Default.Extension,
                            label = stringResource(R.string.property_type),
                            value = ".${file.extension.uppercase()}"
                        )
                    }
                    
                    // Hidden file indicator
                    if (file.name.startsWith(".")) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        PropertyRow(
                            icon = Icons.Default.VisibilityOff,
                            label = stringResource(R.string.property_hidden),
                            value = stringResource(R.string.property_yes)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.action_close))
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun PropertyRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
