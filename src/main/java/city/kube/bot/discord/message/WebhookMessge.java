package city.kube.bot.discord.message;

import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.webhook.WebhookClient;

public class WebhookMessge extends DiscordMessage {
    private final String nickname;
    private Icon avatar;
    private Message message;

    public WebhookMessge(TextChannel channel, String nickname) {
        super(channel);
        this.nickname = nickname;
    }

    public void setAvatar(Icon avatar) {
        this.avatar = avatar;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public void send() {
        if(message == null) return;
        Webhook webhook = getChannel().createWebhook(nickname).setAvatar(avatar).complete();
        try(WebhookClient client = webhook.newClient().build()) {
            client.send(message).join();
        } finally {
            webhook.delete().complete();
        }
    }

}
