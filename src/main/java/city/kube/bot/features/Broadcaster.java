package city.kube.bot.features;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.discord.BotInstance;
import city.kube.bot.discord.message.SimpleMessage;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Broadcaster implements Feature {
    private final BotInstance bot = KubeCityBotPlugin.getInstance().getBot();

    private List<String> channels = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
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

    public List<String> getChannels() {
        return channels;
    }
}