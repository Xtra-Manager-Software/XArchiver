@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================================================
REM XArchiver - Clean & Build Release Script (Windows Batch)
REM Port dari clean_build_release.sh untuk lingkungan Windows
REM ============================================================================

echo.
echo ===================================================
echo  XArchiver - Clean ^& Build Release Script
echo ===================================================
echo.

:: Setup Java & Android SDK paths if not present in current session
if "%JAVA_HOME%"=="" if exist "C:\jdk-17.0.2" set "JAVA_HOME=C:\jdk-17.0.2"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin" set "PATH=%JAVA_HOME%\bin;%PATH%"

if "%ANDROID_HOME%"=="" if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools" set "PATH=%ANDROID_HOME%\platform-tools;%PATH%"
    if exist "%ANDROID_HOME%\cmdline-tools\latest\bin" set "PATH=%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%"
)

REM --- 1. Membersihkan Cache ---
echo Membersihkan cache build...
if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist "build" rmdir /s /q "build" 2>nul
if exist ".gradle" rmdir /s /q ".gradle" 2>nul

echo Membersihkan cache Gradle...
call gradlew.bat clean
echo Pembersihan selesai.
echo.

REM --- 2. Meminta Informasi Keystore Secara Interaktif ---
echo Silakan masukkan detail keystore untuk menandatangani aplikasi:
echo.

REM Deteksi otomatis lokasi keystore di mesin lokal
set "DEFAULT_KEYSTORE=C:\Users\Gustyx-Power\Downloads\xarc-release-key.jks"
if exist "%DEFAULT_KEYSTORE%" (
    echo [Ditemukan keystore: %DEFAULT_KEYSTORE%]
    set /p "KEYSTORE_PATH=Masukkan path keystore [Tekan Enter untuk default]: "
    if "!KEYSTORE_PATH!"=="" set "KEYSTORE_PATH=%DEFAULT_KEYSTORE%"
) else (
    set /p "KEYSTORE_PATH=Masukkan path ke file keystore Anda: "
)

REM Hapus tanda kutip ganda jika pengguna melakukan drag-and-drop
set "KEYSTORE_PATH=!KEYSTORE_PATH:"=!"

REM Validasi apakah file keystore ada
if not exist "!KEYSTORE_PATH!" (
    echo.
    echo [ERROR] File keystore tidak ditemukan di "!KEYSTORE_PATH!"
    pause
    exit /b 1
)

echo [OK] Keystore ditemukan.
echo.

REM Input Key Alias
set /p "KEY_ALIAS=Masukkan alias key Anda [default: xarckey]: "
if "!KEY_ALIAS!"=="" set "KEY_ALIAS=xarckey"

REM Input Keystore Password (karakter tersembunyi via PowerShell)
echo Masukkan password keystore Anda:
for /f "usebackq delims=" %%P in (`powershell -NoProfile -Command "$p = Read-Host -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))"`) do set "KEYSTORE_PASSWORD=%%P"
if "!KEYSTORE_PASSWORD!"=="" (
    set /p "KEYSTORE_PASSWORD=Password keystore kosong, masukkan manual: "
)

REM Input Key Password
echo Masukkan password untuk alias '!KEY_ALIAS!' [Tekan Enter jika sama dengan password keystore]:
for /f "usebackq delims=" %%P in (`powershell -NoProfile -Command "$p = Read-Host -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))"`) do set "KEY_PASSWORD=%%P"
if "!KEY_PASSWORD!"=="" set "KEY_PASSWORD=!KEYSTORE_PASSWORD!"

echo.
REM Input Changelog
set /p "CHANGELOG=Masukkan catatan changelog singkat untuk rilis ini: "
if "!CHANGELOG!"=="" set "CHANGELOG=Pembaruan rilis XArchiver"

echo.
echo ===================================================
echo  Memulai build release dengan informasi keystore...
echo ===================================================
echo Keystore : !KEYSTORE_PATH!
echo Alias    : !KEY_ALIAS!
echo.

REM Menjalankan Gradle task buildAndPublish
call gradlew.bat buildAndPublish ^
    -PmyKeystorePath="!KEYSTORE_PATH!" ^
    -PmyKeystorePassword="!KEYSTORE_PASSWORD!" ^
    -PmyKeyAlias="!KEY_ALIAS!" ^
    -PmyKeyPassword="!KEY_PASSWORD!" ^
    -PmyChangelog="!CHANGELOG!"

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===================================================
    echo  Build release selesai!
    echo  Anda bisa menemukan APK di folder:
    echo    - app\build\outputs\apk\release\
    echo    - dist\
    echo ===================================================
) else (
    echo.
    echo ===================================================
    echo  Build gagal. Silakan periksa pesan log di atas.
    echo ===================================================
)

echo.
pause
