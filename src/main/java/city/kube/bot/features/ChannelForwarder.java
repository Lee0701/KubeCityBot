package city.kube.bot.features;

import city.kube.bot.IconStorage;
import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.discord.message.EmbedForwarderMessage;
import city.kube.bot.discord.message.ForwarderMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class ChannelForwarder implements Feature, Listener {

    private String prefix = "#";
    private final List<Channel> defaultChannels = new ArrayList<>();
    private final List<Channel> channels = new LinkedList<>();

    private final Map<Player, Set<Channel>> playerChannels = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        Set<Channel> channels = playerChannels.get(event.getPlayer());
        if(channels == null) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if(message == null || message.length() == 0) return;

        if(message.startsWith("\\" + prefix)) {
            // Escaped message.
            event.setMessage(message.substring(1));
        } else if(message.equals(prefix)) {
            removeFromAllChannel(player);
            addDefaultChannels(player);
            sendChannelList(player);
            event.setCancelled(true);
            return;
        } else if(message.startsWith(prefix)) {
            String command = message.split(" ")[0].substring(prefix.length());
            int index = prefix.length() + command.length() + 1;
            String messageWithoutCommand = message.length() >= index ? message.substring(index) : "";

            if(messageWithoutCommand.length() == 0) {
                if(command.startsWith("@")) {
                    removeFromAllChannel(player);
                    channels.addAll(this.channels);
                    this.channels.forEach(channel -> channel.addPlayer(player));
                    sendChannelList(player);
                    event.setCancelled(true);
                } else if(command.startsWith("+")) {
                    String shortName = command.substring(1);
                    Channel channel = this.channels.stream().filter(c -> c.getShortName().equals(shortName)).findAny().orElse(null);
                    if(channel == null) {
                        player.sendMessage(String.format(plugin.getMessage("channel-forwarder.channel-not-found", "§eChannel %1$s not found."), shortName));
                    } else {
                        channels.add(channel);
                        channel.addPlayer(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                } else if(command.startsWith("-")) {
                    String shortName = command.substring(1);
                    Channel channel = this.channels.stream().filter(c -> c.getShortName().equals(shortName)).findAny().orElse(null);
                    if(channel == null) {
                        player.sendMessage(String.format(plugin.getMessage("channel-forwarder.channel-not-found", "§eChannel %1$s not found."), shortName));
                    } else {
                        channels.remove(channel);
                        channel.removePlayer(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                } else {
                    String shortName = command;
                    Channel channel = this.channels.stream().filter(c -> c.getShortName().equals(shortName)).findAny().orElse(null);
                    if(channel == null) {
                        player.sendMessage(String.format(plugin.getMessage("channel-forwarder.channel-not-found", "§eChannel %1$s not found."), shortName));
                    } else {
                        removeFromAllChannel(player);
                        channels.add(channel);
                        channel.addPlayer(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                }
            } else {
                if(command.startsWith("@")) {
                    String newMessage = message.substring(prefix.length() + command.length() + 1);
                    event.setMessage(newMessage);
                    plugin.getBot().sendDiscordMessages(this.channels.stream().map(c -> c.getDiscordChannel().getId()).collect(Collectors.toList()),
                            c -> new EmbedForwarderMessage(c, new ForwarderMessage(
                                    player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), newMessage))
                    );
                } else {
                    String shortName = command;
                    Channel channel = channels.stream().filter(c -> c.getShortName().equals(shortName)).findAny().orElse(null);
                    if(channel == null) {
                        player.sendMessage(String.format(plugin.getMessage("channel-forwarder.channel-not-found", "§eChannel %1$s not found."), shortName));
                    } else {
                        event.getRecipients().clear();
                        event.getRecipients().addAll(channel.getPlayers());
                        String newMessage = message.substring(prefix.length() + command.length() + 1);
                        event.setMessage(newMessage);
                        plugin.getBot().sendDiscordMessage(new EmbedForwarderMessage(channel.getDiscordChannel(), new ForwarderMessage(
                                player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), newMessage)));
                    }
                }
            }
            return;
        }
        List<Player> recipients = channels.stream().flatMap(c -> c.getPlayers().stream()).distinct().collect(Collectors.toList());
        event.getRecipients().clear();
        event.getRecipients().addAll(recipients);
        plugin.getBot().sendDiscordMessages(channels.stream().map(c -> c.getDiscordChannel().getId()).collect(Collectors.toList()),
                c -> new EmbedForwarderMessage(c, new ForwarderMessage(
                        player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), message))
        );
    }

    public void forwardFromDiscord(Message message) {
        prefix = getConfigurationSection().getString("prefix");

        Channel channel = channels.stream()
                .filter(c -> c.getDiscordChannel().getId().equals(message.getTextChannel().getId()))
                .findFirst().orElse(null);
        Member member = message.getMember();
        if(channel == null || member == null) return;

        String text = message.getContentDisplay();
        String username = member.getEffectiveName();
        String minecraftName = username;

        Bukkit.getLogger()
                .info(String.format("[%s](%s)<%s>: %s", "Discord", channel.getDiscordChannel().getName(), username, text));

        String format = "[Discord] <%s> %s";

        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(member.getId());
        if(kubeCityPlayer.getUuid() != null) {
            if(kubeCityPlayer.getChatFormat() != null) format = "[Discord] " + kubeCityPlayer.getChatFormat();
            if(kubeCityPlayer.getNickname() != null) minecraftName = kubeCityPlayer.getNickname();
        }

        // Send minecraft messages.
        for(Player recipient : channel.getPlayers()) {
            recipient.sendMessage(String.format(format, minecraftName, text));
        }

    }

    public void sendChannelList(Player player) {
        Set<Channel> channels = playerChannels.get(player);
        if(channels == null) return;
        player.sendMessage(KubeCityBotPlugin.getInstance().getMessage("channel-forwarder.channel-list", "§fRegistered channels: ")
                + channels.stream().map(c -> prefix + c.getShortName()).collect(Collectors.joining(", ")));
    }

    public void addDefaultChannels(Player player) {
        defaultChannels.forEach(channel -> {
            channel.addPlayer(player);
            playerChannels.get(player).add(channel);
        });
    }

    public void removeFromAllChannel(Player player) {
        playerChannels.get(player).clear();
        channels.forEach(channel -> channel.removePlayer(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerChannels.put(event.getPlayer(), new HashSet<>());
        removeFromAllChannel(event.getPlayer());
        addDefaultChannels(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeFromAllChannel(event.getPlayer());
        playerChannels.remove(event.getPlayer());
    }

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, KubeCityBotPlugin.getInstance());
        channels.clear();
        playerChannels.clear();
        getConfigurationSection().getStringList("channels").forEach(line -> {
            String[] config = line.split(" ");
            if(config.length >= 2) {
                Channel channel = new Channel(config[1], config[0]);
                if(channel.getDiscordChannel() == null) {
                    Bukkit.getLogger().warning(String.format("ChannelForwarder channels config \"%s\" has been ignored due to invalid id.", line));
                } else {
                    channels.add(channel);
                }
            } else {
                Bukkit.getLogger().warning(String.format("ChannelForwarder channels config \"%s\" has been ignored due to invalid format.", line));
            }
        });
        getConfigurationSection().getStringList("default-channels").forEach(shortName -> {
            channels.stream().filter(c -> c.getShortName().equals(shortName)).findFirst().ifPresent(defaultChannels::add);
        });
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            playerChannels.put(player, new HashSet<>());
            removeFromAllChannel(player);
            addDefaultChannels(player);
        });
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("channel-forwarder");
    }

    static class Channel {
        private final String shortName;
        private final TextChannel discordChannel;
        private final Set<Player> players = new HashSet<>();

        public Channel(String shortName, String discordChannelId) {
            this.shortName = shortName;
            this.discordChannel = KubeCityBotPlugin.getInstance()
                    .getBot().getGuild().getTextChannelById(discordChannelId);
        }

        public void addPlayer(Player player) {
            this.players.add(player);
        }

        public void removePlayer(Player player) {
            this.players.remove(player);
        }

        public String getShortName() {
            return shortName;
        }

        public TextChannel getDiscordChannel() {
            return discordChannel;
        }

        public Set<Player> getPlayers() {
            return players;
        }
    }

}
