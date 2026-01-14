package dev.Fjc.ultraBans.api;

import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.kick.KickEntry;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import dev.Fjc.ultraBans.api.warns.WarnEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an entry, which contains basic information about a punishment applied on an object.
 * @param <V> The object receiving the punishment
 */
public interface Entry<V> {

    /**
     * Returns the target of this punishment entry.
     * @return The target
     */
    V getTarget();

    /**
     * Returns the source of the punishment (the name of the executor)
     * @return The source
     */
    String source();

    /**
     * Returns the reason for this entry.
     * @return The reason
     */
    String reason();

    /**
     * Returns the {@link LocalDateTime} this entry was created.
     * @return The time, without respect to timezones
     */
    LocalDateTime creationTime();

    Type type();

    /**
     * Generates a random UUID for this entry. Override this if you want to set your own ID.
     * @return a UUID
     */
    default UUID id() {
        return UUID.randomUUID();
    }

    default String targetToString() {
        var target = getTarget();
        final UltraBans plugin = UltraBans.getInstance();
        Player player = plugin.getServer().getPlayer(target.toString());
        if (player != null) return player.getName();
        else {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(target.toString());
            return offlinePlayer.getName();
        }
    }

    /**
     * An abstract enum that represents different punishment types. This only holds data types and should NOT
     * be used to pass any actual entries!
     */
    enum Type {
        BAN(null),
        KICK(new KickEntry<>(null)),
        WARN(new WarnEntry<>(null)),
        MUTE(new MuteEntry<>()),
        /**
         * An unknown punishment type.
         */
        UNKNOWN();

        private Entry<?> entry;

        Type() {
        }

        Type(Entry<?> entry) {
            this.entry = entry;
        }
    }

}
