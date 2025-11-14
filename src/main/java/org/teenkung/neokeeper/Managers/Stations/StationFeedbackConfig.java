package org.teenkung.neokeeper.Managers.Stations;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StationFeedbackConfig {

    private final NeoKeeper plugin;
    private final FeedbackEntry tradeSuccess;
    private final FeedbackEntry tradeFail;
    private final Map<String, SoundSetting> guiSounds = new HashMap<>();

    public StationFeedbackConfig(NeoKeeper plugin) {
        this.plugin = plugin;
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("Station.Feedback");

        tradeSuccess = FeedbackEntry.fromConfig(plugin, root, "Trade.Success",
                "<green>Successfully crafted the item.",
                "ENTITY_PLAYER_LEVELUP", 1.0f, 1.2f);

        tradeFail = FeedbackEntry.fromConfig(plugin, root, "Trade.Fail",
                "<red>You don't have the required materials.",
                "BLOCK_NOTE_BLOCK_BASS", 1.0f, 0.6f);

        ConfigurationSection guiSection = root != null ? root.getConfigurationSection("GUI.Actions") : null;
        if (guiSection != null) {
            for (String key : guiSection.getKeys(false)) {
                ConfigurationSection actionSection = guiSection.getConfigurationSection(key);
                SoundSetting sound = SoundSetting.fromConfig(plugin, actionSection != null ? actionSection.getConfigurationSection("Sound") : null,
                        null, 0.0f, 0.0f);
                guiSounds.put(key.toUpperCase(Locale.ROOT), sound);
            }
        }
    }

    public void notifyTradeSuccess(Player player) {
        if (tradeSuccess != null) {
            tradeSuccess.notify(player);
        }
    }

    public void notifyTradeFail(Player player) {
        if (tradeFail != null) {
            tradeFail.notify(player);
        }
    }

    public void notifyGuiAction(Player player, String action) {
        if (action == null) {
            return;
        }
        SoundSetting sound = guiSounds.get(action.toUpperCase(Locale.ROOT));
        if (sound != null) {
            sound.play(player);
        }
    }

    private static class FeedbackEntry {
        private final NeoKeeper plugin;
        private final String message;
        private final SoundSetting sound;

        private FeedbackEntry(NeoKeeper plugin, String message, SoundSetting sound) {
            this.plugin = plugin;
            this.message = message;
            this.sound = sound;
        }

        static FeedbackEntry fromConfig(NeoKeeper plugin, @Nullable ConfigurationSection root, String path,
                                        String defaultMessage, String defaultSound, float defaultVolume, float defaultPitch) {
            ConfigurationSection section = root != null ? root.getConfigurationSection(path) : null;
            String message = section != null ? section.getString("Message", defaultMessage) : defaultMessage;
            SoundSetting sound = SoundSetting.fromConfig(plugin, section != null ? section.getConfigurationSection("Sound") : null,
                    defaultSound, defaultVolume, defaultPitch);
            return new FeedbackEntry(plugin, message, sound);
        }

        void notify(Player player) {
            if (message != null && !message.isBlank()) {
                player.sendMessage(plugin.colorize(message));
            }
            if (sound != null) {
                sound.play(player);
            }
        }
    }

    private static class SoundSetting {
        private final boolean enabled;
        private final Sound sound;
        private final float volume;
        private final float pitch;

        private SoundSetting(boolean enabled, Sound sound, float volume, float pitch) {
            this.enabled = enabled;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        static SoundSetting fromConfig(NeoKeeper plugin, @Nullable ConfigurationSection section,
                                       @Nullable String defaultSoundName, float defaultVolume, float defaultPitch) {
            boolean enabled = section == null || section.getBoolean("Enabled", defaultSoundName != null);
            String soundName = section != null ? section.getString("Name", defaultSoundName) : defaultSoundName;
            float volume = section != null ? (float) section.getDouble("Volume", defaultVolume) : defaultVolume;
            float pitch = section != null ? (float) section.getDouble("Pitch", defaultPitch) : defaultPitch;
            Sound sound = null;
            if (soundName != null && !soundName.isBlank()) {
                try {
                    sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Unknown sound '" + soundName + "' in config.");
                    enabled = false;
                }
            } else {
                enabled = false;
            }
            return new SoundSetting(enabled, sound, volume, pitch);
        }

        void play(Player player) {
            if (!enabled || sound == null || player == null) {
                return;
            }
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}
