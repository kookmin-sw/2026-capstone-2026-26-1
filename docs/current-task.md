# Current Task

Date: 2026-04-01
Project: PassedPath Android app

## Current snapshot
- Route-first refactor is complete and applied in code.
- `feature/route` owns route state, route load coordination, rendering, and route actions.
- Today tracking toggle behavior is implemented and preserves the user's intended ON/OFF state.
- Permission and GPS UX is active on the main map through a bottom banner and settings shortcuts.
- App entry is allowed even when background location permission is not granted.
- `feature/permission` owns shared permission policy helpers such as permission-state resolution and action-target resolution.
- Issue 10 is complete for the current scope:
  - debug logger exists for route, permission, tracking, and main-flow events
  - debug panel state exists in `MainUiState`
  - debug actions can refresh system state and reload the selected route
  - debug-build QA checklist exists in `docs/issue-10-qa-checklist.md`

## Feature ownership
- `feature/main`: screen shell, selected date, composition, screen-level orchestration
- `feature/route`: route state, today/past load behavior, rendering, route actions
- `feature/permission`: permission state policy, service-state policy, intro/settings guidance flow
- `feature/daynote`: note/title editing

## Next work
- Past-date playback and animation behavior
- Remote day-route caching policy
- `feature/daynote` extraction boundary cleanup
- Replace temporary Android `DatePickerDialog` when the real calendar UI is ready

## Files worth reading first
- `docs/main-screen-architecture-decision-2026-03-31.md`
- `docs/issue-10-qa-checklist.md`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/state/MainUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/state/MainDebugStateMapper.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainMapSection.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainDebugPanel.kt`
- `app/src/main/java/com/example/passedpath/feature/permission/presentation/policy/LocationAccessPolicy.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/coordinator/RouteStateCoordinator.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/coordinator/RouteDebugSnapshotFactory.kt`

## Guardrails
- Do not make `MainScreen` the long-term owner of route/daynote business rules.
- Keep background tracking contracts separated by collection, storage, sync, and presentation responsibilities.
- Keep permission intro behavior advisory for app entry unless product policy changes.
- Prefer interface-based dependencies where tests need Android-state fakes.
