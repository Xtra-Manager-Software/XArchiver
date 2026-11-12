package id.xms.xarchiver

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.xms.xarchiver.ui.archive.ArchiveExplorerScreen
import id.xms.xarchiver.ui.editor.TextEditorScreen
import id.xms.xarchiver.ui.explorer.CategoryExplorerScreen
import id.xms.xarchiver.ui.explorer.ExplorerScreen
import id.xms.xarchiver.ui.home.HomeScreen
import id.xms.xarchiver.ui.theme.XArchiverTheme
import id.xms.xarchiver.ui.viewer.AudioPlayerScreen
import id.xms.xarchiver.ui.viewer.ImageViewerScreen
import id.xms.xarchiver.ui.home.AboutScreen
import id.xms.xarchiver.ui.home.AppSpecsScreen
import id.xms.xarchiver.ui.home.SupportedFormatsScreen
import id.xms.xarchiver.ui.changelog.ChangelogScreen
import id.xms.xarchiver.ui.settings.SettingsScreen
import id.xms.xarchiver.ui.viewer.VideoPlayerScreen
import id.xms.xarchiver.ui.payload.PayloadViewerScreen
import id.xms.xarchiver.ui.SetupScreen
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.xms.xarchiver.ui.components.DynamicIslandNotificationHost
import id.xms.xarchiver.ui.components.LocalNotificationHost
import id.xms.xarchiver.ui.components.NotificationHostState


import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface

import id.xms.xarchiver.core.update.UpdateNotificationHelper
import id.xms.xarchiver.ui.update.UpdateScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.OnBackInvokedDefaultInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.setViewTreeNavigationEventDispatcherOwner

class MainActivity : ComponentActivity(), NavigationEventDispatcherOwner {
    override val navigationEventDispatcher: NavigationEventDispatcher = NavigationEventDispatcher()
    private var shouldOpenUpdateScreen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            navigationEventDispatcher.addInput(OnBackInvokedDefaultInput(onBackInvokedDispatcher))
        }
        window.decorView.setViewTreeNavigationEventDispatcherOwner(this)
        unlockHighRefreshRate()
        requestNotificationPermission()
        handleUpdateIntent(intent)

        // Initialize Coil for video frame decoding
        val imageLoader = coil.ImageLoader.Builder(this)
            .components {
                add(coil.decode.VideoFrameDecoder.Factory())
            }
            .build()
        coil.Coil.setImageLoader(imageLoader)

        setContent {
            XArchiverTheme {
                val notificationHostState = remember { NotificationHostState() }
                CompositionLocalProvider(
                    LocalNavigationEventDispatcherOwner provides this,
                    LocalNotificationHost provides notificationHostState
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppContent(
                            openUpdateScreen = shouldOpenUpdateScreen,
                            onUpdateScreenOpened = { shouldOpenUpdateScreen = false }
                        )
                        DynamicIslandNotificationHost(
                            hostState = notificationHostState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleUpdateIntent(intent)
    }

    private fun handleUpdateIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(UpdateNotificationHelper.EXTRA_OPEN_UPDATE_DIALOG, false) == true) {
            shouldOpenUpdateScreen = true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1002)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        unlockHighRefreshRate()
    }

    private fun unlockHighRefreshRate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display ?: (getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)?.getDisplay(Display.DEFAULT_DISPLAY)
                val modes = currentDisplay?.supportedModes ?: emptyArray()
                val maxMode = modes.maxByOrNull { it.refreshRate }
                if (maxMode != null && maxMode.refreshRate > 60f) {
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            } else {
                val params = window.attributes
                @Suppress("DEPRECATION")
                params.preferredRefreshRate = 120f
                window.attributes = params
            }
        } catch (_: Exception) {}
    }
}

@Composable
private fun AppContent(
    openUpdateScreen: Boolean = false,
    onUpdateScreenOpened: () -> Unit = {}
) {
    MaterialTheme {
        val navController = rememberNavController()
        val context = LocalContext.current

        LaunchedEffect(openUpdateScreen) {
            if (openUpdateScreen) {
                navController.navigate("update")
                onUpdateScreenOpened()
            }
        }

        val isAndroid11OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val hasPermission = if (isAndroid11OrAbove) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val startDest = if (hasPermission) "home" else "setup"

        NavHost(navController = navController, startDestination = startDest) {
            composable("setup") {
                SetupScreen {
                    navController.navigate("home") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            }
            composable("home") { HomeScreen(navController) }

            // Category Explorer route
            composable("category_explorer/{categoryName}") { backStackEntry ->
                val categoryName = Uri.decode(
                    backStackEntry.arguments?.getString("categoryName") ?: "Images"
                )
                CategoryExplorerScreen(categoryName = categoryName, navController = navController)
            }

            composable("explorer/{encodedPath}") { backStackEntry ->
                val path = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: Uri.encode("/sdcard")
                )
                ExplorerScreen(path = path, navController = navController)
            }

            composable("root_explorer/{encodedPath}") { backStackEntry ->
                val path = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: Uri.encode("/")
                )
                ExplorerScreen(path = path, navController = navController)
            }

            // Archive Explorer routes
            composable("archive_explorer/{encodedArchivePath}") { backStackEntry ->
                val archivePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedArchivePath") ?: ""
                )
                ArchiveExplorerScreen(archivePath = archivePath, navController = navController)
            }

            composable("archive_explorer/{encodedArchivePath}/{encodedNestedPath}") { backStackEntry ->
                val archivePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedArchivePath") ?: ""
                )
                val nestedPath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedNestedPath") ?: ""
                )
                ArchiveExplorerScreen(
                    archivePath = archivePath,
                    nestedPath = nestedPath,
                    navController = navController
                )
            }
            
            // Text Editor route
            composable("text_editor/{encodedPath}") { backStackEntry ->
                val filePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: ""
                )
                TextEditorScreen(filePath = filePath, navController = navController)
            }
            
            // Payload Viewer route
            composable("payload_viewer/{encodedPath}") { backStackEntry ->
                val filePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: ""
                )
                PayloadViewerScreen(
                    payloadPath = filePath,
                    navController = navController
                )
            }
            
            // Image Viewer route
            composable("image_viewer/{encodedPath}") { backStackEntry ->
                val filePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: ""
                )
                ImageViewerScreen(filePath = filePath, navController = navController)
            }
            
            // Audio Player route
            composable("audio_player/{encodedPath}") { backStackEntry ->
                val filePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: ""
                )
                AudioPlayerScreen(filePath = filePath, navController = navController)
            }
            
            // Video Player route
            composable("video_player/{encodedPath}") { backStackEntry ->
                val filePath = Uri.decode(
                    backStackEntry.arguments?.getString("encodedPath") ?: ""
                )
                VideoPlayerScreen(filePath = filePath, navController = navController)
            }
            
            // About Screen route
            composable("about") {
                AboutScreen(navController = navController)
            }

            // About Specs Screen route
            composable("about/specs") {
                AppSpecsScreen(navController = navController)
            }

            // About Formats Screen route
            composable("about/formats") {
                SupportedFormatsScreen(navController = navController)
            }

            // Changelog Screen route
            composable("changelog") {
                ChangelogScreen(navController = navController)
            }
            
            // Settings Screen route
            composable("settings") {
                SettingsScreen(navController = navController)
            }

            // Software Update Screen route (ColorOS style)
            composable("update") {
                UpdateScreen(navController = navController)
            }
        }
    }
}
