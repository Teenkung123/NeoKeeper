# Station multi-slot buttons

- Updated all station GUI button placement helpers to accept both `Slot` and `Slots` entries simultaneously, so any control (close, back, etc.) can now populate multiple locations without additional code changes.
- Config remains compatible; specifying either key or both now duplicates the button into each slot.
- Mirrored config changes to `bin/main/config.yml`. Build still not rerun locally (missing Gradle wrapper).
