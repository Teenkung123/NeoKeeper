package org.teenkung.neokeeper.Managers.Stations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.teenkung.neokeeper.GUIs.station.StationEditorListGUI;
import org.teenkung.neokeeper.GUIs.station.StationRecipeEditorGUI;
import org.teenkung.neokeeper.GUIs.station.StationPlayerListGUI;
import org.teenkung.neokeeper.GUIs.station.StationPlayerRecipeGUI;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StationDefinition {

    private final NeoKeeper plugin;
    private final File file;
    private final YamlConfiguration config;
    private final String id;

    private Component title;
    private List<StationRecipe> recipes;
    private Map<String, StationRecipe> recipeIndex;

    private StationPlayerListGUI playerListGUI;
    private StationPlayerRecipeGUI playerRecipeGUI;
    private StationEditorListGUI editorListGUI;
    private StationRecipeEditorGUI recipeEditorGUI;

    public StationDefinition(NeoKeeper plugin, File file, YamlConfiguration config, String id) {
        this.plugin = plugin;
        this.file = file;
        this.config = config;
        this.id = id;
        reload();
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public String getPlainTitle() {
        return PlainTextComponentSerializer.plainText().serialize(title);
    }

    public List<StationRecipe> getRecipes() {
        return recipes;
    }

    public StationRecipe getRecipe(String key) {
        return recipeIndex.get(key);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public StationPlayerListGUI getPlayerListGUI() {
        if (playerListGUI == null) {
            playerListGUI = new StationPlayerListGUI(plugin, this);
        }
        return playerListGUI;
    }

    public StationEditorListGUI getEditorListGUI() {
        if (editorListGUI == null) {
            editorListGUI = new StationEditorListGUI(plugin, this);
        }
        return editorListGUI;
    }

    public StationPlayerRecipeGUI getPlayerRecipeGUI() {
        if (playerRecipeGUI == null) {
            playerRecipeGUI = new StationPlayerRecipeGUI(plugin, this);
        }
        return playerRecipeGUI;
    }

    public StationRecipeEditorGUI getRecipeEditorGUI() {
        if (recipeEditorGUI == null) {
            recipeEditorGUI = new StationRecipeEditorGUI(plugin, this);
        }
        return recipeEditorGUI;
    }

    public void reload() {
        loadTitle();
        loadRecipes();
        invalidateGuiCaches();
    }

    private void invalidateGuiCaches() {
        this.playerListGUI = null;
        this.playerRecipeGUI = null;
        this.editorListGUI = null;
        this.recipeEditorGUI = null;
    }

    private void loadTitle() {
        String rawTitle = config.getString("Option.Title", "Station");
        this.title = plugin.colorize(rawTitle);
    }

    private void loadRecipes() {
        ConfigurationSection recipesSection = config.getConfigurationSection("Recipes");
        if (recipesSection == null) {
            this.recipes = Collections.emptyList();
            this.recipeIndex = Collections.emptyMap();
            return;
        }

        List<String> keys = new ArrayList<>(recipesSection.getKeys(false));
        keys.sort((a, b) -> {
            try {
                int ai = Integer.parseInt(a);
                int bi = Integer.parseInt(b);
                return Integer.compare(ai, bi);
            } catch (NumberFormatException ex) {
                return a.compareToIgnoreCase(b);
            }
        });

        List<StationRecipe> parsed = new ArrayList<>();
        Map<String, StationRecipe> indexed = new LinkedHashMap<>();
        for (String recipeKey : keys) {
            ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeKey);
            if (recipeSection == null) {
                continue;
            }
            StationRecipe recipe = new StationRecipe(recipeKey, recipeSection);
            parsed.add(recipe);
            indexed.put(recipeKey, recipe);
        }

        this.recipes = Collections.unmodifiableList(parsed);
        this.recipeIndex = Collections.unmodifiableMap(indexed);
    }

    public String nextRecipeKey() {
        ConfigurationSection recipesSection = config.getConfigurationSection("Recipes");
        if (recipesSection == null) {
            return "0";
        }
        int nextId = 0;
        for (String key : recipesSection.getKeys(false)) {
            try {
                nextId = Math.max(nextId, Integer.parseInt(key) + 1);
            } catch (NumberFormatException ignored) {
                // ignore non numeric keys
            }
        }
        while (recipesSection.isSet(String.valueOf(nextId))) {
            nextId++;
        }
        return String.valueOf(nextId);
    }

    public void removeRecipe(String recipeKey) {
        ConfigurationSection recipesSection = config.getConfigurationSection("Recipes");
        if (recipesSection == null) {
            return;
        }
        recipesSection.set(recipeKey, null);
        saveConfig();
        reload();
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save station " + id + ": " + e.getMessage());
        }
    }
}
