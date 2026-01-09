package dev.Fjc.ultraBans.file.mutes;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.file.Keys;
import dev.Fjc.ultraBans.file.mutes.backers.UltraMuteList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a mute entry.
 * @param <P> A player conforming object that will be assigned to this entry
 */
public class MuteEntry<P> {

    private MuteEntry<P> instance;

    private final P muteTarget;
    private String source;

    private Date creationTime;
    private @Nullable Long duration;

    private @Nullable String reason;

    /**
     * A class that represents a mute entry. A mute entry contains basic information about a player who was muted.
     * @param muteTarget The target being muted
     * @param source Who is muting the target. Can be any string, but it's recommended to put a unique identifier or UUID
     * @param creationTime The date this was created
     * @param duration How long the mute will last
     * @param reason The reason assigned for this mute. If left null, a default reason will be applied.
     */
    public MuteEntry(P muteTarget, String source, Date creationTime, @Nullable Long duration, @Nullable String reason) {
        instance = this;
        this.muteTarget = muteTarget;
        this.source = source;
        this.creationTime = creationTime;
        this.duration = duration;
        this.reason = reason;
    }

    //Getters

    @NotNull
    public P getMuteTarget() {
        return this.muteTarget;
    }

    @NotNull
    public Date getCreated() {
        return this.creationTime;
    }

    @Nullable
    public Long getDuration() {
        return this.duration;
    }

    @NotNull
    public String getSource() {
        return this.source;
    }

    public String getReason() {
        if (reason == null) return "No reason specified";
        return this.reason;
    }

    @Nullable
    public Date getExpiry() {
        if (duration == null) return null;
        long time = this.creationTime.getTime() + duration;

        return new Date(time);
    }

    public long getRemainingTime() {
        long current = System.currentTimeMillis();
        if (getExpiry() == null) return -1;
        return getExpiry().getTime() - current;
    }

    @Nullable
    public LocalDateTime getRemainingTimeAsLocalDate() {
        if (duration == null) return null;
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime end = current.plus(Duration.ofSeconds(duration));

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

    public long getRemainingTimeWithRespectToUnit(TemporalUnit unit) {
        if (duration == null) return -1;
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime end = current.plus(duration, unit);

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

    public void setDuration(@NotNull Long duration) {
        this.duration = duration;
    }

    //Setters


    // Helper
    public boolean isExpired() {
        return getRemainingTime() == 0;
    }

    public boolean isPermanent() {
        return getRemainingTime() == -1;
    }

    public boolean save(@Nullable TemporalUnit unit) {
        UltraMuteList<P> muteList = new UltraMuteList<>();
        muteList.addMute(muteTarget, reason, duration != null ? new Date(duration) : null, source);

        return muteList.isMuted(muteTarget);
    }

    /**
     * Removes this entry from the mute entry list. This also unmutes the player.
     * @return Whether the removal was successful
     */
    public boolean remove() {
        PersistentDataContainer container = null;
        if (muteTarget instanceof Player player) {
            container = player.getPersistentDataContainer();
            container.remove(Keys.commandsBlocked);
            container.remove(Keys.mutedKey);
        }
        if (muteTarget instanceof UUID uuid) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return false;

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

        setDuration(0L);
        instance = null;
        return container != null && !container.has(Keys.mutedKey);
    }
}
