package city.kube.bot.features;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class Forwarder implements Feature, Listener {

    protected List<String> channels = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        channels = getConfigurationSection().getStringList("channels");
    }

    @Override
    public void save() {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        KubeCityPlayer.of(event.getPlayer()).ifPresent(kubeCityPlayer -> {
            kubeCityPlayer.setChatFormat(event.getFormat());
            kubeCityPlayer.setNickname(event.getPlayer().getDisplayName());
        });

        String message = ChatColor.stripColor(event.getMessage());
        forwardFromMinecraft(event.getPlayer(), message);

    }

    public abstract void forwardFromMinecraft(Player player, String message);

    public abstract void forwardFromDiscord(Message message);

    public List<String> getChannels() {
        return channels;
    }

}
