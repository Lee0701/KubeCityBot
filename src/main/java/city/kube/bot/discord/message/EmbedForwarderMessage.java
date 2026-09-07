package city.kube.bot.discord.message;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class EmbedForwarderMessage extends EmbedMessage {
    public EmbedForwarderMessage(net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel, ForwarderMessage message) {
        super(channel, String.format("[%s]%s", message.getSide(), message.isLinked() ? " :link:" : ""), message.getMessage());
        this.setNickname(message.getNickname());
        this.setAvatar(message.getIcon());
    }
}
