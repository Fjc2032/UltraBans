package dev.Fjc.ultraBans.file.mutes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Set;

/**
 * Represents an abstract list of {@link MuteEntry} entries.
 * @param <P> The target of said entry
 */
public interface MuteList<P> {

    /**
     * Gets the {@link MuteEntry} associated with this target.
     * @param target The target
     * @return The {@link MuteEntry} associated with the target, or null if no such entry exists
     */
    @Nullable MuteEntry<P> getMuteEntry(@NotNull P target);

    MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Date expiration, String source);

    MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Duration duration, TemporalUnit unit, String source);

    boolean removeMute(@NotNull P target);

    Set<MuteEntry<P>> getEntries();

    LocalDateTime getRemainingTime(P target);

    boolean isMuted(@NotNull P target);

    void clear();

}
