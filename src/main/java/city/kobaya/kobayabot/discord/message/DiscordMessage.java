package city.kobaya.kobayabot.discord.message;

import net.dv8tion.jda.core.entities.TextChannel;

public abstract class DiscordMessage {
    private final TextChannel channel;

    public DiscordMessage(TextChannel channel) {
        this.channel = channel;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public abstract void send();

}
