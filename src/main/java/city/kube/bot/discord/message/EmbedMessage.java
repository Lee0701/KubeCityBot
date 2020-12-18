package city.kube.bot.discord.message;

import city.kube.bot.PlayerIcon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class EmbedMessage extends DiscordMessage {
    private final String nickname;
    private final PlayerIcon avatar;
    private final String title;
    private final String content;

    public EmbedMessage(TextChannel channel, String nickname, PlayerIcon avatar, String title, String content) {
        super(channel);
        this.nickname = nickname;
        this.avatar = avatar;
        this.title = title;
        this.content = content;
    }

    @Override
    public void send() {
        EmbedBuilder builder = new EmbedBuilder();
        if(nickname != null) {
            String avatarUrl = null;
            if(avatar != null) avatarUrl = avatar.getUrl();
            builder.setAuthor(nickname, null, avatarUrl);
        }
        builder.setTitle(title);
        builder.setDescription(content);
        getChannel().sendMessage(builder.build()).complete();
    }
}
