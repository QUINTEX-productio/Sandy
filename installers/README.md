# Installers (Ready to Upload to GitHub Releases)

GitHub regular commits reject files over 100 MB, so `.exe` / `.AppImage` installers are kept out of Git commits (see `.gitignore`) and uploaded to **GitHub Releases** instead.

## How to Publish One-Click Downloads on GitHub
1. Create the repository `https://github.com/QUINTEX-productio/sandy-app` and push this folder.
2. Open `https://github.com/QUINTEX-productio/sandy-app/releases/new`.
3. Set Tag: **`v0.1.0-alpha`** and Title: **`Sandy ver Alpha`**.
4. Paste the contents of `../RELEASE_NOTES_ALPHA.md` into the release description.
5. Drag and drop the installer files from this folder into the **Attach binaries** box:
   - `Sandy-Setup-Alpha-x64.exe` (Windows x64 NSIS Installer)
   - `Sandy-Alpha.apk` (Android APK)
   - `Sandy-Alpha.AppImage` (Linux, once built)
6. Click **Publish release**.

Your website `download.html` buttons will immediately trigger one-click browser downloads from:
- `https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Setup-Alpha-x64.exe`
- `https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.apk`
- `https://github.com/QUINTEX-productio/sandy-app/releases/latest/download/Sandy-Alpha.AppImage`
