# DEVELOPMENT_GUIDELINES

## Naming Rules

- `XxxRoute`: navigation, dependency lookup, and ViewModel creation
- `XxxScreen`: pure UI composable, receives only state and callbacks
- `XxxViewModel`: owns feature state and side effect orchestration
- `XxxUiState`: screen state data
- `XxxEffect`: one-off events such as navigation, toast, or opening settings

## String Rules

- Do not hardcode user-facing strings
- Use `app/src/main/res/values/strings.xml`
- In composables, use `stringResource(...)`
- If a ViewModel needs an error message, use a string resource id or a typed effect instead of a raw string

## UI State Pattern

- Use the shared `AsyncUiState<T>` pattern
- States: `Idle`, `Loading`, `Success`, `Error(@StringRes messageResId)`
- ViewModel owns state, Route collects, Screen renders

## Feature Folder Rules

- `feature/<name>/data`
- `feature/<name>/presentation/screen`
- `feature/<name>/presentation/state`
- `feature/<name>/presentation/viewmodel`
- Add `domain` only when it is actually needed; do not create empty folders in advance
