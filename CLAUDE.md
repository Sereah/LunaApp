# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build a specific app module (replace {flavor} with app, system, or harmony)
./gradlew :app:conflux:assemble{Flavor}Debug       # main app
./gradlew :app:connection:assemble{Flavor}Debug     # Bluetooth/WiFi test app
./gradlew :app:media:assemble{Flavor}Debug          # media browser
./gradlew :app:videoplayer:assemble{Flavor}Debug    # video player
./gradlew :app:gallery:presentation:assemble{Flavor}Debug
./gradlew :app:statemachinedemo:assemble{Flavor}Debug

# Run unit tests (Robolectric, runs on JVM — no device needed)
./gradlew :app:conflux:test{Flavor}DebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew :app:conflux:connected{Flavor}DebugAndroidTest

# Run a single test class
./gradlew :app:conflux:testAppDebugUnitTest --tests "com.lunacattus.conflux.ExampleTest"

# Generate JaCoCo coverage report (unit + instrumented)
./gradlew :app:conflux:create{Flavor}DebugJacocoReport

# Build the state machine AAR (outputs to build/dist/statemachine-1.0.0/)
./gradlew :statemachine:assembleRelease
```

**Flavors**: `app`, `system`, `harmony` — all app modules define these three product flavors. Flavor class names: `App`, `System`, `Harmony` (capitalized first letter).

**Build system note**: `build-logic` and `screen-adaptation-plugin` are Gradle composite builds. Changes to convention plugins take effect without re-publication.

## Architecture

### Module Map

| Module | Type | UI | Architecture | DI |
|---|---|---|---|---|
| `:app:conflux` | App (main) | Compose + M3 Adaptive | MVI | Hilt |
| `:app:connection` | App | Compose | MVI | Hilt |
| `:app:media` | App (system) | View + ViewBinding | MVI (data class) | Hilt |
| `:app:videoplayer` | App | Compose | Multi-layer NavHost | Hilt |
| `:app:gallery:domain` | Library | N/A | Clean Architecture | — |
| `:app:gallery:data` | Library | N/A | Clean Architecture | — |
| `:app:gallery:presentation` | App | View + ViewBinding | MVI (sealed class) | — |
| `:app:statemachinedemo` | App | Compose | Simple demo | — |
| `:feature:voice` | Library | N/A | Speech recognition (Baidu SDK) | Hilt |
| `:ui-design` | Library | Compose + View | Shared UI components | — |
| `:record` | Library | N/A | Screen/audio recording | Hilt |
| `:statemachine` | Library | N/A | State machine engine | — |

### Convention Plugins (`build-logic/convention`)

Do not apply standard Android/Kotlin plugins directly in module `build.gradle.kts` files. Use these convention plugins instead:

- **app.android.application** — base for any app module (compileSdk 36, minSdk 31, JVM 17, 3 flavors, ProGuard, test deps)
- **app.android.application.compose** — adds Compose + Kotlin Serialization
- **app.android.application.view** — adds ViewBinding
- **app.android.library** / **.compose** / **.view** — same for library modules
- **app.hilt** — Hilt DI (KSP processor + dagger runtime)
- **app.android.room** — Room database (KSP, schema export)
- **framework.jar** — compileOnly against framework JARs in `frameworkLibs/` for system APIs
- **app.android.application.jacoco** / **library.jacoco** — JaCoCo coverage

### MVI Pattern

Both Compose and View-based modules use MVI:
- **Compose MVI** (`conflux`, `connection`): Screen-level `ViewModel` exposes `StateFlow<UiState>`, composables collect state and call VM functions for intents.
- **View MVI** (`media`, `gallery`): `BaseViewModel<UiState, UiEvent>` base class (in the common library AAR), with `viewModelScope.launch` collecting events.

### Framework JARs

`app:connection`, `app:media`, and `app:gallery:data` compile against custom Android framework JARs (`frameworkLibs/framework-*.jar`) to access hidden/system APIs. These modules declare `framework.jar` plugin with a version number. Apps using system APIs run with `sharedUserId="android.uid.system"` and must be signed with the platform key.

### Navigation

- **Navigation3** (type-safe, new): `conflux`, `connection`
- **Navigation2** (string routes, old): `videoplayer`
- **View Navigation**: `media`, `gallery`

### Internal AAR Dependencies

Three shared libraries are consumed as AARs from Maven (not built from source):
- `com.lunacattus.android:common:1.0.2` — base classes (`BaseViewModel`, utils)
- `com.lunacattus.android:logger:1.1.0` — logging
- `com.lunacattus.android:network:1.0.0` — networking

### Testing

- **Unit tests**: Robolectric (Android framework on JVM), MockK, coroutines-test
- **Instrumented tests**: AndroidX Test + Espresso, MockK Android
- Test directories exist only in app modules, not library modules

### Main App Key Dependencies (`:app:conflux`)

- On-device LLM inference: `com.google.ai.edge.litertlm:litertlm-android:0.10.0` (Gemma/NNAPI)
- Media playback: Media3 ExoPlayer + MediaSessionService (`PlaybackService`)
- Accessibility: `ConfluxAccessibilityService`
- Native libs: `libvndksupport`, `libOpenCL`, `libcdsprpc`
- Adaptive layouts: Material3 Adaptive Navigation Suite
- Blur effects: Haze
