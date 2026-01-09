package dev.Fjc.ultraBans;

import org.bukkit.plugin.java.JavaPlugin;

public final class UltraBans extends JavaPlugin {

    private static UltraBans instance;

    private static final Boot booter = new Boot();

    @Override
    public void onEnable() {
        instance = this;
        booter.load();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static UltraBans getInstance() {
        return instance;
    }

    public static Boot getBooter() {
        return booter;
    }

}
