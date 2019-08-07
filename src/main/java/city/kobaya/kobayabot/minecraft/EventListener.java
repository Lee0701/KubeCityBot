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

        Feature feature;
        feature = KobayaBotPlugin.getInstance().getFeature(SimpleForwarder.class);
        if(feature instanceof SimpleForwarder) {
            ((SimpleForwarder) feature).forwardFromMinecraft(event.getPlayer(), message);
        }
        feature = KobayaBotPlugin.getInstance().getFeature(TranslatorForwarder.class);
        if(feature instanceof TranslatorForwarder) {
            ((TranslatorForwarder) feature).forwardFromMinecraft(event.getPlayer(), message);
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Feature feature = KobayaBotPlugin.getInstance().getFeature(Broadcaster.class);
        if(feature instanceof Broadcaster) {
            ((Broadcaster) feature).broadcast(event.getPlayer().getName() + " joined.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Feature feature = KobayaBotPlugin.getInstance().getFeature(Broadcaster.class);
        if(feature instanceof Broadcaster) {
            ((Broadcaster) feature).broadcast(event.getPlayer().getName() + " left.");
        }
    }

}
