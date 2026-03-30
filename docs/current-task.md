# Current Task

Date: 2026-03-31
Project: PassedPath Android app

## Current status
- Issue 1 through Issue 7 are closed for ongoing work.
- Issue 8 has started and its first core step is done:
  - today date now observes local Room-backed route data
  - past date still fetches remote day-route data
  - `MainViewModel` cancels stale route jobs when the selected date changes
  - unit tests cover today-vs-past branching and stale-state clearing
- The next work is the large `MainScreen` / `MainViewModel` architecture refactor agreed on 2026-03-31.

## Issue 8 agreed scope
- Issue 8 is a route-focused separation task.
- The primary goal is to separate route behavior by date mode without prematurely splitting every Main feature.
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
- `MainScreen` will be split internally into mode-based screen content:
  - `TodayContent`
  - `PastContent`
- This screen split is explicitly adopted.
- The split is for presentation clarity and maintainability, because today and past route behavior are meaningfully different.
- However, feature ownership is not the same thing as screen ownership.

## Feature ownership decision
- `feature/main`
  - orchestrates selected date, mode decision, and screen composition
- `feature/route`
  - owns today local route, past remote route, and playback entry/state
- `feature/place`
  - owns manual places and major places
- `feature/daynote`
  - owns title and memo
- `feature/favorite`
  - not split out yet as an independent feature
  - keep it near the closest existing owner until the policy and volume justify extraction

## Explicitly adopted principles
- Main is a coordinator/orchestrator, not the long-term owner of every record-screen responsibility.
- Feature split is based on responsibility, not on whether something is shown on the same screen.
- Screen split is based on date mode.
- Today and past are the only required mode split for now.
- Do not over-commit the architecture around future dates yet.
- Do not over-split features too early.
- Route separation comes first.
- Place/daynote are expected follow-up feature separations.
- Favorite stays deferred until the product policy is stable enough.

## What is not part of Issue 8 right now
- Full favorite-feature extraction
- Future-date-centered architecture work
- Detailed playback implementation
- Final place-edit interaction design
- Final memo/title save policy

## Recommended next implementation step
- The next step is a refactor with architectural preparation, not a pure new feature and not a cosmetic cleanup.
- Concretely:
  - refactor `MainScreen` into common shell plus `TodayContent` / `PastContent`
  - refactor `MainViewModel` state toward mode-aware route state
  - extract route-oriented state handling so Main stops accumulating route-specific branching
  - keep place/daynote/favorite functional scope minimal until their dedicated follow-up issues

## Why this is the next step
- The current code already started route source separation, but the presentation and state structure still assume a flatter Main-centric model.
- Without this refactor, new today/past differences will keep increasing conditionals and state coupling.
- Doing the structure work now reduces the cost of later place/daynote/playback expansion.

## Files worth reading first in future sessions
- `docs/main-screen-architecture-decision-2026-03-31.md`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainScreen.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/domain/repository/DayRouteRepository.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/repository/RoomDayRouteRepository.kt`
- `app/src/test/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModelTest.kt`

## Guardrails
- Do not weaken app entry from background-permission gating to foreground-only gating.
- `DEV_SKIP_LOGIN` skips only login, not the permission gate.
- Keep background tracking contracts separated by collection, storage, sync, and presentation responsibilities.
- Prefer interface-based dependencies where tests need to fake Android state.
- Do not let `MainScreen` become the permanent owner of place/daynote/favorite business rules.
