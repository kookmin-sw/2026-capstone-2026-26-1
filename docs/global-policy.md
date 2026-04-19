# Global Policy

## Scope
- This document is the current source of truth for stable app-side policy decisions.
- Decision-history, task-history, and issue-tracking documents may still exist, but repeated policy summaries should point back here.

## Architecture ownership
- `feature/main` owns shared shell and screen orchestration.
- `feature/route` owns route-specific behavior, state, loading, rendering, and actions.
- `feature/daynote` owns title/memo edit rules even when rendered inside Main.
- `feature/place` owns place write APIs and place-list read flow.
- `feature/permission` owns permission-state resolution and permission action-target policy.

## Main route read policy
- Today:
  - route path, distance, and path point count come from local Room-backed route data
  - title and memo come from remote `dayroute/{date}` read data
  - place data for map and sheet comes from `GET /api/day-routes/{date}/places`
- Past dates:
  - route path, title, and memo come from remote `dayroute/{date}`
  - place data for map and sheet comes from `GET /api/day-routes/{date}/places`

## Route gap render policy
- Route rendering keeps the base path color unchanged for both solid and gap segments.
- The app renders only the connecting line between two adjacent route points as dashed when both conditions are true:
  - point time gap is at least 10 minutes
  - straight-line distance between the two points is at least 1km
- Dashed rendering is evaluated per adjacent point pair, not for the whole route polyline.
- Current limitation:
  - today/local route points include timestamps, so the dashed-gap rule can be applied
  - past-date remote route points currently do not include per-point timestamps in `dayroute/{date}`, so the time-gap rule cannot be evaluated there until the server contract widens

## Place read and synchronization policy
- Place data for both map markers and the place bottom sheet comes from `GET /api/day-routes/{date}/places`.
- Fetch trigger policy:
  - fetch once on selected-date entry
  - refresh on `PLACE` tab entry
  - refresh again when the already-selected `PLACE` tab is tapped
  - do not fetch on bottom-sheet height changes
- After place-list fetch succeeds, that result is the single source of truth for both map and sheet place rendering.
- Place identity is always matched by `placeId`.
- Place ordering is always based on server-provided `orderIndex`.
- After place CRUD succeeds, refresh via the place-list API rather than reloading place state from `dayroute/{date}`.
- `dayroute/{date}` place payload is not the UI source of truth for place rendering.
- Marker interaction policy:
  - marker tap opens the `PLACE` tab and bottom sheet
  - marker tap focuses the map camera near the tapped place, slightly above center
  - marker tap scrolls the sheet to the matching card and plays a one-time shake animation
  - `selectedPlaceId` is cleared after the one-time interaction is handled

## Main map and bottom-sheet interaction policy
- Bottom-sheet state uses three stable UI states:
  - `HIDDEN`
  - `MIDDLE`
  - `EXPANDED`
- `HIDDEN` is a bottom-bar-safe hidden state, not a full zero-height removal.
  - hidden visible height stays `92dp`
- Sheet visibility policy:
  - map tap requests `HIDDEN`
  - re-tapping the already-selected `MAIN` bottom tab requests `HIDDEN`
  - marker tap requests `MIDDLE`
  - bottom-sheet tab selection requests `MIDDLE`
- `feature/main` owns only screen-local sheet interaction orchestration.
- Bottom-bar reselection is a navigation-shell event and must be passed down as an interaction signal, not handled inside map/sheet UI code.

## Main camera intent policy
- Map camera movement is driven by one-time camera intent state, not directly by raw route/location stream updates.
- Route data loading and current-location updates must not directly re-trigger camera movement after an intent has already been consumed.
- Camera intent rules:
  - if a date enters with route data available, request route-fit camera once
  - if a date enters without route data but with current location available, request current-location centering once
  - if current location arrives first while the route is still empty, request current-location centering once
  - if route data later becomes available for the same date after previously being empty, request route-fit once
- After camera movement is applied, the pending camera intent is cleared.
- `feature/main/presentation/viewmodel` may decide when to issue camera intents, but camera side effects stay in the screen/effect layer.

## Permission policy
- Permission intro is advisory, not a hard blocker for entering `Main`.
- Users may continue into `Main` without background location permission.
- Background location permission is still required for background tracking behavior.
- Permission-state resolution and action-target selection stay centralized in `feature/permission`.

## Daynote save policy
- `title` and `memo` use overwrite-style update APIs.
- There is no separate delete API for either field.
- If request value is `null`, `""`, or blank-only text, the server treats it as delete and stores `null`.
- Read normalization:
  - server `null` is normalized to `""` in app state
- Write normalization:
  - both `title` and `memo` are trimmed before validation and save
  - trimmed blank becomes delete semantics
- Dirty check uses normalized values, not raw input.
- The UI exposes one save button for both fields.
- The save button is enabled when either normalized field differs from its original value.
- Skip the request for any field whose normalized value did not change.
- If both fields changed, send both requests from one save action in this order:
  - title PATCH
  - memo PATCH
- If the first request fails, do not send the second request in the same save attempt.
- Treat the save action as fully successful only when every required request succeeds.
- After save success, store normalized values as the new originals.
- Frontend length limits:
  - `title`: max 60 characters
  - `memo`: max 1000 characters

## Location tracking local retention policy
- `gps_points` and `day_routes` are managed together as one date-scoped local route cache.
- Past-date route data is served from the remote API, while local Room data is treated as a temporary cache and upload buffer.
- Synced local data:
  if a date has been uploaded successfully and is more than 3 days old, delete that date's `gps_points` and `day_routes` together.
- Unsynced local data:
  if a date has not been uploaded successfully and is more than 14 days old, delete that date's `gps_points` and `day_routes` together.
- Tracking debug logs:
  delete `tracking_debug_logs` entries more than 1 day old.
- Cleanup runs by date, not by individual point.
- Deletion removes both:
  - all `gps_points` for the target `dateKey`
  - the `day_routes` row for the same `dateKey`
- A date is considered synced when `day_routes.lastSyncedAtEpochMillis != null`.
- A date is considered unsynced when `day_routes.lastSyncedAtEpochMillis == null`.

## Location tracking request and save policy
- Tracking keeps `PRIORITY_HIGH_ACCURACY` for path quality.
- Location request policy uses adaptive request modes:
  - moving mode:
    - request interval: `60s`
    - minimum update interval: `30s`
    - minimum update distance: `20m`
  - idle mode:
    - request interval: `5m`
    - minimum update interval: `2m`
    - minimum update distance: `50m`
- Mode transition policy:
  - tracking starts in moving mode
  - if a local point is saved, stay in moving mode
  - if no point is saved for `5m`, switch to idle mode
  - if a point is saved again while idle, switch back to moving mode
- Local save policy:
  - drop locations whose accuracy is worse than `50m`
  - skip local save when moved distance from the latest saved point is less than `20m`
- Policy split by responsibility:
  - location callback request policy belongs in `feature/locationtracking/domain/policy/LocationRequestPolicy.kt`
  - moving/idle transition policy belongs in `feature/locationtracking/domain/policy/AdaptiveTrackingModePolicy.kt`
  - local save acceptance policy belongs in `feature/locationtracking/domain/policy/LocationPersistencePolicy.kt`
  - upload scheduling policy belongs in `feature/locationtracking/domain/policy/LocationUploadPolicy.kt`

## Notes
- This document intentionally captures stable policy only.
- Open questions and future reconsiderations should stay in `docs/future-work.md` or issue/history documents.
