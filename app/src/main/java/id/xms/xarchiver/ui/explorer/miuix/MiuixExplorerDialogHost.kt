package id.xms.xarchiver.ui.explorer.miuix

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import id.xms.xarchiver.R
import id.xms.xarchiver.core.FileItem
import id.xms.xarchiver.core.FileOperationProgress
import id.xms.xarchiver.core.archive.*
import id.xms.xarchiver.ui.archive.ArchivePasswordDialog
import id.xms.xarchiver.ui.archive.CreateArchiveDialog
import id.xms.xarchiver.ui.explorer.utils.FileTypeDetector
import java.io.File

/**
 * Host component managing all modals, dialogs, bottom sheets, and progress overlays
 * for the MIUIX File Explorer screen.
 */
@Composable
fun MiuixExplorerDialogHost(
    currentPath: String? = null,
    // Dialog visibility & targets
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

    // Styling & Theme tokens
    isDark: Boolean,
    cardBgColor: Color,
    cardBorderColor: Color,
    dividerColor: Color,
    inputBgColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    miuixBlue: Color,
    miuixRed: Color,

    // Callbacks
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
        val hasArchiveSingle = singleFile != null && !singleFile.isDirectory && FileTypeDetector.isArchiveExtension(singleFile.name)
        MiuixSelectionMoreBottomSheet(
            selectedCount = selectedPaths.size,
            isDark = isDark,
            sheetBgColor = cardBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            dividerColor = dividerColor,
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
        MiuixPropertiesDialog(
            filePath = filePath,
            isDark = isDark,
            cardBg = cardBgColor,
            inputBg = inputBgColor,
            primaryColor = miuixBlue,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            onDismiss = onDismissPropertiesDialog
        )
    }

    // 3. Rename File Dialog
    showRenameDialog?.let { file ->
        MiuixTextInputDialog(
            title = stringResource(R.string.dialog_rename),
            initialValue = file.name,
            placeholder = "Name",
            confirmText = stringResource(R.string.dialog_rename),
            isDark = isDark,
            cardBg = cardBgColor,
            inputBg = inputBgColor,
            primaryColor = miuixBlue,
            textColor = primaryTextColor,
            secondaryColor = secondaryTextColor,
            onConfirm = { newName -> onConfirmRename(file, newName) },
            onDismiss = onDismissRename
        )
    }

    // 4. Delete Confirmation Dialog
    showDeleteDialog?.let { paths ->
        MiuixDeleteConfirmDialog(
            count = paths.size,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            deleteColor = miuixRed,
            onConfirm = { onConfirmDelete(paths) },
            onDismiss = onDismissDelete
        )
    }

    // 5. Create Folder Dialog
    if (showNewFolderDialog) {
        MiuixTextInputDialog(
            title = stringResource(R.string.dialog_new_folder),
            initialValue = "",
            placeholder = "Folder name",
            confirmText = stringResource(R.string.action_create),
            isDark = isDark,
            cardBg = cardBgColor,
            inputBg = inputBgColor,
            primaryColor = miuixBlue,
            textColor = primaryTextColor,
            secondaryColor = secondaryTextColor,
            icon = Icons.Default.CreateNewFolder,
            onConfirm = onConfirmNewFolder,
            onDismiss = onDismissNewFolder
        )
    }

    // 6. Create File Dialog
    if (showNewFileDialog) {
        MiuixTextInputDialog(
            title = stringResource(R.string.dialog_new_file),
            initialValue = "",
            placeholder = "filename.txt",
            confirmText = stringResource(R.string.action_create),
            isDark = isDark,
            cardBg = cardBgColor,
            inputBg = inputBgColor,
            primaryColor = miuixBlue,
            textColor = primaryTextColor,
            secondaryColor = secondaryTextColor,
            icon = Icons.AutoMirrored.Filled.NoteAdd,
            onConfirm = onConfirmNewFile,
            onDismiss = onDismissNewFile
        )
    }

    // 7. File Already Exists Dialog
    showFileExistsDialog?.let { name ->
        MiuixFileExistsDialog(
            name = name,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            miuixRed = miuixRed,
            miuixBlue = miuixBlue,
            onOverwrite = { onOverwriteExists(name) },
            onDuplicate = { onDuplicateExists(name) },
            onDismiss = onDismissFileExists
        )
    }

    // 8. APK Installation Dialog
    pendingApk?.let { apkFile ->
        MiuixApkInstallDialog(
            apkFile = apkFile,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            primaryColor = miuixBlue,
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
        MiuixQuickExtractDialog(
            file = file,
            isDark = isDark,
            cardBg = cardBgColor,
            cardBorder = cardBorderColor,
            primaryColor = miuixBlue,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            onDismiss = onDismissQuickExtract,
            onExtractHere = { onExtractHere(file) },
            onOpenArchive = { onOpenArchive(file) },
            onCustomPath = { onCustomPathExtractRequest(file) }
        )
    }

    // 11. Custom Path Folder Picker BottomSheet for Extraction
    showCustomPathDialogFor?.let { file ->
        MiuixFolderPickerBottomSheet(
            operation = "EXTRACT",
            itemCount = 1,
            initialPath = currentPath,
            isDark = isDark,
            cardBgColor = cardBgColor,
            cardBorderColor = cardBorderColor,
            dividerColor = dividerColor,
            inputBgColor = inputBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            miuixBlue = miuixBlue,
            onDismiss = onDismissCustomPath,
            onFolderSelected = { selectedPath -> onCustomPathFolderSelected(file, selectedPath) }
        )
    }

    // 12. Password Dialog for Encrypted Archive Extraction
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
        MiuixFolderPickerBottomSheet(
            operation = operation.first,
            itemCount = operation.second.size,
            initialPath = currentPath,
            isDark = isDark,
            cardBgColor = cardBgColor,
            cardBorderColor = cardBorderColor,
            dividerColor = dividerColor,
            inputBgColor = inputBgColor,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor,
            miuixBlue = miuixBlue,
            onDismiss = onDismissFileOperation,
            onFolderSelected = { selectedPath ->
                onFileOperationDestinationSelected(operation.first, operation.second, selectedPath)
            }
        )
    }

    // 14. Extraction Progress Overlay
    extractionProgress?.let { progress ->
        MiuixProgressDialog(
            title = "Extracting...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${formatFileSizeMiuix(progress.bytesProcessed)} / ${formatFileSizeMiuix(progress.totalBytes)}",
            icon = Icons.Default.Unarchive,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryColor = miuixBlue,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )
    }

    // 15. File Operation Progress Overlay
    fileOperationProgress?.let { progress ->
        MiuixProgressDialog(
            title = if (fileOperationProgress.totalFiles == 1) "Processing..." else "Moving/Copying...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${progress.filesProcessed} / ${progress.totalFiles} files (${formatFileSizeMiuix(progress.bytesProcessed)} / ${formatFileSizeMiuix(progress.totalBytes)})",
            icon = Icons.Default.SwapHoriz,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryColor = miuixBlue,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )
    }

    // 16. Archive Creation Progress Overlay
    archiveCreationProgress?.let { progress ->
        MiuixProgressDialog(
            title = "Compressing...",
            currentFile = progress.currentFile,
            percentage = progress.percentage,
            detailText = "${progress.filesProcessed} / ${progress.totalFiles} files (${formatFileSizeMiuix(progress.bytesProcessed)} / ${formatFileSizeMiuix(progress.totalBytes)})",
            icon = Icons.Default.Archive,
            isDark = isDark,
            cardBg = cardBgColor,
            primaryColor = miuixBlue,
            primaryTextColor = primaryTextColor,
            secondaryTextColor = secondaryTextColor
        )
    }
}
