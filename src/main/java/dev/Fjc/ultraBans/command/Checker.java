package dev.Fjc.ultraBans.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class Checker {

    /**
     * A standalone method that ensures validation between sender and target of a punishment.
     * The checks are as follows: <br>
     * - Make sure the specified target is not null <br>
     * - Make sure the sender has permissions to run this <br>
     * - Make sure the target doesn't have a permission that grants immunity <br>
     * - Make sure the sender and target's UUIDs don't match (if they do, then they're the same player)
     * @param sender The command sender, which is anything that can run a command
     * @param target The target player
     * @return Whether the conditions are met or not
     */
    public static boolean conditionsMet(@NotNull CommandSender sender, Player target) {
        boolean equals = false;
        if (target == null) {
            sender.sendMessage("The specified player does not exist.");
            return false;
        }

        if (sender instanceof Player playerSender) {
            equals = playerSender.getUniqueId().equals(target.getUniqueId());
            sender.sendMessage("You cannot run a punishment on yourself! (Well you can but use the console)");
        }

        if (!sender.isOp()) sender.sendMessage("You do not have permission to run this command.");
        if (target.isOp()) sender.sendMessage("This player has permissions that prevent that from receiving punishments from this plugin.");
        return sender.isOp() && !target.isOp() && !equals;
    }

    public static boolean conditionsMet(@NotNull CommandSender sender, Player target, Set<Permission> permissions) {

    }
}
