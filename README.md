<div align="center">

# SANDY

### Real-Time AI Sitting Posture Detection & Spine Wellness App

[![Release](https://img.shields.io/badge/Version-ver%20Alpha-00F0FF?style=for-the-badge)](https://github.com/QUINTEX-productio/sandy-app/releases/latest)
[![Windows](https://img.shields.io/badge/Windows-10%20%2F%2011%20x64-0078D6?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Setup-Alpha-x64.exe)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20APK-3DDC84?style=for-the-badge&logo=android&logoColor=black)](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.apk)
[![Linux](https://img.shields.io/badge/Linux-AppImage%20%2F%20deb-FCC624?style=for-the-badge&logo=linux&logoColor=black)](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.AppImage)
[![License](https://img.shields.io/badge/License-MIT-FF007F?style=for-the-badge)](LICENSE)

**Sandy** monitors your sitting posture in real time using **YOLOv5** vision detection, pairs directly with Bluetooth **ESP32-S3** sensors, records your posture history & media, and delivers personalized ergonomic coaching powered by **Qwen3.6-35B**.

[**Download Latest Release**](https://github.com/QUINTEX-productio/sandy-app/releases/latest) • [**Official Website (macOS & iOS Web App)**](https://quintex-productio.github.io/sandy.io/) • [**Report an Issue**](https://github.com/QUINTEX-productio/sandy-app/issues)

</div>

---

## Downloads

Click your operating system below to start downloading immediately:

| Platform | Package | Requirements | Direct Download |
| :--- | :--- | :--- | :--- |
| **Windows** | `Sandy-Setup-Alpha-x64.exe` | Windows 10 / 11 (64-bit) | [**Download for Windows (.exe)**](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Setup-Alpha-x64.exe) |
| **Android** | `Sandy-Alpha.apk` | Android 8.0 (API 26) or newer | [**Download for Android (.apk)**](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.apk) |
| **Linux** | `Sandy-Alpha.AppImage` | Ubuntu / Debian / Arch (x64) | [**Download for Linux (.AppImage)**](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.AppImage) |
| **macOS / iOS** | Web App (No Install) | Safari / Chrome on Apple devices | [**Open Sandy Web App**](https://quintex-productio.github.io/sandy.io/) |

> **Note:** On **macOS and iOS**, Sandy runs directly inside your browser on the [official website](https://quintex-productio.github.io/sandy.io/) with zero installation required. On **Windows, Android, and Linux**, use the native apps in this repository.

---

## Key Features

- **Clean Onboarding, Personalization Survey & Interactive Tutorial**
  - Sign in or register with Email (synced with Firebase) or Google / Gmail.
  - Complete the 3-step personalization survey (Medical Background, Daily Sitting Routine, Primary Posture Goal) followed by an interactive 7-step tutorial introducing every feature.
- **Smooth Scroll-to-Circle Navigation & Home Menu**
  - Left vertical icon rail (**Menu**, **Devices**, **Interface**, **AI Chatbot**, **Report**, **Settings**) with a glowing animated circle indicator that glides to your selection while scrolling smoothly to that workspace.
  - Top-right circular profile bar with quick access to **User Profile** and **Sign Out**.
- **Bluetooth Hardware & Device Manager**
  - Pair **ESP32-S3 / PostureAI** Bluetooth devices using the red **Add Device** button (with built-in **How to Add Device** guide).
  - Sideways beveled device list displaying **Device Name**, **Model**, and live **Connection Status** (`Pairing`, `Connected`, `Disconnected`), plus a top-left `⋮` menu for **Preferences** (model type, manufacture date, firmware, hardware ID), **Rename**, and **Delete**.
- **Distraction-Free Live Interface**
  - Shows only what matters in front: live **Camera**, bold **GOOD / BAD** live posture reading, centered circular start/stop icon, and **Choose device** Bluetooth dropdown.
  - Auto-wakes the Colab **YOLOv5** backend, triggers gentle 740 Hz slouch alerts, syncs daily telemetry, and records posture images & clips silently in the backend.
- **Sandy AI Chatbot (`Qwen3.6-35B`)**
  - Automatically wakes the Colab Qwen GPU backend and enforces a strict posture, ergonomics, and spine-wellness topic boundary.
- **Posture Reports & Recorded Media (`YOLOv5 → Qwen3.6 → Report`)**
  - Switch between **Daily**, **Weekly**, **Monthly**, and **Yearly** boxes.
  - Automatically detects days lacking data and compares your current score against your **Day-1 baseline** so you always know how your posture is trending.
  - Browse, play, and manage automatically recorded posture images and clips.
- **Settings (`ver Alpha`)**
  - Switch Cyberpunk accent themes (**Cyber Cyan**, **Neon Magenta**, **Solar Gold**, **Emerald Pulse**), configure slouch alert delay, toggle auto-recording & Colab auto-wake, and replay the app tutorial anytime.

---

## Installation Guide

### Windows (`.exe`)
1. Download [`Sandy-Setup-Alpha-x64.exe`](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Setup-Alpha-x64.exe).
2. Double-click the installer to run Setup.
   - *If Windows SmartScreen shows **"Windows protected your PC"*** (normal for unsigned Alpha builds): click **More info** → **Run anyway**.
3. Choose your install directory, keep Desktop & Start Menu shortcuts checked, and launch **Sandy**.

### Android (`.apk`)
1. Download [`Sandy-Alpha.apk`](https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.apk) on your Android phone.
2. Open the downloaded `.apk` file.
3. If prompted, enable **Install unknown apps** for your browser or Files app, then tap **Install**.
4. Grant **Bluetooth / Nearby Devices** permission when pairing an ESP32-S3 posture sensor.

### Linux (`.AppImage`)
```bash
chmod +x Sandy-Alpha.AppImage
./Sandy-Alpha.AppImage
```

---

## Pairing a Posture Detection Device

1. Power on your **Sandy / PostureAI ESP32-S3** hardware via USB-C until the status LED blinks cyan (BLE Pairing Mode).
2. Enable **Bluetooth** on your Windows PC or Android phone.
3. Open Sandy → go to the **Devices** tab → click the red **Add Device** button.
4. Pair your device. Once status turns **Connected**, open the **Interface** tab, select your device under **Choose device**, and press the center circle icon to begin live monitoring.

---

## Build from Source

### Repository Structure
```text
sandy-app/
├── windows-app/          # Electron Desktop App (Windows x64 & Linux)
├── android-app/          # Native Jetpack Compose Android App (Kotlin)
├── installers/           # Local built installers ready for GitHub Releases
├── README.md             # Project documentation & download guide
├── RELEASE_NOTES_ALPHA.md
└── LICENSE
```

### Build Windows App (Electron)
Requirements: **Node.js 20+**
```powershell
cd windows-app
npm install
# Run in development
npm start
# Build Windows NSIS Setup Installer (.exe)
npm run build:win
```
Output installer is written to `windows-app/dist/Sandy-Setup-Alpha-x64.exe`.

### Build Android App (Jetpack Compose / Gradle)
Requirements: **JDK 17+** & **Android SDK**
```powershell
cd android-app
.\gradlew.bat assembleDebug
```
Output APK is written to `android-app/app/build/outputs/apk/debug/app-debug.apk`.

---

## Changelog

### `ver Alpha` (`v0.1.0-alpha`)
- Initial Alpha release for **Windows (x64 NSIS Installer)** and **Android (APK)**.
- Added First-Launch Splash, Firebase Email + Google Auth, 3-Step Personalization Survey, and 7-Step Interactive Tutorial.
- Added Left Sidebar Rail with animated scroll-to-circle navigation and Top-Right Profile/Sign Out bar.
- Added Bluetooth Devices Manager (sideways beveled list, top-left `⋮` menu, red Add Device button, How to Add Device guide).
- Added Minimal Live Interface with background YOLOv5 auto-wake, slouch chime, and automatic media recording.
- Added Sandy AI Chatbot (`Qwen3.6-35B`) with strict posture-only guardrail and YOLO → Qwen → Report analytics with Day-1 comparison.

---

## Privacy & Medical Disclaimer

- **Privacy:** Posture recordings and device history are stored locally on your device. User profile and daily summary scores sync securely to your personal Firebase account (`posturedection`).
- **Medical Notice:** Sandy is an ergonomic wellness tool and does not diagnose, treat, or cure medical conditions. Always consult a licensed physician or physical therapist for persistent or severe back/neck pain.

---

<div align="center">
Made by <strong>QUINTEX Production</strong>
</div>
