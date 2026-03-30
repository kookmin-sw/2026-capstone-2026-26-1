# Main Screen Architecture Decision

Date: 2026-03-31
Project: PassedPath Android app
Status: Accepted

## Decision summary
The record screen keeps a single user-facing `MainScreen`, but its internal presentation is split by date mode.

Adopted screen split:
- `MainScreen`
- route content is split by date mode
- route split currently exists inside `feature/route`

Adopted ownership split:
- `feature/main` owns orchestration and screen composition.
- `feature/route` owns route-source differences and route-specific behavior.
- `feature/place` owns manual places and major places.
- `feature/daynote` owns title and memo.
- `feature/favorite` is deferred as an independent feature until policy and volume justify extraction.

## What has already been applied in code
- `feature/route` package was created.
- Route presentation state moved out of Main-specific state files.
- Route UI mapping/factory logic moved out of `MainViewModel` into route-owned mapping code.
- Route section rendering moved out of `feature/main` screen files into route-owned screen code.

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
`MainScreen` should continue moving toward:
- common shell / shared top-level layout
- route-specific mode sections owned by `feature/route`

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

## Current follow-up tasks
1. Split `feature/route` screen content into clearer today/past files.
2. Move route overlay and map-specific route rendering ownership further toward `feature/route` when the map boundary is ready.
3. Introduce route-specific actions for refresh, retry, tracking toggle, and playback entry.
4. Keep place/daynote/favorite scope minimal until their follow-up issues start.

## Guidance for future sessions
When adding new behavior, decide first:
- Is this route-specific?
- Is this date-mode-specific?
- Is this a cross-date record-editing concern?

Only then decide whether the code belongs in Main, Route, Place, or Daynote.
