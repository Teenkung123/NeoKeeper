# Station editor crash fix

- Guarded all station GUI `NBT.modify` calls by cloning through a helper that replaces null/air/zero stacks with a barrier placeholder. This prevents the NBT API from throwing when recipes lack a configured result (see station GUI files).
- Added similar protection to station action buttons so missing config items don't propagate null stacks.
- Recipe editor now clears its editable slots after filling the frame so admins actually get empty slots to place items into instead of glass panes.
- Documented fixes in this log entry. Build still not re-run locally (gradle wrapper unavailable here); please build in your environment.
