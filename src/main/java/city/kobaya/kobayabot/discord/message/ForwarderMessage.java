package city.kobaya.kobayabot.discord.message;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.TextChannel;

public class ForwarderMessage extends WebhookMessge {

    public ForwarderMessage(String side, TextChannel channel, String nickname, String message, Icon icon, boolean linked) {
        super(channel, nickname);
        setAvatar(icon);
        setMessage(
                new MessageBuilder().setContent(String.format("[%s]%s\n%s", side, linked ? " :link:" : "", message))
                        .build());
    }

}
