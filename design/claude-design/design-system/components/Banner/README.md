# Banner

Persistent, inline notice at the top of a list about the state of the data; it stays while the condition lasts.

- Provide: an icon, a short title, one line of detail, and optionally a text action.
- Neutral (`note` fill) for offline, warning (`primary-10`) for old data, error (`danger-soft`, `danger` icon) for a failed background sync.
- Use a banner when the user must keep knowing it (offline, data a week old); use a snackbar for one-shot results.
