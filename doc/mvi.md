# MVI (presentation layer)

Every screen follows a **Model–View–Intent** unidirectional data flow:

```
UI ──Intent──▶ ViewModel ──(use cases)──▶ new UiState ──▶ UI
                    └──Effect (one-shot)──▶ UI
```

## Contract per screen
- **`XxxUiState`**: immutable `sealed interface` or `data class`, annotated `@Immutable`/`@Stable`. Contains
  everything the screen renders. Prefer immutable collections for lists.
- **`XxxIntent`**: `sealed interface` of user actions (`Refresh`, `CragClicked(id)`, …).
- **`XxxEffect`**: `sealed interface` of one-shot events (snackbar, navigation) that must not be replayed.
- **ViewModel**:
  - single public entry point `fun onIntent(intent: XxxIntent)`;
  - `val uiState: StateFlow<XxxUiState>` built with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`;
  - `val effects: Flow<XxxEffect>` backed by `Channel(Channel.BUFFERED).receiveAsFlow()`;
  - state is derived from **DB Flows via use cases** (offline-first) combined with local UI state;
  - state transitions are pure functions (`reduce(state, …)`); use Arrow optics for nested updates
    (see [functional-programming.md](functional-programming.md));
  - errors arrive as `Either` from use cases and are mapped to state or effects, never thrown.
- **Composables**: a stateful `XxxRoute` collects state (`collectAsStateWithLifecycle`) and effects, and passes
  `uiState` + `onIntent` to a **stateless** `XxxScreen` (previewable, testable).

## Example skeleton
```kotlin
sealed interface DiscoverIntent { data object Refresh : DiscoverIntent }
sealed interface DiscoverEffect { data object SyncFailed : DiscoverEffect }

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    getAllFalesie: GetAllFalesieUseCase,
    private val syncCrags: SyncCragsUseCase,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)
    private val _effects = Channel<DiscoverEffect>(Channel.BUFFERED)
    val effects: Flow<DiscoverEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<DiscoverUiState> = combine(getAllFalesie(), isRefreshing, ::reduce)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DiscoverUiState.Loading)

    fun onIntent(intent: DiscoverIntent) = when (intent) {
        DiscoverIntent.Refresh -> refresh()
    }
    // ...
}
```

## Current state (known gap)
`presentation/FalesiaItemViewModel.kt` predates this convention: it exposes `onRefresh()` and `syncErrorEvent`
directly instead of `onIntent()` / `effects`. Migrate it to the contract above the next time it is touched,
updating `FalesiaItemViewModelTest` accordingly. Tracked in [known-issues.md](known-issues.md).
