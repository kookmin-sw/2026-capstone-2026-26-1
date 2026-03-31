# Current Task

Date: 2026-03-31
Project: PassedPath Android app

## Current status
- Issue 1 through Issue 7 are closed for ongoing work.
- Issue 8 is complete for its intended scope:
  - today date observes local Room-backed route data
  - past date fetches remote day-route data
  - date changes cancel stale route work
  - unit tests cover today-vs-past branching and stale-state clearing
- The follow-up route-first architecture refactor is complete for the agreed scope.
- `feature/route` now owns route presentation state, route UI mapping, route load coordination, route map rendering, and route-specific actions.

## Final route-first refactor result
- `MainScreen` remains the record-screen container and shared screen shell.
- `feature/route` owns:
  - route mode state
  - today-vs-past route mapping
  - route polyline and place marker rendering
  - route overlay state UI
  - route action model
  - route load coordination through `RouteStateCoordinator`
- `MainViewModel` now keeps screen-level orchestration only:
  - selected date
  - permission state
  - current location
  - route action delegation

## Feature ownership decision
- `feature/main`
  - selected date, screen composition, shared screen shell
- `feature/route`
  - today local route, past remote route, route state, route UI, route load coordination, route actions
- `feature/place`
  - manual places and major places
- `feature/daynote`
  - title and memo
- `feature/favorite`
  - still deferred until policy and volume justify extraction

## Next product-facing work
- Implement the real behavior behind route actions.
  - today: tracking toggle
  - past: playback entry
- Define the extraction boundary for `feature/place` when place work resumes.
- Define the extraction boundary for `feature/daynote` when note work resumes.
- Expand route tests beyond current ViewModel-centered coverage.
- Revisit future-date mode only after product policy is finalized.

## Not part of the finished route refactor
- Full favorite-feature extraction
- Future-date-centered architecture work
- Detailed playback implementation
- Final place-edit interaction design
- Final memo/title save policy

## Files worth reading first in future sessions
- `docs/main-screen-architecture-decision-2026-03-31.md`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainScreen.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/state/RouteUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/mapper/RouteUiMapper.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/coordinator/RouteStateCoordinator.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/screen/RouteMapContent.kt`
- `app/src/test/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModelTest.kt`

## Guardrails
- Do not weaken app entry from background-permission gating to foreground-only gating.
- `DEV_SKIP_LOGIN` skips only login, not the permission gate.
- Keep background tracking contracts separated by collection, storage, sync, and presentation responsibilities.
- Prefer interface-based dependencies where tests need to fake Android state.
- Do not let `MainScreen` become the permanent owner of place/daynote/favorite business rules.
