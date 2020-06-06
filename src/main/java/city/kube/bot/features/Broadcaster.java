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

    private String joinMessage = "%1$s joined.";
    private String quitMessage = "%1$s left.";

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        channels = getConfigurationSection().getStringList("channels");
        this.joinMessage = KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-join");
        this.quitMessage = KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-quit");
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
        broadcast(String.format(joinMessage, event.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        broadcast(String.format(quitMessage, event.getPlayer().getName()));
    }

    public List<String> getChannels() {
        return channels;
    }
}
