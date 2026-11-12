package id.xms.xarchiver.ui.explorer.utils

import android.os.Build
import android.os.FileObserver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Realtime file system directory observer leveraging Linux kernel inotify via [FileObserver].
 * Automatically debounces high-frequency event bursts (e.g. copying multiple files or streaming writes)
 * and guarantees clean resource release upon stopping.
 */
class DirectoryWatcher(
    private val path: String,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 400L,
    private val maxDelayMs: Long = 1500L,
    private val onDirectoryChanged: () -> Unit
) {
    private var fileObserver: FileObserver? = null
    private var debounceJob: Job? = null
    private var firstEventTime = 0L

    companion object {
        private const val TAG = "DirectoryWatcher"

        // Watch directory modifications: creation, deletion, move in/out, and finished writes
        private const val WATCH_FLAGS =
            FileObserver.CREATE or
            FileObserver.DELETE or
            FileObserver.DELETE_SELF or
            FileObserver.MOVED_FROM or
            FileObserver.MOVED_TO or
            FileObserver.MOVE_SELF or
            FileObserver.CLOSE_WRITE or
            FileObserver.ATTRIB
    }

    /**
     * Start observing the target directory.
     * Resolves symbolic links (such as /sdcard -> /storage/emulated/0) to ensure
     * inotify watches the real physical mount.
     */
    fun start() {
        stop()
        try {
            val file = try {
                File(path).canonicalFile
            } catch (e: Exception) {
                File(path)
            }

            if (!file.exists() || !file.isDirectory) return

            fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                object : FileObserver(file, WATCH_FLAGS) {
                    override fun onEvent(event: Int, name: String?) {
                        handleEvent(event)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                object : FileObserver(file.absolutePath, WATCH_FLAGS) {
                    override fun onEvent(event: Int, name: String?) {
                        handleEvent(event)
                    }
                }
            }
            fileObserver?.startWatching()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start FileObserver for $path", e)
        }
    }

    /**
     * Stop observing the directory and release native inotify descriptors.
     */
    fun stop() {
        debounceJob?.cancel()
        debounceJob = null
        firstEventTime = 0L
        try {
            fileObserver?.stopWatching()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping FileObserver", e)
        }
        fileObserver = null
    }

    private fun handleEvent(event: Int) {
        if ((event and WATCH_FLAGS) == 0) return

        val now = System.currentTimeMillis()
        if (firstEventTime == 0L) {
            firstEventTime = now
        }

        // Throttle long continuous bursts (e.g. 100+ files transferring over MTP/ADB)
        if (now - firstEventTime >= maxDelayMs) {
            firstEventTime = now
            debounceJob?.cancel()
            scope.launch {
                onDirectoryChanged()
            }
            return
        }

        // Standard trailing debounce for discrete operations
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceMs)
            firstEventTime = 0L
            onDirectoryChanged()
        }
    }
}
