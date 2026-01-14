package dev.Fjc.ultraBans;

import dev.Fjc.ultraBans.command.BanCommand;
import dev.Fjc.ultraBans.command.MuteCommand;
import dev.Fjc.ultraBans.file.database.SQLiteManager;
import dev.Fjc.ultraBans.file.yaml.EntrySaver;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

public class Boot {

    private final UltraBans plugin = UltraBans.getInstance();

    // Command classes
    private BanCommand banCommand;
    private MuteCommand muteCommand;
    private MuteCommand.UnmuteCommand unmuteCommand;

    // Builders
    private EntrySaver.BanSaver<?> banSaver;
    private EntrySaver.MuteSaver<?> muteSaver;
    private EntrySaver.WarnSaver<?> warnSaver;

    // Managers
    private SQLiteManager sqLiteManager;

    public void load() {
        registerCommand("ban", this.banCommand = new BanCommand());
        registerCommand("mute", this.muteCommand = new MuteCommand());
        registerCommand("unmute", this.unmuteCommand = new MuteCommand.UnmuteCommand());

        banSaver = new EntrySaver.BanSaver<>();

        muteSaver = new EntrySaver.MuteSaver<>();
        muteSaver.loadMap();

        warnSaver = new EntrySaver.WarnSaver<>();
        warnSaver.loadMap();
        if (!EntrySaver.loadEverything()) plugin.getLogger().warning("Something went wrong while attempting to load entry data!");

        sqLiteManager = new SQLiteManager();
        sqLiteManager.startConnection();
    }

    public SQLiteManager getSqLiteManager() {
        return sqLiteManager;
    }

    private void registerCommand(String command, TabExecutor executor) {
        PluginCommand commandBuilder = plugin.getServer().getPluginCommand(command);
        if (commandBuilder != null) commandBuilder.setExecutor(executor);
    }
}
