package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.IconStorage;
import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import city.kobaya.kobayabot.discord.message.ForwarderMessage;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SimpleForwarder extends Forwarder {
    @Override
    public ConfigurationSection getConfigurationSection() {
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("simple-forwarder");
    }

    @Override
    public void forwardFromMinecraft(Player player, String message) {
        String name = player.getName();
        KobayaBotPlugin.getInstance().getBot().sendDiscordMessages(
                channels,
                channel -> new ForwarderMessage("Minecraft", channel, name, message,
                        IconStorage.getIconFor(player.getUniqueId()),
                        KobayaPlayer.checkLinked(player)
                ));
    }

    @Override
    public void forwardFromDiscord(Message message) {
        TextChannel channel = message.getTextChannel();
        User author = message.getAuthor();
        String text = message.getContentDisplay();

        if(!channels.contains(channel.getId())) return;

        String username = message.getMember().getEffectiveName();
        String minecraftName = username;

        Bukkit.getLogger()
                .info(String.format("[%s](%s)<%s>: %s", "Discord", channel.getName(), username, text));

        String format = "[Discord] <%s> %s";

        KobayaPlayer kobayaPlayer = KobayaPlayer.of(author.getId());
        if(kobayaPlayer.getUuid() != null) {
            if(kobayaPlayer.getChatFormat() != null) format = "[Discord] " + kobayaPlayer.getChatFormat();
            if(kobayaPlayer.getNickname() != null) minecraftName = kobayaPlayer.getNickname();
        }

        // Send minecraft messages.
        List<Player> recipients = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        for(Player recipient : recipients) {
            recipient.sendMessage(String.format(format, minecraftName, text));
        }

        // Send discord message.
        KobayaBotPlugin.getInstance().getBot().sendDiscordMessage(
                new ForwarderMessage("Discord", channel, username, text, IconStorage.getIconFor(author), false));

        // Delete original message.
        message.delete().queue();
    }
}
