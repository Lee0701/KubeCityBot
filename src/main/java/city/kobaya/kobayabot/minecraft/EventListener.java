package city.kobaya.kobayabot.minecraft;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import city.kobaya.kobayabot.features.*;
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
        KobayaPlayer.of(event.getPlayer()).ifPresent(kobayaPlayer -> {
            kobayaPlayer.setChatFormat(event.getFormat());
            kobayaPlayer.setNickname(event.getPlayer().getDisplayName());
        });

        String message = ChatColor.stripColor(event.getMessage());

        KobayaBotPlugin.getInstance().getFeature(SimpleForwarder.class).forwardFromMinecraft(event.getPlayer(), message);
        KobayaBotPlugin.getInstance().getFeature(TranslatorForwarder.class).forwardFromMinecraft(event.getPlayer(), message);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        KobayaBotPlugin.getInstance().getFeature(Broadcaster.class).broadcast(event.getPlayer().getName() + " joined.");
        KobayaBotPlugin.getInstance().getFeature(GroupLinker.class).reloadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        KobayaBotPlugin.getInstance().getFeature(Broadcaster.class).broadcast(event.getPlayer().getName() + " left.");
    }

}
