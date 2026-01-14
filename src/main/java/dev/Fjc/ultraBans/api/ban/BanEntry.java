package dev.Fjc.ultraBans.api.ban;

import dev.Fjc.ultraBans.api.Entry;

import java.time.Duration;
import java.time.LocalDateTime;

public class BanEntry<N> implements Entry<N> {

    private N target;
    private String source;
    private String reason;
    private LocalDateTime creationTime;
    private Duration duration;

    public BanEntry(N target, String source, String reason, LocalDateTime creationTime, Duration duration) {
        this.target = target;
        this.source = source;
        this.reason = reason;
        this.creationTime = creationTime;
        this.duration = duration;
    }
    @Override
    public N getTarget() {
        return null;
    }

    @Override
    public String source() {
        return "";
    }

    @Override
    public String reason() {
        return "";
    }

    @Override
    public LocalDateTime creationTime() {
        return null;
    }

    @Override
    public Type type() {
        return Type.BAN;
    }
}
