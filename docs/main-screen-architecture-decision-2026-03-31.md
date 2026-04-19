# Main Screen Architecture Decision

Date: 2026-03-31
Project: PassedPath Android app
Status: Accepted and applied in code

## Note
- Stable app-side policy has been consolidated into `docs/global-policy.md`.
- This document remains as the architecture decision record for the main-screen split.

## Decision summary
- `MainScreen` stays as the single record-screen entry point.
- `feature/main` owns shared shell and screen orchestration.
- `feature/route` owns route-specific behavior, state, loading, rendering, and actions.
- `feature/daynote` remains a separate feature boundary even when rendered inside Main.

## Applied result
- Route state and route load policy moved out of Main-specific files.
- Today and past route behavior are split behind route-owned presentation and coordination code.
- Tracking toggle behavior is implemented and persisted.
- Main-map permission/GPS UX is handled through permission-owned policy helpers plus a bottom banner UI.
- Issue 10 debug and QA support is applied through shared debug logging, route debug snapshots, and a debug panel on the main map.

## Ongoing follow-up
- Finalize `feature/daynote` extraction boundary when that flow resumes.
- Decide whether remote day-route responses should be cached locally.
- Keep future-date mode out of the architectural center until product policy is fixed.

## Guidance for future sessions
- Route-specific logic belongs in `feature/route`.
- Permission and location-access policy belongs in `feature/permission`.
- Cross-feature screen composition belongs in `feature/main`.
- Temporary QA/debug helpers should stay isolated from product-facing state as they grow.
