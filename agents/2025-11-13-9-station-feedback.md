# Station feedback + NPC support

- Added `StationFeedbackConfig` with configurable sounds/messages for trade success/failure and GUI actions (back/next/prev/save/etc.), hooked throughout `StationGUIHandler`.
- Player recipe list now shows ?/? indicators (configurable) and counts based on the viewer’s inventory. Player/editor recipe GUIs honour per-config material/result slots.
- Extended Citizens integration: station NPC creation/binding commands (`/neokeeper station npc|bindnpc`), station-aware NPC listeners, and extra APIs in `CitizensUtils`/`StationManager`.
- Updated default `config.yml` (and `bin/main/config.yml`) with the new feedback + slot sections, status indicators, and IA GUI layout.
- Documented this work in attempt 9.
