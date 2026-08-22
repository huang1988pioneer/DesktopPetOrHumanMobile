# DesktopPetOrHumanMobile

Android mobile app for a small desktop-pet style companion that can switch
between pet and human modes. The current version runs as a real Android overlay:
after the user grants "display over other apps", the companion floats on top of
the user's actual One UI Home desktop and other apps.

## Features

- Pet / human mode switch
- Real Android overlay using `SYSTEM_ALERT_WINDOW`
- 12 companion characters from the desktop reference project
- PNG companion assets for idle, happy, and sleep states per character
- Tap, drag, and long-press menu interaction
- Character switch, feed, play, rest, mode toggle, and close actions
- Happiness, energy, and hunger meters

## Build

Open the project in Android Studio, or build from a terminal with Gradle:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project uses Android Gradle Plugin 8.10.1, `compileSdk 36`, and JDK 17.

## iOS release

`iOS/` is a native SwiftUI companion. iOS does not allow third-party apps to
draw over other apps, so the pet lives on this app's own home-screen desktop
instead of a system overlay.

Minimum iOS version is 16. Bundle ID is
`com.huang1988pioneer.desktoppetorhumanmobile`.

```shell
cd iOS
./package-ipa.sh
```

Output:

- `releases/ios/v1.0.0/DesktopPetOrHumanMobile-v1.0.0-ios.ipa` (Apple Development signed)
- `releases/ios/v1.0.0/DesktopPetOrHumanMobile-v1.0.0-ios.ipa.sha256`
- `releases/ios/v1.0.0/DesktopPetOrHumanMobile-v1.0.0-ios-unsigned.ipa`
- `releases/ios/v1.0.0/DesktopPetOrHumanMobile-v1.0.0-ios-unsigned.ipa.sha256`

The signed IPA can be installed on devices registered to team `CR63JJ5KWY`.
Unsigned IPA is for later re-signing. App Store / TestFlight still needs
Archive and Distribute App in Xcode.
