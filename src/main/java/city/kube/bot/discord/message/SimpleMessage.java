package city.kube.bot.discord.message;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SimpleMessage extends DiscordMessage {
    private final MessageCreateData message;

    public SimpleMessage(TextChannel channel, MessageCreateData message) {
        super(channel);
        this.message = message;
    }

    @Override
    public void send() {
        getChannel().sendMessage(message).complete();
    }

}
