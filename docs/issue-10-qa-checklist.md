# Issue 10 QA Checklist

Date: 2026-04-01
Project: PassedPath Android app

## Scope
- Debug build only
- Goal: make route, permission, GPS, and tracking flows observable for QA

## Checks
1. Launch the app on today's date and confirm the debug panel shows `today / local`.
2. Switch to a past date and confirm the debug panel shows `past / remote`.
3. Change the selected date repeatedly and confirm stale route work is cancelled without leaving the old route state visible.
4. Deny location permission and confirm the permission banner appears and the debug panel shows `DENIED`.
5. Allow foreground-only permission and confirm the shared permission banner appears and the debug panel shows `FOREGROUND_ONLY`.
6. Turn GPS off and confirm the GPS banner appears and the debug panel shows `GPS: OFF`.
7. Keep user intent for tracking ON, force the tracking service inactive, and confirm automatic restart is attempted.
8. Turn tracking OFF from the main route controls and confirm the debug panel shows user intent OFF.
9. Trigger a past-date remote failure and confirm the route status becomes `error`.
10. Trigger an empty route case for today or past and confirm the route status becomes `empty`.
11. Use the debug panel actions and confirm system-state refresh and route reload work as expected.
