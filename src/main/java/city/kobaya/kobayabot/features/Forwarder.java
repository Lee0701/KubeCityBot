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

    public void forwardFromMinecraft(Player player, String message) {
        String name = player.getName();
        KobayaBotPlugin.getInstance().getBot().sendDiscordMessages(
                channels,
                channel -> new ForwarderMessage("Minecraft", channel, name, message, IconStorage.getIconFor(player.getUniqueId()), false)
        );
    }

    public void forwardFromDiscord(Message message) {
        TextChannel channel = message.getTextChannel();
        User author = message.getAuthor();
        String text = message.getContentDisplay();

        if(!channels.contains(channel.getId())) return;

        String username = author.getName();

        Bukkit.getLogger()
                .info(String.format("[%s](%s)<%s>: %s", "Discord", channel.getName(), username, text));

        String format = "[Discord] <%s> %s";
        String formattedMessage = String.format(format, username, text);

        // Send minecraft messages.
        List<Player> recipients = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        for(Player recipient : recipients) {
            recipient.sendMessage(formattedMessage);
        }

        // Send discord message.
        KobayaBotPlugin.getInstance().getBot().sendDiscordMessage(
                new ForwarderMessage("Discord", channel, username, text, IconStorage.getIconFor(author), false));

        // Delete original message.
        message.delete().queue();
    }

    public List<String> getChannels() {
        return channels;
    }

}
