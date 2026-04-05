# Place Policy Issues

Date: 2026-04-03
Project: PassedPath Android app

## Scope
- This document covers the place-data policy split for the main record screen.
- Scope includes `dayroute/{date}`, the place-list read API, initial map-marker rendering, bottom-sheet freshness, and upcoming marker-to-list interaction.
- `feature/daynote` rules are out of scope.

## Current code baseline
- `feature/main` owns screen composition.
- `feature/route` owns route loading and the initial route-owned marker seed.
- `feature/place` now owns place write APIs and the new place-list read flow.
- Initial map markers are still derived from `dayroute/{date}` route data.
- The place bottom sheet now reads from place-list state instead of reading directly from `selectedRoute.places`.

## Agreed policy summary
- Initial map markers come from `dayroute/{date}`.
- That initial place data is treated as a first-render marker seed.
- When the place bottom sheet opens, the app calls `GET /api/day-routes/{date}/places`.
- After place-list fetch succeeds, that result becomes the latest source of truth for the place sheet.
- Place identity is always matched by `placeId`.
- Place ordering is always based on server-provided `orderIndex`.
- After place CRUD succeeds, the app should refresh via the place-list API rather than reloading place state from `dayroute/{date}`.
- A short mismatch is allowed right after initial screen entry, but after place-list fetch succeeds, map and sheet should converge to the same place set.

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
Status: Done

### What was implemented
- Route-owned marker state was made explicit.
- `SelectedDayRouteUiState` now exposes `markerPlaces` as the route-owned marker seed.
- `MainUiState` exposes `mapPlaces` for the map layer.
- `RouteMapContent` now renders markers from `mapPlaces`.
- Initial marker rendering still comes from `dayroute/{date}` and remains available before the place sheet is opened.

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
- `MainScreen` triggers place-list refresh when:
  - the selected tab is `PLACE`
  - the bottom sheet is not collapsed
- Place add/update/delete/reorder success now refreshes via `GET /api/day-routes/{date}/places` instead of relying on generic same-date re-selection.
- `PlaceBottomSheetContent` now renders from place-list state instead of route place markers.
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
- Mutation-followed-by-refresh coverage still needs to be added in tests.
- UX validation and visual polish are intentionally deferred until the place issue flow is functionally complete.

## Issue 4. Synchronize map markers and place-sheet list after place-list fetch
Status: Not started

### Next goal
- After place-list fetch succeeds, feed the same result into map marker state.
- Move from:
  - route seed for initial markers only
- To:
  - route seed first
  - place-list result as the latest shared place state after sheet fetch

## Issue 5. Re-fetch flow after add, update, delete, reorder
Status: Partially started

### Next goal
- Mutation success now refreshes the place-list API for sheet state.
- The next step is to use that refreshed result to update both map and sheet state.

## Issue 6. Date-switch and lifecycle fetch policy
Status: Partially started

### What is already applied
- On selected-date change, `MainRoute` updates `PlaceViewModel` with the new date key.
- Place-sheet fetch is already tied to selected date and sheet open state.

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

### What is still open
- Bottom-sheet-open trigger coverage
- marker-to-sheet interaction coverage
- CRUD-followed-by-refresh coverage
- end-to-end QA scenarios for sheet refresh and marker synchronization

## Future work note
- The visual design for place-sheet loading is still temporary.
- Policy and state handling are in place first; final loading UI design and UX validation remain follow-up items after the place issue flow is functionally closed.

## Guardrails
- `feature/main` should remain a composition layer.
- `feature/route` should remain responsible for initial route data and initial marker seed only.
- `feature/place` should own place read/write rules and place-list freshness behavior.
- Do not grow `dayroute/{date}` back into the bottom-sheet source of truth.
