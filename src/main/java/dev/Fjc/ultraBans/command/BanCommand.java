package dev.Fjc.ultraBans.command;

import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.Util;
import dev.Fjc.ultraBans.builders.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class BanCommand implements TabExecutor {

    private final UltraBans plugin = UltraBans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You do not have permission to run this command.");
            return true;
        }

        if (args.length == 1) {
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player != null) {
                if (player.isOp()) {
                    sender.sendMessage("You cannot ban a server operator.");
                    return true;
                }
                if (sender instanceof Player executor && executor.getUniqueId().equals(player.getUniqueId())) {
                    sender.sendMessage("You cannot ban yourself. (Well you could but you should use a console for that.)");
                    return true;
                }
                Punishment.BanBuilder punishment = new Punishment.BanBuilder(player, sender instanceof Player executor ? executor : null);
                punishment
                        .setReason(null)
                        .setDuration(null)
                        .setSource(sender.getName())
                        .ban(false);

                return true;
            }
        } else if (args.length == 2) {
            Player player =  plugin.getServer().getPlayer(args[0]);
            Duration parsed = Util.parse(args[1]);
            if (player != null) {
                Punishment.BanBuilder punishment = new Punishment.BanBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                punishment
                        .setDuration(parsed)
                        .setReason(null)
                        .setSource(sender.getName())
                        .ban(false);
                return true;
            }
        } else if (args.length == 3) {
            Player player = plugin.getServer().getPlayer(args[0]);
            Duration parsed = Util.parse(args[1]);
            if (player != null) {
                Punishment.BanBuilder punishment = new Punishment.BanBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                punishment
                        .setDuration(parsed)
                        .setReason(args[2])
                        .setSource(sender.getName())
                        .ban(false);
                return true;
            }
        } else if (args.length == 4) {
            Player player = plugin.getServer().getPlayer(args[0]);
            Duration parsed = Util.parse(args[1]);
            if (player != null) {
                Punishment.BanBuilder punishment = new Punishment.BanBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                punishment
                        .setDuration(parsed)
                        .setReason(args[2])
                        .setSource(sender.getName())
                        .ban(args[3].equalsIgnoreCase("-b"));
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return Util.onlinePlayerNameList();
        return List.of();
    }
}
