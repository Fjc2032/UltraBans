package dev.Fjc.ultraBans.builders;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.warns.WarnEntry;
import dev.Fjc.ultraBans.builders.data.MutedDataType;
import dev.Fjc.ultraBans.file.EntrySaver;
import dev.Fjc.ultraBans.file.Keys;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Punishment {

    public static class BanBuilder {
        private final UltraBans plugin = UltraBans.getInstance();

        private final @NotNull Player player;
        private final @Nullable Player executor;

        private @Nullable String reason;
        private @Nullable String source;

        private @Nullable Duration duration;

        public BanBuilder(@NotNull Player player, @Nullable Player executor) {
            this.player = player;
            this.executor = executor;
        }

        public BanBuilder setDuration(Duration duration) {
            this.duration = duration;

            return this;
        }

        public BanBuilder setReason(@Nullable String reason) {
            if (reason == null) reason = "No reason specified";
            this.reason = ChatColor.translateAlternateColorCodes('&', reason);

            return this;
        }

        public BanBuilder setSource(@Nullable String source) {
            this.source = source;

            return this;
        }

        public boolean ban(boolean broadcast) {
            BanEntry<PlayerProfile> entry = player.ban(reason, duration != null ? duration : ChronoUnit.FOREVER.getDuration(), executor != null ? executor.getName() : source, true);
            if (entry != null) entry.save();

            if (broadcast) plugin.getServer().broadcast(Component.text(player.getName() + " was banned by " + source + " with reason " + reason + ". The ban will last " + duration));
            EntrySaver.BanSaver<PlayerProfile> saver = new EntrySaver.BanSaver<>();
            saver.save(entry);
            return player.isBanned();
        }
    }

    public static class MuteBuilder {

        private final Player player;
        private final @Nullable Player executor;

        private @NotNull String reason = "No reason specified.";

        private @Nullable Duration duration;

        /**
         * A builder to construct a new {@link MuteEntry}
         * @param player Player being muted
         * @param executor Player doing the muting, or null for a default executor
         */
        public MuteBuilder(Player player, @Nullable Player executor) {
            this.player = player;
            this.executor = executor;
        }

        public MuteBuilder setReason(@Nullable String reason) {
            if (reason == null) return this;
            this.reason = reason;

            return this;
        }

        public MuteBuilder setDuration(@Nullable Duration duration) {
            this.duration = duration;

            return this;
        }

        public MuteBuilder blockCommands() {
            PersistentDataContainer container = player.getPersistentDataContainer();
            container.set(Keys.commandsBlocked, new MutedDataType(), reason);

            return this;
        }

        public boolean mute() {
            PersistentDataContainer container = player.getPersistentDataContainer();
            container.set(Keys.mutedKey, new MutedDataType(), reason);


            MuteEntry<@NotNull PlayerProfile> entry = new MuteEntry<>(
                    player.getPlayerProfile(),
                    executor != null ? executor.getName() : Bukkit.getConsoleSender().getName(),
                    new Date(),
                    duration,
                    reason);
            return entry.save();
        }
    }

    public static class WarnBuilder {

        private final Player player;
        private final Player executor;

        private @Nullable String reason;

        public WarnBuilder(Player player, Player executor) {
            this.player = player;
            this.executor = executor;
        }

        public WarnBuilder setReason(@Nullable String reason) {
            this.reason = reason;
            return this;
        }

        public boolean warn() {
            WarnEntry<@NotNull PlayerProfile> entry = new WarnEntry<>(
                    player.getPlayerProfile(),
                    executor.getName(),
                    LocalDateTime.now(),
                    reason
            );
            return entry.save();
        }
    }
}
