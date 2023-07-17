package net.seanomik.tamablefoxes.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.seanomik.tamablefoxes.TamableFoxes;

import java.util.HashMap;
import java.util.Map;

public class ConversationUtil implements Listener {

    private static ConversationUtil instance;
    private final Map<Player, ConversationListener> conversations = new HashMap<>() {
        @Override
        public ConversationListener remove(Object key) {
            if (containsKey(key)) {
                get(key).onExit();
            }
            return super.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            if (containsKey(key)) {
                get(key).onExit();
            }
            return super.remove(key, value);
        }
    };

    public ConversationUtil() {
        instance = this;
    }

    public static ConversationUtil getInstance() {
        return instance;
    }

    public void createConversation(Player player, ConversationListener messageConsumer) {
        this.conversations.put(player, messageConsumer);
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e) {
        if (conversations.containsKey(e.getPlayer())) {
            e.setCancelled(true);
            ConversationListener conversationListener = conversations.get(e.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (e.getMessage().equalsIgnoreCase("exit")) {
                        conversations.remove(e.getPlayer());
                        return;
                    }
                    if (!conversationListener.onMessage(e.getMessage())) {
                        conversations.remove(e.getPlayer());
                    }
                }
            }.runTask( TamableFoxes.getPlugin() );
        }
    }

    public static abstract class ConversationListener {

        public abstract boolean onMessage(String message);

        public abstract void onExit();

    }

}
