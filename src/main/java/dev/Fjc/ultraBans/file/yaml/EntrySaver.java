package dev.Fjc.ultraBans.file.yaml;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.Util;
import dev.Fjc.ultraBans.api.Entry;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import dev.Fjc.ultraBans.api.warns.WarnEntry;
import dev.Fjc.ultraBans.builders.Checker;
import org.bukkit.BanEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents an entry saver. An {@link EntrySaver} contains methods to save entries to a file.
 */
public class EntrySaver {

    private static final UltraBans plugin = UltraBans.getInstance();

    private static File file;

    private static YamlConfiguration configuration;

    /**
     * Represents an entry saver. An {@link EntrySaver} contains methods to save entries to a file.
     */
    public EntrySaver() {}

    public static boolean loadEverything() {
        file = new File(plugin.getDataFolder(), "entries.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
        configuration.options().setHeader(List.of(
                "Welcome to entries.yml",
                "This file contains entries of all punishments handled by the UltraBans plugin.",
                "It's not recommended to modify these directly, as it may corrupt the player profile."
        ));
        Util.info("Load successful.");
        save(configuration, file);

        MuteSaver<@NotNull PlayerProfile> muteSaver = new MuteSaver<>();
        muteSaver.loadMap();
        return configuration != null;
    }

    public static class BanSaver<T> {
        static String base = "bans.";
        /**
         * Saves a ban entry to the file.
         * @param entry The ban entry to save to the file
         * @return Whether the save was successful
         */
        public boolean save(BanEntry<T> entry) {
            String path = base + entry.getBanTarget() + ".";
            configuration.setComments(base, List.of(
                    "The following lines contain info on ban entries."
            ));

            configuration.set(path + "target", entry.getBanTarget().toString());
            configuration.set(path + "source", entry.getSource());
            configuration.set(path + "creation", entry.getCreated());
            configuration.set(path + "expiration", entry.getExpiration());
            configuration.set(path + "reason", entry.getReason());

            return EntrySaver.save(configuration, file);
        }
    }

    public static class MuteSaver<P> {

        /**
         * Loads all valid mutes from the disk into memory. If the expiration time is reached,
         * the player is considered to be unmuted and the entry is discarded.
         * @return A map of targets and their entries
         */
        public Map<PlayerProfile, MuteEntry<@NotNull PlayerProfile>> loadMap() {

            Map<PlayerProfile, MuteEntry<@NotNull PlayerProfile>> finished = new HashMap<>();
            MuteEntry.Manager manager = new MuteEntry.Manager();

            ConfigurationSection section = configuration.getConfigurationSection("mutes");
            if (section == null) return finished;

            loadFromConfiguration(section, Entry.Type.MUTE).stream()
                    .filter(pred -> pred instanceof MuteEntry<?>)
                    .map(func -> (MuteEntry<?>) func)
                    .filter(p -> p.getTarget() instanceof PlayerProfile)
                    .map(f -> (MuteEntry<PlayerProfile>) f)
                    .forEach(a -> {
                        finished.put(a.getTarget(), a);
                        manager.register(a);
                    });
            return finished;
        }

        public Map<String, MuteEntry<String>> loadStringMap() {

            Map<String, MuteEntry<String>> entries = new HashMap<>();
            MuteEntry.Manager manager = new MuteEntry.Manager();

            ConfigurationSection section = configuration.getConfigurationSection("mutes");
            if (section == null) return entries;

            loadFromConfiguration(section, Entry.Type.MUTE).stream()
                    .filter(pred -> pred instanceof MuteEntry<?>)
                    .map(func -> (MuteEntry<?>) func)
                    .filter(pred -> pred.getTarget() instanceof String)
                    .map(func -> (MuteEntry<String>) func)
                    .forEach(action -> {
                        entries.put(action.getTarget(), action);
                        manager.register(action);
                    });

            return entries;
        }

        public boolean save(MuteEntry<P> entry) {
            String path = "mutes." + entry.getTarget() + ".";
            if (entry.getTarget() instanceof PlayerProfile profile) {
                path = "mutes." + profile.getId() + ".";
                configuration.set(path + "target", profile.getName());
            }
            else configuration.set(path + "target", entry.getTarget());
            configuration.set(path + "source", entry.source());
            configuration.set(path + "creation", entry.getCreated().toString());
            configuration.set(path + "expiration", entry.getExpiry().toString());
            configuration.set(path + "duration", entry.getDuration());
            configuration.set(path + "reason", entry.reason());

            return EntrySaver.save(configuration, file);
        }

        public boolean remove(MuteEntry<P> entry) {
            String path = "mutes." + entry.getTarget() + ".";
            configuration.set(path, null);

            return EntrySaver.save(configuration, file);
        }
    }

    public static class WarnSaver<R> {

        /**
         * Loads all warn entries from the disk into memory. A target {@link R} may have multiple entries, so
         * a list will be initialized. Warns do not expire so they will never be invalid unless manually altered.
         * @return A map of a target {@link R} and a list of warn entries
         */
        public Map<PlayerProfile, List<WarnEntry<@NotNull PlayerProfile>>> loadMap() {

            Map<PlayerProfile, List<WarnEntry<@NotNull PlayerProfile>>> entries = new HashMap<>();
            ConfigurationSection section = configuration.getConfigurationSection("warns");

            if (section == null) return entries;
            for (String key : section.getKeys(false)) {
                String path = "warns." + key + ".";
                final Player target = checkArgs(key);
                if (target == null) continue;

                String source = configuration.getString(path + "source");
                LocalDateTime createdAt = LocalDateTime.parse(configuration.getString(path + "creation", new Date().toString()));
                String reason = configuration.getString(path + "reason");

                WarnEntry<@NotNull PlayerProfile> entry = new WarnEntry<>(
                        target.getPlayerProfile(), source, createdAt, reason
                );
                entries.get(target.getPlayerProfile()).add(entry);
            }

            return entries;
        }

        public Map<String, List<WarnEntry<String>>> loadStringMap() {
            Map<String, List<WarnEntry<String>>> entries = new HashMap<>();
            ConfigurationSection section = configuration.getConfigurationSection("warns");

            if (section == null) return entries;
            loadFromConfiguration(section, Entry.Type.WARN).stream()
                    .filter(p -> p instanceof WarnEntry<?>)
                    .map(f -> (WarnEntry<?>) f)
                    .filter(p -> p.getTarget() instanceof String)
                    .map(f -> (WarnEntry<String>) f)
                    .forEach(a -> {
                        entries.putIfAbsent(a.getTarget(), List.of());
                        entries.get(a.getTarget()).add(a);
                    });
            return entries;
        }

        public boolean save(WarnEntry<R> entry) {
            String path = "warns." + entry.getTarget() + ".";
            if (entry.getTarget() instanceof PlayerProfile profile) {
                path = "warns." + profile.getId() + ".";
                configuration.set(path + "target", profile.getName());
            } else {
                configuration.set(path + "target", entry.getTarget());
            }
            configuration.set(path + "source", entry.source());
            configuration.set(path + "creation", entry.creationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configuration.set(path + "reason", entry.reason());

            return EntrySaver.save(configuration, file);
        }

        public boolean remove(WarnEntry<R> entry) {
            String path = "warns." + entry.getTarget() + ".";
            configuration.set(path, null);

            return EntrySaver.save(configuration, file);
        }
    }

    /**
     * Check if the provided player name actually exists.
     * @param name The name of a player, as a string
     * @return A {@link Player}, or null if no match exists
     */
    private static @Nullable Player checkArgs(String name) {
        Player player;
        UUID id;
        try {
            id = UUID.fromString(name);
            player = null;
        } catch (IllegalArgumentException e) {
            id = null;
            player = plugin.getServer().getPlayerExact(name);
        }
        if (id != null) return plugin.getServer().getPlayer(id);

        return player;
    }

    private static List<Entry<?>> loadFromConfiguration(ConfigurationSection section, Entry.Type type) {
        List<Entry<?>> entries = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            Player target = checkArgs(key);
            if (target == null) continue;

            String path = switch (type) {
                case MUTE -> "mutes." + key + ".";
                case WARN -> "warns." + key + ".";
                case BAN -> "bans." + key + ".";
                case KICK -> "kick." + key + ".";
                case UNKNOWN -> null;
            };

            if (path == null) return entries;
            String source = configuration.getString(path + "source");
            Date creation = Checker.parse(path + "creation");
            @Nullable Duration duration = Util.parse(configuration.getString(path + "duration", "0s"));
            @Nullable LocalDateTime createdAt = LocalDateTime.parse(configuration.getString(path + "creation", new Date().toString()), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String reason = configuration.getString(path + "reason");


            switch (type) {
                case Entry.Type.MUTE -> entries.add(new MuteEntry<>(target, source, creation, duration, reason));
                case WARN -> entries.add(new WarnEntry<>(target, source, createdAt, reason));
            }
        }

        return entries;
    }

    private static boolean save(YamlConfiguration yaml, File file) {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        try {
            yaml.save(file);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            plugin.getLogger().warning("Something went wrong while attempting to save entries.");
            plugin.getLogger().warning(e.getLocalizedMessage());
        }

        return false;
    }

    public static @Nullable YamlConfiguration getConfiguration() {
        return configuration;
    }
}
