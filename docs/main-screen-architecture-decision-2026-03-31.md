# Main Screen Architecture Decision

Date: 2026-03-31
Project: PassedPath Android app
Status: Accepted and applied in code

## Decision summary
The record screen keeps a single user-facing `MainScreen`, but its internal presentation is split by date mode and route ownership is separated from the shared screen shell.

Adopted screen split:
- `MainScreen`
- shared shell remains in `feature/main`
- route-specific content lives in `feature/route`

Adopted ownership split:
- `feature/main` owns screen-level orchestration and composition.
- `feature/route` owns route-source differences, route state, route rendering, route actions, and route load coordination.
- `feature/place` owns manual places and major places.
- `feature/daynote` owns title and memo.
- `feature/favorite` is deferred as an independent feature until policy and volume justify extraction.

## What has already been applied in code
- `feature/route` package was created.
- Route presentation state moved out of Main-specific state files.
- Route UI mapping/factory logic moved out of `MainViewModel` into route-owned mapping code.
- Route section rendering moved out of `feature/main` screen files into route-owned screen code.
- Route polyline, place marker rendering, and route overlay moved under route-owned screen code.
- Route-specific actions were introduced for refresh, retry, tracking toggle, and playback entry.
- Route loading policy moved behind `RouteStateCoordinator`, so `MainViewModel` no longer directly owns today-vs-past loading branches.

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
It decides selected date, assembles the screen, and keeps shared screen-level state.
It should not permanently own business rules for route/place/daynote/favorite.

### 2. Route-first split
The current architecture change was route-first.
Issue 8 primarily separated route behavior between today and past.

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
`MainScreen` should remain:
- common shell
- shared map and top-level layout coordinator
- host for route-owned content

### 4. Feature split by responsibility
Feature boundaries should follow responsibility, not screen membership.
A feature can appear on Main without being owned by Main.

Recommended boundaries:
- `feature/main`: coordinator, selected date, shared shell, screen assembly
- `feature/route`: route state, route rendering, route actions, route load coordination
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
- finish route architecture first
- prepare place/daynote separation next when those features resume
- defer favorite extraction until it is justified

## Current follow-up tasks
1. Implement the actual behavior behind route actions that are already exposed in UI.
2. Expand route tests beyond the current ViewModel-centered coverage.
3. Define the extraction boundary for place and daynote when those follow-up issues start.
4. Keep future-date mode out of the architectural center until product policy is fixed.

## Guidance for future sessions
When adding new behavior, decide first:
- Is this route-specific?
- Is this date-mode-specific?
- Is this a cross-date record-editing concern?

Only then decide whether the code belongs in Main, Route, Place, or Daynote.
