package dev.Fjc.ultraBans.api.warns;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.api.Entry;
import dev.Fjc.ultraBans.api.warns.backers.StringWarnList;
import dev.Fjc.ultraBans.api.warns.backers.UltraWarnList;
import dev.Fjc.ultraBans.file.yaml.EntrySaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Represents a warn entry. Warn entries are not considered real punishments but are still logged.
 * A player may have multiple warns.
 * @param <R> A player conforming object that will receive the warning
 */
public class WarnEntry<R> implements Entry<R> {

    private final R target;

    private final String source;

    private final @NotNull LocalDateTime createdAt;

    private final String reason;

    /**
     * Represents a warn entry. Warn entries are not considered real punishments but are still logged.
     * A player may have multiple warns.
     * @param target The target being warned
     * @param source The sender of the warning, or null to imply default
     * @param createdAt The time this was created
     * @param reason The reason for this warn, or null to imply default
     */
    public WarnEntry(@NotNull R target, @Nullable String source, @NotNull LocalDateTime createdAt, @Nullable String reason) {
        this.target = target;
        if (source == null) this.source = "CONSOLE"; else this.source = source;
        this.createdAt = createdAt;
        if (reason == null) this.reason = "No reason specified"; else this.reason = reason;
    }

    public WarnEntry(@Nullable R target) {
        this.target = target;
        this.source = null;
        this.createdAt = LocalDateTime.MIN;
        this.reason = null;
    }

    @Override
    public R getTarget() {
        return this.target;
    }

    @Override
    public String source() {
        return this.source;
    }

    @Override
    public @NotNull LocalDateTime creationTime() {
        return this.createdAt;
    }

    @Override
    public String reason() {
        return this.reason;
    }

    @Override
    public Type type() {
        return Type.WARN;
    }

    public boolean save() {
        if (target instanceof PlayerProfile profile) {
            UltraWarnList<@NotNull PlayerProfile> warnList = new UltraWarnList<>();
            EntrySaver.WarnSaver<@NotNull PlayerProfile> saver = new EntrySaver.WarnSaver<>();
            WarnEntry<@NotNull PlayerProfile> s = warnList.addWarn(profile, source, createdAt, reason);
            warnList.save(profile);
            return s != null && saver.save(s);
        }
        else {
            String name = target.toString();
            StringWarnList<String> warnList = new StringWarnList<>();
            EntrySaver.WarnSaver<String> saver = new EntrySaver.WarnSaver<>();

            WarnEntry<String> p = warnList.addWarn(name, source, createdAt, reason);
            warnList.save(name);
            return p != null && saver.save(p);
        }
    }
}

