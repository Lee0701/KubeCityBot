package kr.kubecity.bot.discord.message;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SimpleForwarderMessage extends SimpleMessage {
    public SimpleForwarderMessage(TextChannel channel, ForwarderMessage message) {
        super(channel, String.format(
                "[%s]%s <%s> %s",
                message.getSide(),
                message.isLinked() ? " :link:" : "",
                message.getNickname(),
                message.getMessage()
        ));
    }
}
