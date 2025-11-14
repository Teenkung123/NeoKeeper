package org.teenkung.neokeeper.Utils;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.teenkung.neokeeper.Managers.ItemManager;

public final class ItemComparison {

    private ItemComparison() {}

    public static boolean matches(ItemManager provided, ItemManager required) {
        if (provided == null || required == null) {
            return false;
        }

        boolean baseMatch = provided.getStringItem().equals(required.getStringItem())
                && provided.getType().equals(required.getType());

        if (!baseMatch) {
            return false;
        }

        boolean providedHasName = provided.hasDisplayName();
        boolean requiredHasName = required.hasDisplayName();

        if (providedHasName && requiredHasName && provided.getDisplayName() != null && required.getDisplayName() != null) {
            String providedName = PlainTextComponentSerializer.plainText().serialize(provided.getDisplayName());
            String requiredName = PlainTextComponentSerializer.plainText().serialize(required.getDisplayName());
            if (!providedName.equals(requiredName)) {
                return false;
            }
        }

        if (providedHasName != requiredHasName) {
            return false;
        }

        boolean providedHasStack = provided.getItem() != null;
        boolean requiredHasStack = required.getItem() != null;

        if (providedHasStack && requiredHasStack) {
            if (provided.getItem().getType() != required.getItem().getType()) {
                return false;
            }
        }

        return providedHasStack == requiredHasStack;
    }
}
