package city.kube.bot.minecraft;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.features.*;
import city.kube.bot.features.TranslatorForwarder;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        KubeCityPlayer.of(event.getPlayer()).ifPresent(kobayaPlayer -> {
            kobayaPlayer.setChatFormat(event.getFormat());
            kobayaPlayer.setNickname(event.getPlayer().getDisplayName());
        });

        String message = ChatColor.stripColor(event.getMessage());

        KubeCityBotPlugin.getInstance().getFeature(SimpleForwarder.class).ifPresent(forwarder -> forwarder.forwardFromMinecraft(event.getPlayer(), message));
        KubeCityBotPlugin.getInstance().getFeature(TranslatorForwarder.class).ifPresent(forwarder -> forwarder.forwardFromMinecraft(event.getPlayer(), message));

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KubeCityBotPlugin.getInstance().getFeature(Broadcaster.class).ifPresent(broadcaster -> broadcaster.broadcast(event.getPlayer().getName() + " joined."));
        KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KubeCityBotPlugin.getInstance().getFeature(Broadcaster.class).ifPresent(broadcaster -> broadcaster.broadcast(event.getPlayer().getName() + " left."));
    }

}
