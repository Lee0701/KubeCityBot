package city.kube.bot.discord.message;

import city.kube.bot.PlayerIcon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class EmbedMessage extends DiscordMessage {
    private String nickname;
    private PlayerIcon avatar;
    private final String title;
    private final String content;

    public EmbedMessage(TextChannel channel, String title, String content) {
        super(channel);
        this.title = title;
        this.content = content;
    }

    @Override
    public void send() {
        EmbedBuilder builder = new EmbedBuilder();
        if(nickname != null) {
            if(avatar != null) builder.setAuthor(nickname, null, avatar.getUrl());
            builder.setAuthor(nickname);
        }
        builder.setTitle(title);
        builder.setDescription(content);
        getChannel().sendMessage(builder.build()).complete();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public PlayerIcon getAvatar() {
        return avatar;
    }

    public void setAvatar(PlayerIcon avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
