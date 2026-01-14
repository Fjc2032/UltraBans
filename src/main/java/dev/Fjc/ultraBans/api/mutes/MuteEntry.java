package dev.Fjc.ultraBans.api.mutes;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.Entry;
import dev.Fjc.ultraBans.api.mutes.backers.StringMuteList;
import dev.Fjc.ultraBans.file.Keys;
import dev.Fjc.ultraBans.api.mutes.backers.UltraMuteList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a mute entry.
 * @param <P> A player conforming object that will be assigned to this entry
 */
public class MuteEntry<P> implements Entry<P> {

    private final P muteTarget;
    private String source;

    private Date creationTime;
    private @Nullable Long durationAsLong;
    private @Nullable Duration duration;

    private @Nullable String reason;

    /**
     * A class that represents a mute entry. A mute entry contains basic information about a player who was muted.
     * @param muteTarget The target being muted
     * @param source Who is muting the target. Can be any string, but it's recommended to put a unique identifier or UUID
     * @param creationTime The date this was created
     * @param durationAsLong How long the mute will last
     * @param reason The reason assigned for this mute. If left null, a default reason will be applied.
     */
    public MuteEntry(@NotNull P muteTarget, String source, Date creationTime, @Nullable Long durationAsLong, @Nullable String reason) {
        this.muteTarget = muteTarget;
        this.source = source;
        this.creationTime = creationTime;
        this.durationAsLong = durationAsLong;
        this.reason = reason;
    }

    /**
     * A class that represents a mute entry. A mute entry contains basic information about a player who was muted.
     * @param muteTarget The target being muted
     * @param source The source muting the target. Can be any string, but a unique identifier is recommended
     * @param creationTime The date this was created
     * @param duration The duration of the mute, as a {@link Duration}, or null to imply infinite
     * @param reason The reason, or null to defer to default
     */
    public MuteEntry(@NotNull P muteTarget, String source, Date creationTime, @Nullable Duration duration, @Nullable String reason) {
        this.muteTarget = muteTarget;
        this.source = source;
        this.creationTime = creationTime;
        this.duration = duration;
        this.reason = reason;
    }

    public MuteEntry() {
        this.muteTarget = null;
        this.source = null;
        this.creationTime = null;
        this.durationAsLong = null;
        this.duration = null;
        this.reason = null;
    }

    //Getters

    @Override
    public P getTarget() {
        return this.muteTarget;
    }

    @NotNull
    public Date getCreated() {
        return this.creationTime;
    }

    public Duration getDuration() {
        if (this.duration == null) this.duration = ChronoUnit.FOREVER.getDuration();
        return this.duration;
    }

    @Nullable
    public Long getDurationAsLong() {
        return this.durationAsLong;
    }

    @Override
    @NotNull
    public String source() {
        return this.source;
    }

    @Override
    public String reason() {
        if (reason == null) return "No reason specified";
        return this.reason;
    }

    @Nullable
    public Date getExpiry() {
        if (durationAsLong == null) return null;
        long time = this.creationTime.getTime() + durationAsLong;

        return new Date(time);
    }

    public long getRemainingTime() {
        long current = System.currentTimeMillis();
        if (getExpiry() == null) return -1;
        return getExpiry().getTime() - current;
    }

    @Override
    @Nullable
    public LocalDateTime creationTime() {
        if (durationAsLong == null) return null;
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime end = current.plus(Duration.ofSeconds(durationAsLong));

        LocalDateTime n = current;
        long years = ChronoUnit.YEARS.between(n, end);

        n = n.plusYears(years);
        long months = ChronoUnit.MONTHS.between(n, end);

        n = n.plusMonths(months);
        long days = ChronoUnit.DAYS.between(n, end);

        n = n.plusDays(days);
        long hours = ChronoUnit.HOURS.between(n, end);

        n = n.plusHours(hours);
        long minutes = ChronoUnit.MINUTES.between(n, end);

        n = n.plusMinutes(minutes);
        long seconds = ChronoUnit.SECONDS.between(n, end);

        n = n.plusSeconds(seconds);
        return n;
    }

    @Override
    public Type type() {
        return Type.MUTE;
    }

    public long getRemainingTimeWithRespectToUnit(TemporalUnit unit) {
        if (durationAsLong == null) return -1;
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime end = current.plus(durationAsLong, unit);

        return ChronoUnit.valueOf(unit.toString().toUpperCase(Locale.ROOT)).between(current, end);

    }

    //Getters


    //Setters

    public void setCreated(Date created) {
        this.creationTime = created;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    public void setDurationAsLong(@NotNull Long durationAsLong) {
        this.durationAsLong = durationAsLong;
    }

    //Setters


    // Helper
    public boolean isExpired() {
        return getRemainingTime() == 0;
    }

    public boolean isPermanent() {
        return getRemainingTime() == -1;
    }

    public boolean save() {
        boolean isMuted;
        if (muteTarget instanceof PlayerProfile profile) {
            UltraMuteList<@NotNull PlayerProfile> muteList = new UltraMuteList<>();
            muteList.addMute(profile, reason, duration, source);

            isMuted = muteList.isMuted(profile);
        }
        else {
            StringMuteList<String> muteList = new StringMuteList<>();
            muteList.addMute(muteTarget.toString(), reason, duration, source);
            isMuted = muteList.isMuted(muteTarget.toString());
        }

        return isMuted;
    }

    /**
     * Removes this entry from the mute entry list. This also unmutes the player.
     * @return Whether the removal was successful
     */
    public boolean remove() {
        PersistentDataContainer container;
        if (muteTarget instanceof Player player) {
            container = player.getPersistentDataContainer();
            container.remove(Keys.commandsBlocked);
            container.remove(Keys.mutedKey);
        }
        if (muteTarget instanceof PlayerProfile profile) {
            UUID uuid = profile.getId();
            if (uuid == null) return false;
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return false;

            container = player.getPersistentDataContainer();
            container.remove(Keys.commandsBlocked);
            container.remove(Keys.mutedKey);
        }
        else {
            final UltraBans plugin = UltraBans.getInstance();
            Player player = plugin.getServer().getPlayer(muteTarget.toString());
            if (player == null) return false;

            container = player.getPersistentDataContainer();
            container.remove(Keys.commandsBlocked);
            container.remove(Keys.mutedKey);
        }

        setDurationAsLong(0L);
        return !container.has(Keys.mutedKey);
    }

    public static class Manager {
        private static final UltraBans plugin = UltraBans.getInstance();
        private static final BukkitScheduler scheduler = plugin.getServer().getScheduler();

        /**
         * Registers an entry to a scheduler. This will prevent mute durations from being
         * lost after a restart.
         * @param entry The entry to register
         * @return True if the entry is expired and consequently, removed. False if the entry is still valid.
         * A permanent mute will always return false.
         */
        public boolean register(MuteEntry<?> entry) {

            if (entry.isPermanent()) return false;

            long delay = entry.getRemainingTime();
            scheduler.runTaskLater(plugin, entry::remove, delay);

            return entry.isExpired();
        }
    }
}
