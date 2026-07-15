# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on connected device
./gradlew lint                   # Run Android lint
./gradlew clean                  # Clean build artifacts
```

No tests exist in this project.

Targets: `minSdk=26`, `compileSdk/targetSdk=35`, `jvmTarget=17`, Kotlin 2.0, Compose enabled.

## Architecture

MVVM + Repository, no DI framework. Single `:app` module.

```
MainActivity → RadioScreen (Compose)
                    ↓
             RadioViewModel (StateFlow<RadioUiState>)
                    ↓
             RadioRepository
            /       |        \
RadioApiClient  RadioGardenClient  TudoRadioClient
(RadioBrowser)   (Radio.Garden)     (TudoRadio GQL)
```

**State:** Single `RadioUiState` data class in `RadioViewModel`, exposed as `StateFlow`. All UI state lives here — playing, buffering, signal level, current station, station list, metadata.

**Playback:** `PlaybackService` (MediaSessionService) owns ExoPlayer with OkHttp datasource (ICY metadata headers). `RadioViewModel` connects via `MediaController`. ICY + MediaMetadata callbacks update UI state.

**Station loading:** `RadioRepository.getByCountry()` fires 9 async requests in parallel (RadioBrowser votes/clicks/trend/language/search/tags + RadioGarden + TudoRadio), deduplicates by UUID, sorts by votes. `getByRegion()` filters by Brazilian state/city with accent-stripped string matching.

**Persistence:** Station-specific custom PS/PTY/RT text stored in SharedPreferences via `StationMetadataStore`, keyed by station UUID, loaded on `selectStation()`.

## Key Files

| File | Purpose |
|------|---------|
| `viewmodel/RadioViewModel.kt` | All state, player lifecycle, signal simulation |
| `data/RadioRepository.kt` | Multi-API aggregation, region filtering |
| `ui/MainScreen.kt` | Main composable (~766 lines), portrait+landscape layouts |
| `ui/StationSheet.kt` | Station list, search, region filter chips |
| `PlaybackService.kt` | ExoPlayer + MediaSession, ICY metadata |
| `data/RadioStation.kt` | Data class with computed props (frequency, PI code, PS name) |

## Skills

| Skill | Trigger | When to use |
|-------|---------|-------------|
| add-api | `/add-api` | Add new radio station API source (creates client + wires into RadioRepository) |
| add-region | `/add-region` | Add new Brazilian state/region to filter chips and getByRegion() |

## Notable Patterns

- **Signal simulation:** Smooth decay on pause (×0.65/frame), noise-driven oscillation around buffer level on play — in `RadioViewModel`.
- **Frequency extraction:** Regex parses FM (87–108 MHz) and AM (530–1700 kHz) from station name in `RadioStation.extractedFrequency`.
- **PI code:** `uuid.hashCode() & 0xFFFF` as 4-hex-digit string.
- **Orientation layout:** `LocalConfiguration.current.orientation` switches Row/Column in `RadioScreen`.
- **Cleartext allowed:** `usesCleartextTraffic="true"` — many streams are HTTP.
- **No minification:** `isMinifyEnabled = false` in release build.
- **Brazilian focus:** Station list targets Brazil (`getByCountry("BR")`); `BRAZIL_REGIONS` hardcoded in `StationSheet`.
- **API singletons:** `RadioApiClient.api`, `RadioGardenClient.api`, `TudoRadioClient.api` are lazy companion object singletons — no DI.
