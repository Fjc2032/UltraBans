package dev.Fjc.ultraBans.file.mutes.backers;

import dev.Fjc.ultraBans.file.mutes.MuteEntry;
import dev.Fjc.ultraBans.file.mutes.MuteList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link MuteList} that allows interaction with mute entries.
 * @param <P>
 */
public class UltraMuteList<P> implements MuteList<P> {

    private final Map<P, MuteEntry<P>> entries = new ConcurrentHashMap<>();

    @Override
    public @Nullable MuteEntry<P> getMuteEntry(@NotNull P target) {
        return entries.get(target);
    }

    @Override
    public MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Date expiration, String source) {
        long expire = expiration != null ? expiration.getTime() : -1;
        long duration = expire - new Date().getTime();
        MuteEntry<P> entry = new MuteEntry<>(target, source, new Date(), duration, reason);

        return entries.put(target, entry);
    }

    /**
     * Adds a mute entry to the available list of mute entries. This will mute the player.
     * @param target The target of the entry
     * @param reason The reason for the mute, or null to specify default
     * @param duration The duration of the mute, or null to specify infinite
     * @param unit The unit of time to use in conjunction with the duration
     * @param source The executor. Can be any string, but a valid player, UUID, or identifier is recommended
     * @return The entry that was added
     */
    @Override
    public MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Duration duration, TemporalUnit unit, String source) {
        long expire = duration != null ? duration.get(unit) : -1;
        MuteEntry<P> entry = new MuteEntry<>(target, source, new Date(), expire, reason);

        return entries.put(target, entry);
    }

    @Override
    public boolean removeMute(@NotNull P target) {
        return entries.get(target).remove() && entries.remove(target) != null;
    }

    @Override
    public Set<MuteEntry<P>> getEntries() {
        return Set.copyOf(entries.values());
    }

    @Override
    public LocalDateTime getRemainingTime(P target) {
        return entries.get(target).getRemainingTimeAsLocalDate();
    }

    @Override
    public boolean isMuted(@NotNull P target) {
        return entries.containsKey(target);
    }

    @Override
    public void clear() {
        entries.clear();
    }
}
