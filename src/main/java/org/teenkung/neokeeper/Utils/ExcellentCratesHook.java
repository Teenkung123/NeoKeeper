package org.teenkung.neokeeper.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ExcellentCratesHook {

    private static final String CRATES_API_CLASS = "su.nightexpress.excellentcrates.CratesAPI";

    private static boolean available = false;
    private static Object cachedKeyManager;

    private ExcellentCratesHook() {}

    public static boolean isAvailable() {
        ensureLoaded();
        return available;
    }

    public static ItemStack getKeyItem(String keyId) {
        Object manager = ensureLoaded();
        if (manager == null || keyId == null || keyId.isEmpty()) {
            return null;
        }
        try {
            Object crateKey = invoke(manager, "getKeyById", new Class[]{String.class}, keyId);
            if (crateKey == null) {
                return null;
            }
            Object keyStack = invoke(crateKey, "getItem");
            if (keyStack instanceof ItemStack stack) {
                return stack.clone();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String getKeyId(ItemStack stack) {
        Object manager = ensureLoaded();
        if (manager == null || stack == null) {
            return null;
        }
        try {
            Object crateKey = invoke(manager, "getKeyByItem", new Class[]{ItemStack.class}, stack);
            if (crateKey == null) {
                return null;
            }
            Object result = invoke(crateKey, "getId");
            if (result instanceof String id && !id.isEmpty()) {
                return id;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static Component getKeyDisplay(String keyId) {
        Object manager = ensureLoaded();
        if (manager == null || keyId == null || keyId.isEmpty()) {
            return null;
        }
        try {
            Object crateKey = invoke(manager, "getKeyById", new Class[]{String.class}, keyId);
            if (crateKey == null) {
                return null;
            }
            Object keyStack = invoke(crateKey, "getItem");
            if (keyStack instanceof ItemStack stack) {
                if (stack.hasItemMeta() && stack.getItemMeta().displayName() != null) {
                    return stack.getItemMeta().displayName();
                }
            }
            Object translated = invoke(crateKey, "getNameTranslated");
            if (translated instanceof String text && !text.isEmpty()) {
                return LegacyComponentSerializer.legacySection().deserialize(text);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Object ensureLoaded() {
        if (cachedKeyManager != null) {
            return cachedKeyManager;
        }
        try {
            Class<?> apiClass = Class.forName(CRATES_API_CLASS);
            Method method = apiClass.getMethod("getKeyManager");
            cachedKeyManager = method.invoke(null);
            available = cachedKeyManager != null;
        } catch (Throwable throwable) {
            available = false;
            cachedKeyManager = null;
        }
        return cachedKeyManager;
    }

    private static Object invoke(Object instance, String name, Class<?>[] parameterTypes, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = instance.getClass().getMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, params);
    }

    private static Object invoke(Object instance, String name)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invoke(instance, name, new Class[0], new Object[0]);
    }
}

