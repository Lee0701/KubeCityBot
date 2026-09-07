package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.discord.message.ForwarderMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
        var name = player.getName();
        var icon = IconStorage.getIconFor(player.getUniqueId());
        var linked = KubeCityPlayer.checkLinked(player);
        KubeCityBotPlugin.getInstance().getBot().sendDiscordMessages(
                channels,
                c -> wrapForwarderMessage(c, new ForwarderMessage(name, icon, "Minecraft", linked, message))
        );
    }

    @Override
    public void forwardFromDiscord(Message message) {
        TextChannel channel = message.getChannel().asTextChannel();
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

    }
}
