# Station material display polish

- Editor listing lore now uses the same aggregation logic as the player view, so duplicate ingredient stacks merge into a single line (e.g. `x128 Stone`).
- Player recipe GUI still shows individual ingredient stacks for clicking, but the `[have/need]` suffix is now calculated per stack so multi-stack recipes display `64/64` then `1/64` when the player partially meets the requirement.
