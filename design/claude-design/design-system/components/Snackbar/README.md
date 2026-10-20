# Snackbar

One-shot feedback after an action, shown above the bottom bar for about 4 seconds; it never blocks the screen.

- Provide: a status icon, one short sentence, and at most one action (Retry, Settings). Errors with an action stay until dismissed or acted on.
- Fill `inverse-surface`, text `on-inverse`, action `inverse-primary`, `radius-lg`, `shadow-lg`. Icons: `inverse-success` (done), `inverse-danger` (failed), `inverse-info` (offline, info).
- Copy says what happened and what the user still has: "Couldn't refresh. Showing saved data." Never show raw exception messages.
- One at a time. A newer snackbar replaces the current one.
