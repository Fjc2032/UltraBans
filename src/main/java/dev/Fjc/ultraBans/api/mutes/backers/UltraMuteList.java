package dev.Fjc.ultraBans.api.mutes.backers;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.api.mutes.MuteEntry;
import dev.Fjc.ultraBans.api.mutes.MuteList;
import dev.Fjc.ultraBans.file.yaml.EntrySaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link MuteList} that allows interaction with mute entries.
 * @param <P> Represents the target being muted. This implementation maps a target {@link P} to a
 *           {@link MuteEntry}. A target cannot have multiple entries, so a new entry will overwrite an old one.
 *           A history of all entries are saved to entries.yml. The target type should be {@link PlayerProfile} or some
 *           other implementation of PlayerProfile (at least for this MuteList implementation).
 */
public class UltraMuteList<P extends PlayerProfile> implements MuteList<@NotNull PlayerProfile>, MuteList.Store<@NotNull PlayerProfile> {

    public UltraMuteList() {
        muteSaver = new EntrySaver.MuteSaver<>();
        entries.putAll(muteSaver.loadMap());
    }

    private final EntrySaver.MuteSaver<@NotNull PlayerProfile> muteSaver;

    private final Map<PlayerProfile, MuteEntry<@NotNull PlayerProfile>> entries = new ConcurrentHashMap<>();

    @Override
    public @Nullable MuteEntry<@NotNull PlayerProfile> getMuteEntry(@NotNull PlayerProfile target) {
        return entries.get(target);
    }

    @Override
    public MuteEntry<@NotNull PlayerProfile> addMute(@NotNull PlayerProfile target, @Nullable String reason, @Nullable Date expiration, String source) {
        long expire = expiration != null ? expiration.getTime() : -1;
        long duration = expire - new Date().getTime();
        MuteEntry<@NotNull PlayerProfile> entry = new MuteEntry<>(target, source, new Date(), duration, reason);

        if (entries.containsKey(target)) return entries.replace(target, entry);
        return entries.put(target, entry);
    }

    /**
     * Adds a mute entry to the available list of mute entries. This will mute the player.
     * @param target The target of the entry
     * @param reason The reason for the mute, or null to specify default
     * @param duration The duration of the mute, or null to specify infinite
     * @param source The executor. Can be any string, but a valid player, UUID, or identifier is recommended
     * @return The entry that was added
     */
    @Override
    public MuteEntry<@NotNull PlayerProfile> addMute(@NotNull PlayerProfile target, @Nullable String reason, @Nullable Duration duration, String source) {
        MuteEntry<@NotNull PlayerProfile> entry = new MuteEntry<>(target, source, new Date(), duration, reason);

        if (entries.containsKey(target)) return entries.replace(target, entry);
        return entries.put(target, entry);
    }

    @Override
    public boolean removeMute(@NotNull PlayerProfile target) {
        return entries.get(target).remove() && muteSaver.remove(entries.get(target));
    }

    @Override
    public Set<MuteEntry<@NotNull PlayerProfile>> getEntries() {
        return Set.copyOf(entries.values());
    }

    @Override
    public LocalDateTime getRemainingTime(PlayerProfile target) {
        return entries.get(target).creationTime();
    }

    @Override
    public boolean isMuted(@NotNull PlayerProfile target) {
        return entries.containsKey(target);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Map<PlayerProfile, MuteEntry<@NotNull PlayerProfile>> load() {
        EntrySaver.MuteSaver<P> saver = new EntrySaver.MuteSaver<>();
        Map<PlayerProfile, MuteEntry<@NotNull PlayerProfile>> targets = saver.loadMap();
        entries.putAll(targets);
        return targets;
    }

    @Override
    public void save(PlayerProfile target) {
        EntrySaver.MuteSaver<@NotNull PlayerProfile> saver = new EntrySaver.MuteSaver<>();
        saver.save(entries.get(target));
    }
}
