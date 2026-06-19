# SkyTrace

A production-ready Android astronomy observation app for amateur astronomers.

## Features

- **Sky Map** — Real-time star/planet/deep-sky map using phone sensors and GPS
- **Telescope Pointing** — Guides you to point your telescope at any object
- **Object Search** — Search stars, planets, Messier, NGC, asteroids, satellites
- **Observation Log** — Record observations with equipment, conditions, photos
- **Collection** — Track your personal Messier/NGC/planet observation progress
- **Asteroid Check** — Report and verify suspected moving objects
- **Image Blink** — Compare multiple telescope images for motion detection
- **Data Sync** — Download and cache TLE, MPC, JPL data for offline use

## Architecture

- Kotlin + Jetpack Compose
- MVVM with StateFlow
- Hilt dependency injection
- Room database for local storage
- Retrofit for API access
- WorkManager for background sync
- Repository pattern with offline caching

## Data Sources

| Source | Purpose |
|--------|---------|
| CelesTrak | Satellite TLE orbital elements |
| NASA JPL | Small-body database, Horizons ephemeris |
| Minor Planet Center | Asteroid/comet orbital data |
| Built-in catalogs | Bright stars, Messier (110), planets |

## Astronomy Engine

The app includes a real computation engine based on Jean Meeus "Astronomical Algorithms":

- Julian Date conversion
- Sidereal Time calculation
- Equatorial ↔ Horizontal coordinate transforms
- Planet position (Keplerian elements)
- Moon position and phase
- Sun position
- Angular separation
- Rise/set time calculation
- Satellite pass prediction (simplified SGP4)

## Building

```
./gradlew assembleDebug
```

Requires Android SDK 34 and JDK 17.

## Privacy

- All data stored locally on device
- No personal data transmitted to external servers
- Location used only for astronomical calculations
- Network requests only for public astronomy databases

## Disclaimer

This app cannot confirm an official asteroid discovery. Official discovery requires follow-up observations and submission to the Minor Planet Center.
