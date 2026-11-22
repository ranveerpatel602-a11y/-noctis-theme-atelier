# Noctis Theme Atelier — Android app scaffold

This native Android version currently offers seven theme cards and a real **Apply Wallpaper** action that sets a generated 1080×2160 wallpaper on the Home screen using Android's WallpaperManager.

## Build APK
The GitHub Actions workflow builds a debug APK and uploads it as an Actions artifact:
- Open the repository's Actions tab.
- Select **Build Android APK**.
- Run workflow, then download the artifact after the build succeeds.

## Current limitations
- Wallpaper apply is native and real (Home screen).
- This scaffold does not yet replace the system icon pack or lock-screen wallpaper in one tap; Android/OEM launcher permissions and supported APIs vary.
- No store/payment/checkout has been included. Any selling or payment setup must be managed by a parent/guardian.
