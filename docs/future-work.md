# Future Work

## Product follow-up
- Past-date playback and animation behavior
- Remote day-route caching policy
- Real calendar UI to replace the temporary Android `DatePickerDialog`

## Architecture follow-up
- Revisit future-date mode only after product policy is fixed
- Permission model alignment:
- Decide whether the app truly supports `ACCESS_COARSE_LOCATION`; if not, remove the manifest declaration, and if yes, reflect it in runtime request and permission-state evaluation
- Clarify permission terminology across the codebase so `foreground location`, `fine location`, `background location`, and `foreground service` are not mixed in names or policy descriptions
- Revisit whether the current app-level permission states (`ALWAYS`, `FOREGROUND_ONLY`, `DENIED`) are sufficient, especially for coarse-only or other partially granted Android location cases
- Review whether the onboarding/settings flow should continue navigation when the user dismisses the settings dialog without granting the intended permission
- Offline tracking reliability:
- Revisit whether `PRIORITY_BALANCED_POWER_ACCURACY` is sufficient for unstable-network or offline tracking scenarios, or whether tracking should move to `PRIORITY_HIGH_ACCURACY`
- Revisit whether the current `50m` max acceptable accuracy filter is too aggressive for real-world offline or weak-signal environments
- Add debug logs that distinguish `no location callback received` from `callback received but dropped before Room save`
- Review whether pending upload retry should cover older unsynced dates beyond only the current and previous tracking dates

## UI polish
- Design and implement a dedicated loading state for route fetches instead of reusing the map overlay
- Design the loading UI for place bottom sheet fetches, including the state shown when the sheet opens from a marker tap before the place list has loaded
