# Changelog

All notable changes to XArchiver will be documented in this file.

## [2.1.9] - 2026-11-12

### UI Redesign - Modernized MIUIX HyperOS Home Screen
- **2-Column x 3-Row Category Pills Layout**:
  - Adopted the official HyperOS File Manager visual layout with 18dp squircle pill cards: **Docs** (Amber), **Images** (Royal Blue), **Videos** (Purple), **Music** (Coral Red), **Archives** (Cyan), and **APK** (Emerald Green).
  - Features dedicated squircle icon containers with distinct vibrant accent colors and live asynchronous item count indicators.
- **Consolidated Quick Access Group**:
  - Refined 20dp rounded surface combining direct access to **Downloads** (`/sdcard/Download`) and **Favorites** (integrated with the local `BookmarksManager` displaying live bookmarked items count).
- **Modern Storage Card & Privileged Root Integration**:
  - Displays primary internal storage ("My phone") with remaining capacity breakdown, a slim 4dp linear progress bar, and filesystem badge (e.g. F2FS/ext4).
  - Seamlessly integrates a dedicated **System Storage (Root)** card row with amber Root badge when privileged access is enabled.
- **Dedicated Sources Section**:
  - Grouped access to core media folders: **Camera** (`DCIM/Camera`), **Screenshots & screen recordings** (`Pictures/Screenshots`), and messaging media directories (WhatsApp, Telegram, Bluetooth).
- **Dual-Capsule Floating Navigation**:
  - Features a floating frosted glassmorphism navigation capsule for seamless switching between **Recent** and **Browse** tabs.

### New Features & Improvements - Spring-Animated Selection Dock & Clean HyperOS Menu
- **macOS Dock-Inspired Spring & Scale Animation**:
  - Applied bouncy spring overshoot physics (`dampingRatio = 0.68f`) combined with dynamic bottom-origin scale (`scaleIn` 0.85) to the floating selection and clipboard docks. The dock pops up with elasticity from the screen edge upon entering multi-selection mode.
  - Added tactile spring press feedback to all dock action buttons: items subtly depress (`0.88f`) when touched and spring back upon release.
- **Minimalist, Text-Only "More" Dropdown Menu**:
  - Replaced the bottom sheet with a compact, text-only `WindowIconDropdownMenu` matching official HyperOS File Manager aesthetics without redundant item icons.
  - Updated the "More" button to use HyperOS's circular horizontal three-dots icon (`MoreHoriz`) and removed redundant Share action from the main dock.
  - Resolved `IllegalStateException` on `LocalNavigationEventDispatcherOwner` across Android 14/15 (HyperOS 2) by binding `NavigationEventDispatcherOwner` to the view tree and Compose roots.

### New Features - Native MIUIX Details Dialog & POSIX Permissions
- **Native MIUIX Properties Dialog (`MiuixPropertiesDialog`)**:
  - Replaced the generic Material dialog with an authentic MIUIX-styled dialog conforming to `MiuixTheme`, squircle surface styling, and primary pill buttons.
- **Linux POSIX Permission Formatting (`644 (rw-r--r--)` / `755 (rwxr-xr-x)`)**:
  - Upgraded permission strings from raw concatenations ("BacaTulisEksekusi") to standard Linux octal and rwx notations using `android.system.Os.stat` (`st_mode`), such as `644 (rw-r--r--)` for files and `755 (rwxr-xr-x)` for directories/executables.
  - Consistently applied across both MIUIX and Material dialog implementations.

### New Features - Universal File & Folder Bookmarks
- **Universal Storage Bookmarking**:
  - Expanded `BookmarksManager` to support bookmarking individual files of any type (images, videos, documents, archives, APKs) in addition to folders.
  - Directly opens bookmarked files with their respective handlers or navigates into directories upon clicking.
- **Material & MIUIX Home Screen Integration**:
  - Added a dedicated Favorites/Bookmarks card in the Material Home Categories grid with an interactive management modal bottom sheet to view, open, or delete favorites.

### Quality & Full Localization (i18n)
- Eliminated all hardcoded strings on action docks and clipboard bars, migrating them to Android string resources with complete English and Indonesian support.

### New Features - Modal BottomSheet Destination Picker for Copy, Move & Extract
- **Replaced Desktop Popup Dialog with Native Modal BottomSheets**:
  - Replaced legacy centered popup `FolderPickerDialog` with modern mobile modal bottom sheets sliding smoothly from the bottom.
- **Dedicated Material 3 and MIUIX HyperOS Implementations**:
  - **Material 3 (`MaterialFolderPickerBottomSheet`)**: Built using Material You tokens, `surfaceContainerLow` backdrop, standard drag handle, and adaptive color containers.
  - **MIUIX HyperOS (`MiuixFolderPickerBottomSheet`)**: Styled with HyperOS file manager aesthetics, 24dp rounded corners, centered pill drag handle, warm amber folder squircle containers, and MIUIX vibrant pill action buttons.
- **Unified Warm Amber Solid Folder Icon across MIUIX Explorer & Home**:
  - Replaced the thin outline folder icon (`MiuixIcons.Folder`) with the rich solid filled folder icon (`Icons.Default.Folder`) in vibrant Amber Gold (`#F59E0B`) with a soft translucent squircle container backdrop across MIUIX Explorer file lists and empty folder state.
- **MIUIX Recent Tab Feature Parity with HyperOS Aesthetic**:
  - Brought complete feature parity from Material to the MIUIX Recent tab (`MiuixRecentContent`): folder/source bucket cards (Camera, Screenshots, Downloads, WhatsApp, Pictures, etc.) with clickable headers navigating to the folder.
  - Horizontal media thumbnail preview row (`LazyRow`) for image/video groups with play button overlay and instant file opening via `openRecentItem`.
  - Rich document/archive/APK mixed list cards with file type squircle badges and instant file execution.
- **Hierarchical Storage & Subfolder Navigation**:
  - Partition selector (Internal Storage, SD Card, Root) with available space breakdown and a "Current Folder" fast-jump card.
  - Deep folder traversal with back arrow navigation and Android system back gesture (`BackHandler`) integration that steps up folder levels instead of abruptly closing the sheet.
  - Inline "New Folder" creation button right in the navigation bar to create and automatically enter newly created destination folders.
  - Contextual confirm action button ("Copy here", "Move here", "Extract here") enabled dynamically upon valid directory selection.

### New Features - Automated Realtime File & Directory Auto-Refresh
- **Kernel-Level Realtime Directory Watching (DirectoryWatcher & inotify)**:
  - Integrated `android.os.FileObserver` to capture Linux kernel inotify events directly on the active folder (file/directory creation, deletion, moves in/out, and write completions).
  - Newly added files transferred from PC (via MTP or ADB), captured camera photos, browser downloads, or folders created from external file manager apps appear immediately without leaving or re-entering the folder.
- **Silent Background Refresh Mechanism**:
  - Live directory updates are fetched silently on `Dispatchers.IO` without triggering disruptive skeleton loading shimmers.
  - Preserves user scroll position and selection state cleanly through Compose key diffing mechanics.
- **Event Burst Debouncing & Throttle Guards**:
  - Employs a 400ms trailing coroutine debounce to prevent redundant disk I/O when batch transferring multiple files.
  - Features an internal 1.5-second throttle guard that periodically refreshes file views even during prolonged multi-file transfers.
- **Lifecycle Resume Auto-Refresh (ON_RESUME Observer)**:
  - Automatically queries and updates directory listings whenever XArchiver returns to the foreground from other apps or device sleep.
- **Full Parity Across Material 3 and MIUIX HyperOS Explorers**:
  - Identically operational on both Material You Monet dynamic theming and MIUIX HyperOS file explorers.

### New Features - Intelligent Scoped Search & XArcDeep Engine
- **Directory-Scoped Search Workflow**:
  - Implemented modern folder-scoped search mechanics inspired by the ColorOS 17 file manager: searching inside specific directories (e.g. `Documents` or `Download`) strictly scans that active folder hierarchy.
  - Substantially decreases storage I/O and query latency compared to unrestricted full-device scanning.
- **Instant Category Filter Chips**:
  - Horizontally scrollable category chips row situated atop search results for instant file type narrowing: **All**, **Images**, **Videos**, **Audio**, **Documents**, **Archives**, and **APK**.
  - Distinctive visual icons and accent colors for instantaneous visual identification of categories.
- **Query Keyword Highlighting**:
  - Exact matches in file and directory names are dynamically highlighted with high-contrast accent color (`primaryAccentColor`), allowing users to easily spot search matches in long result listings.
- **Scope Indicator & Result Statistics**:
  - Informative header row displaying total item matches ("X items total") alongside the current search scope folder name (e.g. "This device" or active directory name).
- **Fast XArcDeep Scan Engine**:
  - Context-aware action footer on empty or localized results ("Can't find the file? Try **XArcDeep.**").
  - Seamlessly expands search boundaries to perform an all-encompassing storage traversal across the entire device if the requested file is not within the current folder.
- **Comprehensive Multilingual Localization (i18n)**:
  - Extracted and centralized all search and explorer strings into `res/values/strings.xml` (English) and `res/values-in/strings.xml` (Indonesian), eliminating hardcoded strings and delivering an authentic native experience across both languages.

### Layout Redesign - Material Theme Home Screen (Material Home UI Layout)
- **Enhanced Storage Card Layout (StorageCard)**:
  - Reorganized storage card elements: filesystem type badge (e.g. F2FS) and health status indicator on the top-left, with the `> Open` action capsule button on the top-right.
  - Device icon and internal storage title are now positioned side-by-side horizontally across the middle of the card.
  - Capacity usage breakdown and percentage used are neatly aligned above a sleek rounded linear progress bar at the bottom.
- **Tiered Category Grid Layout (CategoryGrid)**:
  - First row features 2 wide horizontal cards for primary categories (**Images** and **APK**) with an icon container on the left and category title plus item count on the right.
  - Second row displays 4 compact vertical cards in a single row for **Videos**, **Audio**, **Documents**, and **Archives**.
- **Full-Width Bookmarks / Favorites Card (BookmarkLongItem)**:
  - Integrated Bookmarks directly into the Categories section as a wide full-width card with active bookmark count indicator and tertiary Monet container.
  - Tapping opens the bookmarked item directly (directory in Explorer or file via its dedicated viewer/player/editor/installer) when 1 bookmark is saved, or opens a dedicated Modal Bottom Sheet in both Material and MIUIX themes to manage and open multiple bookmarks with dynamic file type icons and instant un-bookmark options.
- **2-Column Grid for Quick Access (ShortcutList)**:
  - Restructured quick access shortcuts (Bluetooth, Downloads, WhatsApp, Telegram, etc.) into a clean 2-column grid of horizontal cards with icons on the left and text labels on the right.
- **Floating Capsule Navigation Bar**:
  - Sleek pill-shaped floating bottom bar with prominent active pill capsule styling for fluid switching between **Recent** and **Files** tabs.
- **Pure Material You Theming Integrity**:
  - All positional and layout enhancements strictly adhere to native dynamic Monet and Material 3 design tokens without static hardcoded dark palettes or blur effects.

### Redesign & New Features - Modern Material Explorer UI & Dynamic View Modes
- **Three Dynamic Explorer View Modes**: Introduced flexible layout switching accessible directly from the overflow menu (`⋮`) with persistent DataStore state:
  - **List View (Default)**: Full-width vertical list showing direct directory item counts or file sizes alongside localized modification dates.
  - **Grid View**: Modern 3-column card grid with rounded corners and Material 3 Monet surface containers (`surfaceContainer`).
  - **Waterfall View**: Clean 3-column layout without bounding card boxes where file and folder icons float directly on the canvas background.
- **Modern Circular Action TopBar**: Overhauled the Material Explorer TopBar with tactile circular action buttons (back, new folder/file `+`, search, and overflow options `⋮`).
- **Interactive Breadcrumb Navigation**: Minimalist horizontal breadcrumb bar ("Semua file > ...") for seamless directory hierarchy navigation.
- **Comprehensive Material You Monet Theming**: Replaced hardcoded color styles with dynamic `MaterialTheme.colorScheme` tokens across backgrounds, card containers, circular buttons, and contextual file/folder accents.
- **Advanced Persistent Sorting**: Interactive bottom sheet modal for sorting by Name, Date, Size, or Type (Ascending / Descending) saved across sessions.
- **Direct Directory Item Counts**: Added direct `itemCount` metadata field to `FileItem` and directory scanning services (`FileService` & `RootFileService`) for responsive folder item count display.

### Added & UI Polish - Dedicated MIUIX/HyperOS Explorer & Architecture Modularization
- **Full MIUIX/HyperOS Explorer Experience**: Implemented a dedicated File Explorer interface when MIUIX theme mode is active (`isMiuixUiEnabled`), featuring HyperOS cards, format color badges, shimmer loading skeleton, and empty states.
- **Dynamic Breadcrumbs Navigation (MIUIX Chips)**: Added a scrollable horizontal breadcrumbs bar with pill chips for fluid path navigation across folder hierarchies.
- **Quick Action Docks & Speed-Dial FAB**: Introduced floating selection action dock (`MiuixSelectionDock`), floating clipboard paste dock (`MiuixClipboardDock`), and expandable speed-dial FAB for file and folder creation.
- **Native HyperOS Dialogs & Modals**: Redesigned all explorer operation dialogs (new folder/file, rename, delete confirm, quick extract, APK installer, progress overlays) with native MIUIX aesthetic.
- **Full Explorer Architecture Modularization (Material & MIUIX)**: Decomposed both monolithic Material (`ExplorerScreen`) and MIUIX (`MiuixExplorerScreen`) explorer screens from >2.2k lines down to clean, modular architectures with dedicated subpackages (`material/` and `miuix/`: TopBars, Docks, FileItemCard, Dialogs, and DialogHost) for superior maintainability and developer ergonomics.

### Fixed & Stability - Standalone OTA Update Engine
- **Semantic Version Comparison Fix**: Fixed version string splitting in `UpdateManager`. Previously, local builds with date suffixes (e.g. `2.1.3-20261004`) caused the patch digit to fail integer parsing, truncating the version to `2.1.0` and causing false-positive update loops for already installed releases.
- **Build Date & Suffix Awareness**: Implemented dedicated semantic version parsing separating base components (`major.minor.patch`) from build/date suffixes (`buildSuffix`), preventing false update triggers when base versions match.
- **Dynamic App Version Resolution**: Replaced hardcoded version fallback strings in `UpdateManager` with dynamic runtime resolution via `PackageManager` (`getAppVersionName`).
- **Automatic Notification & Cache Dismissal**: Reset `availableUpdate` to null, auto-cancelled system update notifications, and cleared update preferences whenever the app confirms it is running the latest version (`UpToDate`).
- **Auto-Invalidation on Update Screen**: Automatically clears outdated update prompts upon opening the update screen if the cached update is no longer newer than the installed version.
- **Comprehensive Unit Tests**: Added automated unit test suite (`UpdateManagerTest`) covering diverse tag naming conventions and comparison scenarios.

---

## [2.1.3] - 2026-10-04

### Added - ZIP Password Encryption (AES-256 & ZipCrypto)
- **Password Encryption on Compression**: Full support for creating password-protected ZIP archives with selectable encryption algorithms: industry-standard **AES-256** (maximum security) and **ZipCrypto** (legacy compatibility).
- **Automatic Locked Archive Detection**: Smart detection of encrypted ZIP archives upon previewing or extracting, seamlessly triggering the password entry dialog.
- **Zip4j Engine Integration**: Leveraged Zip4j library for robust and reliable ZIP password encryption and decryption handling.

### Redesigned & UI Enhancements
- **App-Wide Proportional Dialog Redesign**: Completely overhauled all popup dialogs across the application (compression, extraction, create folder/file, rename, overwrite confirmation, etc.) for a clean, proportional, and balanced Material 3 look.
- **Symmetric 50/50 Action Buttons**: Replaced uneven buttons with balanced 50/50 horizontal action rows designed for natural thumb reach.
- **Structured Header Icon Containers**: Added rounded squircle icon containers at the top of each dialog with thematic accent background tones.
- **Accurate Password Prompt Labels**: Cleaned up the unlock dialog label to "Password" (mandatory) instead of displaying an optional hint.

### Fixed & Stability
- **Encrypted Archive Preview Crash Fix**: Fixed a fatal crash when tapping once to view password-protected ZIP archives in the Explorer; the app now gracefully requests the archive password and opens contents safely.

---

## [2.1.0] - 2026-10-03

### Added - Recent Files & Activity Tracking
- **Recent Files Manager**: Added `RecentManager` to track recently accessed, modified, and extracted archives and files.
- **Dedicated Recent Files Tab**: Built-in tab on the Home Screen with category grouping, file metadata, and quick access actions.
- **Text Marquee Animation**: Implemented animated marquee for filenames and overflowing card titles.

### Added - Standalone OTA App Updates & Markdown Rendering
- **Dedicated Update Screen**: Standalone software update screen tailored for both MIUIX / HyperOS and Material Design 3 modes without dialog popups.
- **Automatic GitHub Releases Checker**: Detects new app releases directly from GitHub Releases API without Firebase dependencies.
- **Progress-Filling Pill**: Download action button with an interactive pill that fills smoothly horizontally as download progresses in real-time.
- **Native Markdown Renderer**: Renders rich release notes with headers, accented bullet lists, monospace inline code chips, and clickable links.
- **Notifications & Badge Indicators**: Displays an update badge dot on the Settings icon, About screen rows, and posts high-priority system notifications.

### Added - MIUIX / HyperOS Aesthetic Interface
- **MIUIX KMP Integration**: Integrated MIUIX Kotlin Multiplatform design components (`miuix-ui`, `miuix-blur`, `miuix-icons` v0.9.3).
- **HyperOS-styled Home Screen (`MiuixHomeScreen`)**: Features squircle cards, texture blur headers, and modern floating navigation pill.
- **HyperOS-styled Settings (`MiuixSettingsScreen`)**: Grouped settings cards, smooth switch tiles, and themed icons.
- **HyperOS "About Phone" Style About Screen (`MiuixAboutScreen`)**: Detailed app identity card, storage quick info, and specs navigation.
- **Dedicated Detailed App Specs Screen**: Standalone screens for both MIUIX (`MiuixAppSpecsScreen`) and Material 3 (`AppSpecsScreen`) displaying comprehensive app architecture, archiver engines, UI framework, and system parameters.
- **Dedicated Supported Formats Screen**: Standalone catalog screens (`MiuixSupportedFormatsScreen` and `SupportedFormatsScreen`) detailing archive types, single-file streams, packaging specs, and capability badges.

### Added - In-App Release Notes Screen
- **Native In-App Changelog Viewer**: Added dedicated `ChangelogScreen` and `MiuixChangelogScreen` accessible directly from the top bar next to Settings.
- **Proportional Full-Width Typography**: Clean full-width layouts for release items, micro-category pills, version filter pills, copy release notes, and summary callout cards.

### Added - Intelligent Device Auto-Detection
- **Xiaomi / Redmi / Poco Auto-Switch**: Automatic activation of MIUIX UI mode on Xiaomi family devices running MIUI or Xiaomi HyperOS.
- **Multi-tier Detection**: Checks hardware brand/manufacturer identity, MIUI/HyperOS system properties, and framework introspection.
- **User Preference Override**: Manual toggle in Settings remains fully respected and persisted in DataStore.

### Added - In-App HTML Web Runner & Preview
- **Interactive HTML Runner**: Added a Play button in the Text Editor when opening or editing `.html`/`.htm` files to render and test websites directly in-app without launching an external browser.
- **Full Relative Asset Support**: Automatically resolves and loads local CSS stylesheets, JavaScript scripts, and images in the same folder or subdirectories.
- **Mobile / Desktop Viewport Toggle**: Desktop mode switcher that overrides the viewport meta tag (1280px width), enables overview scaling, and simulates desktop Chrome to test responsive layouts.

### Changed & Modernized
- **Minimum SDK Raised**: Increased `minSdk` from Android 8.0 (API 26) to Android 10 (API 29) across all app and core modules (`core-storage`, `core-archive`, `core-extract`, `core-root`).
- **Manifest Merger Compatibility**: Added `tools:overrideLibrary` configuration for MIUIX KMP libraries to ensure smooth runtime execution on Android 10+.
- **Full Localization (English & Indonesian)**: Complete resource-based localization (`strings.xml` and `values-in/strings.xml`) for all About, Specs, and Supported Formats screens and badges.
- **App Version Bump**: Bumped to version `2.1.0` (versionCode `2`).

### Optimized - File Explorer & Scrolling Performance
- **LazyColumn Item Recycling (`contentType`)**: Implemented `contentType` distinguishing folders and files to allow optimal composition slot recycling during fast scrolling.
- **In-Memory APK Icon Cache (`LruCache`)**: Added memory caching for APK icons to prevent repeated disk I/O and zip manifest parsing during scrolling.
- **Eliminated Heavy Touch Animations**: Removed coroutine-based spring physics animations on card interaction to ensure instantaneous, stutter-free scroll response.
- **Overdraw Reduction & Memoization**: Reduced unnecessary card shadow overdraw and memoized file metadata formatting and category color lookups.

---

## [2.0.0] - 2026-08-19

### Added - Elevated Privileges & Root Integration
- **Shizuku Integration**: Added support for Shizuku API (v13+) via `dev.rikka.shizuku` for privileged file operations without root.
- **Root Shell Service**: Integrated `com.github.topjohnwu.libsu:core` v6.0.0 for full superuser (su) capabilities.
- **Privileged File Operations**: Implemented `ShizukuFileService` and `RootFileService` with automated fallback in `FileOperationsManager`.
- **Privileged Text Editing**: Enabled reading and writing protected system files via `TextEditorScreen`.
- **Privileged Access UI**: Added dedicated Shizuku status indicator, setup guide, and root toggle in Settings.

### Added - Storage Management & File Explorer Overhaul
- **Multi-Storage Horizontal Pager**: Redesigned home storage section supporting Internal Storage, SD Card, OTG USB, and Root partitions.
- **Robust Storage Detection**: Implemented Android `StorageManager` volume discovery with automatic polling and refresh for OTG drives.
- **Filesystem Diagnostics**: Real-time filesystem type detection (F2FS, ext4, FAT32) and storage health metrics.
- **Enhanced File Explorer**: Multi-file selection mode (`SelectionManager`), batch extraction, copy, move, delete, and rename.
- **Media Previews & APK Badges**: Integrated Coil image and video thumbnail decoding; extracted application icons for APK files.
- **Conflict Resolution**: Added file collision dialog for duplicate file handling during extraction/copying.

### Added - Native Payload Parser & Archive Capabilities
- **Rust Native Payload Engine**: Integrated native Rust `payload-parser` JNI module for rapid Android OTA system image extraction.
- **`PayloadViewerScreen`**: Interactive partition browser with individual/batch extraction and SHA256 integrity verification.
- **Unified `ArchiveReader`**: Standardized stream-based archive reader architecture.
- **Streaming Extraction Progress**: Byte-level real-time extraction progress updates.
- **Selective Extraction**: Temporary storage extraction and inspection without uncompressing full archives.

### Added - UI/UX & Dynamic Island Notifications
- **Expressive Material 3 Design**: Overhauled color palette, custom squircle shapes, and AMOLED dark mode.
- **Dynamic Island Notifications**: Replaced stock snackbars with animated pill notification banners (`DynamicIslandNotificationHost`).
- **Modernized Settings & About**: Full-screen Settings redesign and dynamic version metadata in About screen.
- **Permission Flow Setup**: Streamlined onboarding with permission-gated `SetupScreen`.
- **String Internationalization**: Standardized string resources for multi-language support.

---

## [1.1-Release] - 2026-02-26

### Added - Payload.bin Viewer (NEW!)
- **Complete Android OTA Payload.bin Support**
  - View and extract Android OTA system images
  - Browse partition list (system, vendor, boot, product, etc.)
  - Extract individual partitions to .img files
  - Extract all partitions at once
  - Real-time extraction progress tracking
  - SHA256 hash verification after extraction
  
- **Protobuf Manifest Parsing**
  - Parse Android OTA manifest using Protocol Buffers
  - Display partition information (name, size, compression type)
  - Support for multiple compression types (XZ, BZIP2, uncompressed)
  - Block-level operation parsing
  
- **Advanced Decompression**
  - XZ/LZMA decompression support
  - BZIP2 decompression support
  - On-the-fly decompression (no temporary files)
  - Memory-efficient streaming decompression

### Added - Archive Management
- New `ArchiveManager` class for centralized archive operations
- Real-time extraction progress tracking with byte-level accuracy
- Support for large archive files (3GB+) with smooth progress updates
- Enhanced archive preview functionality
- File size validation before opening in text editor (10MB limit)
- Binary file type detection to prevent opening non-text files

### Added - Code Refactoring
- Created `ExplorerViewModel` for better separation of concerns
- Created `FileTypeDetector` utility for file type detection
- Created `FileActionHandler` for centralized file click handling
- Modularized `ExplorerScreen` (reduced from 1304 lines)
- Improved code maintainability and testability

### Fixed
- **Critical**: Fixed application crash when previewing compressed files (ZIP, TAR, GZ)
  - Resolved Flow invariant violation by properly using `flowOn(Dispatchers.IO)`
  - Archive preview now works reliably without crashes
- **Critical**: Fixed OutOfMemoryError when opening large binary files (e.g., payload.bin)
  - Added 10MB file size limit for text editor
  - Prevented binary files from being opened as text
  - Added proper error messages for unsupported file types
- **Major**: Fixed extraction progress bar stuck at 0%
  - Implemented single-pass extraction with byte-level progress tracking
  - Progress bar now updates smoothly during extraction of large files
  - Eliminated unnecessary two-pass archive scanning
- Fixed archive adapter interface to support batch extraction operations
- Fixed memory issues when viewing large archive entries
- Fixed compilation errors in ExplorerViewModel (type mismatches)
- Fixed FileItem import conflicts between core and storage packages

### Changed
- Improved extraction performance for large archives
- Progress updates now emit every 1MB for optimal UI responsiveness
- Refactored archive extraction to use Flow-based progress reporting
- Text editor now validates file size and type before loading
- Archive entry viewer now has size limits to prevent OOM errors
- Default extraction output: `Downloads/XArchiver/extracted/`

### Technical Improvements
- Added Protocol Buffers support for Android OTA manifest parsing
- Integrated `org.tukaani:xz:1.9` for XZ decompression
- Integrated Apache Commons Compress for BZIP2 decompression
- Added `flowOn` operator for proper coroutine context switching
- Implemented byte-based progress calculation for accurate tracking
- Enhanced error handling in archive operations
- Added file size validation throughout the application
- Improved memory management for large file operations
- Cleaned up build artifacts from version control
- Added protobuf gradle plugin configuration

### Documentation
- Created comprehensive project structure documentation
- Added Phase 2 completion documentation (Protobuf parsing)
- Added Phase 3 completion documentation (Partition extraction)
- Updated refactoring summary

### Dependencies Added
- `com.google.protobuf:protobuf-javalite:3.25.1` - Protobuf parsing
- `org.tukaani:xz:1.9` - XZ/LZMA decompression
- Protobuf Gradle Plugin v0.9.4

### Known Limitations
- Delta OTA updates not supported (requires old partition image)
- No custom output directory selection
- Sequential partition extraction (no parallel processing)
- No extraction resume support

---

## [1.0] - Initial Release

### Added
- Basic archive viewing and extraction
- Support for ZIP, TAR, GZ, TGZ formats
- File explorer with archive integration
- Material Design 3 UI
