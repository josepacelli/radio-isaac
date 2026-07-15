---
name: add-region
description: Add a new Brazilian state/region to the station filter — updates BRAZIL_REGIONS chip list and RadioRepository.getByRegion() filter logic
trigger: /add-region
---

# /add-region

Add a new Brazilian region/state to the station filter system.

## Usage

```
/add-region <StateName> [cityList] [radioGardenPlaceId] [tudoRadioStateCode]
```

Example: `/add-region "Mato Grosso" MT`

## Steps

### 1. Read existing regions for reference

Read `app/src/main/java/com/radioisaac/ui/StationSheet.kt` — find `BRAZIL_REGIONS` list.
Read `app/src/main/java/com/radioisaac/data/RadioRepository.kt` — find `getByRegion()`.

### 2. Add to `BRAZIL_REGIONS` in `StationSheet.kt`

Add a new `BrazilRegion` entry to the list:
```kotlin
BrazilRegion(
    name = "<StateName>",
    stateCode = "<XX>",           // 2-letter code, e.g. "MT"
    radioGardenId = "<id>",       // optional, Radio Garden place ID
    tudoRadioCode = "<code>"      // optional, TudoRadio state code
)
```

Match order with existing entries (alphabetical by name or grouped by region).

### 3. Update `getByRegion()` in `RadioRepository.kt`

Check if the new region needs special filter logic:
- State name filter uses `removeAccents()` — accent handling is automatic.
- RadioGarden place ID → add to the RadioGarden async call if `region.radioGardenId != null`.
- TudoRadio code → add to TudoRadio async call if `region.tudoRadioCode != null`.

No changes needed if the state name alone is sufficient for RadioBrowser filtering.

### 4. Verify

Build: `./gradlew assembleDebug`
Run on device, open station list, confirm new region chip appears and returns stations.
