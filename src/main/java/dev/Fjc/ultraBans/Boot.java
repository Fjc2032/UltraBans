package dev.Fjc.ultraBans;

import dev.Fjc.ultraBans.command.BanCommand;
import dev.Fjc.ultraBans.command.MuteCommand;
import dev.Fjc.ultraBans.command.WarnCommand;
import dev.Fjc.ultraBans.file.database.SQLiteManager;
import dev.Fjc.ultraBans.file.yaml.EntrySaver;
import dev.Fjc.ultraBans.listener.PunishmentListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Boot {

    public Boot(UltraBans plugin) {
        this.plugin = plugin;
    }
    private final UltraBans plugin;

    boolean isLoaded = false;

    // Command classes
    private BanCommand banCommand;
    private MuteCommand muteCommand;
    private MuteCommand.UnmuteCommand unmuteCommand;
    private WarnCommand warnCommand;

    // Listeners
    private PunishmentListener punishmentListener;

    // Builders
    private EntrySaver.BanSaver<?> banSaver;
    private EntrySaver.MuteSaver<?> muteSaver;
    private EntrySaver.WarnSaver<?> warnSaver;

    // Managers
    private SQLiteManager sqLiteManager;

    public void load() {
        this.banCommand = new BanCommand();
        this.muteCommand = new MuteCommand();
        this.warnCommand = new WarnCommand();

        Util.info("Loading listeners...");
        this.punishmentListener = new PunishmentListener();
        registerListener(punishmentListener);

        Util.info("Loading commands...");
        registerCommand("ban", this.banCommand);
        registerCommand("mute", this.muteCommand);
        registerCommand("warn", this.warnCommand);

        delayInit();

        Util.info("Loading the entry saver...");
        if (!EntrySaver.loadEverything()) plugin.getLogger().warning("Something went wrong while attempting to load entry data!");
        banSaver = new EntrySaver.BanSaver<>();

        muteSaver = new EntrySaver.MuteSaver<>();
        muteSaver.loadMap();

        warnSaver = new EntrySaver.WarnSaver<>();
        warnSaver.loadMap();

        sqLiteManager = new SQLiteManager();
        sqLiteManager.startConnection();
        sqLiteManager.buildTable();

        isLoaded = true;
    }

    public void unload() {
        if (!isLoaded) return;
        sqLiteManager.closeConnection();

        warnSaver = null;
        muteSaver = null;
        banSaver = null;

        banCommand = null;
        muteCommand = null;
        unmuteCommand = null;
        warnCommand = null;

        sqLiteManager = null;

        isLoaded = false;
    }

    public SQLiteManager getSqLiteManager() {
        return sqLiteManager;
    }

    private void registerCommand(String command, TabExecutor executor) {
        PluginCommand commandBuilder = plugin.getServer().getPluginCommand(command);
        if (commandBuilder != null) commandBuilder.setExecutor(executor);
    }

    /**
     * Some command executors need to wait until the YAML/database managers are ready. This method will force them to wait.
     * @param command The name of the command
     * @param executor The executor handling this command
     * @param wait How long the application should wait before trying to register the command
     * @apiNote If the manager still isn't ready, the task will just be queued again
     */
    private void registerCommand(String command, TabExecutor executor, long wait) {
        PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pluginCommand != null) pluginCommand.setExecutor(executor);
        }, wait);

    }

    private void delayInit() {
        Util.info("Preparing delayed constructors for initialization...");
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                task -> {
                    this.unmuteCommand = new MuteCommand.UnmuteCommand();
                    registerCommand("unmute", unmuteCommand, 600L);
                    Util.info("All delayed constructors finished.");
                },
                600L
        );
    }

    private void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
