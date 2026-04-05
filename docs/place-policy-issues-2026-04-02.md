# Place Policy Issues

Date: 2026-04-03
Project: PassedPath Android app

## Scope
- This document covers the place-data policy split for the main record screen.
- Scope includes `dayroute/{date}`, the place-list read API, initial map-marker rendering, bottom-sheet freshness, and upcoming marker-to-list interaction.
- `feature/daynote` rules are out of scope.

## Current code baseline
- `feature/main` owns screen composition.
- `feature/route` owns route loading and route-specific read composition.
- `feature/place` owns place write APIs and place-list read flow.
- Map markers and the place bottom sheet now both read place data from the place-list API state.
- `dayroute/{date}` is still used for route path and daynote read data.

## Agreed policy summary
- Place data for both map markers and the place bottom sheet comes from `GET /api/day-routes/{date}/places`.
- The app fetches the place list on selected-date entry and refreshes it again when the place sheet opens.
- After place-list fetch succeeds, that result is the single source of truth for both map and sheet place rendering.
- Place identity is always matched by `placeId`.
- Place ordering is always based on server-provided `orderIndex`.
- After place CRUD succeeds, the app should refresh via the place-list API rather than reloading place state from `dayroute/{date}`.
- `dayroute/{date}` place payload is no longer used as the UI source of truth for place rendering.

## Today-date read policy
- For today:
  - route path, distance, and path point count come from local Room-backed route data
  - title and memo come from remote `dayroute/{date}` read data
  - place data for map and sheet comes from `GET /api/day-routes/{date}/places`
- For past dates:
  - route path, title, and memo come from remote `dayroute/{date}`
  - place data for map and sheet comes from `GET /api/day-routes/{date}/places`

## Shared interaction state
- `selectedDateKey`
- `mapPlaces`
- `sheetPlaces`
- `selectedPlaceId`
- `isPlaceSheetOpen`

## Marker to sheet interaction policy
- Marker tap updates `selectedPlaceId`.
- Marker tap opens the place sheet.
- If the current-date place list is not loaded yet, the app fetches it first.
- After fetch succeeds, the sheet scrolls to the matching card.
- That card should play a one-time small vertical shake animation.
- If the sheet is already open and the user taps another marker, selection changes without re-fetch.

## Issue 1. Split place-data ownership and finalize API responsibility
Status: Done

### Decision summary
- `dayroute/{date}` keeps its current DTO for now.
- App-side policy treats `dayroute/{date}` as route + initial marker seed input.
- The place-list read API is the source of truth for the place bottom sheet and later place synchronization.
- `placeId` is the canonical link key across route markers and place-list items.
- `orderIndex` is the canonical ordering field.

### Outcome
- API responsibility is documented and no longer ambiguous.
- DTO slimming is explicitly deferred; responsibility split was prioritized first.

## Issue 2. Keep initial map-marker rendering on dayroute
Status: Reframed

### What was implemented
- Route-owned marker state was made explicit.
- `SelectedDayRouteUiState` now exposes `markerPlaces` as the route-owned marker seed.
- `MainUiState` exposes `mapPlaces` for the map layer.
- `RouteMapContent` now renders markers from `mapPlaces`.

### Current decision
- The earlier route-seed split was useful for clarifying responsibilities, but the current UI policy is simpler:
  - map markers and place sheet now both use the place-list API as the read source of truth
  - route-owned place seed is no longer the active UI rendering source

### Applied files
- `app/src/main/java/com/example/passedpath/feature/route/presentation/state/RouteUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/mapper/RouteUiMapper.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/state/MainUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/screen/RouteMapContent.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainMapSection.kt`

### Verification
- Route mapper and viewmodel tests were updated and passed during the implementation step.

## Issue 3. Connect place-list API on bottom-sheet open
Status: In progress

### What is done
- `GET /api/day-routes/{date}/places` was added under `feature/place`.
- Place-list DTO, mapper, repository method, domain models, and use case were added.
- `404 + DAY_ROUTE_NOT_FOUND` is now treated as an empty place list instead of a sheet error.
- `PlaceViewModel` now owns place-list read state:
  - `places`
  - `placeCount`
  - `isLoading`
  - `errorMessage`
- `MainRoute` now creates and provides `PlaceViewModel`.
- `MainRoute` now also fetches the place list on selected-date entry, so place data can render before the sheet is opened.
- `MainScreen` triggers place-list refresh when:
  - the selected tab is `PLACE`
  - the bottom sheet is not collapsed
- Place add/update/delete/reorder success now refreshes via `GET /api/day-routes/{date}/places` instead of relying on generic same-date re-selection.
- `PlaceBottomSheetContent` now renders only from place-list state.
- Loading, error, empty, and success branches were added to the place sheet.

### Applied files
- `app/src/main/java/com/example/passedpath/feature/place/data/remote/api/PlaceApi.kt`
- `app/src/main/java/com/example/passedpath/feature/place/data/remote/dto/PlaceListResponseDto.kt`
- `app/src/main/java/com/example/passedpath/feature/place/data/remote/mapper/PlaceRemoteMapper.kt`
- `app/src/main/java/com/example/passedpath/feature/place/data/repository/PlaceRepositoryImpl.kt`
- `app/src/main/java/com/example/passedpath/feature/place/domain/model/PlaceSourceType.kt`
- `app/src/main/java/com/example/passedpath/feature/place/domain/model/VisitedPlace.kt`
- `app/src/main/java/com/example/passedpath/feature/place/domain/model/VisitedPlaceList.kt`
- `app/src/main/java/com/example/passedpath/feature/place/domain/repository/PlaceRepository.kt`
- `app/src/main/java/com/example/passedpath/feature/place/domain/usecase/GetVisitedPlacesUseCase.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/state/PlaceUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/viewmodel/PlaceViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/screen/PlaceBottomSheetContent.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainRoute.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainScreen.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainBottomSheet.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainBottomSheetScaffold.kt`
- `app/src/main/java/com/example/passedpath/app/AppContainer.kt`

### Verification
- Place mapper tests passed.
- Place repository test passed for `404 + DAY_ROUTE_NOT_FOUND -> empty list`.
- Place viewmodel tests passed.
- `compileDebugKotlin` passed after the main/place integration.

### What is still open
- Place-list retry UX is still minimal.
- UX validation and visual polish are intentionally deferred until the place issue flow is functionally complete.

## Issue 4. Synchronize map markers and place-sheet list after place-list fetch
Status: In progress

### What is done
- After place-list fetch succeeds, the same result is now fed into map marker state.
- Map markers no longer depend on route place seed for rendering.
- Marker rendering was decoupled from polyline presence, so place markers can render even when the route line is empty.

### What is still open
- `selectedPlaceId` state and marker-to-sheet focus interaction are not implemented yet.
- Marker tap should eventually open the sheet and focus the matching place card.

## Issue 5. Re-fetch flow after add, update, delete, reorder
Status: Partially started

### Current status
- Mutation success now refreshes the place-list API.
- That refreshed result is now used for both map and sheet place state.

### What is still open
- Marker selection state still needs to survive the mutation-followed-by-refresh cycle cleanly.

## Issue 6. Date-switch and lifecycle fetch policy
Status: Partially started

### What is already applied
- On selected-date change, `MainRoute` updates `PlaceViewModel` with the new date key.
- Place-list fetch now happens on selected-date entry and again on place-sheet open.
- Map markers are cleared and repopulated through place-list state on date change.

### What is still open
- Re-entry and resume policy is not finalized.
- Explicit retry UX is not finalized.
- `selectedPlaceId` lifecycle is not implemented yet.

## Issue 7. Tests and QA
Status: In progress

### What is done
- Route mapper and main viewmodel tests were updated for route-owned marker state.
- Place mapper tests were added.
- Place viewmodel tests were added for:
  - successful fetch
  - invalid date rejection
  - fetch failure
  - date-key reset behavior
- Mutation-followed-by-refresh tests were added for place viewmodel.
- Main viewmodel tests cover fetched marker override and date-change reset.

### What is still open
- Bottom-sheet-open trigger coverage
- marker-to-sheet interaction coverage
- end-to-end QA scenarios for sheet refresh and marker synchronization

## Future work note
- The visual design for place-sheet loading is still temporary.
- Policy and state handling are in place first; final loading UI design and UX validation remain follow-up items after the place issue flow is functionally closed.

## Guardrails
- `feature/main` should remain a composition layer.
- `feature/route` should remain responsible for route path loading and route read composition.
- `feature/place` should own place read/write rules and place-list freshness behavior.
- Do not grow `dayroute/{date}` back into the map or bottom-sheet place source of truth.
