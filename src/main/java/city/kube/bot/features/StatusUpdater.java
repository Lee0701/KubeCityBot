package city.kube.bot.features;

import city.kube.bot.KubeCityBotPlugin;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StatusUpdater implements Feature, Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateStatus();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        updateStatus();
    }

    public void updateStatus() {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        int online = plugin.getServer().getOnlinePlayers().size();
        int maximum = plugin.getServer().getMaxPlayers();
        plugin.getBot().getJda().getPresence().setPresence(
                Activity.playing(String.format(plugin.getMessage("status-updater.online-players", "Online players: (%1$d/%2$d)"), online, maximum)), false);
    }

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updateStatus();
    }

    @Override
    public void save() {
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("status-updater");
    }
}
