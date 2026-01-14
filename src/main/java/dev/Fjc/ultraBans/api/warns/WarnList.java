package dev.Fjc.ultraBans.api.warns;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a list of warn entries.
 * @param <R> The target of said entry. A target may have multiple entries at once.
 */
public interface WarnList<R> {

    /**
     * Returns all warn entries of a target.
     * @param target The target to get the entries from
     * @return A list of warn entries, or an empty list if no entries are present
     */
    @NotNull List<WarnEntry<R>> getWarns(R target);

    /**
     * Adds a {@link WarnEntry} to the available list of entries.
     * @param target The target being warned
     * @param source The source of the warning, or null for default
     * @param createdAt The date and time this entry was created
     * @param reason The reason for the warning, or null for default
     * @return The new entry that was added, or null if the target cannot be warned
     */
    @Nullable WarnEntry<R> addWarn(R target, String source, LocalDateTime createdAt, String reason);

    /**
     * Returns the amount of warns a target has
     * @param target The target to check
     * @return The amount of warns
     */
    int getWarnCount(R target);

    /**
     * Removes a warn entry from the target by a specific ID
     * @param target The target the warn entry is assigned to
     * @param index The index of the warn entry
     * @param identifier The specific warn to remove, or null if not available
     * @return Whether the removal was successful
     */
    boolean removeWarn(R target, int index, @Nullable String identifier);

    /**
     * Removes all warns from a target
     * @param target The target to clear warns from
     * @return Whether the removal was successful
     */
    boolean clearWarns(R target);

    /**
     * Wipes ALL warn entries. Every target {@link R} present in the map of entries will be erased from disk.
     */
    void wipe();

    /**
     * Represents an abstract interface to load and save a map of entries.
     * @param <R> The target of said entry or entries
     */
    interface Store<R> extends WarnList<R> {

        /**
         * Builds a map of all entry data from the disk and loads it into memory.
         * @return A map of target {@link R} and a list of warn entries, or an empty
         * map if no entries are present
         */
        Map<R, List<WarnEntry<R>>> load();

        void save(R target);
    }
}
