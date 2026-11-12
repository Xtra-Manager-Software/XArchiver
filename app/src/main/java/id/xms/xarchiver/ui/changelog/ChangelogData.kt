package id.xms.xarchiver.ui.changelog

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import id.xms.xarchiver.R

enum class ChangelogCategory(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val colorHex: Long
) {
    FEATURES(R.string.changelog_cat_features, Icons.Outlined.AutoAwesome, 0xFF10B981),
    UI(R.string.changelog_cat_ui, Icons.Outlined.Palette, 0xFF8B5CF6),
    STORAGE_ROOT(R.string.changelog_cat_storage_root, Icons.Outlined.Security, 0xFFF59E0B),
    ENGINE(R.string.changelog_cat_engine, Icons.Outlined.Bolt, 0xFF3B82F6),
    FIXES(R.string.changelog_cat_fixes, Icons.Outlined.Build, 0xFFEC4899)
}

data class ChangelogItem(
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val category: ChangelogCategory,
    val tags: List<String> = emptyList()
)

data class VersionRelease(
    val version: String,
    @StringRes val dateRes: Int,
    @StringRes val summaryRes: Int,
    val isLatest: Boolean = false,
    @StringRes val badgeRes: Int = if (isLatest) R.string.changelog_badge_latest else R.string.changelog_badge_stable,
    val items: List<ChangelogItem>
)

object ChangelogRepository {
    val releases: List<VersionRelease> = listOf(
        VersionRelease(
            version = "2.1.9",
            dateRes = R.string.changelog_date_2_1_9,
            summaryRes = R.string.changelog_summary_2_1_9,
            isLatest = true,
            badgeRes = R.string.changelog_badge_latest,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_realtime_title,
                    descRes = R.string.changelog_item_2_1_9_realtime_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("Realtime", "Auto-Refresh", "FileObserver", "inotify", "MTP", "Silent Update")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_home_ui_title,
                    descRes = R.string.changelog_item_2_1_9_home_ui_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Material 3", "Home Screen", "StorageCard", "CategoryGrid", "Bookmarks", "Quick Access", "Floating Bar")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_miuix_home_title,
                    descRes = R.string.changelog_item_2_1_9_miuix_home_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("MIUIX", "HyperOS", "Home Screen", "Category Grid", "Pill Cards", "Storage Card")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_search_title,
                    descRes = R.string.changelog_item_2_1_9_search_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("ColorOS 17", "Scoped Search", "XArcDeep", "Filter Chips", "Query Highlight")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_viewmodes_title,
                    descRes = R.string.changelog_item_2_1_9_viewmodes_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Material 3", "View Modes", "List", "Grid", "Waterfall")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_topbar_title,
                    descRes = R.string.changelog_item_2_1_9_topbar_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("TopBar", "Breadcrumbs", "Navigation")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_monet_title,
                    descRes = R.string.changelog_item_2_1_9_monet_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Monet", "Material You", "Dynamic Color", "Theme")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_sort_title,
                    descRes = R.string.changelog_item_2_1_9_sort_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("Sorting", "DataStore", "Persistence")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_i18n_title,
                    descRes = R.string.changelog_item_2_1_9_i18n_desc,
                    category = ChangelogCategory.FIXES,
                    tags = listOf("Localization", "i18n", "English", "Indonesian", "String Resources")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_9_miuix_title,
                    descRes = R.string.changelog_item_2_1_9_miuix_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("MIUIX", "HyperOS", "Modular", "Refactor")
                )
            )
        ),
        VersionRelease(
            version = "2.1.3",
            dateRes = R.string.changelog_date_2_1_3,
            summaryRes = R.string.changelog_summary_2_1_3,
            isLatest = false,
            badgeRes = R.string.changelog_badge_stable,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_3_encryption_title,
                    descRes = R.string.changelog_item_2_1_3_encryption_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("ZIP Encryption", "AES-256", "ZipCrypto", "Password Prompt")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_3_dialogs_title,
                    descRes = R.string.changelog_item_2_1_3_dialogs_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Proportional Dialogs", "Material 3", "50/50 Buttons", "Rounded Icons")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_3_crashfix_title,
                    descRes = R.string.changelog_item_2_1_3_crashfix_desc,
                    category = ChangelogCategory.FIXES,
                    tags = listOf("Encrypted Archive", "Crash Fix", "Explorer", "Security")
                )
            )
        ),
        VersionRelease(
            version = "2.1.0",
            dateRes = R.string.changelog_date_2_1_0,
            summaryRes = R.string.changelog_summary_2_1_0,
            isLatest = false,
            badgeRes = R.string.changelog_badge_stable,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_ota_title,
                    descRes = R.string.changelog_item_2_1_ota_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("OTA Update", "GitHub Releases", "Markdown", "Progress Pill")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_miuix_title,
                    descRes = R.string.changelog_item_2_1_miuix_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("HyperOS", "MIUIX KMP", "Squircle Blur")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_recent_title,
                    descRes = R.string.changelog_item_2_1_recent_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("RecentManager", "File Tracking", "Marquee")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_html_title,
                    descRes = R.string.changelog_item_2_1_html_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("HTML Runner", "WebView", "Desktop Mode", "Live Preview")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_specs_title,
                    descRes = R.string.changelog_item_2_1_specs_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Specs Screen", "Formats Catalog", "Architecture")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_autodetect_title,
                    descRes = R.string.changelog_item_2_1_autodetect_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("Xiaomi", "Redmi", "Poco", "Auto-Switch")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_minsdk_title,
                    descRes = R.string.changelog_item_2_1_minsdk_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("Android 10", "API 29", "Core Modules")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_i18n_title,
                    descRes = R.string.changelog_item_2_1_i18n_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("Bilingual", "Indonesian", "English")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_1_perf_title,
                    descRes = R.string.changelog_item_2_1_perf_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("Performance", "LazyColumn", "LruCache", "Smooth Scroll")
                )
            )
        ),
        VersionRelease(
            version = "2.0.0",
            dateRes = R.string.changelog_date_2_0_0,
            summaryRes = R.string.changelog_summary_2_0_0,
            isLatest = false,
            badgeRes = R.string.changelog_badge_stable,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_0_root_title,
                    descRes = R.string.changelog_item_2_0_root_desc,
                    category = ChangelogCategory.STORAGE_ROOT,
                    tags = listOf("Shizuku v13+", "libsu 6.0", "Superuser")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_0_storage_title,
                    descRes = R.string.changelog_item_2_0_storage_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("Horizontal Pager", "USB OTG", "F2FS", "ext4")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_0_payload_title,
                    descRes = R.string.changelog_item_2_0_payload_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("Rust Native", "libpayload_parser", "SHA256")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_0_m3_title,
                    descRes = R.string.changelog_item_2_0_m3_desc,
                    category = ChangelogCategory.UI,
                    tags = listOf("Material 3", "Dynamic Island", "AMOLED")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_2_0_explorer_title,
                    descRes = R.string.changelog_item_2_0_explorer_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("SelectionManager", "Coil Thumbnails", "APK Icons")
                )
            )
        ),
        VersionRelease(
            version = "1.1-Release",
            dateRes = R.string.changelog_date_1_1_0,
            summaryRes = R.string.changelog_summary_1_1_0,
            isLatest = false,
            badgeRes = R.string.changelog_badge_stable,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_1_1_payload_title,
                    descRes = R.string.changelog_item_1_1_payload_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("OTA payload.bin", "Protocol Buffers", ".img Extraction")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_1_1_streaming_title,
                    descRes = R.string.changelog_item_1_1_streaming_desc,
                    category = ChangelogCategory.ENGINE,
                    tags = listOf("XZ / LZMA", "BZIP2", "Byte-level Progress")
                ),
                ChangelogItem(
                    titleRes = R.string.changelog_item_1_1_fixes_title,
                    descRes = R.string.changelog_item_1_1_fixes_desc,
                    category = ChangelogCategory.FIXES,
                    tags = listOf("Flow Crash Fix", "OOM Protection", "Buffer Size")
                )
            )
        ),
        VersionRelease(
            version = "1.0",
            dateRes = R.string.changelog_date_1_0_0,
            summaryRes = R.string.changelog_summary_1_0_0,
            isLatest = false,
            badgeRes = R.string.changelog_badge_stable,
            items = listOf(
                ChangelogItem(
                    titleRes = R.string.changelog_item_1_0_initial_title,
                    descRes = R.string.changelog_item_1_0_initial_desc,
                    category = ChangelogCategory.FEATURES,
                    tags = listOf("ZIP", "TAR", "GZ", "TGZ", "Material Design 3")
                )
            )
        )
    )
}
