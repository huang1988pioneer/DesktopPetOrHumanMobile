# DesktopPetOrHumanMobile

Android mobile app for a small desktop-pet style companion that can switch
between pet and human modes. The first version is a native Android app with a
custom-drawn interactive character, status meters, and simple actions.

## Features

- Pet / human mode switch
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
