package city.kube.bot.discord;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.Registration;
import city.kube.bot.discord.message.EmbedMessage;
import city.kube.bot.features.ChannelForwarder;
import city.kube.bot.features.GroupLinker;
import city.kube.bot.features.SimpleForwarder;
import city.kube.bot.features.TranslatorForwarder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DiscordChatListener extends ListenerAdapter {

    private static final String COMMAND_PREFIX = "/";

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        Guild guild = event.getGuild();
        if(!guild.getId().equals(plugin.getServerId())) return;

        Member member = event.getMember();
        if(guild.getSelfMember().equals(member)) return;

        Message message = event.getMessage();

        if(message.isWebhookMessage()) return;

        if(handleCommand(message)) return;

        Registration registration = KubeCityPlayer.REGISTRATIONS.stream()
                .filter(it -> it.isCommandMatches(message.getContentDisplay()))
                .findFirst()
                .orElse(null);
        if(registration != null) {
            Player player = registration.getPlayer();

            KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(event.getAuthor().getId());
            kubeCityPlayer.setNickname(player.getDisplayName());
            kubeCityPlayer.setUuid(player.getUniqueId().toString());

            player.sendMessage(plugin.getMessage("registration.register-complete", ChatColor.GREEN + "You are now registered."));

            KubeCityPlayer.REGISTRATIONS.remove(registration);
            message.delete().queue();

            plugin.getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(member));

            return;
        }

        if(message.getContentDisplay().startsWith("/register") || message.getContentDisplay().startsWith("http:register")) {
            message.delete().queue();
            return;
        }

        plugin.getFeature(SimpleForwarder.class).ifPresent(forwarder -> forwarder.forwardFromDiscord(message));
        plugin.getFeature(TranslatorForwarder.class).ifPresent(forwarder -> forwarder.forwardFromDiscord(message));
        plugin.getFeature(ChannelForwarder.class).ifPresent(forwarder -> forwarder.forwardFromDiscord(message));

    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        Guild guild = event.getGuild();
        if(!guild.getId().equals(plugin.getServerId())) return;

        plugin.getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(event.getMember()));

    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        Guild guild = event.getGuild();
        if(!guild.getId().equals(plugin.getServerId())) return;

        plugin.getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadMember(event.getMember()));

    }

    private boolean handleCommand(Message message) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        BotInstance bot = plugin.getBot();
        StringTokenizer tokens = new StringTokenizer(message.getContentDisplay());

        String command;
        if(tokens.hasMoreTokens()) command = tokens.nextToken();
        else return false;

        String[] args = new String[tokens.countTokens()];
        for(int i = 0; i < args.length; i++) args[i] = tokens.nextToken();

        command = command.toLowerCase();
        if(command.startsWith(COMMAND_PREFIX)) command = command.substring(COMMAND_PREFIX.length());
        else return false;

        if(command.equals("list") || (command.equals("minecraft") && args.length >= 1 && args[0].equals("list"))) {
            List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
            String title = String.format(plugin.getMessage("discord-command.player-list", "List of online players (%1$d/%2$d)") + "\n", Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers());
            String content = players.stream().map(Player::getPlayerListName).map(name -> "- " + name).collect(Collectors.joining("\n"));
            bot.sendDiscordMessage(new EmbedMessage(message.getTextChannel(), title, content));
            return true;

        } else if(command.equals("discord")) {
            if(args.length < 1) {
                message.getChannel().sendMessage("/discord [whois]").queue();
                return true;
            }
            if(args[0].equals("whois")) {
                String content = message.getContentDisplay();
                String longArgs = content.substring(content.indexOf(args[0]) + args[0].length()).trim();
                KubeCityPlayer kubeCityPlayer = null;
                if(longArgs.length() > 0) {
                    List<Member> members = message.getGuild().getMembersByEffectiveName(longArgs, true);
                    if(!members.isEmpty()) {
                        kubeCityPlayer = KubeCityPlayer.of(members.get(0).getUser().getId());
                    }
                } else {
                    message.getChannel().sendMessage("/discord whois [name]").queue();
                }
                if(kubeCityPlayer != null) {
                    String uuid = kubeCityPlayer.getUuid();
                    String minecraftName = kubeCityPlayer.getNickname();
                    Member member = bot.getGuild().getMemberById(kubeCityPlayer.getDiscordId());
                    if(member != null) {
                        String discordName = member.getEffectiveName();
                        message.getChannel().sendMessage(String.format(plugin.getMessage(
                                "registration.player-info-discord",
                                "Player info:\n" +
                                        " - UUID: %1$s\n" +
                                        " - Minecraft Name: %2$s\n" +
                                        " - Discord Name: %3$s"
                        ), uuid, minecraftName, discordName)).queue();
                        return true;
                    }
                }
                message.getChannel().sendMessage(plugin.getMessage(
                        "registration.player-not-found-discord",
                        "Player not found.")).queue();
            }
            return true;

        } else if(command.equals("minecraft")) {
            if(args.length < 1) {
                message.getChannel().sendMessage("/minecraft [whois]").queue();
                return true;
            }
            if(args[0].equals("whois")) {
                if(args.length < 2) {
                    message.getChannel().sendMessage("/minecraft whois [nickname]").queue();
                    return true;
                }
                KubeCityPlayer kubeCityPlayer = null;
                OfflinePlayer offlinePlayer;
                try {
                    UUID uuid = UUID.fromString(args[1]);
                    offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                } catch(IllegalArgumentException ex) {
                    offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers()).filter(p -> p.getName().equals(args[1])).findAny().orElse(null);
                }
                if(offlinePlayer != null) kubeCityPlayer = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
                if(kubeCityPlayer != null) {
                    String uuid = kubeCityPlayer.getUuid();
                    String minecraftName = kubeCityPlayer.getNickname();
                    Member member = bot.getGuild().getMemberById(kubeCityPlayer.getDiscordId());
                    if(member != null) {
                        String discordName = member.getEffectiveName();
                        message.getChannel().sendMessage(String.format(plugin.getMessage(
                                "registration.player-info-discord",
                                "Player info:\n" +
                                        " - UUID: %1$s\n" +
                                        " - Minecraft Name: %2$s\n" +
                                        " - Discord Name: %3$s"
                        ), uuid, minecraftName, discordName)).queue();
                        return true;
                    }
                }
                message.getChannel().sendMessage(plugin.getMessage(
                        "registration.player-not-found-discord",
                        "Player not found.")).queue();
            }
            return true;

        } else {
            return false;
        }

    }

}
