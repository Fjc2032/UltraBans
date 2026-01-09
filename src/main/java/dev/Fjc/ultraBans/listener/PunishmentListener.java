package dev.Fjc.ultraBans.listener;

import dev.Fjc.ultraBans.builders.data.MutedDataType;
import dev.Fjc.ultraBans.file.Keys;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.persistence.PersistentDataContainer;

public class PunishmentListener implements Listener {

    @EventHandler
    public void onBan(PlayerCommandPreprocessEvent event) {

    }

    @EventHandler
    public void onMessageSend(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (container.has(Keys.mutedKey, new MutedDataType())) {
            event.setCancelled(true);
            player.sendMessage("You are currently muted and cannot send messages!");
        }

    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (container.has(Keys.commandsBlocked, new MutedDataType())) {
            event.setCancelled(true);
            player.sendMessage("You are blocked from using commands until your mute expires.");
        }
    }
}
