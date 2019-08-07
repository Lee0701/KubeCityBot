package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.discord.BotInstance;
import city.kobaya.kobayabot.discord.message.SimpleMessage;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Broadcaster implements Feature {
    private final BotInstance bot = KobayaBotPlugin.getInstance().getBot();

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
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("broadcaster");
    }

    public void broadcast(String message) {
        Message discordMessage = new MessageBuilder().setContent(message).build();
        bot.sendDiscordMessages(channels, channel -> new SimpleMessage(channel, discordMessage));
    }

    public List<String> getChannels() {
        return channels;
    }
}
