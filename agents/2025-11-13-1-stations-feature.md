# Station feature rollout

## Data and persistence
- Introduced `StationManager`, `StationDefinition`, and `StationRecipe` (in `org.teenkung.neokeeper.Managers.Stations`) to load/save stations from `Stations/`.
- Each station tracks metadata + recipes (variable-length material lists + single result) with helper APIs for GUI access.
- Added sample configuration stub at `src/main/resources/Stations/exampleStation.yml` and ensured files are created under `plugins/NeoKeeper/Stations`.

## GUIs and handlers
- Added full player/editor GUI stack under `org.teenkung.neokeeper.GUIs.station` (list + recipe views, editor list, editor recipe, shared constants/item builder).
- Built dedicated `StationGUIHandler` to process all station inventory events, craft items (with custom item support via new `ItemComparison` util), handle pagination, add/remove flows, and enforce validation.
- Integrated handler registration + safe shutdown closing in `NeoKeeper`.

## Commands and config
- Added `/neokeeper station <open|edit|create|remove>` command (`StationCommand`) with tab completion + help updates.
- Extended `config.yml` with `Station` section covering player/editor GUI layout, fillers, and button metadata.
- Added `ConfigItemBuilder` + `ItemComparison` utilities and updated villager trade handler to reuse comparison logic.

## Misc
- Updated `agents` log and attempted `gradlew build` (wrapper missing in repo, so build could not be run locally).
