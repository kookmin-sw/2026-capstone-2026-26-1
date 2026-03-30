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
- The follow-up route-first architecture refactor is in progress.
- `feature/route` now exists and owns route presentation state, route UI mapping, and route section UI.

## Completed route-first refactor work
- `MainScreen` remains the record-screen container.
- `MainScreen` now delegates route-specific UI to `feature/route`.
- `MainViewModel` still orchestrates date selection and route loading, but route mode creation and route UI mapping moved out of Main-specific files.
- Route-specific code extracted so far:
  - `feature/route/presentation/state/RouteUiState.kt`
  - `feature/route/presentation/mapper/RouteUiMapper.kt`
  - `feature/route/presentation/screen/MainRouteSection.kt`

## Issue 8 agreed scope
- Issue 8 was a route-focused separation task.
- Its completed goal was to separate route behavior by date mode without prematurely splitting every Main feature.
- Today route policy:
  - route data comes from local Room observation
  - route and distance update in real time
  - tracking control entry belongs to the today route experience
- Past route policy:
  - route data comes from backend `GET /api/day-routes/{date}`
  - route playback belongs to the past route experience
- Future date policy:
  - do not make it the architectural center yet
  - future-specific route behavior can be added later when the product policy is finalized

## Main screen architecture decision
- `MainScreen` remains the user-facing record screen container.
- `MainScreen` is split internally into mode-based route content.
- This split is explicitly adopted for presentation clarity and maintainability.
- Feature ownership is not the same thing as screen ownership.

## Feature ownership decision
- `feature/main`
  - orchestrates selected date, mode decision, and screen composition
- `feature/route`
  - owns today local route, past remote route, route UI state, route UI mapping, and route section rendering
- `feature/place`
  - owns manual places and major places
- `feature/daynote`
  - owns title and memo
- `feature/favorite`
  - not split out yet as an independent feature
  - keep it near the closest existing owner until the policy and volume justify extraction

## Next refactor tasks
- Split `feature/route` screen content more explicitly into `TodayRouteSection` and `PastRouteSection` files.
- Move route overlay / marker rendering ownership further toward `feature/route` when the map composition boundary is clearer.
- Introduce route-specific action/event structure for today refresh, tracking toggle, retry, and playback entry.
- Keep Main focused on orchestration while place/daynote remain minimal until their follow-up issues start.

## What is not part of the current route refactor
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
- `app/src/main/java/com/example/passedpath/feature/route/presentation/screen/MainRouteSection.kt`
- `app/src/test/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModelTest.kt`

## Guardrails
- Do not weaken app entry from background-permission gating to foreground-only gating.
- `DEV_SKIP_LOGIN` skips only login, not the permission gate.
- Keep background tracking contracts separated by collection, storage, sync, and presentation responsibilities.
- Prefer interface-based dependencies where tests need to fake Android state.
- Do not let `MainScreen` become the permanent owner of place/daynote/favorite business rules.
