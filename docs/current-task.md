# Current Task

Date: 2026-03-20
Project: PassedPath Android app

## Service rules
- This service records daily movement paths on a map.
- Background location permission is a core requirement.
- If background location is not granted, app entry should go to `permission_intro`.
- Main screen may still track finer-grained internal permission state for UI branching.
- `DEV_SKIP_LOGIN` must skip only login, not the permission gate.

## Current implementation status
- `maps-compose` is already connected.
- Main screen state model exists: permission state, current location, first camera centering flag, today path points.
- Entry and permission intro flow use the original background-permission-first policy.
- Step 3 is implemented: one-shot current location fetch and initial camera move.
- Step 4 is implemented: current location button moves camera to the latest known location.
- Step 5 is implemented: Main subscribes to continuous foreground location updates while the screen is active.

## Decisions already made
- Use `ACCESS_FINE_LOCATION` as the real tracking baseline.
- Marker design stays default for now.
- Initial camera should move to current location.
- Live location updates and background service will come later.
- When permission is missing, current-location marker should not be shown.

## Next steps
1. Polyline accumulation and daily persistence.
2. Background service and background permission refinement.

## Guardrails for future sessions
- Do not weaken app entry from background permission gating to foreground-only gating.
- Keep implementation incremental; do not mix background service work into the current step.
- Preserve the `MainUiState`-based structure unless there is a clear reason to refactor it.
