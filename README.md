# THIS IS AI SLOP

# Random Drawer

Private random picker for Android. Random Drawer lets users save text entries or private cached files, organize them into separate draw spaces, and draw one or many random results without sending data to a server.

## Why This Project Exists

Most random picker tools are simple text lists. Random Drawer is built around a more practical mobile use case: keeping multiple private lists, mixing text and file entries, preserving custom display names, and controlling how random selection behaves over time.

The app is intentionally local-first. Saved entries live in a Room database, copied file contents stay in app-private storage, and cache can be cleared without deleting the saved list metadata.

## Features

- Multiple draw spaces with a drawer/sidebar switcher
- Automatic space naming from the first saved entry
- Rename and delete individual draw spaces
- Add text entries
- Add files into private app storage
- Add files with custom display names
- Delete individual saved entries
- Delete all cached file contents while keeping saved names
- Single random draw
- Multiple random draw with configurable draw count
- Repeat controls for single and multiple draws
- Last result persistence per draw space
- Expandable multiple-result popup
- AMOLED black theme by default
- Light theme toggle
- Adjustable draw animation delay
- Option to disable draw animation entirely

## Random Behavior Controls

Random Drawer supports two repeat-limit settings:

- **Single repeat limit**: limits how many times the same item can be picked in a row. `0` means unlimited.
- **Multiple repeat limit**: limits how many times the same item can appear in one multi-draw result. `0` means unlimited.

These settings make the picker flexible for different use cases, from strict no-repeat draws to intentionally chaotic random selection.

## Privacy Model

Random Drawer does not need accounts or network access for its core behavior.

- Text entries are stored locally in Room.
- File entries are copied into app-private storage.
- The database stores file metadata and cached file paths.
- Clearing cache removes copied file contents but keeps saved entry names.

## Tech Stack

- Kotlin
- Jetpack Compose Material 3
- AndroidX Activity Compose
- Room
- Kotlin coroutines and Flow
- FileProvider for opening cached files
- JUnit and kotlinx-coroutines-test
- Robolectric repository tests
- AndroidX Compose UI tests

## Architecture

The app uses a small layered structure:

- `domain`: draw models and random selection rules
- `data`: Room entities, DAO, repository, and private file cache manager
- `ui`: Compose screen, UI state, theme, and ViewModel
- `MainActivity`: Android entry point, file picker integration, and file opening

The random draw logic is kept separate from Android APIs so repeat-limit behavior can be unit tested without device dependencies.

## Build And Test

This project currently expects JDK 21 for reliable local tests. Robolectric fails under Java 26 in this environment.

```powershell
$env:JAVA_HOME="C:\Users\win112603\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Build the debug APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

Run unit tests and compile Android tests:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin
```

Install on an emulator or connected Android device:

```powershell
.\gradlew.bat :app:installDebug
```

## Project Status

Random Drawer is a working Android prototype focused on local persistence, private file caching, configurable random selection, and a polished Compose UI. It is suitable as a portfolio project showing Android app architecture, Compose UI work, Room persistence, file handling, and test coverage.
