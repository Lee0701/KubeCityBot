package city.kube.bot.discord.message;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class WebhookForwarderMessage extends WebhookMessage {
    public WebhookForwarderMessage(TextChannel channel, ForwarderMessage message) {
        super(channel, message.getNickname());
        setMessage(new WebhookMessageBuilder()
                .setContent(String.format("[%s]%s\n%s", message.getSide(), message.isLinked() ? " :link:" : "", message.getMessage()))
                .build());
        setAvatar(message.getIcon().getIcon());
    }
}
