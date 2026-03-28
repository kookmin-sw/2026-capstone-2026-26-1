# Background Tracking Progress

Date: 2026-03-23
Project: PassedPath Android app

## Summary
- Background tracking work has progressed through Issue 6.
- The current baseline now includes:
  - shared location-tracking domain structure
  - tracking policy constants and documented policy decisions
  - Room-based local persistence for raw GPS points and day-route summary
  - foreground service for background location collection
  - save-time filtering and incremental distance accumulation
- Server route fetch, date-based source split, and UX hardening are still pending.

## Architecture snapshot
- Collection layer:
  - `LocationTracker` abstracts current-location fetch and continuous updates.
  - `CurrentLocationProvider` is the current fused-location implementation.
  - `LocationTrackingService` is the background execution owner.
- Storage layer:
  - Room stores raw points in `gps_points`.
  - Room stores day summary in `day_routes`.
  - `RoomLocationTrackingRepository` owns save-time filtering and day-summary updates.
- Sync layer:
  - Not implemented yet.
  - The repository already tracks `isUploaded`, so upload state can be layered on top.
- Presentation layer:
  - Main still shows current location and in-memory today path state.
  - Full Room-backed live route rendering and server route rendering are still pending.

## Issue-by-issue log

### Issue 1. Split out the location-tracking domain structure
- Status:
  - Completed as a structural baseline.
- What was added:
  - `feature/locationtracking/domain/model/TrackedLocation.kt`
  - `feature/locationtracking/domain/model/DailyPath.kt`
  - `feature/locationtracking/domain/repository/LocationTrackingRepository.kt`
  - `feature/locationtracking/domain/repository/DayRouteRepository.kt`
  - `feature/locationtracking/domain/tracker/LocationTracker.kt`
- What changed:
  - `CurrentLocationProvider` was converted from a UI-local helper into a `LocationTracker` implementation.
  - `MainRoute` stopped owning a concrete location provider instance and now depends on the shared tracker exposed from `AppContainer`.
  - `AppContainer` now exposes tracking-related dependencies centrally.
- Applied concepts:
  - dependency inversion around location collection
  - separation between domain contracts and Android-specific implementation
  - preparation for reuse from UI, service, and future use cases

### Issue 2. Define the tracking policy
- Status:
  - Completed as an initial documented and coded baseline.
- Agreed policy baseline:
  - `PRIORITY_BALANCED_POWER_ACCURACY`
  - request interval `25000ms`
  - minimum callback interval `10000ms`
  - minimum update distance `10m`
  - save only when moved at least `10m` from the last saved point
  - discard points with accuracy worse than `50m`
  - date split uses each point's `recordedAt` and device local time
  - upload baseline remains `20` pending points or `3min`
- Where it lives:
  - `feature/locationtracking/domain/policy/LocationTrackingPolicy.kt`
  - `docs/current-task.md`
- Applied concepts:
  - battery-first update policy
  - policy centralization through constants instead of scattered literals
  - keeping OS update filtering and app save filtering separate for safety

### Issue 3. Build the local persistence layer with Room
- Status:
  - Completed as a local persistence baseline.
- What was added:
  - Room dependencies in Gradle
  - `GpsPointEntity`
  - `DayRouteEntity`
  - `GpsPointDao`
  - `DayRouteDao`
  - `PassedPathDatabase`
  - `TrackingLocalMapper`
  - `RoomLocationTrackingRepository`
  - `RoomDayRouteRepository`
- Data design:
  - `gps_points` stores raw coordinates, timestamp, accuracy, and upload flag.
  - `day_routes` stores daily summary fields such as total distance, point count, last recorded time, last synced time, and future server `encodedPath`.
- Why it was structured this way:
  - entity and domain model were kept separate through mapper functions
  - repository implementations hide Room details from the rest of the app
  - `day_routes` exists so future UI and sync flows do not have to recalculate everything repeatedly
- Applied concepts:
  - local source of truth
  - domain-to-local mapping boundary
  - repository pattern over DAO

### Issue 4. Add foreground-service-based background collection
- Status:
  - Completed as a service baseline.
- What was added:
  - `LocationTrackingService`
  - `TrackingNotificationFactory`
  - `StartLocationTrackingUseCase`
  - `StopLocationTrackingUseCase`
  - foreground-service permissions and service registration in `AndroidManifest.xml`
  - notification strings
  - `MainActivity.createIntent()` for notification return behavior
- How it works:
  - service starts in foreground mode with a persistent notification
  - service subscribes to `LocationTracker`
  - each received `TrackedLocation` is persisted through `LocationTrackingRepository`
  - stop action removes tracking and stops the service
- Important design choices:
  - notification creation was split out from the service to keep service logic small
  - start/stop entrypoints were wrapped in use cases for future UI integration
  - the service protects against duplicate session creation
- Applied concepts:
  - foreground service as the runtime owner for background collection
  - composition over inline notification code
  - service lifecycle cleanup via `LocationTrackingSession`

### Issue 5. Implement point accumulation and distance calculation
- Status:
  - Completed as the first save-time filtering layer.
- What changed:
  - `GpsPointDao` now exposes the latest saved point by date.
  - `RoomLocationTrackingRepository.saveRawLocation()` now filters before writing:
    - inaccurate point -> drop
    - moved less than `10m` from last saved point -> drop
  - day summary updates now use incremental distance addition instead of always recalculating the full route.
- Distance logic:
  - `TrackingLocalMapper.distanceBetweenMeters()` calculates point-to-point distance.
  - `TrackingLocalMapper.calculateTotalDistanceMeters()` still exists for full reconstruction cases.
  - normal save flow now prefers incremental update for `DayRouteEntity.totalDistanceMeters`.
- Why this matters:
  - noisy points no longer pollute the local route
  - day summary updates stay cheap as more points accumulate
  - storage behavior now matches the agreed policy baseline
- Applied concepts:
  - save-time domain policy enforcement
  - incremental aggregation
  - keeping expensive recomputation as fallback, not the default path

## Files worth understanding first next time
- `app/src/main/java/com/example/passedpath/feature/locationtracking/domain/policy/LocationTrackingPolicy.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/domain/tracker/LocationTracker.kt`
- `app/src/main/java/com/example/passedpath/feature/main/data/manager/CurrentLocationProvider.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/service/LocationTrackingService.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/repository/RoomLocationTrackingRepository.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/local/mapper/TrackingLocalMapper.kt`
- `app/src/main/java/com/example/passedpath/app/AppContainer.kt`

## Remaining major work
- Issue 7:
  - server day-route fetch and polyline rendering
- Issue 8:
  - split today's live Room-backed route from past-date server-backed route
- Issue 9:
  - permission, GPS-off, and service-state UX hardening
- Issue 10:
  - logs, QA hooks, and debugging support

## Notes for the next session
- `current-task.md` should remain the planning document.
- This file is the implementation progress snapshot as of 2026-03-23.
- The next logical implementation target is Issue 7.



