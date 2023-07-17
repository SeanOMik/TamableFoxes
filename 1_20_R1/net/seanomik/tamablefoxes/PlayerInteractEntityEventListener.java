package net.seanomik.tamablefoxes;

import org.bukkit.entity.Fox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractEntityEventListener implements Listener {
    public static class SynchronizeFoxObject {
        Fox interactedFox;

        SynchronizeFoxObject() {
            interactedFox = null;
        }

        SynchronizeFoxObject(Fox fox) {
            this.interactedFox = fox;
        }
    }

    TamableFoxes plugin;
    Map<UUID, SynchronizeFoxObject> players;

    PlayerInteractEntityEventListener(TamableFoxes plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (players.containsKey(event.getPlayer().getUniqueId()) && event.getRightClicked() instanceof Fox) {
            SynchronizeFoxObject syncObject = players.get(event.getPlayer().getUniqueId());
            synchronized (syncObject) {
                syncObject.interactedFox = (Fox) event.getRightClicked();
                syncObject.notify();
            }
        }
    }
}
