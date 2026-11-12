@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================================================
REM XArchiver - Release Build Script with Telegram Progress
REM ============================================================================

echo.
echo ========================================
echo  XArchiver Release Build Script
echo ========================================
echo.

:: Setup Java & Android SDK paths if not present in current session
if "%JAVA_HOME%"=="" if exist "C:\jdk-17.0.2" set "JAVA_HOME=C:\jdk-17.0.2"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin" set "PATH=%JAVA_HOME%\bin;%PATH%"

if "%ANDROID_HOME%"=="" if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if defined ANDROID_HOME (
    if exist "%ANDROID_HOME%\platform-tools" set "PATH=%ANDROID_HOME%\platform-tools;%PATH%"
    if exist "%ANDROID_HOME%\cmdline-tools\latest\bin" set "PATH=%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%"
)

REM --- Configuration ---
if exist "C:\Users\Gustyx-Power\Downloads\xarc-release-key.jks" (
    set "KEYSTORE_PATH=C:\Users\Gustyx-Power\Downloads\xarc-release-key.jks"
) else (
    set KEYSTORE_PATH=C:\Users\putri\Documents\Project\XMS\Keystore\Keystore-XArchive\xarc-release-key.jks
)
set KEY_ALIAS=xarckey
set KEY_PASSWORD=gusti717
set KEYSTORE_PASSWORD=gusti717

set PROJECT_ROOT=%~dp0
set LOG_FILE=%PROJECT_ROOT%build_log.txt

REM --- Load Telegram credentials from gradle.properties ---
set TG_BOT_TOKEN=
set TG_CHAT_ID=
set TG_MESSAGE_ID=

for /f "usebackq tokens=1,2 delims==" %%A in ("%PROJECT_ROOT%gradle.properties") do (
    if "%%A"=="telegramBotToken" set "TG_BOT_TOKEN=%%B"
    if "%%A"=="telegramChatId" set "TG_CHAT_ID=%%B"
)

REM --- Initialize log file ---
echo XArchiver Release Build Log > "%LOG_FILE%"
echo Started: %DATE% %TIME% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

REM --- Send initial Telegram message ---
call :SendTelegramStart "XArchiver Release Build Started" "Initializing..." "0"

REM =========================================================================
REM [STEP 1/6] VALIDATE KEYSTORE
REM =========================================================================
echo [1/6] Validating keystore...

call :UpdateProgress "Validate Keystore" "Checking keystore file..." "" "5"

if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Keystore not found! >> "%LOG_FILE%"
    call :BuildFailed "Keystore not found at: %KEYSTORE_PATH%"
    pause
    exit /b 1
)
echo OK Keystore found: %KEYSTORE_PATH%
echo.

call :UpdateProgress "Validate Keystore" "Keystore validated!" "OK" "10"

REM =========================================================================
REM [STEP 2/6] CLEAN BUILD
REM =========================================================================
echo [2/6] Cleaning previous builds...

call :UpdateProgress "Clean Builds" "Removing old files..." "" "15"

if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist "app\dist" rmdir /s /q "app\dist" 2>nul
echo OK Clean complete
echo.

call :UpdateProgress "Clean Builds" "Clean complete!" "OK" "20"

REM =========================================================================
REM [STEP 3/6] BUILD RELEASE APK
REM =========================================================================
echo [3/6] Building signed release APK...
echo This may take a few minutes...
echo.

call :UpdateProgress "Android Release APK" "Building..." "assembleRelease" "25"

call gradlew.bat assembleRelease --console=plain ^
    -PmyKeystorePath="!KEYSTORE_PATH!" ^
    -PmyKeystorePassword=!KEYSTORE_PASSWORD! ^
    -PmyKeyAlias=!KEY_ALIAS! ^
    -PmyKeyPassword=!KEY_PASSWORD!

set GRADLE_EXIT=!ERRORLEVEL!

if !GRADLE_EXIT! neq 0 (
    echo GRADLE BUILD FAILED!
    call :BuildFailed "Gradle release build failed!"
    pause
    exit /b 1
)

call :UpdateProgress "Android Release APK" "Complete!" "APK built successfully" "60"
echo OK Release APK built successfully!
echo.

REM =========================================================================
REM [STEP 4/6] RENAME APK
REM =========================================================================
echo [4/6] Renaming APK with version...

call :UpdateProgress "Renaming APK" "Renaming..." "Adding version" "70"

call gradlew.bat renameReleaseApk ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

call :UpdateProgress "Renaming APK" "Complete!" "APK renamed" "75"
echo.

REM =========================================================================
REM [STEP 5/6] UPLOAD TO TELEGRAM
REM =========================================================================
echo [5/6] Uploading APK to Telegram...

call :UpdateProgress "Upload to Telegram" "Preparing..." "Uploading APK" "80"

call gradlew.bat uploadReleaseApkToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
    call :UpdateProgress "Upload Warning" "May have failed" "Check logs" "88"
    echo WARNING: Upload may have failed.
) else (
    echo OK APK uploaded to Telegram successfully!
    call :UpdateProgress "Upload to Telegram" "Complete!" "APK uploaded" "90"
)
echo.

REM =========================================================================
REM [STEP 6/6] SEND BUILD NOTIFICATION
REM =========================================================================
echo [6/6] Sending build notification to Telegram...

call :UpdateProgress "Notification" "Sending..." "Notifying users" "95"

call gradlew.bat notifyBuildStatusToTelegram ^
    -PmyKeystorePath="%KEYSTORE_PATH%" ^
    -PmyKeystorePassword=%KEYSTORE_PASSWORD% ^
    -PmyKeyAlias=%KEY_ALIAS% ^
    -PmyKeyPassword=%KEY_PASSWORD% >> "%LOG_FILE%" 2>&1

call :UpdateProgress "Build Complete" "Done!" "All tasks finished" "100"

REM =========================================================================
REM BUILD SUCCESS
REM =========================================================================
echo.
echo ========================================
echo  BUILD COMPLETED SUCCESSFULLY!
echo ========================================
echo.

call :SendFinalSuccess

echo APK Location:
echo   - app\build\outputs\apk\release\app-release.apk
echo   - app\dist\XArchiver-[version].apk
echo.

if exist "app\dist" (
    set /p OPEN_FOLDER="Open dist folder? (Y/N): "
    if /i "!OPEN_FOLDER!"=="Y" start "" "app\dist"
)

echo.
pause
endlocal
exit /b 0

REM =========================================================================
REM TELEGRAM FUNCTIONS (using external PowerShell helper)
REM =========================================================================

:SendTelegramStart
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0

set "TITLE=%~1"
set "STATUS=%~2"
set "PROGRESS=%~3"

set TG_RESPONSE=%TEMP%\tg_msgid.txt

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "start" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -Title "!TITLE!" ^
    -Stage "!STATUS!" ^
    -Progress !PROGRESS! ^
    -Time "%TIME%" ^
    -BuildType "Release" ^
    -ResponseFile "%TG_RESPONSE%"

if exist "%TG_RESPONSE%" (
    set /p TG_MESSAGE_ID=<"%TG_RESPONSE%"
    del "%TG_RESPONSE%" 2>nul
)
exit /b 0

:UpdateProgress
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0
if "!TG_MESSAGE_ID!"=="" exit /b 0

set "STAGE=%~1"
set "STATUS=%~2"
set "DETAIL=%~3"
set "PROGRESS=%~4"

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "update" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -MessageId "!TG_MESSAGE_ID!" ^
    -Stage "!STAGE!" ^
    -Status "!STATUS!" ^
    -Detail "!DETAIL!" ^
    -Progress !PROGRESS! ^
    -Time "%TIME%" ^
    -BuildType "Release"

exit /b 0

:BuildFailed
echo.
echo BUILD FAILED!
echo Error: %~1

if "!TG_BOT_TOKEN!"=="" goto :SkipTGError
if "!TG_CHAT_ID!"=="" goto :SkipTGError

if not "!TG_MESSAGE_ID!"=="" (
    powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
        -Action "failed" ^
        -BotToken "!TG_BOT_TOKEN!" ^
        -ChatId "!TG_CHAT_ID!" ^
        -MessageId "!TG_MESSAGE_ID!" ^
        -ErrorMsg "%~1" ^
        -Time "%TIME%" ^
        -BuildType "Release"
)

if exist "%LOG_FILE%" (
    echo Sending error log to Telegram...
    curl -s -X POST "https://botapi.arasea.dpdns.org/bot!TG_BOT_TOKEN!/sendDocument" ^
        -F "chat_id=!TG_CHAT_ID!" ^
        -F "document=@%LOG_FILE%" ^
        -F "caption=Release build failed. See log for details." ^
        > nul 2>&1
)

:SkipTGError
exit /b 0

:SendFinalSuccess
if "!TG_BOT_TOKEN!"=="" exit /b 0
if "!TG_CHAT_ID!"=="" exit /b 0
if "!TG_MESSAGE_ID!"=="" exit /b 0

powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%telegram_helper.ps1" ^
    -Action "success" ^
    -BotToken "!TG_BOT_TOKEN!" ^
    -ChatId "!TG_CHAT_ID!" ^
    -MessageId "!TG_MESSAGE_ID!" ^
    -Time "%TIME%" ^
    -BuildType "Release"

exit /b 0
