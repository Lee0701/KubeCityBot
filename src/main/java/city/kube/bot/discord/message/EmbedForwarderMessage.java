package city.kube.bot.discord.message;

import net.dv8tion.jda.api.entities.TextChannel;

public class EmbedForwarderMessage extends EmbedMessage {
    public EmbedForwarderMessage(TextChannel channel, ForwarderMessage message) {
        super(channel, message.getNickname(), message.getIcon(), String.format("[%s]%s", message.getSide(), message.isLinked() ? " :link:" : ""), message.getMessage());
    }
}
