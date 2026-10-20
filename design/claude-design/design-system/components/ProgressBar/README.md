# ProgressBar

Determinate progress for long work the user waits for, such as the first download of all crags.

- Provide: a fraction (0–1) and a label with counts ("37 of 110 crags").
- 8px track in `primary-10`, fill `primary`, `radius-full`; meta row in `caption`, `ink-muted`.
- Use only when real progress is known; otherwise use an indeterminate indicator.
