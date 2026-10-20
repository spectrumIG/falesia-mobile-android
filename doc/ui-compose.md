# UI (Jetpack Compose)

## Rules
- UI is **100% Jetpack Compose** and must stay so: no XML layouts, no Views (except platform interop if
  unavoidable).
- **Material 3** themed with the design-system tokens, in the `:core-ui` module
  (`core-ui/src/main/java/it/softsamurai/core/ui/`). Never hardcode colors, text styles, radii or
  spacing in screens: see "Design tokens in code" below.
- **Light and dark** are both first-class: every screen and component has `@PreviewLightDark`
  previews, and UI tests cover both themes where colors matter.
- Screens follow [mvi.md](mvi.md): stateful `XxxRoute` + stateless `XxxScreen(uiState, onIntent)`.
- Keep composables small, stateless and reusable; hoist state; pass `Modifier` as first optional parameter.
- Every reusable composable has `@Preview`s (`@PreviewLightDark` + large font) with realistic sample
  data, as in `FalesiaItemScreen.kt` and the `:core-ui` components.
- Watch stability: UI state classes are `@Immutable`/`@Stable`; the Compose stability analyzer plugin is enabled.
- Images with Coil 3 (`AsyncImage`), always with placeholder/error handling.
- Strings shown to the user belong in resources, in **English** (`values/`) and **Italian**
  (`values-it/`):
  same keys, same order, same format arguments (enforced by `StringResourcesParityTest`). Write
  labels, tags and overlines in normal case and uppercase them in code (`uppercase(Locale)`), as the
  design says.

## Design tokens in code

`design/claude-design/design-system/tokens.json` is mapped 1:1 in `:core-ui`:

| Design                                                                                  | Code                                                                               |
|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| Color tokens with an M3 slot (`primary`, `on-primary`, `bg`, `ink`, `ink-muted` …)      | `MaterialTheme.colorScheme` (`Theme.kt`, `lightScheme` / `darkScheme`)             |
| All other color tokens (`primary-10`, `surface-card`, `line`, `chip`, `nav-inactive` …) | `FalesiaTheme.colors` (`FalesiaColors` in `Color.kt`)                              |
| Named type styles (`screen-title`, `section-title`, `card-title`, `stat`, `label` …)    | `FalesiaTheme.typography`; M3 slots reuse them (`titleLarge` = `card-title` …)     |
| `space-*`, `radius-*`, `size-*`                                                         | `FalesiaTheme.spacing` / `.radius` / `.size` (`Dimens.kt`), `MaterialTheme.shapes` |

- Lexend is a downloadable Google Font registered in every weight the design uses (300–700, 900).
- **Deviation from the design:** in the light theme, accent *text and icons* use
  `FalesiaTheme.colors.primaryText`
  = `#9A5A00` (5.1:1 on `bg`) instead of `#F49D25` (2.0:1). Fills stay `#F49D25`. Use `primaryText`
  for accent text, `MaterialTheme.colorScheme.primary` for accent fills.
- Translucent tokens (`primary-5` cards …) are composited over `bg` for M3 slots that need solid
  colors.
- Components from the design system live in `core-ui/.../component/`: `FalesiaTopAppBar` (+
  `TopBarNavigation`),
  `FalesiaIconButton`, `FalesiaSearchField`, `FalesiaFilterChip(Row)`, `SectionHeader`, `StyleTag`.
  Shared icons (`ic_menu`, `ic_arrow_back`, `ic_search`, `ic_expand_more`, `ic_chevron_right`) are
  in `:core-ui` resources; screen-specific icons stay in `:app`. Icons are Material Symbols
  Outlined, `_filled` variants for active items.
- Window background (`themes.xml`, DayNight) uses `bg` per theme (`values/colors.xml`,
  `values-night/colors.xml`).

## Navigation
- **Navigation 3**: routes are `@Serializable` objects/classes implementing `NavKey` in
  `presentation/navigation/AppRoute.kt`; `NavDisplay` setup in `Navigation.kt`; bottom bar in
  `BottomBar.kt` (tabs defined in `BottomNavItem.kt`).
- Top-level destinations (bottom bar tabs) are `data object`s; detail destinations are `data class`
  es that carry the id of what they show, e.g. `AppRoute.CragDetails(falesiaId)`. Each route
  declares `topLevel`: the tab that stays selected while it is shown (detail screens reached from
  Discover keep Discover active).
- Back stack rules are pure functions in `TopLevelNavigation.kt` (`navigateToTopLevel`,
  `navigateTo`, `goBack`,
  `selectedTopLevel`): Discover is always the root, another tab gives `[Discover, tab]`, so back
  from a tab returns to Discover.
- Sections not built yet use `PlaceholderScreen` (top bar + page name): `LogbookScreen`,
  `TicklistScreen`,
  `ProfileScreen`, `CragDetailsPlaceholderScreen`.
- New destination = new `AppRoute` entry (with `topLevel`) + entry in `NavDisplay` + tests in
  `NavigationConfigTest` / `TopLevelNavigationTest`.

## Design provenance
- The current UI was derived from a **Stitch.io** mockup. Reference files live in `design/`:
  `crag_discovery.html`, `crag_discovery.png` (first screen) and `stitch_crag_discovery.zip` (full export:
  5 screens — crag_discovery, crag_details, route_list, route_detail_topo, logbook — each in 2 variants that
  differ only in the bottom bar). It is temporary.
- The Stitch export has been imported into **Claude Design** (1:1 copy, dark + light):
  - Design system "Falesia Mobile": https://claude.ai/artifact/RAbZEfFEBx8kRkw5syHe4A (tokens, type scale,
    16 component specs, contrast notes).
  - Screens "Falesia Mobile UI": https://claude.ai/artifact/M6Zfv3snFfotKZtua44WQz (Discover, Crag details,
    Route list, Route detail/topo, Logbook, Menu bottom sheet, Open source licenses).
  - A snapshot of both, readable without claude.ai, lives in
    [`design/claude-design/`](../design/claude-design/README.md): tokens (`tokens.json`), usage rules, component
    specs and screen markup. Read it for any UI work; re-export it when the design changes.
- The tokens are mapped to `:core-ui` (see "Design tokens in code"). Next: rework the UI in Claude
  Design; keep the UI theme-driven and componentized so the redesign mostly touches `:core-ui` and
  leaf components.
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
   Fixed in code with `primaryText` = `#9A5A00` (light only); still to be added to Claude Design as
   a token.
3. **Below 3:1 in both themes:** `nav-inactive` (inactive bottom-bar items, 2.4:1) and `field-placeholder`
   (search placeholder, about 2.2–2.3:1).
4. **Below 4.5:1 for small text:** `ink-faint` in light (dates, GRADE captions, 2.6:1), `ink-dim` in dark
   (route type, note timestamps, 3.3:1), `success` in light (SENT/FLASH tags, 3.3:1).
5. **Sample content is placeholder:** English copy, US places, YDS (`5.12a`) and V (`V8`) grades, invented
   counts. The implemented screens already use string resources (EN + IT) and falesiaonline.it data.
6. **Inconsistencies inherited from Stitch:** three FAB shapes (16px, 24px, round); icons filled by
   default on crag details and topo but outlined elsewhere; info/stat tiles swap `primary-5`/`primary-10`
   between themes differently per screen; a different bottom bar in every screen (unified in Claude Design
   to Discover / Logbook / Ticklist / Profile). Pick one rule for each.
7. **Placeholder photos:** the canvas uses low-resolution crops of Stitch's screenshots (the original
   images were not in the export). Replace with real crag photos.
8. **Menu entries without a page:** Settings, Terms & conditions and Privacy policy in the menu bottom sheet
   have no screen yet; Terms and Privacy need the real legal text. The profile header shows only the guest
   state (the app has no sign-in).
9. **Crag map:** Crag details has a Location section with an interactive map (crag pin, parking, approach
   path, zoom and recenter) and an "Open navigation" button that should hand off to an external maps app. The
   map in the design is a vector placeholder: pick a map library (KMP-friendly, e.g. Google Maps Compose vs
   MapLibre) and get real coordinates first — the scraper does not extract them yet (known issue #10 in
   [known-issues.md](known-issues.md)).
