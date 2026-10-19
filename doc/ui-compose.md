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
- The current UI was derived from a **Stitch.io** mockup. Reference files live in `design/`:
  `crag_discovery.html`, `crag_discovery.png` (first screen) and `stitch_crag_discovery.zip` (full export:
  5 screens — crag_discovery, crag_details, route_list, route_detail_topo, logbook — each in 2 variants that
  differ only in the bottom bar). It is temporary.
- The Stitch export has been imported into **Claude Design** (1:1 copy, dark + light):
  - Design system "Falesia Mobile": https://claude.ai/artifact/RAbZEfFEBx8kRkw5syHe4A (tokens, type scale,
    16 component specs, contrast notes).
  - Screens "Falesia Mobile UI": https://claude.ai/artifact/M6Zfv3snFfotKZtua44WQz (Discover, Crag details,
    Route list, Route detail/topo, Logbook).
  - A snapshot of both, readable without claude.ai, lives in
    [`design/claude-design/`](../design/claude-design/README.md): tokens (`tokens.json`), usage rules, component
    specs and screen markup. Read it for any UI work; re-export it when the design changes.
- Planned: rework the UI there, then map the design system's tokens to `:core-ui`. Until then, prefer
  theme-driven, componentized UI so the redesign mostly touches `:core-ui` and leaf components.
- Update this file when the design source of truth changes.

## Open design issues (by priority)
Issues found while importing the Stitch export into Claude Design. Highest priority first.
**When an issue is fixed (in Claude Design and/or in code), remove it from this list.**

1. **Features that contradict the read-only scope.** The mockup has Log Ascent, Add Note, community beta
   notes, Save List, SENT/PROJECT route status, the "+" FAB on crag details, route list and logbook.
   [project-overview.md](project-overview.md) says users create no remote content. Decide what stays (e.g. a
   local-only logbook/ticklist) and drop the rest from the design before reworking the screens.
2. **Light theme: `primary` (#f49d25) as text fails contrast** — about 2.0:1 on `bg`, 2.2:1 on white.
   It is used for grades, links (Map View, SEE ALL), the active bottom-bar item, tags and overlines.
   Needs a darker text variant of the accent for the light theme.
3. **Below 3:1 in both themes:** `nav-inactive` (inactive bottom-bar items, 2.4:1) and `field-placeholder`
   (search placeholder, about 2.2–2.3:1).
4. **Below 4.5:1 for small text:** `ink-faint` in light (dates, GRADE captions, 2.6:1), `ink-dim` in dark
   (route type, note timestamps, 3.3:1), `success` in light (SENT/FLASH tags, 3.3:1).
5. **Sample content is placeholder:** English copy, US places, YDS (`5.12a`) and V (`V8`) grades, invented
   counts. Real screens need Italian strings (in resources), French grades and data from falesiaonline.it.
6. **Inconsistencies inherited from Stitch:** three FAB shapes (16px, 24px, round); icons filled by
   default on crag details and topo but outlined elsewhere; info/stat tiles swap `primary-5`/`primary-10`
   between themes differently per screen; a different bottom bar in every screen (unified in Claude Design
   to Discover / Logbook / Ticklist / Profile). Pick one rule for each.
7. **Placeholder photos:** the canvas uses low-resolution crops of Stitch's screenshots (the original
   images were not in the export). Replace with real crag photos.
8. **Tokens not yet in code:** map the Claude Design tokens (colors per theme, Lexend type scale, radii)
   to `MaterialTheme` in `:core-ui`.
