# Station material consolidation & npc id tweaks

- Added `StationMaterialUtils.mergeMaterials` and use it inside lore/rendering plus the crafting removal logic so duplicate ingredient stacks merge into one logical requirement (lore now shows e.g. `x128` but the GUI keeps separate stacks so totals still sum correctly).
- `/neokeeper station npc` now accepts an optional `[id]` parameter (use `-` to skip for random IDs while still providing a custom title); tab completion reflects the new syntax and `StationManager` can create NPC stations with a requested id.
- Station tab completion for `open/edit/remove` hides auto-generated `station_npc_*` ids, keeping those ephemeral stations out of common command suggestions.
