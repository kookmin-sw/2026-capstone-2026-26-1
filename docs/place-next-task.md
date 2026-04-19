# Place Next Task

Date: 2026-04-20
Project: PassedPath Android app
Status: Next implementation note

## Scope
- Finish the remaining user-facing place features:
  - search-based manual place add
  - drag reorder UI

## Current status
- Done:
  - place list read source of truth is `GET /api/day-routes/{date}/places`
  - selected-date entry / `PLACE` tab entry / `PLACE` tab re-tap fetch policy is applied
  - stale list retention and retry policy is applied
  - marker-to-sheet interaction is applied
  - place CRUD success refreshes the place-list API
- Not done:
  - search-based place add with Naver place search
  - drag reorder UI for place order editing

## Next features

### 1. Search-based place add
- Product requirement:
  - user can search and add a place
  - location permission is not required
  - use Naver place API
- Current gap:
  - `PlaceCreateBottomSheet` still uses raw text input for name/address/lat/lng
- Preferred responsibility split:
  - `feature/place`
    - owns search query state
    - owns search result state
    - owns selected search result to place-create mapping
    - owns place-create request
  - `feature/main`
    - only opens/closes the place-create bottom sheet
- Recommended structure:
  - add a dedicated search state model under `feature/place/presentation/state`
  - add a search use case and repository contract under `feature/place/domain`
  - keep Naver API DTO/mapper/client under `feature/place/data`
  - keep `PlaceCreateBottomSheet` as UI composition only
- Guardrails:
  - do not mix search API DTO into UI state directly
  - do not make `MainScreen` own search logic
  - do not require location permission for search

### 2. Drag reorder UI
- Product requirement:
  - user can drag registered places to change order
- Current gap:
  - reorder API exists
  - UI is still not drag-based
- Preferred responsibility split:
  - `feature/place`
    - owns reorder editing state
    - owns drag result to `placeIds` mapping
    - owns reorder submit and refresh
  - `PlaceBottomSheetContent`
    - renders drag affordance and reordered list
    - emits reordered ids
- Recommended structure:
  - keep drag interaction state local to place UI
  - keep final reorder submission in `PlaceViewModel`
  - submit reordered `placeIds` only after user action is finalized, not on every drag frame
- Guardrails:
  - do not move reorder policy into `feature/main`
  - do not couple drag UI state to map marker state directly
  - after reorder success, keep the existing refresh-via-place-list policy

## Recommended implementation order
1. Finalize search data contract and repository boundary
2. Implement Naver place search flow
3. Replace raw manual input flow in `PlaceCreateBottomSheet` with search-select-add flow
4. Implement drag reorder UI in place sheet
5. Reuse existing reorder API and refresh flow
6. Add tests for:
   - search success / failure / empty result
   - selected search result to place-create mapping
   - reorder submission after drag
   - reorder success refresh

## Likely file areas
- `app/src/main/java/com/example/passedpath/feature/place/data/...`
- `app/src/main/java/com/example/passedpath/feature/place/domain/...`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/state/...`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/viewmodel/PlaceViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/screen/PlaceCreateBottomSheet.kt`
- `app/src/main/java/com/example/passedpath/feature/place/presentation/screen/PlaceBottomSheetContent.kt`

## Definition of done
- user can search places without location permission
- user can select a search result and add it to the selected date
- user can drag places to reorder them
- reordered result persists through the reorder API and refreshes correctly
- place bottom sheet no longer depends on test-only reorder input or raw lat/lng manual entry
