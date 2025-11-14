# Station configurability enhancements

- Player recipe list lore now supports status indicators driven by `RecipeLore.StatusIndicator` config (custom prefixes, on/off, optional count suffix). The GUI uses actual player inventory data to show ?/? and counts inline.
- Player crafting view reads `Station.Player.Recipe.MaterialSlots` and `ResultSlot`, so layouts are fully configurable; handler logic queries the GUI for the configured result slot.
- Updated default `config.yml` (and mirrored under `bin/main/config.yml`) to match your provided layout, lore templates, IA items, and multi-slot controls.
- Added helper methods and accessors (`StationPlayerRecipeGUI#getResultSlot` etc.) used by the handler.
- Build still not run locally (Gradle wrapper absent); please rebuild in your environment.
