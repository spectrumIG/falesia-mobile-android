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
  `FalesiaIconButton`, `FalesiaSearchField`, `FalesiaFilterChip(Row)`, `SectionHeader`, `StyleTag`,
  `CragHero` (photo slot), `InfoTile`, `RouteRow`, `RouteListItem`, `StarRating`,
  `FalesiaPrimaryButton`. Shared icons (`ic_menu`, `ic_arrow_back`, `ic_search`, `ic_expand_more`,
  `ic_chevron_right`,
  `ic_location_on`, `ic_straighten`, `ic_star`, `ic_star_filled`, `ic_star_half_filled`) are in
  `:core-ui` resources; screen-specific icons stay in `:app`. Icons are Material Symbols Outlined
  (from google/material-design-icons, `symbols/android`), `_filled` variants for active items and
  where the design fills them.
- `FalesiaColors.isDark` tells content not drawn with tokens which theme is on (the map style).
- `FalesiaTopAppBar(action = TopBarAction(…))` adds an icon button on the right (with `isLoading`); it is the
  debug download button (see [offline-first-and-sync.md](offline-first-and-sync.md), "Download mode"). Screens
  take `manualDownloads`: when true they drop pull-to-refresh and show that button.
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
  `TicklistScreen`, `ProfileScreen`.
- `NavDisplay` uses `rememberViewModelStoreNavEntryDecorator()`: ViewModels are scoped to their back
  stack entry. Detail ViewModels take their id by Hilt assisted injection
  (`hiltViewModel<VM, VM.Factory>(key = id) { it.create(id) }`), as `CragDetailsRoute` and
  `RouteListRoute` do.
- New destination = new `AppRoute` entry (with `topLevel`) + entry in `NavDisplay` + tests in
  `NavigationConfigTest` / `TopLevelNavigationTest`.

## Crag details and route list

- `CragDetailsScreen` (screens/CragDetails.dc.html) and `RouteListScreen`
  (screens/RouteList.dc.html), state from `CragDetailsViewModel` / `RouteListViewModel`
  ([mvi.md](mvi.md)).
- The map is `presentation/screen/CragMap.kt` (MapLibre, OpenFreeMap light/dark style, see
  [tech-stack.md](tech-stack.md)): crag pin with halo, sector dots and the parking "P" are map
  layers; the crag name chip, zoom buttons, recenter and the parking chip are Compose views over the
  map, as in the design. The attribution is the caption under the map (the built-in ornaments are
  off). Previews show a gray box.
- "Open navigation" opens a `geo:` URI with a labelled pin on the parking (or the crag), so any maps
  app works; without one, a snackbar says so.
- **Deviations from the design** (data or scope):
    - no favourite button, no "+" FAB, no route Status filter or SENT/PROJECT tags (read-only app,
      open issue 1);
    - the WORLD CLASS badge shows the crag's rating and votes (`★ 4.5 · 15 VOTES`), only when it has
      votes;
    - the "Sun: Morning" tile is **Exposure** (the site has exposures, not sun hours): main
      directions weighted by routes (`NE, SW`);
    - grades are **French** (`6a+`), as the site writes them; the route list shows the length
      instead of the bolt count (not on the site) and groups routes by sector;
    - no approach path on the map (no data); the parking chip shows the sectors' approach range;
    - the route list filters are Grade (bands `GradeBand`), Sector and Style, each a chip with a
      menu; Sector and Style appear only when the crag has more than one.
- The info tile value auto-sizes to one line (three tiles share the width).

## Design provenance
- The current UI was derived from a **Stitch.io** mockup. Reference files live in `design/`:
  `crag_discovery.html`, `crag_discovery.png` (first screen) and `stitch_crag_discovery.zip` (full export:
  5 screens — crag_discovery, crag_details, route_list, route_detail_topo, logbook — each in 2 variants that
  differ only in the bottom bar). It is temporary.
- The Stitch export has been imported into **Claude Design** (1:1 copy, dark + light):
  - Design system "Falesia Mobile": https://claude.ai/artifact/RAbZEfFEBx8kRkw5syHe4A (tokens, type scale,
    16 component specs, contrast notes).
  - Screens "Falesia Mobile UI": https://claude.ai/artifact/M6Zfv3snFfotKZtua44WQz (Discover, Crag details,
    Route list, Route detail/topo, Logbook, Menu bottom sheet, Open source licenses, onboarding tour, first
    download, Errors & feedback catalog).
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
   counts. The implemented screens use string resources (EN + IT), falesiaonline.it data and French
   grades; Claude Design still shows YDS.
6. **Inconsistencies inherited from Stitch:** three FAB shapes (16px, 24px, round); icons filled by
   default on crag details and topo but outlined elsewhere; info/stat tiles swap `primary-5`/`primary-10`
   between themes differently per screen; a different bottom bar in every screen (unified in Claude Design
   to Discover / Logbook / Ticklist / Profile). Pick one rule for each.
7. **Placeholder photos:** the canvas uses low-resolution crops of Stitch's screenshots (the original
   images were not in the export). Replace with real crag photos. The onboarding illustrations (`tour-*`)
   come from the project owner's `risorse/` folder: check their license before shipping them.
8. **Menu entries without a page:** Settings, Terms & conditions and Privacy policy in the menu bottom sheet
   have no screen yet; Terms and Privacy need the real legal text. The profile header shows only the guest
   state (the app has no sign-in).
9. **Crag details deviations not yet in Claude Design:** rating badge, Exposure tile, French grades,
   no favourite/FAB, route list without Status (see "Crag details and route list" above). Bring the
   design in line.
10. **Errors & feedback and onboarding are design only.** The catalog (`Errors.dc.html`) marks each message
    `Exists` or `New`; the `New` ones, the onboarding tour and the first download screen need code first:
    progress reporting from `SyncWorker` (`setProgress`, read through `WorkInfo`), a connectivity monitor for
    the offline banner and snackbar, surfacing a background sync that gave up (known issue #17), a Retry
    action on the refresh snackbar, a readable `FalesiaItemUiState.Error` instead of the raw message, and a
    "first launch done" flag (e.g. DataStore) so the tour shows once. The danger and inverse color tokens and the Snackbar, Banner,
    StatePanel and ProgressBar components are new in the design system and not yet in `:core-ui`.
