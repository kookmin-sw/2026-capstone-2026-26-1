# Future Work

## Active backlog
- Issue 9: harden permission, GPS-off, and service-state UX.
- Issue 10: add logs, QA hooks, and debugging support.

## Remaining route-first refactor work
- Decide how far route overlay, polyline rendering, and route marker rendering should move into `feature/route`.
- Introduce route-specific actions for refresh, retry, tracking toggle, and playback entry.
- Reduce route orchestration burden inside `MainViewModel` if a route-focused coordinator becomes justified.
- Define extraction boundaries for `feature/place` and `feature/daynote`.
- Revisit future-date mode only after product policy is finalized.
- Replace temporary route placeholder copy with policy-accurate UI slots.
- Expand route tests beyond ViewModel-centered coverage.

## Secondary polish
- Replace the temporary Android `DatePickerDialog` with the real calendar UI.
- Implement past-route playback behavior after the route split settles.
- Improve place marker styling after the real place UI direction is fixed.
- Decide whether remote day-route responses should be cached locally.
