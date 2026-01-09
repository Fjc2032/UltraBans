package dev.Fjc.ultraBans.api.warns.backers;

import dev.Fjc.ultraBans.api.warns.WarnEntry;
import dev.Fjc.ultraBans.api.warns.WarnList;
import dev.Fjc.ultraBans.file.EntrySaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link WarnList} that adds, saves, and removes a {@link WarnEntry} from memory and disk.
 * @param <R> Represents a target being mapped to an entry. A player may have multiple warns, so the target {@link R} can
 *           be mapped to a list of entries.
 * @apiNote This implementation uses a {@link String} to define targets. This means you can arbitrarily set any target you want, but
 * it may not reflect in the Minecraft environment (e.g., you provide a player name that does not exist)
 * @see UltraWarnList
 */
public class StringWarnList<R extends String> implements WarnList<String>, WarnList.Store<String> {

    private final EntrySaver.WarnSaver<String> saver = new EntrySaver.WarnSaver<>();
    private final Map<String, List<WarnEntry<String>>> entries = new ConcurrentHashMap<>();

    public StringWarnList() {
        entries.putAll(saver.loadStringMap());
    }

    @Override
    public Map<String, List<WarnEntry<String>>> load() {
        return saver.loadStringMap();
    }

    @Override
    public void save(String target) {
        for (WarnEntry<String> entry : entries.get(target)) saver.save(entry);
    }

    @Override
    public @NotNull List<WarnEntry<String>> getWarns(String target) {
        return entries.get(target);
    }

    @Override
    public @Nullable WarnEntry<String> addWarn(String target, String source, LocalDateTime createdAt, String reason) {
        entries.putIfAbsent(target, List.of());
        WarnEntry<String> entry = new WarnEntry<>(target, source, createdAt, reason);
        entries.get(target).add(entry);

        return entry;
    }

    @Override
    public int getWarnCount(String target) {
        return entries.get(target).size();
    }

    @Override
    public boolean removeWarn(String target, int index) {
        WarnEntry<?> removed = entries.get(target).remove(index);

        return !entries.get(target).contains(removed);
    }

    @Override
    public boolean clearWarns(String target) {
        entries.remove(target);
        return !entries.containsKey(target);
    }

    @Override
    public void wipe() {
        entries.clear();
    }
}
