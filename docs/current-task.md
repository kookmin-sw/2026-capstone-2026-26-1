# Current Task

Date: 2026-03-30
Project: PassedPath Android app

## Current status
- Issue 1 through Issue 7 are complete enough to treat as closed for ongoing work.
- The app can collect background location, persist raw points locally, batch upload GPS points, fetch a date-based day route from the backend, decode encoded polyline data, and render route lines plus ordered place markers on the map.
- Automated coverage now exists for remote day-route mapping and `MainViewModel` route-loading behavior.
- The next active implementation target is Issue 8.

## Why Issue 7 is considered done
- `GET /api/day-routes/{date}` fetch is wired through `DayRouteApi` and `RoomDayRouteRepository`.
- Remote DTO mapping normalizes nullable backend fields and decodes `encodedPath`.
- `MainViewModel` exposes loading, empty, success, and error route states by selected date.
- `MainScreen` renders route polyline, ordered place markers, retry UI, empty-state UI, and date selection.
- Camera behavior now fits route bounds when route points exist.
- Unit tests cover mapper normalization and `MainViewModel` route-fetch scenarios.

## Open issues

### Issue 8. Split today-route rendering from past-date rendering
- Goal:
  - Separate live local-today route state from server-fetched historical route state.
- Remaining work:
  - Today should render from local Room-backed points.
  - Past dates should keep using backend `GET /api/day-routes/{date}`.
  - Keep a single stable screen state while switching date source internally.
- Done when:
  - Today's route updates live without depending on a remote fetch.
  - Past dates stay server-backed.
  - Date switching does not cause mixed or stale route state.

### Issue 9. Refine permission / GPS-off / service-state UX
- Goal:
  - Prevent broken combinations between permission state, GPS state, and tracking service state.
- Remaining work:
  - Decide tracking auto-start policy after permission approval.
  - Re-check state when returning from Settings.
  - Detect GPS-off and provider unavailable cases.
  - Add clear guidance when tracking cannot run.
  - Stop tracking safely if permission is revoked.
- Done when:
  - Permission, GPS, and service state stay consistent.
  - The user can understand why tracking is inactive and recover from it.

### Issue 10. Add QA, logs, and debugging support
- Goal:
  - Make collection, persistence, sync, and rendering failures diagnosable.
- Remaining work:
  - Add logging for tracking lifecycle, persistence, sync, and route rendering.
  - Add lightweight debug inspection for tracking/day-route state.
  - Document repeatable emulator or mock-location test scenarios.
- Done when:
  - Failures can be traced without guesswork.

## Architecture snapshot
- Collection:
  - `LocationTracker`
  - `CurrentLocationProvider`
  - `LocationTrackingService`
- Storage:
  - Room `GpsPointDao`, `DayRouteDao`, `PassedPathDatabase`
  - `RoomLocationTrackingRepository`
  - `RoomDayRouteRepository`
- Sync:
  - `UploadGpsPointsBatchUseCase`
  - `DayRouteApi`
- Presentation:
  - `MainViewModel`
  - `MainScreen`
  - `PermissionViewModel`
  - `AppEntryViewModel`

## Files worth reading first in future sessions
- `app/src/main/java/com/example/passedpath/app/AppContainer.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/service/LocationTrackingService.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/repository/RoomLocationTrackingRepository.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/repository/RoomDayRouteRepository.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainScreen.kt`
- `app/src/main/java/com/example/passedpath/feature/permission/data/manager/LocationPermissionStatusReader.kt`

## Guardrails
- Do not weaken app entry from background-permission gating to foreground-only gating.
- `DEV_SKIP_LOGIN` skips only login, not the permission gate.
- Keep background tracking contracts separated by collection, storage, sync, and presentation responsibilities.
- Prefer interface-based dependencies where tests need to fake Android state.
