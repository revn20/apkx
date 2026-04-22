# FB SHARE BX

Native Android app that wraps the `vern-rest-api` auto-share endpoint into a clean, modern UI.

## Features
- Animated splash screen (5s)
- Material 3 UI with light/dark mode toggle (persistent)
- Side navigation drawer: Home, Features, Developer, Theme, Check for Updates, About
- Inputs: cookie, post link, share limit
- Calls `https://vern-rest-api.vercel.app/api/share`
- Automatic update check against the latest GitHub Release on launch
- Custom adaptive launcher icon

## Build
APKs are built automatically by GitHub Actions on every push to `main`.
- Workflow file: `.github/workflows/build-apk.yml`
- Latest APKs are attached to the `latest` GitHub Release.

### Manual local build
```bash
chmod +x ./gradlew
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

## Developer
[notfound500](https://www.facebook.com/notfound500)

## Disclaimer
For educational purposes only.
