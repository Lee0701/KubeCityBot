package city.kobaya.kobayabot.discord;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import city.kobaya.kobayabot.Registration;
import city.kobaya.kobayabot.features.Feature;
import city.kobaya.kobayabot.features.GroupLinker;
import city.kobaya.kobayabot.features.SimpleForwarder;
import city.kobaya.kobayabot.features.TranslatorForwarder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class DiscordChatListener extends ListenerAdapter {

    private static final String COMMAND_PREFIX = "/";

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KobayaBotPlugin.getInstance().getServerId())) return;

        Member member = event.getMember();
        if(guild.getSelfMember().equals(member)) return;

        Message message = event.getMessage();

        if(message.isWebhookMessage()) return;

        if(handleCommand(message)) return;

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

            KobayaBotPlugin.getInstance().getFeature(GroupLinker.class).reloadMember(member);

            return;
        }

        KobayaBotPlugin.getInstance().getFeature(SimpleForwarder.class).forwardFromDiscord(message);
        KobayaBotPlugin.getInstance().getFeature(TranslatorForwarder.class).forwardFromDiscord(message);

    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KobayaBotPlugin.getInstance().getServerId())) return;

        KobayaBotPlugin.getInstance().getFeature(GroupLinker.class).reloadMember(event.getMember());

    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KobayaBotPlugin.getInstance().getServerId())) return;

        KobayaBotPlugin.getInstance().getFeature(GroupLinker.class).reloadMember(event.getMember());

    }

    private boolean handleCommand(Message message) {
        StringTokenizer tokens = new StringTokenizer(message.getContentDisplay());

        String command;
        if(tokens.hasMoreTokens()) command = tokens.nextToken();
        else return false;

        String[] args = new String[tokens.countTokens()];
        for(int i = 0 ; i < args.length ; i++) args[i] = tokens.nextToken();

        command = command.toLowerCase();
        if(command.startsWith(COMMAND_PREFIX)) command = command.substring(1);
        else return false;

        switch(command) {
        case "list":
            List<Player> players = new ArrayList<>(KobayaBotPlugin.getInstance().getServer().getOnlinePlayers());
            String reply = "List of online players:\n";
            reply += players.stream().map(Player::getPlayerListName).map(name -> "- " + name).collect(Collectors.joining("\n"));
            message.getChannel().sendMessage(reply).queue();
            return true;
        default:
            return false;
        }
    }

}
