Falesia Mobile is an Android app for climbers: a catalogue of crags (*falesie*) and their routes (*vie*), readable offline. This system is a faithful copy of the Stitch mockup the app was first built from: warm dark brown, one orange accent, Lexend everywhere, big rounded cards over photos of rock.

## Content

- The Stitch copy is English with placeholder data (Crag Atlas, Siurana, Yosemite, YDS grades like `5.12a`, bouldering grades like `V8`). Keep it as sample data only; real screens show crags from falesiaonline.it.
- Titles in Title Case ("Nearby Crags", "About this Crag"). Labels, tags and overlines are UPPERCASE (`label`, `overline`, `tag`, `micro` styles): write them in normal case and uppercase them with CSS.
- Numbers lead. A crag card shows routes count and grade range; a route shows its grade large, in `primary`.
- No emoji. Icons carry meaning together with a word, except in icon buttons, which need an `aria-label`.

## Color

- Two themes, both taken from the export: **Dark** (the export's default, `<html class="dark">`) and **Light** (the export's non-dark classes). Set `data-theme="dark"` or `data-theme="light"` on a root element; dark is the fallback.
- Screens sit on `bg`. Sticky top bars use `bg-glass-80` (Discover: `bg-glass-95`) with a 12px backdrop blur and a `primary-10` bottom hairline.
- `primary` is the only accent: filled CTAs, the active bottom-bar item, grades, links, icon accents. Text on a `primary` fill is always `on-primary`, never white.
- Tints of the accent are the `primary-5` … `primary-60` tokens; cards, tiles and chips are built from them in dark.
- Text: `ink` for titles and values, `ink-muted` for locations and metadata, `ink-body` for descriptions, `ink-secondary` for note bodies, `ink-faint` and `ink-dim` for dates and captions.
- Status colors pair with a word: `success` (SENT, FLASH), `info` (REDPOINT), `special` (ONSIGHT), `primary` (PROJECT).
- Contrast kept as in Stitch, below WCAG AA: `primary` as text in the Light theme (2.0:1 on `bg`), `nav-inactive` (2.4:1), `field-placeholder` (about 2.2:1), `ink-faint` in Light (2.6:1), `ink-dim` in Dark (3.3:1), `success` in Light (3.3:1). Each token's note says where. Fix these in a later revision, not by re-tinting screens one by one.

## Type

- One family: **Lexend** (Google Fonts, 300–700, plus 900 for the route-list grade). Load it with `<link href="https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700;900&display=swap" rel="stylesheet">`.
- Titles are bold: `hero-title` on photos, `section-title` for the first heading of a list, `screen-title` in top bars, `card-title` and `title-sm` for cards and sections, `item-title` for rows.
- Numbers: `grade-xl` (900) in the route list, `stat-xl` in logbook tiles, `tile-value` in topo tiles, `stat` elsewhere.
- Running text is `body` (16/26) for descriptions and `body-sm` (14/20) for metadata.

## Feedback and errors

- Added on 2026-10-20 (not in Stitch): `danger`, `danger-soft`, `danger-line` for errors and the `inverse-*` tokens for snackbars. Components: `Snackbar`, `Banner`, `StatePanel`, `ProgressBar`.
- One-shot results (refresh failed, already up to date, no maps app) are snackbars above the bottom bar. Lasting conditions (offline, data older than a week, background sync failed) are banners at the top of the list. When a screen has nothing to show, a `StatePanel` replaces its content.
- Copy says what happened and what the user still has, in one sentence: "Couldn't refresh. Showing saved data." Never show raw exception messages or error codes.
- Offer at most one action: Retry when trying again can help, Settings when the user must change something.

## Shape and space

- Tailwind's 4px scale: `space-4` is the screen side padding, `space-6` between cards and sections, `space-3` between rows and chips.
- Stitch overrides Tailwind's radii: `radius` 8px, `radius-lg` 16px, `radius-xl` 24px. Cards, tiles, fields, chips and buttons are `radius-xl`; icon buttons and pills `radius-full`.
- Shadows are light: `shadow-sm` on cards, `shadow-primary` under the primary button and the highlighted stat, `shadow-2xl` under the hero.
- Focus is `focus-ring`: 2px solid `primary`.

## Layout

- Phone first, 390 wide. Content in one column with `space-4` side padding.
- A screen is: top bar (sticky), content, bottom bar (Discover, Logbook, Ticklist, Profile). Detail screens (crag, route list, route) keep Discover active.
- A floating action button, when present, sits `space-6` from the right and `space-24` from the bottom.

## Imagery

- Photos of rock and crags fill cards (16:9), the crag hero and the topo (3:4). They are always cropped with `object-fit: cover`.
- Text over a photo sits on the hero `scrim` gradient or on an `overlay` pill. The topo draws the line in `primary`, 6 units wide in a 400×500 viewBox, with round caps and dots at both ends.
- The Stitch photos are AI-generated placeholders; none ship with this system.

## Iconography

- **Material Symbols Outlined** from Google Fonts, by ligature name, 24px, weight 400, unfilled; the active bottom-bar item and stars use `FILL 1`. Load it with `<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0..1,0&display=swap" rel="stylesheet">` and write `<span class="fm-icon">explore</span>`.
- Names used by the screens: `menu`, `search`, `expand_more`, `location_on`, `chevron_right`, `arrow_back`, `favorite`, `workspace_premium`, `timer`, `wb_sunny`, `altitude`, `star`, `add`, `map`, `more_vert`, `tune`, `bolt`, `hardware`, `share`, `zoom_in`, `edit`, `bookmark`, `check_circle`; bottom bar `explore`, `menu_book`, `check_circle`, `person`.
- No logo: Stitch set the app name in type ("Crag Atlas"). Set the name in `screen-title`.

## Components

Static HTML components, styled by `components/bundle.css` on top of `tokens.css`; class names start with `fm-`. There is no JavaScript bundle. To use them, load `tokens.css`, then `components/bundle.css` (it imports Lexend and Material Symbols), and copy the markup from each component's preview. In Jetpack Compose, map them to composables in `:core-ui` reading `MaterialTheme` tokens.

## Not synced

- The two Stitch variants of each screen differ only in the bottom bar. This system follows the app: Discover, Logbook, Ticklist, Profile. Routes and Community items from the other variants are left out.
- The export defines three FAB shapes (16px square on crag details, 24px on the logbook, round on the route list); `Fab` keeps the first and the round one.
- Hover states and the photo zoom on card hover come from the web export; they have no touch equivalent.
