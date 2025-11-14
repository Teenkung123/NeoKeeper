# Station editor deferred-create pass

- Recipe creation now defers config writes until admins hit `Save`; `StationDefinition#nextRecipeKey` hands new IDs to the GUI and the editor uses the new Delete button (config + GUI) so admins can back out without leaving phantom entries.
- Added `deleteRecipe` flow in `StationGUIHandler` that powers Shift+Right delete and the new button, also tearing down NPC-backed stations via `CitizensUtils.removeStationNpc` when their final recipe disappears.
- Player and editor recipe views reuse a new `StationLoreBuilder`, show real item lore before the “Click to craft” hint, and append configurable `[%have%/%need%]` counters to material names so the player view mirrors the listing UI.
- Config gained `Station.Player.Recipe.MaterialNameSuffix` plus editor delete button settings; no Gradle wrapper here so compile/test still pending locally.
