# Place Search Review

Date: 2026-04-23
Project: PassedPath Android app
Status: Final snapshot for today's place-search work

## Today's Result
- Add-place flow is now search-based.
- Manual place input UI/state was removed from the active flow.
- Search, selection, and create side effect are owned by `AddPlaceViewModel`.
- Place list refresh and reorder submission remain in `PlaceViewModel`.
- Current implementation compiles and unit tests pass.

## Current Implementation
- The add-place entry point is search-based.
  - `PlaceBottomSheetContent` emits `onAddPlaceClick`.
  - `MainScreen` forwards the event through `onNavigateToAddPlace`.
  - `AppNavHost` navigates to `AddPlaceScreen` with `dateKey`.
- Android calls only the backend place-search endpoint:
  - `GET /api/places/search?query={query}`
  - Naver Open API credentials stay on the backend.
- Search data flow is separated by layer:
  - data: `PlaceSearchApi`, DTO, mapper, repository implementation
  - domain: `PlaceSearchResult`, repository contract, `SearchPlacesUseCase`
  - presentation: `AddPlaceUiState`, `AddPlaceViewModel`, `AddPlaceScreen`
- Place creation after selection is handled by `CreatePlaceFromSearchResultUseCase`.
  - It maps a selected search result to the existing add-place API.
  - `AddPlaceViewModel` owns search, selection, and submit state.
- Manual place input was removed from the active flow.
  - `PlaceCreateBottomSheet` is deleted.
  - `PlaceUiState` no longer keeps raw `placeName`, `roadAddress`, `latitude`, or `longitude` form fields.
  - `PlaceViewModel` no longer exposes raw manual add/update/delete functions.
- `PlaceViewModel` is now limited to place-list responsibilities that are still in use:
  - selected date key
  - list loading/stale/error state
  - temporary reorder input
  - reorder submit and refresh
- After create success:
  - `AddPlaceScreen` emits `placeCreated`
  - `AppNavHost` pops back to `Main`
  - `MainRoute` refreshes the selected date's place list
- Tests currently cover:
  - `PlaceSearchMapper`
  - `AddPlaceViewModel` search-select-create flow
  - `PlaceViewModel` list load, stale retention, date reset, and reorder flow

## Problems
- Create success refresh is event-counter based in `AppNavHost`.
  - This is acceptable for the first integration slice.
  - A typed navigation result or shared refresh signal would scale better.
- `PlaceViewModel` still has a temporary comma-separated reorder input.
  - This is not user-facing right now.
  - It should be replaced by drag reorder state when the reorder UI is built.
- Search screen text is still mostly hardcoded.
  - This should move to `strings.xml` before UI polish.
- Search API contract is assumed from the planned backend response.
  - The app expects `places[].category`, `name`, `roadAddress`, `address`, `latitude`, and `longitude`.
- Test coverage is still narrow around failure and debounce behavior.
  - Happy-path search-create is covered.
  - Duplicate-query, create failure, and empty-result UX need additional tests.
- `PlaceSearchCard` and `PlaceSearchTextField` live in common `ui.component`.
  - This is fine while they are reusable components.
  - If their styling becomes place-feature-specific, move them under `feature/place`.
- `AddPlaceScreen` uses `Icons.Outlined.ArrowBack`, which currently compiles with a deprecation warning.
  - The AutoMirrored icon API was not available with the current icon dependency.

## Structural Improvement Plan

### Minimal Fix
1. Add user-facing feedback after create success.
   - Current behavior returns to `Main` and refreshes silently.
2. Add create failure retry polish.
   - Current behavior leaves the user on `AddPlaceScreen` with error text.
3. Move hardcoded search strings into `strings.xml`.
4. Confirm backend search response shape and nullability.

### Better Structure
1. Replace event-counter refresh with a typed navigation result.
   - `AddPlaceScreen` should only report success.
   - `MainRoute` should decide how to refresh.
2. Replace temporary reorder input with drag reorder UI state.
   - `PlaceBottomSheetContent` should emit ordered `placeIds`.
   - `PlaceViewModel` should submit only finalized reorder results.
3. Keep add/update/delete boundaries explicit.
   - Search-based create stays in `AddPlaceViewModel`.
   - Place list and reorder stay in `PlaceViewModel`.
   - Future delete/update should be driven by selected `placeId`, not raw text input.
4. Add tests around edge cases.
   - blank query clears results
   - same query avoids duplicate requests
   - new query clears selection
   - create failure keeps the selected result and shows an error

## Remaining Work
1. Backend contract confirmation
   - Confirm `/api/places/search` response field names and nullability.
   - Confirm whether `category` is always provided.
2. UX polish
   - Decide whether blank query shows a blank area or lightweight guidance.
   - Add clearer submission progress and failure retry affordance.
   - Show a success message after returning to `Main`, if desired.
3. Drag reorder
   - Remove comma-separated reorder input after drag UI is implemented.
   - Submit ordered `placeIds` once per completed reorder action.
4. Tests
   - Add duplicate-query and debounce tests.
   - Add create failure and empty-result tests.
   - Add reorder UI tests after the drag interaction exists.

## Next Start Point
1. Confirm the backend contract for `GET /api/places/search`.
2. Add `AddPlaceViewModel` tests for duplicate query, debounce, create failure, and empty result.
3. Move hardcoded add-place screen text to `strings.xml`.
4. Polish create success/failure UX.
5. Implement drag reorder UI in `PlaceBottomSheetContent`.
6. Replace the temporary comma-separated reorder input with finalized drag result submission.

## Likely File Areas
- `app/src/main/java/com/example/passedpath/feature/place/data/...`
- `app/src/main/java/com/example/passedpath/feature/place/domain/...`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/state/...`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/viewmodel/AddPlaceViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/viewmodel/PlaceViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/screen/AddPlaceScreen.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/screen/PlaceBottomSheetContent.kt`

## Verification
- `./gradlew compileDebugKotlin`: passed
- `./gradlew testDebugUnitTest`: passed
- Remaining known warning:
  - `AddPlaceScreen.kt` uses deprecated `Icons.Outlined.ArrowBack`.

## Current Boundary Decision
- `feature/main` should only navigate to add-place and refresh place data after success.
- `feature/place` owns:
  - search query state
  - search result state
  - selection state
  - search-result to create-request mapping
  - place create side effect
  - place list refresh and reorder submit
- Common UI components stay in `ui.component` only while they are generic:
  - `PlaceSearchTextField`
  - `PlaceSearchCard`
