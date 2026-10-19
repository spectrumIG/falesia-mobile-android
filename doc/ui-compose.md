# UI (Jetpack Compose)

## Rules
- UI is **100% Jetpack Compose** and must stay so: no XML layouts, no Views (except platform interop if
  unavoidable).
- **Material 3**. Theme (colors, typography, `Theme.kt`) lives in the `:core-ui` module
  (`core-ui/src/main/java/it/softsamurai/core/ui/`). Use theme tokens (`MaterialTheme.colorScheme`,
  `MaterialTheme.typography`), never hardcoded colors/text styles in screens.
- Screens follow [mvi.md](mvi.md): stateful `XxxRoute` + stateless `XxxScreen(uiState, onIntent)`.
- Keep composables small, stateless and reusable; hoist state; pass `Modifier` as first optional parameter.
- Every reusable composable has `@Preview`s (light, dark, large font) with realistic sample data, as in
  `FalesiaItemUi.kt` and `MainAppBar.kt`.
- Watch stability: UI state classes are `@Immutable`/`@Stable`; the Compose stability analyzer plugin is enabled.
- Images with Coil 3 (`AsyncImage`), always with placeholder/error handling.
- Strings shown to the user belong in resources.

## Navigation
- **Navigation 3**: routes are `@Serializable` objects/classes implementing `NavKey` in
  `presentation/navigation/Route.kt` (`AppRoute`); `NavDisplay` setup in `Navigation.kt`; bottom bar in
  `BottomBar.kt`.
- New destination = new `AppRoute` entry + entry in `NavDisplay` + test in `NavigationConfigTest`.

## Design provenance
- The current UI was derived from a **Stitch.io** mockup (reference files in `stitch/`:
  `crag_discovery.html`, `crag_discovery.png`). It is temporary.
- Planned: rework the UI and move the design system to **Claude Design**. Until then, prefer theme-driven,
  componentized UI so the redesign mostly touches `:core-ui` and leaf components.
- Update this file when the design source of truth changes.
