package org.teenkung.neokeeper.Managers.Stations;

import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Utils.ItemComparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class StationMaterialUtils {

    private StationMaterialUtils() {}

    /**
     * Aggregates the provided material list into logical stacks while preserving insertion order.
     * The returned entries keep the first encountered item template and sum the total amount separately.
     */
    public static List<AggregatedMaterial> aggregate(List<ItemManager> materials) {
        if (materials == null || materials.isEmpty()) {
            return Collections.emptyList();
        }

        List<Aggregation> aggregations = new ArrayList<>();
        for (ItemManager material : materials) {
            if (material == null) {
                continue;
            }
            Aggregation existing = findAggregation(aggregations, material);
            int addition = Math.max(0, material.getAmount() != null ? material.getAmount() : 0);
            if (existing == null) {
                ItemManager template = duplicateTemplate(material);
                aggregations.add(new Aggregation(template, addition));
            } else {
                existing.add(addition);
            }
        }

        List<AggregatedMaterial> result = new ArrayList<>(aggregations.size());
        for (Aggregation aggregation : aggregations) {
            result.add(new AggregatedMaterial(aggregation.template, aggregation.amount));
        }
        result.sort(Comparator.comparingInt(AggregatedMaterial::amount).reversed());
        return result;
    }

    private static Aggregation findAggregation(List<Aggregation> aggregations, ItemManager candidate) {
        for (Aggregation aggregation : aggregations) {
            if (ItemComparison.matches(aggregation.template, candidate)) {
                return aggregation;
            }
        }
        return null;
    }

    public static List<ItemManager> sortByAmountDesc(List<ItemManager> materials) {
        if (materials == null || materials.isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemManager> sorted = new ArrayList<>(materials);
        sorted.sort(Comparator.comparingInt(StationMaterialUtils::safeAmount).reversed());
        return Collections.unmodifiableList(sorted);
    }

    private static int safeAmount(ItemManager material) {
        if (material == null || material.getAmount() == null) {
            return 0;
        }
        return Math.max(0, material.getAmount());
    }

    public static int findAggregationIndex(List<AggregatedMaterial> aggregated, ItemManager candidate) {
        if (aggregated == null || aggregated.isEmpty() || candidate == null) {
            return -1;
        }
        for (int i = 0; i < aggregated.size(); i++) {
            if (ItemComparison.matches(aggregated.get(i).template(), candidate)) {
                return i;
            }
        }
        return -1;
    }

    private static ItemManager duplicateTemplate(ItemManager original) {
        ItemStack stack = original.getItem();
        if (stack != null) {
            ItemStack clone = stack.clone();
            // Ensure the stack kept for display stays within normal bounds (amount > 0)
            clone.setAmount(Math.max(1, Math.min(clone.getAmount(), clone.getMaxStackSize())));
            return new ItemManager(clone);
        }
        return new ItemManager(original.getType(), original.getStringItem(), original.getAmount());
    }

    private static final class Aggregation {
        private final ItemManager template;
        private int amount;

        private Aggregation(ItemManager template, int initialAmount) {
            this.template = template;
            this.amount = Math.max(0, initialAmount);
        }

        private void add(int addition) {
            this.amount += Math.max(0, addition);
        }
    }

    public record AggregatedMaterial(ItemManager template, int amount) {}
}
