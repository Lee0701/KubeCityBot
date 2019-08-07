package city.kobaya.kobayabot.discord;

import city.kobaya.kobayabot.KobayaBotPlugin;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordChatListener extends ListenerAdapter {

    private static final String COMMAND_PREFIX = "/";

    private final BotInstance bot = KobayaBotPlugin.getInstance().getBot();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {


    }
}
