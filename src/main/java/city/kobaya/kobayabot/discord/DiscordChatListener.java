package city.kobaya.kobayabot.discord;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import city.kobaya.kobayabot.Registration;
import city.kobaya.kobayabot.features.Feature;
import city.kobaya.kobayabot.features.SimpleForwarder;
import city.kobaya.kobayabot.features.TranslatorForwarder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.entity.Player;

public class DiscordChatListener extends ListenerAdapter {

    private static final String COMMAND_PREFIX = "/";

    private final BotInstance bot = KobayaBotPlugin.getInstance().getBot();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KobayaBotPlugin.getInstance().getServerId())) return;

        Member member = event.getMember();
        if(guild.getSelfMember().equals(member)) return;

        Message message = event.getMessage();

        if(message.isWebhookMessage()) return;

        Registration registration = KobayaPlayer.REGISTRATIONS.stream()
                .filter(it -> it.isCommandMatches(message.getContentDisplay()))
                .findFirst()
                .orElse(null);
        if (registration != null) {
            Player player = registration.getPlayer();

            KobayaPlayer kobayaPlayer = KobayaPlayer.of(event.getAuthor().getId());
            kobayaPlayer.setNickname(player.getDisplayName());
            kobayaPlayer.setUuid(player.getUniqueId().toString());

            player.sendMessage("Discord register complete!");

            KobayaPlayer.REGISTRATIONS.remove(registration);
            message.delete().queue();

            return;
        }

        Feature feature;
        feature = KobayaBotPlugin.getInstance().getFeature(SimpleForwarder.class);
        if(feature instanceof SimpleForwarder) {
            ((SimpleForwarder) feature).forwardFromDiscord(message);
        }
        feature = KobayaBotPlugin.getInstance().getFeature(TranslatorForwarder.class);
        if(feature instanceof TranslatorForwarder) {
            ((TranslatorForwarder) feature).forwardFromDiscord(message);
        }

    }
}
