# Future Work

## Active backlog
- Issue 8: refactor `MainScreen` into common shell plus `TodayContent` / `PastContent`, and keep route state mode-aware.
- Issue 9: harden permission, GPS-off, and service-state UX.
- Issue 10: add logs, QA hooks, and debugging support.

## Follow-up architecture work
- Separate `feature/place` from Main when the route split stabilizes.
- Separate `feature/daynote` from Main when note/title save policy is fixed.
- Re-evaluate whether `feature/favorite` deserves an independent boundary after product policy stabilizes.

## Secondary polish
- Replace the temporary Android `DatePickerDialog` with the real calendar UI.
- Implement past-route playback behavior after the route split settles.
- Improve place marker styling after the real place UI direction is fixed.
- Decide whether remote day-route responses should be cached locally.
