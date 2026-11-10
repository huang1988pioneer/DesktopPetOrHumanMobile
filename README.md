# DesktopPetOrHumanMobile

Android mobile app for a desktop pet companion. After the user grants
"display over other apps", the selected character floats above the user's home
screen and other apps.

## Features

- Real Android overlay using `SYSTEM_ALERT_WINDOW`
- 10 new companions: GPT 娘、Claude 娘、鯨魚娘、咕咕嘎嘎、鋒兄、塗哥、喵布布、喵白白、牙妹、魚妹
- Three-view design sheets and transparent front-view artwork for each companion
- Tap, drag, and long-press menu interaction
- Character switch, feed, play, rest, and close actions
- Happiness, energy, and hunger meters

## Build

Open the project in Android Studio, or build from a terminal with Gradle:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project uses Android Gradle Plugin 8.10.1, `compileSdk 36`, and JDK 17.

## Character artwork

Character turnarounds live in `art/turnarounds/`. Both apps use the transparent
front-view PNG artwork generated from those sheets. The old character roster and
pet/human mode have been replaced by the 10 new companions.

After editing a turnaround sheet, regenerate the app art with Python 3 and FFmpeg:

```shell
python3 scripts/extract-turnaround-fronts.py
```

Generated app assets are included, so regular Android and iOS builds do not require FFmpeg.

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

## Meowa2D

`Meowa2D/` is a Unity 2D side-view game (Hourglass Palace) starring Miaobaibai.
Open `Meowa2D/HourglassPalace` in the Unity Editor. Art sources and helper
scripts live beside the Unity project in that folder.
