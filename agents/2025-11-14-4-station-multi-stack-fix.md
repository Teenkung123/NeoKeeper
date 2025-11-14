# Station multi-stack material fix

- Reworked `StationMaterialUtils` into an aggregation helper that keeps a template item plus the total required amount, so lore and player requirement checks can collapse duplicate stacks without ever trying to render >64-sized stacks.
- Updated `StationLoreBuilder` to consume the aggregated entries (showing `x128` etc.) and `StationGUIHandler` to plan removals based on the total aggregated amount, fixing both the command crash and the inaccurate “two separate stacks” material descriptions.
