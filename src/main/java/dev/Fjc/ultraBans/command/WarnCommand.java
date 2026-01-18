package dev.Fjc.ultraBans.command;

import dev.Fjc.ultraBans.Util;
import dev.Fjc.ultraBans.builders.Checker;
import dev.Fjc.ultraBans.builders.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarnCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            final Player player = Checker.parsePlayer(args[0]);
            if (player != null && Checker.conditionsMet(sender, player)) {
                Punishment.WarnBuilder warnBuilder = new Punishment.WarnBuilder(player, sender.getName());
                warnBuilder
                        .setReason(null)
                        .warn();
                return true;
            }
        } else if (args.length == 2) {
            final Player player = Checker.parsePlayer(args[0]);
            if (player != null && Checker.conditionsMet(sender, player)) {
                Punishment.WarnBuilder warnBuilder = new Punishment.WarnBuilder(player, sender.getName());
                warnBuilder
                        .setReason(args[1])
                        .warn();
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return Util.onlinePlayerNameList();
        return List.of();
    }
}
