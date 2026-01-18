package dev.Fjc.ultraBans;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class UltraBans extends JavaPlugin {

    private static UltraBans instance;

    private static Boot booter;

    @Override
    public void onEnable() {
        instance = this;
        booter = new Boot(getInstance());
        booter.load();

    }

    @Override
    public void onDisable() {
        booter.unload();
        booter = null;
        instance = null;
    }

    public static @NotNull UltraBans getInstance() {
        if (instance == null) instance = new UltraBans();
        return instance;
    }

    public static Boot getBooter() {
        return booter;
    }

}
