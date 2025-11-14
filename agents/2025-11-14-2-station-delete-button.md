# Station listing delete control

- Removed the recipe-level delete button and added a `Station.Editor.List` delete control so admins can wipe a station directly from the listing; the GUI now renders the new button and `StationGUIHandler` handles the `DELETE` action by tearing down the station (including any bound NPC) and closing the inventory.
- Recipe editor keeps its add/save/back-only layout; Shift+Right still removes individual recipes, and if that was the last recipe in an NPC station the existing auto-delete logic still reclaims the NPC + station file.
- Config updated to reflect the new button placement/text so the listing makes it obvious this is a full-station delete.
