# Current Task

Date: 2026-03-20
Project: PassedPath Android app

## Service rules
- This service records daily movement paths on a map.
- Background location permission is a core requirement.
- If background location is not granted, app entry should go to `permission_intro`.
- Main screen may still track finer-grained internal permission state for UI branching.
- `DEV_SKIP_LOGIN` must skip only login, not the permission gate.

## Current implementation status
- `maps-compose` is connected.
- Main screen state model exists: permission state, current location, first camera centering flag, today path points.
- Entry and permission intro flow use the original background-permission-first policy.
- One-shot current location fetch is implemented.
- Initial camera moves to current location once.
- Continuous foreground location updates are implemented while Main is active.
- Current location button is implemented.
- Current location marker uses `current_location_marker.png`.
- Marker highlight glow is rendered in Compose with color `#006B5F`.

## Foreground scope status
- Foreground implementation is considered functionally complete for now.
- Remaining foreground work is optional polish only.
- Optional polish examples:
  - GPS off state handling
  - location fetch failure UI
  - loading state while waiting for first fix
  - small visual tuning for marker glow/button states

## Background branch plan
- Background-specific policy and recording work should be implemented in a separate branch.
- The work must be split into 4 layers to avoid coupling:
  1. Collection layer: when and how location is received
  2. Storage layer: where raw points are persisted
  3. Sync layer: when local points are uploaded to server
  4. Presentation layer: what is rendered on the map
- API integration direction:
  - `POST /api/day-routes/{date}/gps-points:batch` uploads raw GPS points
  - `GET /api/day-routes/{date}` returns the server-computed day route with `encodedPath`
  - App strategy:
    - Today route: render immediately from local Room raw points
    - Past dates / re-entry screens: render from server `encodedPath`

## Future work list

### Issue 1. Split out the location-tracking domain structure
- Purpose:
  - Remove location collection responsibility from UI and prepare common interfaces for Service, Room, and API integration.
- Current problem:
  - `MainRoute` directly subscribes to location updates, so tracking stops when the screen is gone.
  - There is no shared tracker/repository layer yet.
- TODO:
  - Create `feature/locationtracking/`
  - Define initial layers:
    - `domain/model/TrackedLocation`
    - `domain/model/DailyPath`
    - `domain/repository/LocationTrackingRepository`
    - `domain/repository/DayRouteRepository`
    - `domain/tracker/LocationTracker`
  - Reposition or wrap `CurrentLocationProvider.kt` as a `LocationTracker` implementation candidate
  - Design toward removing direct location collection calls from `MainRoute`
  - Add tracker/repository injection slots to `AppContainer.kt`
- Done criteria:
  - Location collection is no longer designed around `MainRoute`
  - Interfaces are ready for Room / Service / API implementation
- Related files:
  - `app/src/main/java/com/example/passedpath/feature/main/data/manager/CurrentLocationProvider.kt`
  - `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainRoute.kt`
  - `app/src/main/java/com/example/passedpath/app/AppContainer.kt`

### Issue 2. Define the tracking policy
- Purpose:
  - Lock down which coordinates should be stored before implementing persistence and sync.
- Why first:
  - Without agreed rules for filtering and accumulation, Room/API work will need rework later.
- TODO:
  - Document the tracking session policy
  - Define:
    - Request interval, for example 25s
    - Minimum movement distance, for example 10m+
    - Accuracy filter threshold
    - Stationary-state handling rule
    - Date-boundary split rule based on `recordedAt -> yyyy-MM-dd`
  - Define storage units:
    - Raw GPS point storage
    - Day-route metadata storage
  - Define upload policy:
    - Immediate vs batch upload
    - Retry behavior for app termination / offline mode
  - Confirm backend assumptions:
    - Whether duplicate upload is tolerated
    - Whether repeated batch uploads for the same date are accumulated
    - When `encodedPath` is regenerated
- Done criteria:
  - Store/drop/upload/date-split rules are documented and implementation-ready
- Initial policy snapshot for implementation:
  - Location request interval: `25000ms`
  - Minimum location callback interval: `10000ms`
  - Minimum update distance: request updates only after about `10m` movement
  - Raw-point save threshold: `10m+` movement from the last saved point
  - Accuracy filter: discard points worse than `50m` accuracy
  - Stationary handling: do not persist repeated stationary points; UI may still show the latest foreground fix
  - Date split: derive `yyyy-MM-dd` from each point's `recordedAt` using device local time
  - Upload strategy baseline: batch upload, trigger candidate is `20` pending points or `60s`
  - Offline/failure baseline: keep points locally and retry on the next upload trigger

### Issue 3. Build the local persistence layer with Room
- Purpose:
  - Persist collected coordinates independently of app UI lifecycle.
- Current problem:
  - The current path is in-memory only, so app restart, process death, or service recreation loses it.
- TODO:
  - Add dependencies:
    - `room-runtime`
    - `room-ktx`
    - Room compiler via `ksp` or `kapt`
  - Design entities:
    - `GpsPointEntity`
      - `id`
      - `date`
      - `recordedAt`
      - `latitude`
      - `longitude`
      - `accuracy`
      - `isUploaded`
    - `DayRouteEntity`
      - `date`
      - `totalDistance`
      - `pathPointCount`
      - `lastSyncedAt`
      - `encodedPath` (optional)
  - Design DAO methods:
    - Insert date-based points
    - Query points by date
    - Query non-uploaded points
    - Mark uploaded points after success
    - Upsert day route metadata
  - Create `PassedPathDatabase`
  - Implement repositories:
    - `LocationTrackingRepositoryImpl`
    - `DayRouteRepositoryImpl`
  - Wire everything through `AppContainer.kt`
- Done criteria:
  - Date-based raw GPS points can be stored and queried
  - Upload state can be tracked locally
- Suggested new files:
  - `feature/locationtracking/data/local/entity/GpsPointEntity.kt`
  - `feature/locationtracking/data/local/entity/DayRouteEntity.kt`
  - `feature/locationtracking/data/local/dao/GpsPointDao.kt`
  - `feature/locationtracking/data/local/dao/DayRouteDao.kt`
  - `feature/locationtracking/data/local/PassedPathDatabase.kt`

### Issue 4. Add foreground-service-based background collection
- Purpose:
  - Keep receiving location updates while the app is backgrounded.
- Current problem:
  - There is no service declaration or runtime owner for background tracking.
- TODO:
  - Create `LocationTrackingService`
  - Register it in `AndroidManifest.xml`
  - Set `foregroundServiceType="location"`
  - Create notification channel and ongoing foreground notification
  - Define service copy and tap-to-return behavior
  - Evaluate stop action support
  - Create start/stop use cases:
    - `StartLocationTrackingUseCase`
    - `StopLocationTrackingUseCase`
  - Subscribe to `LocationTracker` from the service
  - Save received points through repository
  - Prevent duplicate service start and handle restart behavior
- Done criteria:
  - Background location collection continues with the app in background
  - Foreground notification is shown correctly
  - Collection stops when the service stops
- Related files:
  - `app/src/main/AndroidManifest.xml`
  - `feature/locationtracking/service/LocationTrackingService.kt`
  - `feature/locationtracking/presentation/notification/TrackingNotificationFactory.kt`

### Issue 5. Implement point accumulation and distance calculation
- Purpose:
  - Turn raw points into usable route data.
- Layer role:
  - This is the transformation layer between collection and presentation.
- TODO:
  - Filter incoming points by:
    - Accuracy threshold
    - Minimum time interval
    - Minimum movement distance
    - Duplicate point removal
  - Accumulate by date
  - Implement total-distance calculation from adjacent points
  - Update `pathPointCount`
  - Start a new route session when the date changes
  - Add `LatLng` mappers if needed
- Done criteria:
  - Feeding raw points produces sensible date-based route totals
  - Noisy or meaningless points do not accumulate excessively
- Note:
  - The app does not need to own canonical encoded polyline generation yet.
  - Server `encodedPath` remains the canonical route format for fetched routes.

### Issue 6. Implement the GPS batch upload pipeline
- Purpose:
  - Upload locally persisted raw points to the backend.
- API:
  - `POST /api/day-routes/{date}/gps-points:batch`
- TODO:
  - Add Retrofit API:
    - `uploadGpsPointsBatch(date, body)`
  - Define DTOs:
    - `GpsPointRequestDto`
    - `GpsPointsBatchRequestDto`
  - Add mapper:
    - `GpsPointEntity -> GpsPointRequestDto`
  - Implement upload use case:
    - Query non-uploaded points by date
    - Build batch body
    - Mark `isUploaded = true` on success
  - Define upload trigger points:
    - When enough points accumulate
    - On time-based interval
    - Flush on service stop if appropriate
  - Define retry policy:
    - Simple retry first
    - Decide whether `WorkManager` is needed
- Done criteria:
  - Today-date GPS point batch upload succeeds
  - Local upload state is updated after success
  - Failed uploads remain retryable without loss
- Suggested new files:
  - `feature/locationtracking/data/remote/api/DayRouteApi.kt`
  - `feature/locationtracking/data/remote/dto/GpsPointsBatchRequestDto.kt`
  - `feature/locationtracking/domain/usecase/UploadGpsPointsBatchUseCase.kt`

### Issue 7. Integrate date-based route fetch and polyline rendering
- Purpose:
  - Show the backend-computed route on the map.
- API:
  - `GET /api/day-routes/{date}`
- TODO:
  - Add Retrofit API:
    - `getDayRoute(date)`
  - Define DTOs:
    - `DayRouteResponseDto`
    - `PlaceResponseDto`
  - Add encoded-path decode utility:
    - `encodedPath -> List<LatLng>`
  - Implement repository fetch logic
  - Extend `MainViewModel` or a date-detail ViewModel for route loading
  - Extend UI state with:
    - `polylinePoints`
    - `totalDistance`
    - `pathPointCount`
    - `places`
    - `isBookmarked`
  - Add `Polyline` rendering to `MainScreen.kt`
  - Verify overlap between place markers, order markers, and route polyline
- Done criteria:
  - Entering a specific date loads and renders the server route
  - Encoded-path polyline rendering works correctly
  - Places and route data stay aligned for the same date
- Related files:
  - `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
  - `app/src/main/java/com/example/passedpath/feature/main/presentation/state/MainUiState.kt`
  - `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainScreen.kt`

### Issue 8. Split today-route rendering from past-date rendering
- Purpose:
  - Keep live collection state and fetched historical state separate.
- Why needed:
  - Mixing both sources in the same path flow will create unstable state handling.
- TODO:
  - Add date-based branching in the ViewModel
  - For today:
    - Subscribe to Room
    - Convert raw points to polyline points
  - For past dates:
    - Call `GET /api/day-routes/{date}`
    - Decode `encodedPath`
  - Keep a unified UI state so the screen does not care about the source
  - Define refresh behavior:
    - Past dates fetch on entry
    - Today uses local-first with optional server sync
- Done criteria:
  - Today route updates in real time
  - Past dates render server-backed route data
  - ViewModel state remains stable across date changes

### Issue 9. Refine permission / GPS-off / service-state UX
- Purpose:
  - Handle failure and recovery states cleanly in real use.
- Current problem:
  - Permission intro exists, but service start policy and recovery flows are not defined.
- TODO:
  - Decide whether tracking auto-starts after permission approval
  - Sync service state when entering Main
  - Re-evaluate state after returning from Settings
  - Detect GPS-off state
  - Add UI guidance and CTA for GPS disabled
  - Handle first-fix failure
  - Handle provider unavailable state
  - Stop service safely if permission is revoked while tracking
  - Verify notification tap restore flow
- Done criteria:
  - Permission, GPS, and service state do not drift into broken combinations
  - The user can understand why tracking is not running
- Related files:
  - `app/src/main/java/com/example/passedpath/navigation/AppEntryViewModel.kt`
  - `app/src/main/java/com/example/passedpath/feature/permission/presentation/viewmodel/PermissionViewModel.kt`
  - `app/src/main/java/com/example/passedpath/feature/permission/presentation/screen/LocationPermissionIntroRoute.kt`

### Issue 10. Add QA, logs, and debugging support
- Purpose:
  - Location tracking cannot be validated reliably by visual checking alone.
- TODO:
  - Add logs for:
    - Location receive events
    - Persistence events
    - Upload request/response
    - Polyline point count
    - Service start/stop
    - Date-boundary switching
  - Add debug UI to inspect today route status
  - Document mock-location test scenarios
- Done criteria:
  - Collection, persistence, upload, and rendering failures can each be traced separately

## Decisions already made
- Use `ACCESS_FINE_LOCATION` as the real tracking baseline.
- Marker design uses image assets, and dynamic glow should be handled in code rather than baked into the image.
- Initial camera should move to current location.
- When permission is missing, current-location marker should not be shown.

## Guardrails for future sessions
- Do not weaken app entry from background permission gating to foreground-only gating.
- Keep implementation incremental; do not mix background service work into the current foreground-finished state unless working on the background branch.
- Preserve the `MainUiState`-based structure unless there is a clear reason to refactor it.





