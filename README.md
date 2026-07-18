# Radio Isaac

Android radio streaming app focused on Brazilian stations. Streams internet radio with ICY metadata display, music recognition via AudD, and background playback.

## Features

- Browse and search Brazilian radio stations (via Radio Browser, Radio Garden, TudoRádio APIs)
- Filter by state/region
- Background playback — music keeps playing when app is closed or screen is off
- ICY metadata display (artist + song from stream)
- Music recognition via [AudD](https://audd.io/) when stream has no metadata
- Last played station restored on open (stopped, not auto-playing)
- Station logos cached locally

## Requirements

- Android 8.0+ (API 26)
- Internet connection

## Build

### Prerequisites

- Android Studio (for JBR) or JDK 17
- Android SDK with API 37

### Debug

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew installDebug
```

### Release

Create `local.properties` (never commit this file):

```properties
sdk.dir=/path/to/Android/Sdk
audd.token=YOUR_AUDD_TOKEN
keystore.path=/absolute/path/to/keystore.jks
keystore.alias=your_alias
keystore.password=your_store_password
key.password=your_key_password
```

Generate keystore (once):

```bash
keytool -genkey -v -keystore radio-isaac.jks -alias radio_isaac \
  -keyalg RSA -keysize 2048 -validity 10000
```

Build signed APK or AAB:

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleRelease
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## AudD Token

Free tier at [audd.io](https://audd.io/) — 300 requests/month. Add token to `local.properties`. If omitted, defaults to `"test"` (very limited).

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
```

- **Playback:** ExoPlayer via `PlaybackService` (MediaSessionService), survives app swipe
- **Metadata:** ICY headers parsed from stream; AudD fallback when no ICY data
- **State:** single `RadioUiState` data class, all UI driven from StateFlow

## Permissions

| Permission | Reason |
|-----------|--------|
| `INTERNET` | Stream audio and fetch station list |
| `ACCESS_NETWORK_STATE` | Check connectivity |
| `FOREGROUND_SERVICE` | Background playback |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media notification |

No microphone used. Music recognition downloads audio directly from the stream URL.

## Privacy

- No user data collected or transmitted
- No analytics or crash reporting
- AudD requests contain only audio chunks from public radio streams
- All data stays on device except AudD API calls when music recognition is enabled
