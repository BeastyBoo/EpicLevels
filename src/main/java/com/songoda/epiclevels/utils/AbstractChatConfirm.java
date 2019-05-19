package com.songoda.epiclevels.utils;

import com.songoda.epiclevels.EpicLevels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AbstractChatConfirm implements Listener {

    private static final List<UUID> registered = new ArrayList<>();

    private final Player player;
    private final ChatConfirmHandler handler;

    private OnClose onClose = null;

    public AbstractChatConfirm(Player player, ChatConfirmHandler hander) {
        this.player = player;
        this.handler = hander;
        player.closeInventory();
        initializeListeners(EpicLevels.getInstance());
        registered.add(player.getUniqueId());
    }

    private Listener listener;

    public void initializeListeners(JavaPlugin plugin) {

        this.listener = new Listener() {
            @EventHandler
            public void onChat(AsyncPlayerChatEvent event) {
                Player player = event.getPlayer();
                if (!AbstractChatConfirm.isRegistered(player)) return;

                AbstractChatConfirm.unregister(player);
                event.setCancelled(true);

                ChatConfirmEvent chatConfirmEvent = new ChatConfirmEvent(player, event.getMessage());

                handler.onChat(chatConfirmEvent);

                if (onClose != null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicLevels.getInstance(), () ->
                            onClose.onClose(), 0L);
                }
                HandlerList.unregisterAll(listener);
            }
        };


        Bukkit.getPluginManager().registerEvents(listener, EpicLevels.getInstance());
    }

    public static boolean isRegistered(Player player) {
        return registered.contains(player.getUniqueId());
    }

    public static boolean unregister(Player player) {
        return registered.remove(player.getUniqueId());
    }

    public interface ChatConfirmHandler {
        void onChat(ChatConfirmEvent event);
    }

    public void setOnClose(OnClose onClose) {
        this.onClose = onClose;
    }

    public interface OnClose {
        void onClose();
    }

    public class ChatConfirmEvent {

        private final Player player;
        private final String message;

        public ChatConfirmEvent(Player player, String message) {
            this.player = player;
            this.message = message;
        }

        public Player getPlayer() {
            return player;
        }

        public String getMessage() {
            return message;
        }
    }

}
