import org.apache.http.client.methods.HttpPost
import org.apache.http.client.config.RequestConfig
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.util.Date
import java.text.SimpleDateFormat
import java.lang.management.ManagementFactory
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import kotlin.text.substringAfterLast

val buildDate = SimpleDateFormat("yyyyMMdd").format(Date())

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    namespace = "id.xms.xarchiver"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.xms.xarchiver"
        minSdk = 29
        targetSdk = 36
        versionCode = 7
        versionName = "2.1.9-$buildDate"
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            storeFile = project.findProperty("myKeystorePath")?.let { file(it) }
            storePassword = project.findProperty("myKeystorePassword") as String?
            keyAlias = project.findProperty("myKeyAlias") as String?
            keyPassword = project.findProperty("myKeyPassword") as String?
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        lint.disable.add("NullSafeMutableLiveData")
    }


        buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
        jvmTarget = "17"
    }

    packaging {
        resources.excludes.add("META-INF/DEPENDENCIES")
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("../native/jniLibs")
        }
    }
}

dependencies {
    implementation(libs.miuix.ui)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)
    implementation("androidx.navigationevent:navigationevent-android:1.1.2")
    implementation("androidx.navigationevent:navigationevent-compose-android:1.1.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(project(":core-storage"))
    implementation(project(":core-archive"))
    implementation(project(":core-extract"))
    implementation(project(":core-root"))
    implementation(libs.androidx.foundation)
    implementation(libs.material)
    implementation(libs.litert)
    implementation(libs.material3)
    implementation(libs.io)
    implementation(libs.androidx.uiautomator.shell)

    // Apache Commons Compress for archive handling (needed for UI classes too)
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("commons-io:commons-io:2.15.1")
    
    // Zip4j for encrypted ZIP archives (AES-256 & ZipCrypto)
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    
    // XZ compression for payload.bin
    implementation("org.tukaani:xz:1.9")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3:1.5.0-alpha03")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Activity + Lifecycle
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Navigation (Compose)
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // DataStore for theme preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Testing
    testImplementation(libs.junit)

    // Okio
    implementation("com.squareup.okio:okio:3.9.0")
    // Add DocumentFile support
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // ExoPlayer for video/audio playback
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    
    // Coil for image and video preview
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")
    
    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}

tasks.register("sendTelegramMessage", SendTelegramMessageTask::class) {
    group = "custom"
    description = "Sends a build status message to Telegram."
    // Properties will be set via convention mapping or direct assignment
}

abstract class SendTelegramMessageTask : DefaultTask() {

    @get:Input
    abstract val telegramBotToken: Property<String>

    @get:Input
    abstract val telegramChatId: Property<String>

    @get:Input
    abstract val appVersionName: Property<String>

    @get:Input
    abstract val appPackageName: Property<String>

    @get:Input
    abstract val appProjectName: Property<String>

    @get:Input
    @get:Optional
    abstract val changelog: Property<String>



    init {
        // Initialize from project properties
        telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
        telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
        // These will be configured by the invoking task or a general build listener
        appVersionName.convention("")
        appPackageName.convention("")
        appProjectName.convention(project.name) // Default to current project name
        changelog.convention(project.findProperty("myChangelog")?.toString() ?: "")

    }

    @TaskAction
    fun sendMessage() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram Bot Token or Chat ID not found in gradle.properties. Skipping message.")
            return
        }

        val buildStatus = if (project.gradle.taskGraph.allTasks.any { it.state.failure != null }) {
            "FAILED"
        } else {
            "SUCCESS"
        }

        val currentAppVersion = appVersionName.getOrElse(project.android.defaultConfig.versionName ?: "N/A")
        val currentAppPackage = appPackageName.getOrElse(project.android.defaultConfig.applicationId ?: "N/A")
        val currentProjectName = appProjectName.get()

        val kotlinVersion = project.getKotlinPluginVersion() ?: "N/A"
        // --- Progress message for build ---
        fun sendTelegramMessage(text: String, disableNotification: Boolean = false): Int? {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"text\": \"${text.replace("\"", "\\\"")}\",\n  \"disable_notification\": $disableNotification\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                EntityUtils.consumeQuietly(response.entity)
                val idRegex = "\\\"message_id\\\":(\\d+)".toRegex()
                return idRegex.find(responseBody)?.groupValues?.get(1)?.toIntOrNull()
            }
        }
        fun editTelegramMessage(messageId: Int, text: String) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/editMessageText"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"message_id\": $messageId,\n  \"text\": \"${text.replace("\"", "\\\"")}\"\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                EntityUtils.consumeQuietly(response.entity)
            }
        }
        fun pinTelegramMessage(messageId: Int) {
            val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/pinChatMessage"
            val jsonPayload = """
            {\n  \"chat_id\": \"${telegramChatId.get()}\",\n  \"message_id\": $messageId,\n  \"disable_notification\": true\n}\n""".trimIndent()
            HttpClients.createDefault().use { httpClient ->
                val post = HttpPost(url)
                post.entity = StringEntity(jsonPayload, "UTF-8")
                post.setHeader("Content-Type", "application/json")
                val response = httpClient.execute(post)
                EntityUtils.consumeQuietly(response.entity)
            }
        }

        val buildMsgId = sendTelegramMessage("Processing build...", disableNotification = true)
        if (buildMsgId != null) pinTelegramMessage(buildMsgId)

        val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
        val javaVersion = JavaVersion.current().toString()
        val gradleVersion = project.gradle.gradleVersion
        val osName = if (isWindows) {
            System.getProperty("os.name")
        } else {
            try {
                val process = ProcessBuilder("cat", "/etc/os-release")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                val prettyNameLine = output.lines().find { it.startsWith("NAME=") }
                prettyNameLine?.substringAfter("=")?.removeSurrounding("\"") ?: System.getProperty("os.name")
            } catch (e: Exception) {
                logger.warn("Could not read /etc/os-release: ${e.message}")
                System.getProperty("os.name") // Fallback
            }
        }
        val osArch = System.getProperty("os.arch")
        val processor = if (isWindows) {
            System.getenv("PROCESSOR_IDENTIFIER") ?: osArch
        } else {
            try {
                val process = ProcessBuilder("cat", "/proc/cpuinfo")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                output.lines().find { it.startsWith("model name") }?.substringAfter(":")?.trim() ?: osArch
            } catch (e: Exception) {
                logger.warn("Could not read /proc/cpuinfo: ${e.message}")
                osArch // Fallback
            }
        }

        // SDK Info
        val compileSdkVersion = project.android.compileSdk ?: "N/A"
        val minSdkVersion = project.android.defaultConfig.minSdk ?: "N/A"
        val targetSdkVersionInt = project.android.defaultConfig.targetSdk
        val targetSdkVersionName = when (targetSdkVersionInt) {
            29 -> "10 [QuinceTart]" // Android 10
            30 -> "11 [RedVelvet]" // Android 11
            31 -> "12 [Snowcone]" // Android 12
            32 -> "12L [Snowcone V2]" // Android 12L
            33 -> "13 [Tiramisu]" // Android 13
            34 -> "14 [UpsideDownCake]" // Android 14
            35 -> "15 [VanillaIceCream]" // Android 15
            36 -> "16 [Baklava]" // Android 16
            else -> "Unknown"
        }
        val minSdkCodename = when (minSdkVersion) {
            // Assuming minSdkVersion is an Int or can be converted to one for this when
            29 -> "10 [QuinceTart]" // Android 10
            30 -> "11 [RedVelvet]" // Android 11
            31 -> "12 [Snowcone]" // Android 12
            32 -> "12L [Snowcone V2]" // Android 12L
            33 -> "13 [Tiramisu]" // Android 13
            34 -> "14 [UpsideDownCake]" // Android 14
            35 -> "15 [VanillaIceCream]" // Android 15
            36 -> "16 [Baklava]" // Android 16
            else -> "Unknown"
        }

        val (totalRamGb, freeRamGb, usedRamGb) = if (isWindows) {
            try {
                val osBean = ManagementFactory.getOperatingSystemMXBean()
                if (osBean is com.sun.management.OperatingSystemMXBean) {
                    val totalBytes = osBean.totalMemorySize
                    val freeBytes = osBean.freeMemorySize
                    val usedBytes = totalBytes - freeBytes
                    val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
                    val freeGb = freeBytes / (1024.0 * 1024.0 * 1024.0)
                    val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
                    Triple(String.format("%.2f GB", totalGb), String.format("%.2f GB", freeGb), String.format("%.2f GB", usedGb))
                } else {
                    Triple("N/A", "N/A", "N/A")
                }
            } catch (e: Exception) {
                Triple("N/A", "N/A", "N/A")
            }
        } else {
            try {
                val process = ProcessBuilder("cat", "/proc/meminfo")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText()
                process.waitFor()
                val lines = output.lines()
                val memTotalKb = lines.find { it.startsWith("MemTotal:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: 0L
                val memFreeKb = lines.find { it.startsWith("MemFree:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: 0L
                // MemAvailable is generally a better indicator of free memory
                val memAvailableKb = lines.find { it.startsWith("MemAvailable:") }?.substringAfter(":")?.trim()?.removeSuffix(" kB")?.toLongOrNull() ?: memFreeKb

                val totalGb = memTotalKb / (1024.0 * 1024.0)
                val freeGb = memAvailableKb / (1024.0 * 1024.0)
                val usedGb = totalGb - freeGb

                Triple(String.format("%.2f GB", totalGb), String.format("%.2f GB", freeGb), String.format("%.2f GB", usedGb))

            } catch (e: Exception) {
                logger.warn("Could not read /proc/meminfo: ${e.message}")
                Triple("N/A", "N/A", "N/A") // Fallback
            }
        }

        val (totalStorageGb, freeStorageGb) = try {
            val rootFile = project.rootDir
            val total = String.format("%.2f GB", rootFile.totalSpace / (1024.0 * 1024.0 * 1024.0))
            val free = String.format("%.2f GB", rootFile.usableSpace / (1024.0 * 1024.0 * 1024.0))
            Pair(total, free)
        } catch (e: Exception) {
            Pair("N/A", "N/A")
        }

        val kernelInfo = if (isWindows) {
            "Windows NT ${System.getProperty("os.version")}"
        } else {
            try {
                val process = ProcessBuilder("uname", "-r")
                    .redirectErrorStream(true)
                    .start()
                val output = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                output.ifEmpty { "N/A" }
            } catch (e: Exception) {
                logger.warn("Could not read kernel info: ${e.message}")
                "N/A" // Fallback
            }
        }

        val buildChangelog = changelog.getOrElse("")

        var message = "[Build Status] ${project.name} - $buildStatus* 🚀\n\n" +
                "📦 App: ${currentProjectName}\n" +
                "🏷️ Version: ${currentAppVersion}\n" +
                "🆔 Package: ${currentAppPackage}\n" +
                "📅 Time: ${Date().toString()}\n\n" +
                "[Build Environment]\n" +
                "  OS: $osName ($osArch)\n" +
                "  Kernel: $kernelInfo\n" +
                "  Processor:\n $processor\n" +
                "  RAM:\n Total: $totalRamGb\n Free: $freeRamGb\n Used: $usedRamGb\n" +
                "  Storage:\n Total: $totalStorageGb\n Free: $freeStorageGb\n" +
                "  Android Studio: Narwhal Feature Drop\n" +
                "  Kotlin: $kotlinVersion\n" +
                "  Java: $javaVersion\n" +
                "  Gradle (Kotlin DSL): $gradleVersion\n\n" +
                "[App SDK Information]\n" +
                "  Min SDK: $minSdkVersion (Android $minSdkCodename)\n" +
                "  Target SDK: $targetSdkVersionInt (Android $targetSdkVersionName)\n"


        if (buildChangelog.isNotBlank()) {
            message += "\nChangelog:\n$buildChangelog\n"
        }

        if (buildStatus == "FAILED") {
            val failedTasks = project.gradle.taskGraph.allTasks.filter { it.state.failure != null }
            if (failedTasks.isNotEmpty()) {
                val errorDetails = failedTasks.joinToString(separator = "\n") { task ->
                    val taskName = task.path
                    val errorMessage =
                        task.state.failure?.message?.lines()?.firstOrNull()?.trim() ?: "No specific error message"
                    "  - Task `$taskName` failed: $errorMessage"
                }
                message += "\n\n⚠️ Error Details:\n$errorDetails"
            } else {
                message += "\n\n⚠️ Build failed. Check build logs for details."
            }
        }


        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendMessage"
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(15 * 1000)
            .setSocketTimeout(30 * 1000)
            .build()



        if (buildMsgId != null) {
            editTelegramMessage(buildMsgId, if (buildStatus == "SUCCESS") "✅ Build finished successfully!" else "❌ Build failed!")
        }

        HttpClients.custom().setDefaultRequestConfig(requestConfig).build().use { httpClient ->
            val post = HttpPost(url)
            val jsonPayload = """
            {
                "chat_id": "${telegramChatId.get().replace("\"", "\\\"")}",
                "text": "${message.replace("\"", "\\\"")}"
            }
            """.trimIndent()
            post.entity = StringEntity(jsonPayload, "UTF-8")
            post.setHeader("Content-Type", "application/json")

            try {
                logger.info("Sending build status message to Telegram...")
                logger.info("Payload: $jsonPayload") // Tambahkan log untuk payload
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    logger.lifecycle("Successfully sent message to Telegram. Response: $responseBody")
                } else {
                    logger.error("Failed to send message to Telegram. Status: ${response.statusLine}, Body: $responseBody. Payload: $jsonPayload")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                logger.error("Failed to send message to Telegram: ${e.message}", e)
            }
        }
    }
}

abstract class UploadApkToTelegramTask : DefaultTask() {
    @get:Input abstract val telegramBotToken: Property<String>

    @get:Input
    abstract val telegramChatId: Property<String>

    @get:InputFile
    abstract val apkFile: RegularFileProperty

    @get:Input
    abstract val appVersionName: Property<String>

    @get:Input
    abstract val appName: Property<String>

    @TaskAction
    fun uploadApk() {
        if (telegramBotToken.get().isEmpty() || telegramChatId.get().isEmpty()) {
            logger.warn("Telegram Bot Token or Chat ID not found. Skipping APK upload.")
            return
        }

        val currentApkFile = apkFile.get().asFile
        if (!currentApkFile.exists()) {
            project.logger.error("Release APK not found at ${currentApkFile.absolutePath}. Ensure assembleRelease has run.")
            return
        }

        val fileSizeMb = currentApkFile.length() / (1024.0 * 1024.0)
        logger.lifecycle("Attempting to upload APK: ${currentApkFile.name} (Size: ${"%.2f".format(fileSizeMb)} MB)")

        // Telegram Bot API limit can be up to 2GB for bots, but typically 50MB for general use without special setup.
        if (fileSizeMb > 199) { // Check slightly less than 200MB to be safe
            project.logger.error("APK size (${"%.2f".format(fileSizeMb)} MB) exceeds Telegram's typical 200MB limit. Skipping upload.")
            // Consider how to trigger SendTelegramMessageTask if needed, as direct task execution from another is not standard.
            // One option is to ensure SendTelegramMessageTask runs via task dependencies or lifecycle hooks.
            return
        }

        // Hilangkan // jika sudah ingin release
        // val caption = "📦 New Release build: ${appName.get()} v${appVersionName.get()}\n" +
        //"Build time: ${Date()}\n" +
        // "File: ${currentApkFile.name} (${"%.2f".format(fileSizeMb)} MB)"

        // Tambahkan // jika sudah tidak ingin menggunakan test release
        val caption = "📦 New Test Release build: ${appName.get()} v${appVersionName.get()}\n" +
                "Build time: ${Date()}\n" +
                "File: ${currentApkFile.name} (${"%.2f".format(fileSizeMb)} MB)"

        val url = "https://botapi.arasea.dpdns.org/bot${telegramBotToken.get()}/sendDocument"
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(30 * 1000)
            .setSocketTimeout(5 * 60 * 1000)
            .build()

        HttpClients.custom().setDefaultRequestConfig(requestConfig).build().use { httpClient ->
            val post = HttpPost(url)
            val entityBuilder = MultipartEntityBuilder.create()
            entityBuilder.addTextBody("chat_id", telegramChatId.get())
            entityBuilder.addTextBody("caption", caption, org.apache.http.entity.ContentType.TEXT_PLAIN.withCharset("UTF-8"))
            entityBuilder.addPart("document", FileBody(currentApkFile))
            post.entity = entityBuilder.build()

            try {
                project.logger.info("Uploading APK to Telegram...")
                val response = httpClient.execute(post)
                val responseBody = EntityUtils.toString(response.entity, "UTF-8")
                if (response.statusLine.statusCode in 200..299) {
                    project.logger.lifecycle("Successfully uploaded APK to Telegram. Response: $responseBody")
                } else if (response.statusLine.statusCode == 400 && responseBody.contains("can't parse entities")) {
                    project.logger.error("Failed to upload APK to Telegram due to Markdown parsing error. Status: ${response.statusLine}, Body: $responseBody. Caption was: $caption")
                } else {
                    project.logger.error("Failed to upload APK to Telegram. Status: ${response.statusLine}, Body: $responseBody")
                }
                EntityUtils.consumeQuietly(response.entity)
            } catch (e: Exception) {
                project.logger.error("Failed to upload APK to Telegram: ${e.message}", e)
                e.printStackTrace() // logger.error with exception will print stack trace with --stacktrace
            }
        }
    }
}


// Task to rename/copy app-release.apk to XArchiver-versionName.apk after assembleRelease
// 1. Task untuk me-rename/menyalin app-release.apk
val renameReleaseApk by tasks.registering(Copy::class) {
    group = "custom"
    description = "Renames/copies app-release.apk to a custom name."

    val versionName = android.defaultConfig.versionName ?: "unknown-version"

    // Sumber file APK dari direktori build
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
    }

    // Direktori tujuan (misalnya 'dist' di root proyek)
    into(layout.projectDirectory.dir("dist"))

    // Mengganti nama file
    rename { "XArchiver-$versionName.apk" }
}

// 2. Task untuk mengunggah APK yang sudah diganti namanya
val uploadReleaseApkToTelegram by tasks.registering(UploadApkToTelegramTask::class) {
    group = "custom"
    description = "Uploads the renamed release APK to Telegram."

    // --- INI BAGIAN UTAMA YANG DIPERBAIKI ---
    // Daripada mencoba menghubungkan output task secara dinamis,
    // kita secara eksplisit menunjuk ke LOKASI FILE yang kita tahu akan dibuat.
    val versionName = android.defaultConfig.versionName ?: "unknown-version"
    val outputApkPath = layout.projectDirectory.file("dist/XArchiver-$versionName.apk")

    // .set() sekarang menerima referensi file yang valid dan "lazy".
    apkFile.set(outputApkPath)
    // -----------------------------------------

    // Konfigurasi lainnya tetap sama
    telegramBotToken.convention(project.findProperty("telegramBotToken")?.toString() ?: "")
    telegramChatId.convention(project.findProperty("telegramChatId")?.toString() ?: "")
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appName.convention(project.name)

    // SANGAT PENTING: Pastikan task ini baru berjalan SETELAH rename selesai.
    mustRunAfter(renameReleaseApk)
}

// 3. Task untuk mengirim notifikasi status build
val notifyBuildStatusToTelegram by tasks.registering(SendTelegramMessageTask::class) {
    group = "custom"
    description = "Sends the final build status to Telegram."

    // Konfigurasi properti
    appVersionName.convention(project.provider { android.defaultConfig.versionName ?: "N/A" })
    appPackageName.convention(project.provider { android.defaultConfig.applicationId ?: "N/A" })
    appProjectName.convention(project.provider { android.namespace?.substringAfterLast('.') ?: project.name })
}

// 4. Task Utama untuk menggabungkan semua langkah menjadi satu pipeline
tasks.register("buildAndPublish") {
    group = "custom"
    description = "Builds, renames, uploads the APK, and sends a notification."

    // Menetapkan urutan eksekusi yang BENAR.
    // Jika salah satu gagal, yang berikutnya tidak akan berjalan.
    dependsOn(tasks.named("assembleRelease"))

    // Rantai tugas dengan `mustRunAfter` untuk memastikan urutan yang ketat
    renameReleaseApk.get().mustRunAfter(tasks.named("assembleRelease"))
    uploadReleaseApkToTelegram.get().mustRunAfter(renameReleaseApk)
    notifyBuildStatusToTelegram.get().mustRunAfter(uploadReleaseApkToTelegram)

    // Menjadikan semua task ini sebagai dependensi dari buildAndPublish
    finalizedBy(
        renameReleaseApk,
        uploadReleaseApkToTelegram,
        notifyBuildStatusToTelegram
    )
}
