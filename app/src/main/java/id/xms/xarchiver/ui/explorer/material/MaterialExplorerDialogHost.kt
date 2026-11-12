package id.xms.xarchiver.ui.explorer.material

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.runtime.Composable
import id.xms.xarchiver.core.FileItem
import id.xms.xarchiver.core.FileOperationProgress
import id.xms.xarchiver.core.archive.*
import id.xms.xarchiver.ui.archive.ArchivePasswordDialog
import id.xms.xarchiver.ui.archive.CreateArchiveDialog
import id.xms.xarchiver.ui.components.PropertiesDialog
import java.io.File

/**
 * Host component orchestrating all dialogs, bottom sheets, folder pickers, and progress overlays
 * for the Material 3 Explorer screen.
 */
@Composable
fun MaterialExplorerDialogHost(
    currentPath: String? = null,
    showSelectionBottomSheet: Boolean,
    selectedPaths: List<String>,
    singleFile: FileItem?,
    showPropertiesDialog: String?,
    showRenameDialog: FileItem?,
    showDeleteDialog: List<String>?,
    showNewFolderDialog: Boolean,
    showNewFileDialog: Boolean,
    showFileExistsDialog: String?,
    pendingApk: File?,
    showCreateArchiveDialog: Boolean,
    showQuickExtractDialog: FileItem?,
    showCustomPathDialogFor: FileItem?,
    pendingPasswordExtraction: Triple<String, String, String>?,
    passwordExtractionError: String?,
    pendingFileOperation: Pair<String, List<String>>?,
    extractionProgress: ExtractionProgress?,
    fileOperationProgress: FileOperationProgress?,
    archiveCreationProgress: ArchiveCreationProgress?,

    onDismissSelectionBottomSheet: () -> Unit,
    onRenameSingleFile: (FileItem) -> Unit,
    onShareSelected: (List<String>) -> Unit,
    onPropertiesSingleFile: (String) -> Unit,
    onBookmarkSingleFile: (FileItem) -> Unit,
    onExtractSingleFile: (FileItem) -> Unit,

    onDismissPropertiesDialog: () -> Unit,
    onConfirmRename: (FileItem, String) -> Unit,
    onDismissRename: () -> Unit,
    onConfirmDelete: (List<String>) -> Unit,
    onDismissDelete: () -> Unit,

    onConfirmNewFolder: (String) -> Unit,
    onDismissNewFolder: () -> Unit,
    onConfirmNewFile: (String) -> Unit,
    onDismissNewFile: () -> Unit,

    onOverwriteExists: (String) -> Unit,
    onDuplicateExists: (String) -> Unit,
    onDismissFileExists: () -> Unit,

    onInstallApk: (File) -> Unit,
    onDismissApk: () -> Unit,

    onCreateArchive: (String, ArchiveFormat, CompressionLevel, String?, EncryptionType) -> Unit,
    onDismissCreateArchive: () -> Unit,

    onExtractHere: (FileItem) -> Unit,
    onOpenArchive: (FileItem) -> Unit,
    onCustomPathExtractRequest: (FileItem) -> Unit,
    onDismissQuickExtract: () -> Unit,

    onCustomPathFolderSelected: (FileItem, String) -> Unit,
    onDismissCustomPath: () -> Unit,

    onConfirmPassword: (String, String, String, String) -> Unit,
    onDismissPassword: () -> Unit,

    onFileOperationDestinationSelected: (String, List<String>, String) -> Unit,
    onDismissFileOperation: () -> Unit
) {
    // 1. Selection More Actions Bottom Sheet
    if (showSelectionBottomSheet) {
        val hasArchiveSingle = singleFile != null && !singleFile.isDirectory && isArchiveExtension(singleFile.name)
        MaterialSelectionBottomSheet(
            selectedCount = selectedPaths.size,
            onDismiss = onDismissSelectionBottomSheet,
            onRename = {
                singleFile?.let(onRenameSingleFile)
                onDismissSelectionBottomSheet()
            },
            onShare = {
                onShareSelected(selectedPaths)
                onDismissSelectionBottomSheet()
            },
            onProperties = {
                singleFile?.let { onPropertiesSingleFile(it.path) }
                onDismissSelectionBottomSheet()
            },
            onBookmark = {
                singleFile?.let(onBookmarkSingleFile)
                onDismissSelectionBottomSheet()
            },
            onExtract = if (hasArchiveSingle) {
                {
                    singleFile?.let(onExtractSingleFile)
                    onDismissSelectionBottomSheet()
                }
            } else null
        )
    }

    // 2. File Properties Dialog
    showPropertiesDialog?.let { filePath ->
        PropertiesDialog(
            filePath = filePath,
            onDismiss = onDismissPropertiesDialog
        )
    }

    // 3. Rename File Dialog
    showRenameDialog?.let { file ->
        MaterialRenameDialog(
            currentName = file.name,
            onConfirm = { newName -> onConfirmRename(file, newName) },
            onDismiss = onDismissRename
        )
    }

    // 4. Delete Confirmation Dialog
    showDeleteDialog?.let { paths ->
        MaterialDeleteConfirmDialog(
            count = paths.size,
            onConfirm = { onConfirmDelete(paths) },
            onDismiss = onDismissDelete
        )
    }

    // 5. Create New Folder Dialog
    if (showNewFolderDialog) {
        MaterialNewItemDialog(
            title = "New Folder",
            placeholder = "Folder name",
            onConfirm = onConfirmNewFolder,
            onDismiss = onDismissNewFolder
        )
    }

    // 6. Create New File Dialog
    if (showNewFileDialog) {
        MaterialNewItemDialog(
            title = "New File",
            placeholder = "filename.txt",
            onConfirm = onConfirmNewFile,
            onDismiss = onDismissNewFile
        )
    }

    // 7. File Already Exists Conflict Dialog
    showFileExistsDialog?.let { name ->
        MaterialFileExistsDialog(
            name = name,
            onOverwrite = { onOverwriteExists(name) },
            onDuplicate = { onDuplicateExists(name) },
            onDismiss = onDismissFileExists
        )
    }

    // 8. APK Installation Dialog
    pendingApk?.let { apkFile ->
        MaterialApkInstallDialog(
            apkFile = apkFile,
            onInstall = { onInstallApk(apkFile) },
            onDismiss = onDismissApk
        )
    }

    // 9. Create Archive Dialog
    if (showCreateArchiveDialog) {
        CreateArchiveDialog(
            selectedFilesCount = selectedPaths.size,
            onDismiss = onDismissCreateArchive,
            onCreate = onCreateArchive
        )
    }

    // 10. Quick Extract Dialog
    showQuickExtractDialog?.let { file ->
        MaterialQuickExtractDialog(
            file = file,
            onExtractHere = { onExtractHere(file) },
            onOpenArchive = { onOpenArchive(file) },
            onCustomPath = { onCustomPathExtractRequest(file) },
            onDismiss = onDismissQuickExtract
        )
    }

    // 11. Folder Picker BottomSheet for Custom Path Extraction
    showCustomPathDialogFor?.let { file ->
        MaterialFolderPickerBottomSheet(
            operation = "EXTRACT",
            itemCount = 1,
            initialPath = currentPath,
            onDismiss = onDismissCustomPath,
            onFolderSelected = { selectedPath -> onCustomPathFolderSelected(file, selectedPath) }
        )
    }

    // 12. Password Prompt Dialog for Encrypted Archive
    pendingPasswordExtraction?.let { (archivePath, archiveName, outputDir) ->
        ArchivePasswordDialog(
            archiveName = archiveName,
            errorMessage = passwordExtractionError,
            onDismiss = onDismissPassword,
            onConfirm = { enteredPassword ->
                onConfirmPassword(archivePath, archiveName, outputDir, enteredPassword)
            }
        )
    }

    // 13. Folder Picker BottomSheet for Copy / Move operations
    pendingFileOperation?.let { operation ->
        MaterialFolderPickerBottomSheet(
            operation = operation.first,
            itemCount = operation.second.size,
            initialPath = currentPath,
            onDismiss = onDismissFileOperation,
            onFolderSelected = { selectedPath ->
                onFileOperationDestinationSelected(operation.first, operation.second, selectedPath)
            }
        )
    }

    // 14. Extraction Progress Overlay
    extractionProgress?.let { progress ->
        MaterialProgressDialog(
            title = "Extracting...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${formatFileSize(progress.bytesProcessed)} / ${formatFileSize(progress.totalBytes)}",
            icon = Icons.Default.Unarchive
        )
    }

    // 15. File Operation Progress Overlay
    fileOperationProgress?.let { progress ->
        MaterialProgressDialog(
            title = if (fileOperationProgress.totalFiles == 1) "Processing..." else "Moving/Copying...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${progress.filesProcessed} / ${progress.totalFiles} files (${formatFileSize(progress.bytesProcessed)} / ${formatFileSize(progress.totalBytes)})",
            icon = Icons.Default.SwapHoriz
        )
    }

    // 16. Archive Creation Progress Overlay
    archiveCreationProgress?.let { progress ->
        MaterialProgressDialog(
            title = "Compressing...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${progress.filesProcessed} / ${progress.totalFiles} files (${formatFileSize(progress.bytesProcessed)} / ${formatFileSize(progress.totalBytes)})",
            icon = Icons.Default.Archive
        )
    }
}
