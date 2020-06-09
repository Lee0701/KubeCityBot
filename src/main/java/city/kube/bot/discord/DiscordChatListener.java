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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

            player.sendMessage(KubeCityBotPlugin.getInstance().getMessage("registration.register-complete", ChatColor.GREEN + "You are now registered."));

            KubeCityPlayer.REGISTRATIONS.remove(registration);
            message.delete().queue();

            KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(member));

            return;
        }

        if(message.getContentDisplay().startsWith("/register") || message.getContentDisplay().startsWith("http:register")) {
            message.delete().queue();
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
            String reply = String.format(KubeCityBotPlugin.getInstance().getMessage("discord-command.player-list", "List of online players (%1$d/%2$d):") + "\n", Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
            reply += players.stream().map(Player::getPlayerListName).map(name -> "- " + name).collect(Collectors.joining("\n"));
            message.getChannel().sendMessage(reply).queue();
            return true;
        case "whois":
            KubeCityPlayer kubeCityPlayer = null;
            if(args.length >= 1) {
                List<Member> members = message.getGuild().getMembersByEffectiveName(args[0], true);
                if(!members.isEmpty()) {
                    kubeCityPlayer = KubeCityPlayer.of(members.get(0).getUser().getId());
                }
            }
            if(kubeCityPlayer != null) {
                String uuid = kubeCityPlayer.getUuid();
                String minecraftName = kubeCityPlayer.getNickname();
                String discordName = KubeCityBotPlugin.getInstance().getBot().getJda()
                        .getGuildById(KubeCityBotPlugin.getInstance().getServerId())
                        .getMemberById(kubeCityPlayer.getDiscordId()).getEffectiveName();
                message.getChannel().sendMessage(String.format(KubeCityBotPlugin.getInstance().getMessage(
                        "registration.player-info-discord",
                        "Player info:\n" +
                                " - UUID: %1$s\n" +
                                " - Minecraft Name: %2$s\n" +
                                " - Discord Name: %3$s"
                ), uuid, minecraftName, discordName)).queue();
            } else {
                message.getChannel().sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                        "registration.player-not-found-discord",
                        "Player not found.")).queue();
            }
            return true;
        default:
            return false;
        }
    }

}
