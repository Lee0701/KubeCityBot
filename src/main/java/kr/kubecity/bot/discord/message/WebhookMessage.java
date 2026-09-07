package kr.kubecity.bot.discord.message;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class WebhookMessage extends DiscordMessage {
    private final String nickname;
    private Icon avatar;
    private club.minnced.discord.webhook.send.WebhookMessage message;

    public WebhookMessage(TextChannel channel, String nickname) {
        super(channel);
        this.nickname = nickname;
    }

    public void setAvatar(Icon avatar) {
        this.avatar = avatar;
    }

    public void setMessage(club.minnced.discord.webhook.send.WebhookMessage message) {
        this.message = message;
    }

    @Override
    public void send() {
        if(message == null) return;
        Webhook webhook = getChannel().createWebhook(nickname).setAvatar(avatar).complete();
        try(WebhookClient client = JDAWebhookClient.from(webhook)) {
            client.send(message).join();
        } finally {
            webhook.delete().complete();
        }
    }

}
