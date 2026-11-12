# Catatan Rilis (Changelog)

Semua perubahan penting pada XArchiver akan didokumentasikan dalam berkas ini.

## [2.1.9] - 2026-10-08

### Desain Ulang Antarmuka - Beranda MIUIX HyperOS Modern
- **Tata Letak Pil Kategori 2 Kolom × 3 Baris**:
  - Mengadopsi arsitektur visual resmi Pengelola Berkas HyperOS dengan deretan pil bersudut squircle 18dp: **Dok** (Amber), **Gambar** (Royal Blue), **Video** (Purple), **Musik** (Coral Red), **Arsip** (Cyan), dan **APK** (Emerald Green).
  - Dilengkapi ikon kontainer bersudut melengkung yang khas dan penghitungan jumlah item berkas aktual secara asinkron.
- **Grup Akses Cepat Terpadu**:
  - Kartu sudut membulat 20dp yang menggabungkan akses langsung ke folder **Unduhan** (`/sdcard/Download`) dan **Favorit** (terintegrasi dengan `BookmarksManager` lokal yang menampilkan jumlah folder yang ditandai).
- **Kartu Penyimpanan Modern & Dukungan Root Privileged**:
  - Menampilkan penyimpanan internal ("Ponsel saya") lengkap dengan indikator sisa kapasitas, *segmented thin progress gauge* 4dp, dan badge sistem berkas (seperti F2FS/ext4).
  - Menghadirkan baris khusus **Penyimpanan Sistem (Root)** dengan badge Root Amber saat opsi akses Root aktif.
- **Seksi Sumber Terdedikasi (Sources)**:
  - Mengelompokkan jalan pintas direktori media utama: **Kamera** (`DCIM/Camera`), **Screenshot & rekaman layar** (`Pictures/Screenshots`), serta pintasan berkas pesan (WhatsApp, Telegram, Bluetooth).
- **Bilah Navigasi Mengambang Kapsul Ganda**:
  - Menggabungkan kapsul navigasi melayang (*texture blur* kaca) untuk peralihan mulus antara tab **Baru-baru ini** dan **Telusuri**.

### Fitur Baru - Pemantauan & Penyegaran Berkas Realtime Otomatis
- **Pemantau Direktori Waktu-Nyata Berbasis Kernel (DirectoryWatcher & inotify)**:
  - Mengintegrasikan `android.os.FileObserver` untuk mendeteksi event inotify kernel Linux secara langsung pada direktori yang sedang dibuka (pembuatan berkas/folder baru, penghapusan, pemindahan berkas keluar/masuk, dan penulisan berkas selesai).
  - Berkas baru yang ditransfer langsung dari PC (via MTP atau ADB), foto yang baru diambil kamera, berkas unduhan dari peramban, maupun folder yang dibuat dari pengelola berkas lain langsung muncul seketika tanpa perlu keluar (back) dan masuk kembali ke folder tersebut.
- **Penyegaran Hening Latar Belakang (Silent Background Refresh)**:
  - Pembaruan berkas dari pemantau waktu-nyata dieksekusi secara hening (*silent*) di `Dispatchers.IO` tanpa menampilkan kerangka skeleton loading yang berkedip.
  - Mempertahankan posisi gulir (*scroll position*) pengguna serta status seleksi berkas secara stabil berkat mekanisme *key diffing* Compose.
- **Pencegah Banjir Event (Coroutine Debounce & Throttle Guard)**:
  - Menerapkan *trailing debounce* sebesar 400 milidetik untuk mencegah pemindaian berulang saat mentransfer berkas dalam jumlah banyak secara beruntun.
  - Dilengkapi *throttle guard* (maksimum interval 1.5 detik) yang tetap memperbarui tampilan berkas secara berkala saat proses transfer berkas berukuran besar atau ratusan berkas masih berlangsung.
- **Penyegaran Otomatis Siklus Hidup (Lifecycle ON_RESUME Observer)**:
  - Menyegarkan direktori secara otomatis saat aplikasi kembali ke latar depan dari latar belakang atau dari aplikasi lain, menjamin daftar berkas selalu akurat.
- **Dukungan Penuh pada Tema Material 3 dan MIUIX HyperOS**:
  - Bekerja secara terpadu dan konsisten baik pada penjelajah berkas antarmuka Material You dinamis maupun MIUIX HyperOS.

### Fitur Baru - Sistem Pencarian Terlingkup Cerdas (Scoped Search) & Mesin XArcDeep
- **Pencarian Terlingkup Direktori Aktif (Folder-Scoped Search)**:
  - Mengadopsi alur pencarian modern ala pengelola berkas ColorOS 17 di mana penelusuran secara cerdas terisolasi pada direktori yang sedang dibuka (misal saat mencari di folder `Documents` atau `Download`, hanya berkas dan subfolder di dalam folder tersebut yang dipindai).
  - Mengurangi beban I/O penyimpanan dan mempercepat pencarian secara drastis dibandingkan pemindaian global tanpa arah.
- **Penyaring Kategori Instan Berbasis Chip (Category Filter Chips)**:
  - Deretan chip filter horizontal yang dapat digeser di bagian atas hasil pencarian untuk penyaringan cepat berdasarkan jenis berkas: **Semua (All)**, **Gambar (Images)**, **Video**, **Audio**, **Dokumen (Documents)**, **Arsip (Archives)**, dan **APK**.
  - Setiap kategori dilengkapi dengan ikon visual yang khas dan warna pembeda untuk identifikasi instan jenis berkas.
- **Penyorotan Kata Kunci Kueri (Search Query Keyword Highlighting)**:
  - Karakter atau kata yang cocok dengan kata kunci pencarian pada nama berkas secara otomatis disorot dengan warna aksen kontras (`primaryAccentColor`), memudahkan pengguna menemukan berkas di antara daftar panjang hasil temuan.
- **Ringkasan Indikator Hasil & Nama Lingkup**:
  - Bilah ringkasan di atas daftar hasil yang menampilkan total temuan ("X item seluruhnya") serta nama folder cakupan pencarian (misalnya "Perangkat ini" atau nama folder yang sedang dibuka).
- **Mesin Penelusuran Cepat XArcDeep**:
  - Tombol aksi cerdas pada footer hasil pencarian dan tampilan kosong ("File tidak ditemukan? Coba **XArcDeep.**").
  - Menawarkan opsi penelusuran mendalam menyeluruh ke seluruh penyimpanan internal perangkat secara instan jika berkas yang dicari tidak berada di folder aktif.
- **Lokalisasi Multibahasa Penuh (i18n)**:
  - Seluruh teks pencarian dan penjelajah berkas telah dipindahkan secara bersih ke `res/values/strings.xml` (Bahasa Inggris) dan `res/values-in/strings.xml` (Bahasa Indonesia) tanpa teks hardcoded, sehingga teks berubah secara konsisten saat bahasa sistem diubah ke bahasa Inggris maupun Indonesia.

### Desain Ulang Tata Letak - Beranda Tema Material (Material Home UI Layout)
- **Tata Letak Kartu Penyimpanan yang Ditingkatkan (StorageCard)**:
  - Menyusun ulang posisi elemen kartu penyimpanan: badge tipe filesystem (seperti F2FS) dan indikator status kesehatan di sisi kiri atas, dengan tombol aksi kapsul `> Buka` di sisi kanan atas.
  - Ikon perangkat dan nama penyimpanan internal kini berdampingan secara horizontal di bagian tengah kartu.
  - Informasi rincian kapasitas terpakai dan persentase penggunaan ditempatkan rapi di atas indikator progres linier berujung membulat di bagian bawah.
- **Tata Letak Kategori Bertingkat Cerdas (CategoryGrid)**:
  - Baris pertama menampilkan 2 kartu horizontal lebar untuk kategori utama (**Images** dan **APK**) dengan kontainer ikon di kiri serta nama dan jumlah file di kanan.
  - Baris kedua menampilkan 4 kartu vertikal kompak dalam satu baris untuk kategori **Videos**, **Audio**, **Documents**, dan **Archives**.
- **Kartu Penuh Favorit / Bookmarks (BookmarkLongItem)**:
  - Mengintegrasikan fitur Bookmarks/Favorit langsung ke dalam seksi Kategori sebagai kartu panjang selebar layar dengan indikator jumlah folder ditandai dan kontainer bertema tersier Monet.
  - Membuka item secara instan (folder di Explorer atau berkas melalui penampil/pemutar/editor/penginstal bawaan) saat terdapat 1 bookmark, atau menampilkan Modal Bottom Sheet di tema Material dan MIUIX untuk menjelajahi dan membuka banyak bookmark sekaligus lengkap dengan ikon tipe berkas dan opsi penghapusan cepat.
- **Grid 2 Kolom untuk Akses Cepat (ShortcutList)**:
  - Menyusun ulang daftar shortcut akses cepat (Bluetooth, Unduhan, WhatsApp, Telegram, dll.) ke dalam tata letak kisi 2 kolom kartu horizontal dengan ikon di sisi kiri dan label teks di sisi kanan.
- **Bilah Navigasi Mengambang Kapsul (Floating Capsule Bar)**:
  - Bilah tab bawah mengambang bergaya pill capsule yang elegan dengan indikator aktif yang menonjol untuk beralih antara tab **Terbaru** dan **File**.
- **Kepatuhan Desain Material You Murni**:
  - Seluruh penyesuaian posisi tata letak tetap mematuhi sistem warna dinamis Monet dan Material 3 murni tanpa bergantung pada warna gelap statis atau efek blur.

### Desain Ulang & Fitur Baru - Material Explorer UI Modern & Mode Tampilan Dinamis
- **Tiga Mode Tampilan Berkas Dinamis**: Menghadirkan opsi tampilan fleksibel yang dapat diubah secara instan melalui menu titik tiga (`⋮`) dan disimpan otomatis di DataStore:
  - **Tampilan Daftar (Default)**: Tata letak vertikal lengkap dengan detail jumlah item folder atau ukuran berkas serta tanggal modifikasi.
  - **Tampilan Kisi (Grid)**: Tata letak kartu kotak 3 kolom berujung membulat dengan kontainer permukaan Monet Material 3 (`surfaceContainer`).
  - **Air Terjun (Waterfall)**: Tata letak kisi bersih 3 kolom tanpa kartu pembungkus di mana ikon berkas dan folder tampil mengambang bebas di atas kanvas latar belakang.
- **Bilah Atas Modern Berbasis Tombol Lingkaran**: Merombak total TopBar Material Explorer dengan tombol aksi lingkaran yang elegan (kembali, tambah berkas/folder baru `+`, pencarian, dan opsi lainnya `⋮`).
- **Navigasi Breadcrumb Interaktif**: Menghadirkan bilah jalur navigasi horizontal minimalis ("Semua file > ...") yang memudahkan penelusuran hierarki direktori secara cepat.
- **Penerapan Penuh Palet Warna Monet Material You**: Seluruh komponen Material Explorer terintegrasi penuh dengan token tema dinamis `MaterialTheme.colorScheme` (warna latar belakang, kartu kontainer, tombol lingkaran, teks judul, serta aksen folder dan badge berkas) tanpa warna hardcoded hitam/biru.
- **Pengurutan Berkas Canggih**: Modal sheet pengurutan interaktif berdasarkan Nama, Tanggal, Ukuran, atau Tipe (Naik / Turun) dengan preferensi yang tersimpan persisten.
- **Metadata Jumlah Item Langsung**: Penambahan field `itemCount` pada model berkas dan layanan direktori (`FileService` & `RootFileService`) untuk pembacaan instan jumlah item di dalam folder.

### Fitur Baru & Peningkatan Antarmuka - MIUIX HyperOS Explorer & Modularisasi Arsitektur
- **Antarmuka Penuh MIUIX/HyperOS File Explorer**: Mengimplementasikan antarmuka khusus MIUIX File Explorer saat mode MIUIX aktif (`isMiuixUiEnabled`), lengkap dengan kartu item file/folder bergaya HyperOS, indikator format badge warna, animasi skeleton shimmer, dan empty state.
- **Navigasi Breadcrumb Interaktif (MIUIX Chips)**: Menambahkan bilah breadcrumb horizontal dengan chips pill untuk penelusuran hierarki path yang mulus dan intuitif.
- **Dock Aksi Cepat & Menu Melayang**: Menghadirkan bilah aksi seleksi bawah melayang (`MiuixSelectionDock`), bar tempel papan klip (`MiuixClipboardDock`), dan tombol FAB speed-dial interaktif untuk pembuatan berkas/folder baru.
- **Dialog & Modal Terintegrasi HyperOS**: Seluruh dialog operasi berkas (buat folder/file, ganti nama, konfirmasi hapus, ekstrak cepat, pasang APK, progress operasi berkas) dirancang ulang dengan estetika native MIUIX.
- **Modularisasi Penuh Arsitektur Explorer (Material & MIUIX)**: Memecah kode monolitik penjelajah berkas Material (`ExplorerScreen`) dan MIUIX (`MiuixExplorerScreen`) dari sebelumnya >2.2k baris menjadi arsitektur modular terpisah (`material/` dan `miuix/` subpackages: TopBars, Docks, FileItemCard, Dialogs, dan DialogHost) sehingga kode menjadi bersih, efisien, dan mudah dipelihara.

### Perbaikan Bug & Stabilitas - Modul Pembaruan Mandiri (OTA Update)
- **Perbaikan Algoritma Komparasi Versi Semantik**: Memperbaiki pemisahan token versi pada `UpdateManager`. Sebelumnya, versi aplikasi yang memiliki suffix tanggal build (seperti `2.1.3-20261004`) menyebabkan digit patch terbuang dan dianggap sebagai versi lebih rendah (`2.1.0`), sehingga memicu permintaan pembaruan berulang (update loop) padahal versi yang sama sudah terpasang.
- **Dukungan Parsing Tanggal Build & Suffix**: Algoritma baru secara cerdas memisahkan komponen semver utama (`major.minor.patch`) dengan suffix nomor/tanggal build (`buildSuffix`), serta membandingkan suffix jika basis versi identik.
- **Resolusi Versi Aplikasi Dinamis**: Menghapus fallback versi statis hardcoded pada `UpdateManager` dan beralih ke deteksi versi runtime dinamis dari `PackageManager` (`getAppVersionName`).
- **Pembersihan Otomatis Status & Notifikasi**: Ketika aplikasi mendeteksi bahwa sistem sudah berada pada versi terbaru (`UpToDate`), cache `availableUpdate` direset, bilah notifikasi pembaruan ditutup otomatis, dan penanda notifikasi dibersihkan.
- **Auto-Invalidation di Layar Pembaruan**: Menambahkan validasi otomatis saat layar pembaruan dibuka agar info update lama yang sudah usang atau setara dengan versi terpasang langsung dibersihkan.
- **Unit Test Komprehensif**: Menambahkan rangkaian unit test otomatis (`UpdateManagerTest`) untuk memvalidasi seluruh variasi format tag dan perbandingan versi.

---

## [2.1.3] - 2026-10-04

### Ditambahkan - Enkripsi Kata Sandi ZIP (AES-256 & ZipCrypto)
- **Enkripsi Kata Sandi Saat Kompresi**: Dukungan penuh pembuatan arsip ZIP yang dilindungi kata sandi, dengan opsi enkripsi standar **AES-256** (sangat aman) dan **ZipCrypto** (kompatibilitas warisan).
- **Deteksi Otomatis Arsip Terkunci**: XArchiver secara cerdas mendeteksi arsip ZIP terenkripsi saat dibuka untuk melihat isi maupun diekstrak, dan menampilkan dialog kata sandi secara otomatis.
- **Dukungan Zip4j Engine**: Mengintegrasikan engine Zip4j untuk penanganan enkripsi dan dekripsi berkas ZIP yang kuat dan andal.

### Desain Ulang & Peningkatan Antarmuka (UI)
- **Desain Ulang Dialog Seluruh Aplikasi**: Merapikan seluruh dialog popup di aplikasi (kompresi, ekstraksi, buat folder/berkas, ubah nama, ganti nama, konfirmasi timpa, dll.) agar berpenampilan proporsional, seimbang, dan modern.
- **Tombol Aksi Simetris 50/50**: Menghilangkan tombol bertumpuk atau tidak simetris; seluruh dialog kini menggunakan baris tombol aksi horizontal dengan rasio pembagian ruang 50/50 yang nyaman disentuh.
- **Wadah Ikon Header Terstruktur**: Menambahkan wadah ikon berbentuk squircle/lingkaran ber-radius di bagian header setiap dialog dengan warna aksen harmonis.
- **Label Kata Sandi yang Akurat**: Memperbaiki label dialog pembuka arsip terkunci menjadi "Kata Sandi" (wajib) menggantikan label opsional.

### Perbaikan Bug & Stabilitas
- **Perbaikan Crash Buka Arsip Berpassword**: Memperbaiki masalah crash aplikasi saat menekan sekali (buka) berkas arsip ZIP yang dikunci kata sandi di Explorer; kini langsung membuka dialog masukkan kata sandi dengan aman dan lancar.

---

## [2.1.0] - 2026-10-03

### Ditambahkan - Berkas Terkini & Pelacakan Aktivitas
- **Pengelola Berkas Terkini**: Menambahkan `RecentManager` untuk melacak arsip dan berkas yang baru diakses, diubah, dan diekstrak.
- **Tab Berkas Terkini Khusus**: Tab bawaan di Layar Beranda dengan pengelompokan kategori, metadata berkas, dan aksi akses cepat.
- **Animasi Teks Berjalan (Marquee)**: Mengimplementasikan animasi teks berjalan untuk nama berkas panjang dan judul kartu yang melebihi batas.

### Ditambahkan - Pembaruan Aplikasi Mandiri (OTA Update) & Rendering Markdown
- **Layar Pembaruan Khusus (Update Screen)**: Halaman pembaruan perangkat lunak mandiri yang mendukung tema MIUIX / HyperOS dan Material Design 3 tanpa popup dialog.
- **Pengecekan Otomatis GitHub Releases**: Mendeteksi versi rilis terbaru langsung dari repository GitHub tanpa ketergantungan Firebase.
- **Progress-Filling Pill**: Tombol aksi unduh berupa kapsul interaktif yang terisi halus dari kiri ke kanan secara proporsional dengan persentase unduhan dan bytes.
- **Renderer Markdown Native**: Mendukung tampilan catatan rilis berbasis Markdown lengkap dengan heading, daftar poin beraksen, kode inline monospace, dan tautan.
- **Notifikasi & Indikator Lencana**: Menampilkan titik indikator lencana pembaruan baru pada ikon Pengaturan dan kartu Tentang serta notifikasi sistem Android.

### Ditambahkan - Antarmuka Estetika MIUIX / HyperOS
- **Integrasi MIUIX KMP**: Mengintegrasikan komponen desain MIUIX Kotlin Multiplatform (`miuix-ui`, `miuix-blur`, `miuix-icons` v0.9.3).
- **Layar Beranda Bergaya HyperOS (`MiuixHomeScreen`)**: Menampilkan kartu squircle, header blur tekstur real-time, dan pil navigasi mengambang modern.
- **Pengaturan Bergaya HyperOS (`MiuixSettingsScreen`)**: Kartu pengaturan terkelompok, sakelar interaktif yang halus, dan ikon bertema.
- **Layar Tentang Bergaya "Tentang Ponsel" HyperOS (`MiuixAboutScreen`)**: Kartu identitas aplikasi mendalam, info cepat penyimpanan, dan navigasi spesifikasi.
- **Layar Spesifikasi Aplikasi Khusus**: Halaman mandiri untuk mode MIUIX (`MiuixAppSpecsScreen`) dan Material 3 (`AppSpecsScreen`) yang menampilkan arsitektur aplikasi, mesin pengarsip, framework UI, dan parameter sistem.
- **Layar Format yang Didukung Khusus**: Halaman katalog mandiri (`MiuixSupportedFormatsScreen` dan `SupportedFormatsScreen`) yang merinci tipe arsip, streaming berkas tunggal, spesifikasi pengemasan, dan lencana kapabilitas.

### Ditambahkan - Layar Catatan Rilis Dalam Aplikasi
- **Penampil Catatan Rilis Native**: Menambahkan `ChangelogScreen` dan `MiuixChangelogScreen` yang dapat diakses langsung dari bilah atas di sebelah ikon pengaturan.
- **Tipografi Proporsional Penuh**: Tata letak lebar penuh yang rapi untuk setiap item rilis, lencana kategori mikro, pil filter versi, aksi salin catatan rilis, dan wadah sorotan pembaruan.

### Ditambahkan - Deteksi Otomatis Perangkat Cerdas
- **Peralihan Otomatis Xiaomi / Redmi / Poco**: Pengaktifan otomatis mode UI MIUIX pada perangkat keluarga Xiaomi yang menjalankan MIUI atau Xiaomi HyperOS.
- **Deteksi Multi-Tahap**: Memeriksa identitas merek/manufaktur perangkat keras, properti sistem MIUI/HyperOS, dan introspeksi framework.
- **Prioritas Preferensi Pengguna**: Sakelar manual di Pengaturan tetap dihormati dan disimpan secara persisten di DataStore.

### Ditambahkan - Runner & Pratinjau Web HTML Dalam Aplikasi
- **Runner HTML Interaktif**: Menambahkan tombol Play pada Text Editor saat membuka atau mengedit berkas `.html`/`.htm` untuk merender dan menjalankan situs web langsung di dalam aplikasi tanpa membuka browser eksternal.
- **Dukungan Aset Relatif Penuh**: Mendukung pemuatan skrip JavaScript, stylesheet CSS eksternal, dan gambar lokal yang berada di direktori yang sama.
- **Peralihan Tampilan Ponsel / Desktop**: Pengaturan mode desktop dengan override tag viewport (lebar 1280px), zoom ikhtisar, dan user-agent desktop untuk menguji layout responsif.

### Diubah & Dimodernisasi
- **Peningkatan Minimum SDK**: Menaikkan `minSdk` dari Android 8.0 (API 26) ke Android 10 (API 29) di seluruh modul aplikasi dan modul core (`core-storage`, `core-archive`, `core-extract`, `core-root`).
- **Kompatibilitas Manifest Merger**: Menambahkan konfigurasi `tools:overrideLibrary` untuk pustaka MIUIX KMP guna memastikan eksekusi runtime lancar di Android 10+.
- **Lokalisasi Penuh (Bahasa Indonesia & Bahasa Inggris)**: Lokalisasi berbasis resource lengkap (`strings.xml` dan `values-in/strings.xml`) untuk seluruh layar Tentang, Spesifikasi, Format yang Didukung, dan Catatan Rilis.
- **Peningkatan Versi Aplikasi**: Naik ke versi `2.1.0` (versionCode `2`).

### Dioptimasi - Performa Penjelajah Berkas & Scrolling
- **Daur Ulang Item LazyColumn (`contentType`)**: Mengimplementasikan `contentType` untuk membedakan item folder dan berkas agar komposisi UI didaur ulang secara efisien saat scrolling cepat.
- **Cache Memori Ikon APK (`LruCache`)**: Menambahkan in-memory cache untuk ikon berkas APK guna menghindari pembacaan ulang berkas dari disk saat scroll.
- **Penghapusan Animasi Sentuh Berat**: Menghapus animasi fisika spring pada interaksi kartu item guna menghasilkan respons scrolling instan tanpa lag.
- **Reduksi Overdraw & Memoization**: Mengoptimalkan bayangan kartu dan menerapkan memoization pada format metadata berkas serta warna kategori.

---

## [2.0.0] - 2026-08-19

### Ditambahkan - Hak Akses Istimewa & Integrasi Root
- **Integrasi Shizuku**: Menambahkan dukungan untuk Shizuku API (v13+) melalui `dev.rikka.shizuku` untuk operasi berkas berhak istimewa tanpa root.
- **Layanan Shell Root**: Mengintegrasikan `com.github.topjohnwu.libsu:core` v6.0.0 untuk kemampuan superuser (su) penuh.
- **Operasi Berkas Berhak Istimewa**: Mengimplementasikan `ShizukuFileService` dan `RootFileService` dengan mekanisme fallback otomatis di `FileOperationsManager`.
- **Pengeditan Teks Berhak Istimewa**: Memungkinkan membaca dan menulis berkas sistem yang dilindungi melalui `TextEditorScreen`.
- **UI Akses Istimewa**: Menambahkan indikator status Shizuku khusus, panduan konfigurasi, dan sakelar root di Pengaturan.

### Ditambahkan - Manajemen Penyimpanan & Perombakan Penjelajah Berkas
- **Pager Horizontal Multi-Penyimpanan**: Desain ulang bagian penyimpanan beranda yang mendukung Penyimpanan Internal, Kartu SD, USB OTG, dan partisi Root.
- **Deteksi Penyimpanan Andal**: Mengimplementasikan pemindaian volume Android `StorageManager` dengan polling otomatis dan penyegaran untuk drive OTG.
- **Diagnostik Sistem Berkas**: Deteksi jenis sistem berkas real-time (F2FS, ext4, FAT32) dan metrik kesehatan penyimpanan.
- **Peningkatan Penjelajah Berkas**: Mode pemilihan multi-berkas (`SelectionManager`), ekstraksi massal, salin, pindah, hapus, dan ganti nama.
- **Pratinjau Media & Lencana APK**: Integrasi dekode thumbnail gambar dan video menggunakan Coil; mengekstrak ikon aplikasi untuk berkas APK.
- **Resolusi Konflik**: Menambahkan dialog tabrakan berkas untuk penanganan berkas duplikat saat ekstraksi atau penyalinan.

### Ditambahkan - Mesin Payload Native & Kapabilitas Arsip
- **Mesin Payload Rust Native**: Mengintegrasikan modul JNI native Rust `payload-parser` untuk ekstraksi cepat gambar sistem Android OTA.
- **`PayloadViewerScreen`**: Penjelajah partisi interaktif dengan ekstraksi individual/massal dan verifikasi integritas SHA256.
- **`ArchiveReader` Terpadu**: Arsitektur pembaca arsip berbasis stream yang terstandarisasi.
- **Progres Ekstraksi Streaming**: Pembaruan progres ekstraksi real-time tingkat byte.
- **Ekstraksi Selektif**: Ekstraksi dan inspeksi penyimpanan sementara tanpa mendekompresi seluruh arsip.

### Ditambahkan - UI/UX & Notifikasi Dynamic Island
- **Desain Material 3 Ekspresif**: Perombakan palet warna, bentuk squircle kustom, dan mode gelap AMOLED murni.
- **Notifikasi Dynamic Island**: Mengganti snackbar bawaan dengan banner notifikasi pil beranimasi (`DynamicIslandNotificationHost`).
- **Pengaturan & Layar Tentang Modern**: Desain ulang Pengaturan layar penuh dan metadata versi dinamis di layar Tentang.
- **Alur Izin Terarah**: Orientasi awal yang disederhanakan dengan `SetupScreen` berbasis izin.
- **Internasionalisasi String**: Standardisasi resource string untuk dukungan multi-bahasa.

---

## [1.1-Release] - 2026-02-26

### Ditambahkan - Penampil Payload.bin
- **Dukungan Penuh Android OTA Payload.bin**
  - Melihat dan mengekstrak gambar sistem Android OTA.
  - Menjelajahi daftar partisi (system, vendor, boot, product, dll.).
  - Mengekstrak partisi individual menjadi berkas .img.
  - Mengekstrak seluruh partisi sekaligus.
  - Pelacakan progres ekstraksi real-time.
  - Verifikasi hash SHA256 setelah ekstraksi.
  
- **Parsing Manifest Protobuf**
  - Membaca manifest Android OTA menggunakan Protocol Buffers.
  - Menampilkan informasi partisi (nama, ukuran, tipe kompresi).
  - Mendukung berbagai tipe kompresi (XZ, BZIP2, tanpa kompresi).
  - Parsing operasi tingkat blok.
  
- **Dekompresi Tingkat Lanjut**
  - Dukungan dekompresi XZ/LZMA.
  - Dukungan dekompresi BZIP2.
  - Dekompresi langsung saat membaca (tanpa berkas sementara).
  - Dekompresi streaming yang hemat memori.

### Ditambahkan - Manajemen Arsip
- Kelas `ArchiveManager` baru untuk operasi arsip terpusat.
- Pelacakan progres ekstraksi real-time dengan akurasi tingkat byte.
- Dukungan untuk berkas arsip berukuran besar (3GB+) dengan pembaruan progres yang lancar.
- Peningkatan fungsi pratinjau arsip.
- Validasi ukuran berkas sebelum dibuka di editor teks (batas 10MB).
- Deteksi tipe berkas biner untuk mencegah pembukaan berkas non-teks.

### Ditambahkan - Refaktor Kode
- Membuat `ExplorerViewModel` untuk pemisahan tanggung jawab yang lebih baik.
- Membuat utilitas `FileTypeDetector` untuk deteksi tipe berkas.
- Membuat `FileActionHandler` untuk penanganan klik berkas terpusat.
- Modularisasi `ExplorerScreen` (dikurangi dari 1304 baris).
- Peningkatan kemudahan pemeliharaan dan pengujian kode.

### Diperbaiki
- **Kritis**: Memperbaiki crash aplikasi saat mempratinjau berkas terkompresi (ZIP, TAR, GZ) dengan memperbaiki pelanggaran invarian Flow menggunakan `flowOn(Dispatchers.IO)`.
- **Kritis**: Memperbaiki OutOfMemoryError saat membuka berkas biner besar (misal payload.bin) dengan batas ukuran 10MB pada editor teks.
- **Utama**: Memperbaiki bilah progres ekstraksi yang sempat terhenti di 0% melalui ekstraksi satu lintasan (single-pass) dengan pelacakan tingkat byte.
- Memperbaiki antarmuka adaptor arsip untuk mendukung operasi ekstraksi massal.
- Memperbaiki masalah memori saat melihat entri arsip berukuran besar.
- Memperbaiki error kompilasi pada ExplorerViewModel (ketidakcocokan tipe).
- Memperbaiki konflik impor FileItem antara paket core dan storage.

### Diubah
- Peningkatan performa ekstraksi untuk arsip besar.
- Pembaruan progres sekarang dipancarkan setiap 1MB untuk responsivitas UI yang optimal.
- Refaktor ekstraksi arsip menggunakan pelaporan progres berbasis Flow.
- Editor teks memvalidasi ukuran dan tipe berkas sebelum memuat.
- Output ekstraksi bawaan: `Downloads/XArchiver/extracted/`.

### Peningkatan Teknis
- Menambahkan dukungan Protocol Buffers untuk parsing manifest Android OTA.
- Mengintegrasikan `org.tukaani:xz:1.9` untuk dekompresi XZ.
- Mengintegrasikan Apache Commons Compress untuk dekompresi BZIP2.
- Menggunakan operator `flowOn` untuk pergantian konteks coroutine yang tepat.
- Menghitung progres berbasis byte untuk pelacakan yang akurat.
- Peningkatan penanganan error pada operasi arsip.

---

## [1.0] - Rilis Awal

### Ditambahkan
- Tampilan dan ekstraksi arsip dasar.
- Dukungan format ZIP, TAR, GZ, TGZ.
- Penjelajah berkas terintegrasi arsip.
- Antarmuka Material Design 3.
