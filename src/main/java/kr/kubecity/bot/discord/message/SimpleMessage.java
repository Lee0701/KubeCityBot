package kr.kubecity.bot.discord.message;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SimpleMessage extends DiscordMessage {
    private final MessageCreateData message;

    public SimpleMessage(TextChannel channel, String message) {
        super(channel);
        this.message = new MessageCreateBuilder().setContent(message).build();
    }

    @Override
    public void send() {
        getChannel().sendMessage(message).complete();
    }

}
