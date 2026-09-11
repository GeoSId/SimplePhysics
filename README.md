# ⚛️ SimplePhysics (Kotlin Multiplatform)

This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### 🌐 Multi-Language Support

SimplePhysics is fully localized and supports multiple languages with in-app instant language switching:

| Language | Native Name | Code | Flag |
| :--- | :--- | :---: | :---: |
| **English** | English | `en` | 🇬🇧 |
| **German** | Deutsch | `de` | 🇩🇪 |
| **Greek** | Ελληνικά | `el` | 🇬🇷 |
| **Spanish** | Español | `es` | 🇪🇸 |
| **French** | Français | `fr` | 🇫🇷 |
| **Italian** | Italiano | `it` | 🇮🇹 |
| **Portuguese** | Português | `pt` | 🇵🇹 |

### Running the apps

#### In Android Studio / IntelliJ IDEA:
- **Desktop app**: Select **`desktopApp`** from the run configuration dropdown at the top toolbar and click the green **Play (Run)** button. (Alternatively, run directly from [main.kt](desktopApp/src/main/kotlin/com/geosid/simplephysics/main.kt) or from the Gradle tab under `desktopApp -> Tasks -> compose desktop -> run`).
- **Android app**: Select **`androidApp`** from the run configuration dropdown and choose your emulator/device.

#### Via Terminal / Command Line:
- Desktop app (Standard): `./gradlew :desktopApp:run` (or `gradlew.bat :desktopApp:run` on Windows)
- Desktop app (Hot reload): `./gradlew :desktopApp:hotRun --auto`
- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests 

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Desktop tests: `./gradlew :shared:jvmTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

### 🎬 Video Generation & Social Media Automation

SimplePhysics includes an automated pipeline for generating YouTube Shorts (9:16 vertical) and standard YouTube videos (16:9 landscape) with realistic IDE code-typing, physics formula cards, voiceovers, and relax background music:

- See the **[Complete Video Automation Guide](scripts/README.md)** for full documentation, track selection, and copy-paste commands.
- Background relax music guide: **[scripts/audio/music/README.md](scripts/audio/music/README.md)**.

![Screenshot from 2026-09-06 22-40-20.png](Screenshot%20from%202026-09-06%2022-40-20.png)