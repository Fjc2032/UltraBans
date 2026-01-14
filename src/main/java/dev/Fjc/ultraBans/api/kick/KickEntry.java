package dev.Fjc.ultraBans.api.kick;

import dev.Fjc.ultraBans.api.warns.WarnEntry;
import dev.Fjc.ultraBans.api.Entry;

import java.time.LocalDateTime;

/**
 * Represents a kick entry, which represents a player being kicked. Kicks are logged using this entry, but are not real punishments, similar to
 * {@link WarnEntry}.
 * @param <Q> The target being kicked. Can be any non-null type
 */
public class KickEntry<Q> implements Entry<Q> {

    private final Q target;

    private final String source;

    private final String reason;

    private final LocalDateTime creation;

    private CharSequence id;

    public KickEntry(Q target) {
        this.target = target;
        this.source = null;
        this.reason = null;
        this.creation = LocalDateTime.MIN;
    }

    public KickEntry(Q target, String source, String reason, LocalDateTime creation) {
        this.target = target;
        this.source = source;
        this.reason = reason;
        this.creation = creation;
    }

    @Override
    public Q getTarget() {
        return this.target;
    }

    @Override
    public String source() {
        return this.source;
    }

    @Override
    public String reason() {
        return this.reason;
    }

    @Override
    public LocalDateTime creationTime() {
        return this.creation;
    }

    @Override
    public Type type() {
        return Type.KICK;
    }
}
