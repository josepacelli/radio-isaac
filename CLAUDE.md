# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug   # debug APK
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleRelease  # release APK (requires keystore)
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew bundleRelease    # AAB for Play Store
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew installDebug     # install on device
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew lint             # lint
```

`JAVA_HOME` must be set — not in PATH. No tests exist.

Targets: `minSdk=26`, `compileSdk=37`, `targetSdk=35`, `jvmTarget=17`, Kotlin 2.2, Compose enabled.

Release: minification + resource shrinking enabled. Signing via `local.properties`:
```
keystore.path=...
keystore.alias=...
keystore.password=...
key.password=...
audd.token=...     # AudD music recognition token — never commit
```

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

**State:** Single `RadioUiState` data class in `RadioViewModel.kt`, all UI state lives here. Key fields: `isPlaying`, `currentStation`, `hasRdsData`, `rdsSource` ("ICY"/"AUD"/""), `isFingerprintLoading`, `fingerprintEnabled`, `auddToken`.

**Playback:** `PlaybackService` (MediaSessionService) owns ExoPlayer + OkHttp datasource with ICY metadata headers. `RadioViewModel` connects via `MediaController`. On reconnect (app reopen while playing), init block syncs `isPlaying`/`isBuffering` immediately from player state.

**Station loading:** `RadioRepository.getByCountry()` fires 9 async requests parallel, deduplicates by UUID, sorts by votes. `getByRegion()` filters by Brazilian state/city. `StationListCache` persists the list to `filesDir/station_list_cache.json` — on startup, cache loads instantly then network refreshes in background.

**Persistence:**
- `StationMetadataStore` — custom PS/PTY/RT per station + last-played UUID (SharedPreferences)
- `StationListCache` — station list JSON (filesDir)
- `AppSettings` — fingerprint enabled flag + AudD token (SharedPreferences, defaults from `BuildConfig.AUDD_TOKEN`)

**Music recognition (AudD):** `AudDClient` downloads ~500KB chunk from stream URL (no mic), POSTs to `api.audd.io`. Triggered automatically 20s after play if no ICY data (`hasRdsData=false`), retries every 90s. Manual trigger via fingerprint button in RT footer. `rdsSource` tracks "ICY" vs "AUD". Garbage metadata filtered in `isGarbageMetadata()` — add patterns there if new garbage strings appear.

**Gradle DSL:** AGP 9.0+ new DSL active (`android.newDsl` default). `org.jetbrains.kotlin.android` plugin not applied — Kotlin built-in via AGP. Token injected as `BuildConfig.AUDD_TOKEN` via `local.properties` → `build.gradle.kts`.

## Key Files

| File | Purpose |
|------|---------|
| `viewmodel/RadioViewModel.kt` | All state, player lifecycle, signal sim, fingerprint jobs |
| `data/RadioRepository.kt` | Multi-API aggregation, region filtering |
| `data/AudDClient.kt` | Music fingerprinting via AudD REST API |
| `data/AppSettings.kt` | User preferences (fingerprint on/off, AudD token) |
| `data/StationListCache.kt` | Station list JSON cache to filesDir |
| `data/StationMetadataStore.kt` | Per-station metadata + last UUID persistence |
| `ui/MainScreen.kt` | Main composable, portrait+landscape layouts, RT footer with fingerprint button |
| `ui/StationSheet.kt` | Station list, search, region filter chips |
| `ui/SettingsDialog.kt` | Fingerprint toggle + AudD token input |
| `PlaybackService.kt` | ExoPlayer + MediaSession, ICY metadata, stays alive if playing when swiped away |
| `data/RadioStation.kt` | Data class with computed props (frequency, PI code, PS name) |

## Skills

| Skill | Trigger | When to use |
|-------|---------|-------------|
| `add-api` | `/add-api` | Add new radio station API source |
| `add-region` | `/add-region` | Add new Brazilian state/region to filter chips |

## Notable Patterns

- **Signal simulation:** Smooth decay on pause (×0.65/frame), noise-driven oscillation on play — `RadioViewModel.startSignalSimulation()`.
- **RT footer format:** `ARTIST + TITLE` when both present; ICY pill = cyan, AudD pill = orange in header.
- **Garbage metadata filter:** `isGarbageMetadata()` in `RadioViewModel` — blocks "aguardando", "off air", "intervalo", "comercial", "no artist", "no title", "unknown".
- **Fingerprint job lifecycle:** Starts on `onIsPlayingChanged(true)`, cancelled only on `selectStation()` (not on buffering). Manual `fingerprintNow()` replaces background job.
- **PI code:** `uuid.hashCode() & 0xFFFF` as 4-hex-digit string.
- **Cleartext:** `usesCleartextTraffic="true"` — many streams are HTTP.
- **Brazilian focus:** Default loads `getByCountry("BR")`; `BRAZIL_REGIONS` hardcoded in `StationSheet`.
- **API singletons:** `RadioApiClient.api`, `RadioGardenClient.api`, `TudoRadioClient.api` — lazy companion object singletons, no DI.
