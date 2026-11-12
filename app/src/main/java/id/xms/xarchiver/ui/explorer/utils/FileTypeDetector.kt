package id.xms.xarchiver.ui.explorer.utils

object FileTypeDetector {
    
    fun isTextExtension(ext: String): Boolean {
        return ext in listOf(
            "txt", "md", "log", "json", "xml", "html", "htm", "css", "js", "ts",
            "java", "kt", "kts", "py", "c", "cpp", "h", "hpp", "cs", "go", "rs",
            "php", "rb", "swift", "sh", "bat", "ps1", "yaml", "yml", "toml", "ini",
            "cfg", "conf", "properties", "gradle", "pro", "gitignore", "env"
        )
    }

    fun isArchiveExtension(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        // Check for compound extensions first
        if (lowerName.endsWith(".tar.gz") || lowerName.endsWith(".tar.bz2") ||
            lowerName.endsWith(".tar.xz") || lowerName.endsWith(".tar.lz")) {
            return true
        }
        
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in listOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "lz", "tgz", "tbz2", "txz"
        )
    }

    fun isImageExtension(ext: String): Boolean {
        return ext in listOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "heic", "heif"
        )
    }

    fun isAudioExtension(ext: String): Boolean {
        return ext in listOf(
            "mp3", "wav", "flac", "m4a", "aac", "ogg", "opus", "wma", "ape"
        )
    }

    fun isVideoExtension(ext: String): Boolean {
        return ext in listOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp"
        )
    }

    fun isDocumentExtension(ext: String): Boolean {
        return ext in listOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp"
        )
    }

    fun isBinaryExtension(ext: String): Boolean {
        return ext in listOf(
            "bin", "so", "apk", "dex", "img", "dat", "exe", "dll"
        )
    }

    fun isPayloadBin(fileName: String): Boolean {
        return fileName.equals("payload.bin", ignoreCase = true)
    }

    fun formatFilePermissions(file: java.io.File): String {
        try {
            val stat = android.system.Os.stat(file.absolutePath)
            val mode = stat.st_mode
            val u = (mode ushr 6) and 7
            val g = (mode ushr 3) and 7
            val o = mode and 7

            fun octalToRwx(v: Int): String {
                val r = if ((v and 4) != 0) "r" else "-"
                val w = if ((v and 2) != 0) "w" else "-"
                val x = if ((v and 1) != 0) "x" else "-"
                return "$r$w$x"
            }
            val rwx = "${octalToRwx(u)}${octalToRwx(g)}${octalToRwx(o)}"
            return "$u$g$o ($rwx)"
        } catch (_: Throwable) {
            // Fallback for JVM or unsupported filesystem
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val perms = java.nio.file.Files.getPosixFilePermissions(file.toPath())
                val r = perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_READ)
                val w = perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_WRITE)
                val x = perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
                val gr = perms.contains(java.nio.file.attribute.PosixFilePermission.GROUP_READ)
                val gw = perms.contains(java.nio.file.attribute.PosixFilePermission.GROUP_WRITE)
                val gx = perms.contains(java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE)
                val or = perms.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_READ)
                val ow = perms.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE)
                val ox = perms.contains(java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE)

                val u = (if (r) 4 else 0) + (if (w) 2 else 0) + (if (x) 1 else 0)
                val g = (if (gr) 4 else 0) + (if (gw) 2 else 0) + (if (gx) 1 else 0)
                val o = (if (or) 4 else 0) + (if (ow) 2 else 0) + (if (ox) 1 else 0)
                val rwx = "${if (r) "r" else "-"}${if (w) "w" else "-"}${if (x) "x" else "-"}" +
                          "${if (gr) "r" else "-"}${if (gw) "w" else "-"}${if (gx) "x" else "-"}" +
                          "${if (or) "r" else "-"}${if (ow) "w" else "-"}${if (ox) "x" else "-"}"
                return "$u$g$o ($rwx)"
            }
        } catch (_: Throwable) {}

        val r = file.canRead()
        val w = file.canWrite()
        val x = file.canExecute()
        val userOctal = (if (r) 4 else 0) + (if (w) 2 else 0) + (if (x) 1 else 0)
        val userRwx = "${if (r) "r" else "-"}${if (w) "w" else "-"}${if (x) "x" else "-"}"
        val groupOthersOctal = if (file.isDirectory) "55" else "44"
        val groupOthersRwx = if (file.isDirectory) "r-xr-x" else "r--r--"
        return "$userOctal$groupOthersOctal ($userRwx$groupOthersRwx)"
    }
}
