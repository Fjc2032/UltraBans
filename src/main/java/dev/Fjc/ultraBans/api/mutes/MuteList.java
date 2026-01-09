package dev.Fjc.ultraBans.api.mutes;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Represents an abstract list of {@link MuteEntry} entries.
 * @param <P> The target of said entry. A target can only have one entry. If a new one is applied,
 *           the old one is overwritten.
 */
public interface MuteList<P> {

    /**
     * Gets the {@link MuteEntry} associated with this target.
     * @param target The target
     * @return The {@link MuteEntry} associated with the target, or null if no such entry exists
     */
    @Nullable MuteEntry<P> getMuteEntry(@NotNull P target);

    MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Date expiration, String source);

    MuteEntry<P> addMute(@NotNull P target, @Nullable String reason, @Nullable Duration duration, String source);

    boolean removeMute(@NotNull P target);

    Set<MuteEntry<P>> getEntries();

    LocalDateTime getRemainingTime(P target);

    boolean isMuted(@NotNull P target);

    void clear();

    interface Store<P> {

        Map<P, MuteEntry<P>> load();

        void save(P target);
    }

}
