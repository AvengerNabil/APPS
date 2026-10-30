<div align="center">

<img src="app/src/main/res/drawable/ic_launcher_foreground.xml" width="120" height="120" alt="NMPLAYER Logo"/>

# 🎬 NMPLAYER

### Professional Android Video Player

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen?style=flat-square&logo=android)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-blueviolet?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue?style=flat-square)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue?style=flat-square)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-AvengerNabil-black?style=flat-square&logo=github)](https://github.com/AvengerNabil)

**A feature-rich, professionally crafted Android video player built with Kotlin, ExoPlayer / Media3, MVVM architecture, and Firebase integration.**

[📥 Download APK](#-download--installation) • [✨ Features](#-features) • [🛠️ Tech Stack](#%EF%B8%8F-technology-stack) • [🏗️ Architecture](#%EF%B8%8F-architecture) • [🔧 Build](#-build-instructions)

---

</div>

## 📖 About NMPLAYER

NMPLAYER is a **professional-grade Android video player** designed to deliver a smooth, immersive, and feature-complete media experience directly from your Android device.

Built on top of Google's **ExoPlayer / Media3** engine, NMPLAYER scans your device storage, organises your video library, and plays back virtually any video format with support for gestures, subtitles, playback speed control, Picture-in-Picture, and much more.

Whether you want a quick player for local videos or a full media management hub with favourites, playlists, and cloud-synced preferences via Firebase — NMPLAYER has you covered.

---

## ✨ Features

### 📁 Video Library
| Feature | Status |
|---|:---:|
| Automatic device video scan (internal + external storage) | ✅ |
| Video thumbnails via Glide | ✅ |
| Displays name, duration, file size, resolution | ✅ |
| Sort by name, date, size, duration | ✅ |
| Search videos by name | ✅ |
| Browse by folder | ✅ |
| All popular formats supported via Media3 | ✅ |

### 🎬 Professional Playback
| Feature | Status |
|---|:---:|
| Smooth playback via ExoPlayer / Media3 | ✅ |
| Play / Pause / Resume | ✅ |
| Seek forward & backward | ✅ |
| Adjustable playback speed (0.25× – 2.0×) | ✅ |
| Full-screen & landscape / portrait | ✅ |
| Screen rotation handling | ✅ |
| Gesture-based brightness control | ✅ |
| Gesture-based volume control | ✅ |
| Lock / Unlock playback controls | ✅ |
| Subtitle support (embedded + external .srt) | ✅ |
| Audio track selection | ✅ |
| Aspect ratio: Fit / Fill / Zoom / Stretch | ✅ |
| Picture-in-Picture (PiP) | ✅ |
| Remember last playback position | ✅ |

### 📋 Playlist & History
| Feature | Status |
|---|:---:|
| Recently played videos | ✅ |
| Continue watching | ✅ |
| Playback history | ✅ |
| Favourite videos | ✅ |
| Custom playlists | ✅ |
| Resume from last position | ✅ |
| Remove items from history | ✅ |

### 📂 File Management
| Feature | Status |
|---|:---:|
| Browse videos by folder | ✅ |
| Share video files | ✅ |
| Delete files (with runtime permission) | ✅ |
| Open videos from external file managers | ✅ |

### ☁️ Firebase Integration
| Feature | Status |
|---|:---:|
| Firebase Authentication (Google Sign-In) | ✅ |
| Cloud Firestore for preferences & playlists | ✅ |
| Offline-first — local playback works without login | ✅ |
| Secure user-specific Firestore rules | ✅ |

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | XML Layouts + ViewBinding |
| **Architecture** | MVVM (ViewModel + LiveData) |
| **Video Engine** | Android Media3 / ExoPlayer |
| **Navigation** | Jetpack Navigation Component |
| **Async** | Kotlin Coroutines |
| **Image Loading** | Glide |
| **Backend** | Firebase Authentication + Cloud Firestore |
| **Preferences** | DataStore / SharedPreferences |
| **Build System** | Gradle (Groovy DSL) |
| **Min SDK** | API 24 (Android 7.0 Nougat) |
| **Target SDK** | API 34 (Android 14) |
| **IDE** | Android Studio |

---

## 🏗️ Architecture

NMPLAYER follows the **MVVM (Model-View-ViewModel)** architecture pattern recommended by Google for Android development.

```
┌──────────────────────────────────────────┐
│               UI Layer                   │
│  Fragments + Activities + Adapters       │
└────────────────┬─────────────────────────┘
                 │ observes LiveData
┌────────────────▼─────────────────────────┐
│            ViewModel Layer               │
│  VideoViewModel  │  PlayerViewModel      │
└────────────────┬─────────────────────────┘
                 │ reads/writes
┌────────────────▼─────────────────────────┐
│             Data Layer                   │
│  VideoUtil / FolderUtil (MediaStore)     │
│  PreferencesManager (local)              │
│  FirebaseManager (cloud sync)            │
└──────────────────────────────────────────┘
```

### Project Structure

```
NMPLAYER/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/nmplayer/video/
│   │       │   ├── model/              # Video, Folder data classes
│   │       │   ├── data/               # PreferencesManager
│   │       │   ├── firebase/           # FirebaseManager
│   │       │   ├── util/               # AppUtil, VideoUtil, FolderUtil
│   │       │   └── ui/
│   │       │       ├── MainActivity.kt
│   │       │       ├── fragments/      # PlayerFragment, VideoFragment, FolderFragment
│   │       │       ├── adapter/        # VideoAdapter, FolderAdapter + ViewHolders
│   │       │       └── viewmodel/      # PlayerViewModel, VideoViewModel
│   │       └── res/
│   │           ├── layout/             # XML UI layouts
│   │           ├── drawable/           # Icons + drawables
│   │           ├── values/             # Colors, strings, themes
│   │           └── navigation/         # Nav graph
│   └── build.gradle
├── releases/
│   └── NMPLAYER.apk                    # Signed release APK
├── screenshots/                        # App screenshots
├── docs/                               # Additional documentation
├── README.md
├── LICENSE
└── .gitignore
```

---

## 📥 Download & Installation

### Option 1 – Direct APK (Recommended)

1. Download **[NMPLAYER.apk](releases/NMPLAYER.apk)** from the `releases/` folder.
2. On your Android device, go to **Settings → Security** and enable **"Install from unknown sources"** (or allow installation for your file manager).
3. Open the downloaded APK and tap **Install**.
4. Launch **NMPLAYER** from your app drawer.

> **Note:** NMPLAYER is not yet on the Google Play Store. You must enable unknown sources to side-load the APK.

### Option 2 – Build from Source

See [Build Instructions](#-build-instructions) below.

---

## 🔧 Build Instructions

### Prerequisites

| Tool | Requirement |
|---|---|
| Android Studio | Flamingo or newer |
| JDK | 17 |
| Android SDK | API 34 |
| Gradle | 7.4+ |

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/AvengerNabil/APPS.git
cd APPS/NMPLAYER

# 2. Open in Android Studio
#    File → Open → select the NMPLAYER/ folder

# 3. Sync Gradle (Android Studio does this automatically)
./gradlew build

# 4. Build a debug APK
./gradlew assembleDebug

# 5. Build a release APK (requires signing config)
./gradlew assembleRelease
```

> **Firebase Setup:** If you want Firebase features (authentication, cloud sync), add your own `google-services.json` to `app/`. Without it, the app works fully in offline-only mode.

---

## 📱 Compatibility

| Android Version | API Level | Support |
|---|:---:|:---:|
| Android 7.0 Nougat | 24 | ✅ |
| Android 8.0 Oreo | 26 | ✅ |
| Android 9.0 Pie | 28 | ✅ |
| Android 10 | 29 | ✅ |
| Android 11 | 30 | ✅ |
| Android 12 | 31 | ✅ |
| Android 13 | 33 | ✅ |
| Android 14 | 34 | ✅ |

---

## 🔐 Permissions

NMPLAYER requests only the permissions it genuinely needs:

| Permission | Purpose |
|---|---|
| `READ_EXTERNAL_STORAGE` | Scan and display videos from device storage |
| `READ_MEDIA_VIDEO` (Android 13+) | Access video files on Android 13+ |
| `WRITE_EXTERNAL_STORAGE` | Delete video files (legacy, Android ≤ 9) |
| `MANAGE_MEDIA` | Manage media files via MediaStore (Android 11+) |
| `INTERNET` | Firebase authentication and cloud sync |
| `FOREGROUND_SERVICE` | Background media playback notification |
| `PICTURE_IN_PICTURE` | PiP mode support |

> No permissions are requested unless actively needed. Video playback works entirely offline without `INTERNET`.

---

## 🚀 Roadmap & Future Improvements

- [ ] Google Play Store release
- [ ] Chromecast support
- [ ] Dark / Light / AMOLED theme options in Settings UI
- [ ] Online streaming support
- [ ] Widget for playback control
- [ ] Equalizer and audio effects
- [ ] Cloud backup of playlists and favourites
- [ ] Tablet / large-screen optimised layout
- [ ] Multi-language (i18n) support

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

> NMPLAYER is based on and inspired by [KVPlayer](https://github.com/KaushalVasava/KVPlayer) by Kaushal Vasava (Apache 2.0). It has been significantly extended, rebranded, and customised.

---

## 👨‍💻 Developer

<div align="center">

**Md. Nabil**

[![GitHub](https://img.shields.io/badge/GitHub-AvengerNabil-black?style=flat-square&logo=github)](https://github.com/AvengerNabil)

*7th Semester — Mobile Application Development*

Built with ❤️ using Kotlin & Android

</div>

---

<div align="center">

⭐ **Star this repository if you found it useful!** ⭐

[Back to top](#-nmplayer)

</div>
