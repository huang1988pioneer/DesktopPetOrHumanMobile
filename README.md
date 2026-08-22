# DesktopPetOrHumanMobile

Android mobile app for a small desktop-pet style companion that can switch
between pet and human modes. The current version uses a portrait mobile home
screen inspired by the supplied visual reference: a night-sky tree wallpaper,
status bar, five-column app grid, page dots, a translucent dock, and a draggable
penguin human companion near the lower-right corner.

## Features

- Pet / human mode switch
- Portrait mobile home screen with night wallpaper, shortcut icons, and dock
- PNG companion assets for idle, happy, and sleep states
- Animated companion drawn with a custom Android `View`
- Tap and drag interaction
- Feed, play, and rest actions
- Happiness, energy, and hunger meters

## Build

Open the project in Android Studio, or build from a terminal with Gradle:

```powershell
.\gradlew.bat :app:assembleDebug
```

The project uses Android Gradle Plugin 8.10.1, `compileSdk 36`, and JDK 17.
