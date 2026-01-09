package dev.Fjc.ultraBans.file;

import dev.Fjc.ultraBans.UltraBans;
import org.bukkit.NamespacedKey;

public abstract class Keys {
    private static final UltraBans plugin = UltraBans.getInstance();

    public static final NamespacedKey mutedKey = new NamespacedKey(plugin, "muted");
    public static final NamespacedKey commandsBlocked = new NamespacedKey(plugin, "cmdsBlocked");
}
