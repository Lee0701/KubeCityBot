package city.kube.bot.features;

import city.kube.bot.IconStorage;
import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.discord.message.ForwarderMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SimpleForwarder extends Forwarder {
    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("simple-forwarder");
    }

    @Override
    public void forwardFromMinecraft(Player player, String message) {
        String name = player.getName();
        KubeCityBotPlugin.getInstance().getBot().sendDiscordMessages(
                channels,
                channel -> new ForwarderMessage("Minecraft", channel, name, message,
                        IconStorage.getIconFor(player.getUniqueId()),
                        KubeCityPlayer.checkLinked(player)
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

        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(author.getId());
        if(kubeCityPlayer.getUuid() != null) {
            if(kubeCityPlayer.getChatFormat() != null) format = "[Discord] " + kubeCityPlayer.getChatFormat();
            if(kubeCityPlayer.getNickname() != null) minecraftName = kubeCityPlayer.getNickname();
        }

        // Send minecraft messages.
        List<Player> recipients = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        for(Player recipient : recipients) {
            recipient.sendMessage(String.format(format, minecraftName, text));
        }

        // Send discord message.
        KubeCityBotPlugin.getInstance().getBot().sendDiscordMessage(
                new ForwarderMessage("Discord", channel, username, text, IconStorage.getIconFor(author), kubeCityPlayer.isLinked()));

        // Delete original message.
        message.delete().queue();
    }
}
