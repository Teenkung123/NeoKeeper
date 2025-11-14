# Station UI polish

- Added `ItemTextFormatter` so station lore uses friendly display names (keeping custom colors) instead of serialized stacks, fixing the vanilla item readability issue.
- Player/editor recipe entries now read lore templates from config (`Station.*.RecipeLore`) with configurable headers/entries; color-aware names are injected via `%item%` placeholders.
- Player list now hides navigation buttons when not applicable, only shows a Close button per request, and the crafting menu only exposes a Back button.
- Editor list drop mode removed; recipes are deleted via shift-right click, and the handler clamps pages when navigating/removing. Player/Editor GUIs also skip rendering unused nav buttons.
- Updated `config.yml` defaults (and mirrored to `bin/main/config.yml`) to reflect the new lore templates and button layout.
- Gradle build still not rerun locally (wrapper missing); please rebuild in your environment.
