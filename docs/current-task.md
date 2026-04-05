# Current Task

Date: 2026-04-03
Project: PassedPath Android app

## Current focus
- Current implementation target is the `feature/daynote` edit flow for selected-date `title` and `memo`.
- Work should start from data contract and selected-date binding, then move to real UI, edit-state rules, save feedback, and QA.

## Current status
- Issue 1 is done.
  - `title`/`memo` save policy, trim policy, blank-delete behavior, dirty-check policy, frontend length limits, and sequential save rule are fixed.
- Issue 2 is done.
  - Selected-date `title`/`memo` now flow from route detail into `feature/daynote`.
  - Date changes re-hydrate daynote input state from the selected route snapshot.
- Issue 3 is mostly done, but not fully closed.
  - The title/memo editor UI is in place.
  - The single save button UI is in place.
  - `BaseInputField` was extracted as a reusable input component.
  - Unneeded headers, dates, banners, and extra section borders were removed from the daynote sheet.
  - Save success/failure feedback currently uses toast, but the toast UI itself is still temporary and not yet aligned with the final design.
- Issue 4 is in progress.
  - Dirty check, normalization, length limits, sequential save, and save blocking during submit are implemented.
  - Unsaved date-switch handling is implemented with a confirm dialog.
  - Date-switch guard logic was separated into `DateSelectionGuardCoordinator`, so `MainScreen` and `MainRoute` no longer own the state-transition rules directly.

## Next start point
- The next session should start with toast message UI design.
  - Replace the current temporary toast presentation with the agreed product design first.
  - Keep success and failure feedback as toast-based behavior unless product policy changes.
- After toast UI is designed and implemented, continue with the remaining Issue 4 work:
  - review validation copy and edge-case behavior
  - confirm date-switch behavior end-to-end
  - verify save success/failure state cleanup
- Then move to the remaining save synchronization and QA work.

## What is already in code
- `DayRouteDetail` already includes `title` and `memo`.
- Day-route detail remote response DTO already includes nullable `title` and `memo`.
- Remote mapper normalizes nullable `title` and `memo` to empty strings.
- Separate save APIs already exist:
  - `PATCH /api/day-routes/{date}/title`
  - `PATCH /api/day-routes/{date}/memo`
- `feature/daynote` already has a temporary bottom-sheet UI and a `DayNoteViewModel`.
- Current daynote save flow is independent for title and memo, and it does not yet use selected-date route detail as the source of truth.

## Gaps that still matter
- Toast feedback UI is functional but still temporary and not yet design-aligned.
- Post-save synchronization policy is not finalized.
  - Current implementation updates daynote local state after save, but broader route snapshot synchronization policy should still be reviewed.
- Edit-state behavior still needs final QA coverage for edge cases.
- Daynote-specific tests exist for core ViewModel flows, but broader UI/interaction coverage is still limited.

## Working agreement for this task
- Keep `feature/daynote` as the owner of daynote edit rules.
- Keep `feature/main` responsible only for selected-date delivery and screen composition.
- Reuse `DayRouteDetail` as the read model unless the server contract forces a separate detail model.
- Prefer removing temporary UI copy and temporary manual date input once selected-date binding is in place.
- Do not preserve document notes that only restate code already visible and add no decision value.

## PassedPath-specific execution plan
- Phase 1. Read contract and delivery path
  - Treat `DayRouteDetail` as the current source read model for `title` and `memo`.
  - Extend the route-to-screen delivery path so selected-date daynote data can reach the bottom sheet.
  - Preferred options:
    - add `title` and `memo` to `SelectedDayRouteUiState`, or
    - pass a dedicated read snapshot from `feature/main` to `feature/daynote`
  - Avoid inventing a separate date-detail domain model unless another feature also needs it.
- Phase 2. Save contract policy
  - Baseline assumption is the existing split API contract:
    - `PATCH /api/day-routes/{date}/title`
    - `PATCH /api/day-routes/{date}/memo`
  - UI save action is one button, but actual network writes stay split by field.
  - Document app-side policy for:
    - null vs empty string
    - whitespace trimming
    - length limits
    - whether unchanged fields should skip requests
- Phase 3. Selected-date binding
  - Remove temporary manual date input from daynote UI.
  - Feed `selectedDateKey` from `feature/main` into `feature/daynote`.
  - Initialize title/memo inputs from the selected route snapshot when the sheet opens and when the date changes.
  - Define behavior for today/local mode where route state may update repeatedly while tracking is active.
- Phase 4. Daynote edit state
  - Add original title/memo values for dirty check.
  - One save button becomes enabled when either normalized title or normalized memo is dirty.
  - Define validation and normalization rules before submit.
  - Decide what happens to unsaved edits on date switch and sheet dismissal.
- Phase 5. Real UI and feedback
  - Replace the temporary debug-like controls with the actual title/memo editor UI.
  - Keep visual language aligned with the current bottom-sheet design used by place UI.
  - Success/failure feedback now uses toast.
  - Next step is to redesign the toast UI so it matches the product design before closing this phase.
- Phase 6. Post-save synchronization
  - After save success, update original values and clear dirty state.
  - Decide whether to:
    - patch only local daynote state,
    - patch the selected route snapshot in memory, or
    - re-fetch remote past-date detail
  - Prevent stale route updates from overwriting a just-saved edit unexpectedly.
- Phase 7. Tests and QA
  - Add mapper/state tests where `title` and `memo` enter the selected-date UI model.
  - Add `DayNoteViewModel` tests for hydration, dirty check, save success/failure, and date switch behavior.
  - Add Compose UI coverage only for stable interaction rules; avoid snapshotting temporary copy.

## Original 1-7 issue fit
- Fits as-is
  - validation and edit-state management
  - save API connection and result handling
  - date-switch consistency
  - tests and QA
- Needs adjustment for PassedPath
  - "date detail domain model definition"
    use existing `DayRouteDetail` first, then only widen the UI/read path as needed.
  - "include title/memo in detail fetch result"
    server/detail layer already includes them; missing part is propagation into `SelectedDayRouteUiState` or equivalent.
  - "save request shape (date, title, memo)"
    current reality is two separate PATCH endpoints, so this cannot be assumed as one combined request without backend confirmation.
  - "save button UI"
    PassedPath uses one save button in UI, while internal requests remain split by field.

## Issue 1 decisions
- Server policy
  - `title` and `memo` both use overwrite-style update APIs.
  - There is no separate delete API for either field.
  - If request value is `null`, `""`, or blank-only text, the server treats it as delete and stores `null`.
- App normalization policy
  - Read:
    - server `null` is normalized to `""` in app state
  - Write:
    - both `title` and `memo` are trimmed before validation and save
    - trimmed blank becomes delete semantics
  - Compare:
    - dirty check uses normalized values, not raw input
    - example: original `""` and current `"   "` are treated as unchanged
- UI save policy
  - The screen exposes one save button for both fields.
  - The save button is enabled when either normalized field differs from its original value.
  - Internal save execution may still call title and memo PATCH endpoints separately.
- Frontend length limits
  - `title`: max 60 characters
  - `memo`: max 1000 characters
  - Reasoning:
    - `title` should stay short enough for bottom-sheet readability and future list usage.
    - `memo` is long enough for daily note usage without making validation and layout too loose.
- Save execution rule
  - Skip the request for any field whose normalized value did not change.
  - If both fields changed, send both requests from the one save action.
  - If both fields changed, call the APIs sequentially in this order:
    - title PATCH
    - memo PATCH
  - If the first request fails, do not send the second request in the same save attempt.
  - Treat the save action as fully successful only when every required request succeeds.
  - After save success, store normalized values as the new originals.

## Files worth reading first
- `docs/main-screen-architecture-decision-2026-03-31.md`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/coordinator/DateSelectionGuardCoordinator.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/domain/model/DayRouteDetail.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/remote/dto/DayRouteDetailResponseDto.kt`
- `app/src/main/java/com/example/passedpath/feature/locationtracking/data/remote/mapper/DayRouteRemoteMapper.kt`
- `app/src/main/java/com/example/passedpath/feature/route/presentation/state/RouteUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/state/MainUiState.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/main/presentation/screen/MainBottomSheet.kt`
- `app/src/main/java/com/example/passedpath/feature/daynote/presentation/screen/DayNoteBottomSheetContent.kt`
- `app/src/main/java/com/example/passedpath/feature/daynote/presentation/viewmodel/DayNoteViewModel.kt`
- `app/src/main/java/com/example/passedpath/feature/daynote/presentation/state/DayNoteUiState.kt`
- `app/src/main/java/com/example/passedpath/ui/component/BaseConfirmDialog.kt`
- `app/src/main/java/com/example/passedpath/ui/component/BaseInputField.kt`
- `app/src/main/java/com/example/passedpath/ui/component/toast/MessageToast.kt`
- `app/src/main/java/com/example/passedpath/feature/daynote/data/remote/api/DayRouteTitleApi.kt`
- `app/src/main/java/com/example/passedpath/feature/daynote/data/remote/api/DayRouteMemoApi.kt`

## Guardrails
- Do not make `MainScreen` the long-term owner of daynote business rules.
- Keep read-side route detail mapping and write-side daynote save policy explicitly separated.
- Avoid locking product behavior to the current temporary split-save API unless backend contract is confirmed.
- Prefer interface-based dependencies where tests need Android-state fakes.
