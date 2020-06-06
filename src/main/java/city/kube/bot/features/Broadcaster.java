package city.kube.bot.features;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.discord.BotInstance;
import city.kube.bot.discord.message.SimpleMessage;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Broadcaster implements Feature, Listener {
    private final BotInstance bot = KubeCityBotPlugin.getInstance().getBot();

    private List<String> channels = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        channels = getConfigurationSection().getStringList("channels");
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("broadcaster");
    }

    public void broadcast(String message) {
        Message discordMessage = new MessageBuilder().setContent(message).build();
        bot.sendDiscordMessages(channels, channel -> new SimpleMessage(channel, discordMessage));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        broadcast(String.format(KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-join", "%1$s joined."), event.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        broadcast(String.format(KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-quit", "%1$s left."), event.getPlayer().getName()));
    }

    public List<String> getChannels() {
        return channels;
    }
}
