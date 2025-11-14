# Station material sorting

- StationMaterialUtils.aggregate now orders aggregated entries by descending total amount, matching the visual priority the player/ editor lore expects. Added sortByAmountDesc helper so raw material stacks can be displayed in the same priority order without mutating stored recipes.
- Station recipe editor/player GUIs both call the new helper before populating material slots, so duplicated stacks appear grouped from largest to smallest (e.g., 64->64->23) while lore aggregation still shows merged totals (151, 8, 7, 5).
- Tests still blocked locally because the Gradle wrapper is absent in this repo; please run your usual Gradle build in an environment where the wrapper (or system Gradle) is available.
