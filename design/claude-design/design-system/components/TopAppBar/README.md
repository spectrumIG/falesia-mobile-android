# TopAppBar

Sticky bar at the top of every screen: a centered title with a menu button, or back button with title, uppercase subtitle and actions.

- Provide: the title, an optional subtitle (crag or area, shown in `overline` and `primary`), a leading icon button (`menu` on top-level screens, `arrow_back` on detail screens) and up to two trailing icon buttons.
- Background is `bg-glass-80` with a 12px blur and a `primary-10` hairline; Discover uses `fm-topbar-strong` (`bg-glass-95`) because search and chips live inside it.
- Centered title: `screen-title`. Title with subtitle: `screen-title` (route list) or `title-sm` (route detail), left aligned.
- Every icon button needs an `aria-label`.
