package dev.Fjc.ultraBans.file;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import dev.Fjc.ultraBans.api.warns.WarnEntry;
import org.antlr.v4.runtime.atn.WildcardTransition;
import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.convert.DurationStyle;

import javax.lang.model.type.WildcardType;
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
        save(configuration, file);

        MuteSaver<@NotNull PlayerProfile> muteSaver = new MuteSaver<>();
        muteSaver.loadMap();
        return file.exists();
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

            for (String key : section.getKeys(false)) {
                UUID id = UUID.fromString(key);
                Player target = plugin.getServer().getPlayer(id);
                if (target == null) {
                    target = plugin.getServer().getPlayer(key);
                    if (target == null) continue;
                }
                String path = "mutes." + key + ".";

                String source = configuration.getString(path + "source");
                Date creation = new Date(configuration.getString(path + "creation"));
                Duration duration = DurationStyle.detectAndParse(configuration.getString(path + "duration", "0s"));
                String reason = configuration.getString(path + "reason");

                MuteEntry<@NotNull PlayerProfile> putter = new MuteEntry<>(target.getPlayerProfile(), source, creation, duration, reason);
                finished.put(
                        target.getPlayerProfile(),
                        putter
                );
                manager.register(putter);
            }
            return finished;
        }

        public boolean save(MuteEntry<P> entry) {
            String path = "mutes." + entry.getMuteTarget() + ".";
            if (entry.getMuteTarget() instanceof PlayerProfile profile) {
                path = "mutes." + profile.getId() + ".";
                configuration.set(path + "target", profile.getName());
            }
            else configuration.set(path + "target", entry.getMuteTarget());
            configuration.set(path + "source", entry.getSource());
            configuration.set(path + "creation", entry.getCreated().toString());
            configuration.set(path + "expiration", entry.getExpiry().toString());
            configuration.set(path + "duration", entry.getDuration());
            configuration.set(path + "reason", entry.getReason());

            return EntrySaver.save(configuration, file);
        }

        public boolean remove(MuteEntry<P> entry) {
            String path = "mutes." + entry.getMuteTarget() + ".";
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
            for (String key : section.getKeys(false)) {
                String path = "warns." + key + ".";
                final Player target = checkArgs(key);
                if (target == null) continue;

                String source = configuration.getString(path + "source");
                LocalDateTime createdAt = LocalDateTime.parse(configuration.getString(path + "creation", new Date().toString()), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String reason = configuration.getString(path + "reason");

                WarnEntry<String> entry = new WarnEntry<>(
                        target.getName(), source, createdAt, reason
                );
                entries.putIfAbsent(target.getName(), List.of());
                entries.get(target.getName()).add(entry);
            }

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
            configuration.set(path + "source", entry.getSource());
            configuration.set(path + "creation", entry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            configuration.set(path + "reason", entry.getReason());

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



}
