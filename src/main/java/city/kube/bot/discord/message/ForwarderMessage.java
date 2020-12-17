package city.kube.bot.discord.message;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.TextChannel;

public class ForwarderMessage extends WebhookMessage {

    public ForwarderMessage(String side, TextChannel channel, String nickname, String message, Icon icon, boolean linked) {
        super(channel, nickname);
        setAvatar(icon);
        setMessage(
                new WebhookMessageBuilder().setContent(String.format("[%s]%s\n%s", side, linked ? " :link:" : "", message))
                        .build());
    }

}
