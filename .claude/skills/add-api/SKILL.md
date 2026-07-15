---
name: add-api
description: Scaffold a new radio station API source into RadioRepository — creates the Retrofit client file and wires it into parallel station loading
trigger: /add-api
---

# /add-api

Add a new radio station API source following the existing singleton pattern.

## Usage

```
/add-api <ApiName> <baseUrl> [REST|GraphQL]
```

Example: `/add-api RadioBrasil https://api.radiobrasil.com.br REST`

## Steps

### 1. Read existing API for reference

Read one of the existing clients to match the pattern exactly:
- `app/src/main/java/com/radioisaac/data/RadioApi.kt` (REST example)
- `app/src/main/java/com/radioisaac/data/TudoRadioApi.kt` (GraphQL example)

### 2. Create `data/<ApiName>Api.kt`

Follow this structure:
```kotlin
// Retrofit interface
interface <ApiName>Api {
    @GET("endpoint") // or @POST for GraphQL
    suspend fun getStations(...): List<RadioStation> // or wrapper type
}

// Singleton client
object <ApiName>Client {
    val api: <ApiName>Api by lazy {
        Retrofit.Builder()
            .baseUrl("<baseUrl>")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(<ApiName>Api::class.java)
    }
}
```

For GraphQL: use `@POST("graphql")` with `@Body` request wrapper, match `TudoRadioClient` pattern.

### 3. Map response to `RadioStation`

If API returns custom types, add a mapping function that produces `RadioStation`. Check `RadioStation.kt` for all fields — `stationuuid` is the dedup key.

### 4. Wire into `RadioRepository`

Open `data/RadioRepository.kt`.

In `getByCountry()`: add `async { <ApiName>Client.api.getStations(...).map { it.toRadioStation() } }` inside the `coroutineScope { ... }` block alongside existing async calls. Add result to the `awaitAll()` flattening.

In `getByRegion()` (optional): add region-filtered call if the API supports it.

### 5. Handle errors

Wrap the async call in `try/catch`, returning `emptyList()` on failure — matches existing pattern in RadioRepository.

### 6. Verify

Build: `./gradlew assembleDebug` — confirm no compile errors.
