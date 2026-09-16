package kr.kubecity.bot.discord.message;

import kr.kubecity.bot.PlayerIcon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class EmbedMessage extends DiscordMessage {
    private String nickname;
    private PlayerIcon avatar;
    private final String title;
    private final String content;
    private String image;
    private Map<String, String> fields = new HashMap<>();

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
        if(image != null) builder.setImage(image);
        fields.forEach((key, value) -> builder.addField(key, value, false));
        getChannel().sendMessageEmbeds(builder.build()).complete();
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    public void addField(String key, String value) {
        this.fields.put(key, value);
    }
}
