# XArchiver

XArchiver Improved Version
This is an enhanced version of the XArchiver file manager, specifically optimized for stability when building on macOS and updated with personalized developer branding.

🚀 Key Improvements & Fixes
1. macOS Metadata Conflict Resolution

Problem: Fixed the "Android resource linking failed" error caused by macOS metadata files (._) being treated as resources during the build process.

Solution: Configured androidResources.ignoreAssetsPattern in app/build.gradle.kts to skip these hidden files.

Automation: Added a custom Gradle cleanup task that automatically deletes any ._* files in the build directory before resources are processed.

2. UI & Branding Updates

About Screen: Updated the "About" section to credit the current developer: Improved by adnan @membuahiiii (jov3).

Thumbnail Support: Enhanced APK thumbnail rendering to ensure application icons are displayed correctly in the file list.

🛠️ Build & Signing Instructions
To generate a valid, signed release APK, use the following terminal command from the project root:

Bash
./gradlew assembleRelease \
  -PmyKeystorePath="/Users/macos/Desktop/adnanjov.jks" \
  -PmyKeystorePassword="your_password" \
  -PmyKeyAlias="jov3" \
  -PmyKeyPassword="your_password"
Important Notes:

Varian: Always select the release variant to avoid "Invalid Package" errors on Android devices.

Signature Conflict: You must uninstall any previous versions of XArchiver from your device before installing this version due to the new signing key (adnanjov.jks).

📂 Output Locations
Signed APK: app/build/outputs/apk/release/app-release.apk.

Metadata: app/build/outputs/apk/release/output-metadata.json.

                                                  Developer Original Special Thanks 
                                                                                        Gusti Aditya Muzaky https://github.com/Gustyx-Power/XArchiver.git

Improve with ❤️ by adnan @membuahiiii
- Android device (API level TBD)
- [Android Studio](https://developer.android.com/studio) (recommended for building)


## Contributing
Contributions are welcome! Please open issues or pull requests for bugs, features, or suggestions.

## License
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Disclaimer
This is an improve version. Use at your own risk. Some features may not work as expected.
