package dev.Fjc.ultraBans.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.Fjc.ultraBans.UltraBans;
import dev.Fjc.ultraBans.api.mutes.MuteList;
import dev.Fjc.ultraBans.api.mutes.backers.UltraMuteList;
import dev.Fjc.ultraBans.builders.Punishment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.convert.DurationStyle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MuteCommand implements TabExecutor {

    private static final UltraBans plugin = UltraBans.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 1) {
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player != null) {
                Punishment.MuteBuilder muteBuilder = new Punishment.MuteBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                muteBuilder
                        .setDuration(null)
                        .setReason(null)
                        .mute();
            }
            return true;
        } else if (args.length == 2) {
            Player player = plugin.getServer().getPlayer(args[0]);
            Duration duration = DurationStyle.detectAndParse(args[1]);
            if (player != null) {
                Punishment.MuteBuilder muteBuilder = new Punishment.MuteBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                muteBuilder
                        .setDuration(duration)
                        .setReason(null)
                        .mute();
            }
            return true;
        } else if (args.length == 3) {
            Player player = plugin.getServer().getPlayer(args[0]);
            Duration duration = DurationStyle.detectAndParse(args[1]);
            String reason = args[2];
            if (player != null) {
                Punishment.MuteBuilder muteBuilder = new Punishment.MuteBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                muteBuilder
                        .setDuration(duration)
                        .setReason(reason)
                        .blockCommands()
                        .mute();
            }
            return true;
        } else if (args.length == 4) {
            Player player = plugin.getServer().getPlayer(args[0]);
            Duration duration = DurationStyle.detectAndParse(args[1]);
            String reason = args[2];
            if (player != null && args[3].equalsIgnoreCase("-BC")) {
                Punishment.MuteBuilder muteBuilder = new Punishment.MuteBuilder(
                        player, sender instanceof Player executor ? executor : null
                );
                muteBuilder
                        .setDuration(duration)
                        .setReason(reason)
                        .blockCommands()
                        .mute();
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            plugin.getServer().getOnlinePlayers().forEach(action -> players.add(action.getName()));
            return players;
        }
        return List.of();
    }

    public static class UnmuteCommand implements TabExecutor {

        private final MuteList<@NotNull PlayerProfile> muteList = new UltraMuteList<>();

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            if (!sender.isOp()) {
                sender.sendMessage("You do not have permission to run this command.");
                return true;
            }

            if (args.length == 1) {
                Player player = plugin.getServer().getPlayer(args[0]);
                if (Checker.conditionsMet(sender, player)) {
                    assert player != null;
                    if (muteList.removeMute(player.getPlayerProfile())) sender.sendMessage(
                                "Successfully unmuted " + player.getName()
                        );
                    else sender.sendMessage(
                            "Could not unmute player " + player.getName() + ", most likely they are not currently muted."
                    );
                }
                return true;
            }

            return false;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            if (args.length == 1) {
                List<String> mutedPlayers = new ArrayList<>();
                muteList.getEntries().forEach(entry -> mutedPlayers.add(entry.getMuteTarget().getName()));
                return mutedPlayers;
            }
            return List.of();
        }
    }
}
