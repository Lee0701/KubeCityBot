package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.IconStorage;
import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.discord.message.ForwarderMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class Forwarder implements Feature {

    protected List<String> channels = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        channels = getConfigurationSection().getStringList("channels");
    }

    @Override
    public void save() {

    }

    public abstract void forwardFromMinecraft(Player player, String message);

    public abstract void forwardFromDiscord(Message message);

    public List<String> getChannels() {
        return channels;
    }

}
