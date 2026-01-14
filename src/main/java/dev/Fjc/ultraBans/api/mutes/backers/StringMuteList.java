package dev.Fjc.ultraBans.api.mutes.backers;

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

public class StringMuteList<P extends String> implements MuteList<String>, MuteList.Store<String> {

    private final EntrySaver.MuteSaver<P> saver = new EntrySaver.MuteSaver<P>();
    private final Map<String, MuteEntry<String>> entries = new ConcurrentHashMap<>();

    public StringMuteList() {
        entries.putAll(saver.loadStringMap());
    }

    @Override
    public @Nullable MuteEntry<String> getMuteEntry(@NotNull String target) {
        return entries.get(target);
    }

    @Override
    public MuteEntry<String> addMute(@NotNull String target, @Nullable String reason, @Nullable Date expiration, String source) {
        return null;
    }

    @Override
    public MuteEntry<String> addMute(@NotNull String target, @Nullable String reason, @Nullable Duration duration, String source) {
        return null;
    }

    @Override
    public boolean removeMute(@NotNull String target) {
        return false;
    }

    @Override
    public Set<MuteEntry<String>> getEntries() {
        return Set.of();
    }

    @Override
    public LocalDateTime getRemainingTime(String target) {
        return null;
    }

    @Override
    public boolean isMuted(@NotNull String target) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Map<String, MuteEntry<String>> load() {
        return Map.of();
    }

    @Override
    public void save(String target) {

    }
}
