package city.kube.bot.discord;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.Registration;
import city.kube.bot.features.GroupLinker;
import city.kube.bot.features.SimpleForwarder;
import city.kube.bot.features.TranslatorForwarder;
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
        if(!guild.getId().equals(KubeCityBotPlugin.getInstance().getServerId())) return;

        Member member = event.getMember();
        if(guild.getSelfMember().equals(member)) return;

        Message message = event.getMessage();

        if(message.isWebhookMessage()) return;

        if(handleCommand(message)) return;

        Registration registration = KubeCityPlayer.REGISTRATIONS.stream()
                .filter(it -> it.isCommandMatches(message.getContentDisplay()))
                .findFirst()
                .orElse(null);
        if (registration != null) {
            Player player = registration.getPlayer();

            KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(event.getAuthor().getId());
            kubeCityPlayer.setNickname(player.getDisplayName());
            kubeCityPlayer.setUuid(player.getUniqueId().toString());

            player.sendMessage("Discord register complete!");

            KubeCityPlayer.REGISTRATIONS.remove(registration);
            message.delete().queue();

            KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(member));

            return;
        }

        KubeCityBotPlugin.getInstance().getFeature(SimpleForwarder.class).ifPresent(forwarder -> forwarder.forwardFromDiscord(message));
        KubeCityBotPlugin.getInstance().getFeature(TranslatorForwarder.class).ifPresent(forwarder -> forwarder.forwardFromDiscord(message));

    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KubeCityBotPlugin.getInstance().getServerId())) return;

        KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(event.getMember()));

    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();
        if(!guild.getId().equals(KubeCityBotPlugin.getInstance().getServerId())) return;

        KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(event.getMember()));

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
            List<Player> players = new ArrayList<>(KubeCityBotPlugin.getInstance().getServer().getOnlinePlayers());
            String reply = "List of online players:\n";
            reply += players.stream().map(Player::getPlayerListName).map(name -> "- " + name).collect(Collectors.joining("\n"));
            message.getChannel().sendMessage(reply).queue();
            return true;
        default:
            return false;
        }
    }

}
