# Place API Progress

## Scope

- Work is limited to `feature/place` and API wiring needed to resolve its dependencies.
- `feature/main` is intentionally untouched.

## Implemented

- Added `feature/place` remote API contract for place registration.
- Added request/response DTOs for `POST /api/day-routes/{date}/places`.
- Added domain model, repository contract, repository implementation, and use case for place registration.
- Registered `feature/place` dependencies in `AppContainer`.

## Current policy

- `date` path parameter uses `yyyy-MM-dd`.
- API failures are propagated with `throw`.
- Successful `POST` responses are not persisted to local DB at this stage.
- Source of truth for later re-entry is expected to be `GET /api/day-routes/{date}`.

## Follow-up note

- When the place registration UX is connected, temporary client-side reflection may still be needed so a newly added place appears immediately before the next `GET /api/day-routes/{date}` refresh.
