package dev.Fjc.ultraBans.api.warns.backers;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.Entry;
import dev.Fjc.ultraBans.api.warns.WarnEntry;
import dev.Fjc.ultraBans.api.warns.WarnList;
import dev.Fjc.ultraBans.file.database.SQLiteManager;
import dev.Fjc.ultraBans.file.yaml.EntrySaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link WarnList} that adds, removes, and saves a {@link WarnEntry} to memory and disk.
 * @param <R> Represents the target being warned. A target may have multiple entries, so this implementation maps a target {@link R}
 *           to a list of warn entries.
 * @apiNote This implementation stores a target via {@link PlayerProfile}. You can make your own implementation of {@link R} if you need
 * something else.
 * @see StringWarnList
 */
public class UltraWarnList<R extends PlayerProfile> implements WarnList<PlayerProfile>, WarnList.Store<PlayerProfile> {

    private final EntrySaver.WarnSaver<@NotNull PlayerProfile> saver;
    private final SQLiteManager manager = UltraBans.getBooter().getSqLiteManager();

    private final Map<PlayerProfile, List<WarnEntry<@NotNull PlayerProfile>>> entries = new HashMap<>();

    public UltraWarnList() {
        saver = new EntrySaver.WarnSaver<>();
        entries.putAll(saver.loadMap());
    }

    @Override
    public @NotNull List<WarnEntry<PlayerProfile>> getWarns(PlayerProfile target) {
        if (entries.get(target) == null) return List.of();
        return entries.get(target);
    }

    @Override
    public @Nullable WarnEntry<PlayerProfile> addWarn(PlayerProfile target, String source, LocalDateTime createdAt, String reason) {
        List<WarnEntry<@NotNull PlayerProfile>> currents = entries.get(target);
        WarnEntry<@NotNull PlayerProfile> newEntry = new WarnEntry<>(target, source, createdAt, reason);
        currents.add(newEntry);

        if (entries.containsKey(target)) entries.replace(target, currents);
        else entries.put(target, currents);

        manager.addEntry(newEntry);
        save(target);
        return newEntry;
    }

    @Override
    public int getWarnCount(PlayerProfile target) {
        return entries.get(target).size();
    }

    @Override
    public boolean removeWarn(PlayerProfile target, int index, String identifier) {
        var removed = entries.get(target).remove(index);
        return saver.remove(removed) && manager.removeEntry(target.getName(), identifier);
    }

    @Override
    public boolean clearWarns(PlayerProfile target) {
        entries.get(target).clear();
        for (WarnEntry<@NotNull PlayerProfile> entry : entries.get(target)) {
            saver.remove(entry);
            manager.removeEntry(target.getName());
        }
        return entries.get(target).isEmpty();
    }

    @Override
    public void wipe() {
        manager.wipeDir(Entry.Type.WARN);
        for (PlayerProfile target : entries.keySet()) clearWarns(target);
        entries.clear();
    }

    @Override
    public Map<PlayerProfile, List<WarnEntry<@NotNull PlayerProfile>>> load() {
        return saver.loadMap();
    }

    @Override
    public void save(PlayerProfile target) {
        for (WarnEntry<@NotNull PlayerProfile> entry : entries.get(target)) {
            saver.save(entry);
        }
    }
}
