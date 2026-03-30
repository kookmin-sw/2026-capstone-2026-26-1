# Main Screen Architecture Decision

Date: 2026-03-31
Project: PassedPath Android app
Status: Accepted

## Decision summary
The record screen keeps a single user-facing `MainScreen`, but its internal presentation is split by date mode.

Adopted screen split:
- `MainScreen`
- `TodayContent`
- `PastContent`

Adopted ownership split:
- `feature/main` owns orchestration and screen composition.
- `feature/route` owns route-source differences and route-specific behavior.
- `feature/place` owns manual places and major places.
- `feature/daynote` owns title and memo.
- `feature/favorite` is deferred as an independent feature until policy and volume justify extraction.

## Why this decision was made
The same record screen contains different kinds of responsibilities:
- live route behavior for today
- fetched route behavior for past dates
- shared record-editing concerns such as places and notes

If all of this continues to grow inside one flat Main structure, the result will be a high-condition, high-coupling screen and ViewModel.

This decision keeps one screen entry point for the product while separating:
- screen composition by date mode
- feature ownership by responsibility

## Explicit policy
### 1. Main as coordinator
`feature/main` is the coordinator.
It decides selected date, decides current mode, and assembles the screen.
It should not permanently own every business rule for route/place/daynote/favorite.

### 2. Route-first split
The current architecture change is route-first.
Issue 8 is primarily about separating route behavior between today and past.

Today route behavior:
- local Room-backed route observation
- real-time route updates
- real-time distance updates
- tracking-control entry belongs here

Past route behavior:
- backend route fetch
- route playback belongs here

### 3. Screen split by mode
Presentation split is explicitly accepted.
`MainScreen` should be refactored toward:
- common shell / shared top-level layout
- `TodayContent`
- `PastContent`

This split exists because today and past route experiences are materially different.

### 4. Feature split by responsibility
Feature boundaries should follow responsibility, not screen membership.
A feature can appear on Main without being owned by Main.

Recommended boundaries:
- `feature/main`: coordinator, date-mode decision, screen assembly
- `feature/route`: route state and route behaviors
- `feature/place`: manual places and major places
- `feature/daynote`: title and memo

### 5. What is intentionally deferred
The following are intentionally not architectural centerpieces right now:
- independent `feature/favorite`
- future-date-centered mode architecture
- detailed playback implementation
- final note-save policy
- final place-edit interaction policy

## Accepted tradeoff
This decision does not fully split every feature immediately.
That is intentional.
The project should avoid both extremes:
- keeping everything inside Main forever
- over-splitting too early before product policy is stable

The chosen tradeoff is:
- split route architecture now
- prepare place/daynote separation next
- defer favorite extraction until it is justified

## Immediate engineering consequence
The next implementation step should be treated as an architectural refactor with scoped follow-up feature work.
It is not just cosmetic cleanup.
It is also not a full feature explosion.

Recommended order:
1. Refactor `MainScreen` into common shell plus `TodayContent` / `PastContent`
2. Refactor `MainViewModel` into mode-aware route state
3. Move route-specific branching away from generic Main state
4. Keep place/daynote/favorite scope minimal until their follow-up issues start

## Guidance for future sessions
When adding new behavior, decide first:
- Is this route-specific?
- Is this date-mode-specific?
- Is this a cross-date record-editing concern?

Only then decide whether the code belongs in Main, Route, Place, or Daynote.
