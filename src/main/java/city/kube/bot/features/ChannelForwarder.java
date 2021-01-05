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
    private final List<Channel> defaultListeningChannels = new ArrayList<>();
    private final List<Channel> defaultSpeakingChannels = new ArrayList<>();
    private final Map<String, Channel> channels = new HashMap<>();

    private final Map<Player, Set<Channel>> listeningChannels = new HashMap<>();
    private final Map<Player, Set<Channel>> speakingChannels = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        KubeCityPlayer.of(event.getPlayer()).ifPresent(kubeCityPlayer -> {
            kubeCityPlayer.setChatFormat(event.getFormat());
            kubeCityPlayer.setNickname(event.getPlayer().getDisplayName());
        });

        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        Set<Channel> speakingChannels = this.speakingChannels.get(event.getPlayer());
        Set<Channel> listeningChannels = this.listeningChannels.get(event.getPlayer());
        if(speakingChannels == null) return;

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
            if(KubeCityPlayer.of(player).orElse(null) == null) return;

            String command = message.split(" ")[0].substring(prefix.length());
            int index = prefix.length() + command.length() + 1;
            String messageWithoutCommand = message.length() >= index ? message.substring(index) : "";

            if(messageWithoutCommand.length() == 0) {
                if(command.startsWith("@")) {
                    // "#@{channel}" Add all to listening
                    this.channels.values().forEach(channel -> channel.addRecipient(player));
                    listeningChannels.addAll(this.channels.values());
                    sendChannelList(player);
                    event.setCancelled(true);
                } else if(command.startsWith("+")) {
                    // "#+{channel}" : Add listening
                    String shortName = command.substring(1);
                    Channel channel = this.channels.get(shortName);
                    if(channel == null) {
                        sendChannelNotFound(player);
                    } else {
                        listeningChannels.add(channel);
                        channel.addRecipient(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                } else if(command.startsWith("-")) {
                    // "#-{channel}" : Remove listening
                    String shortName = command.substring(1);
                    Channel channel = this.channels.get(shortName);
                    if(channel == null) {
                        sendChannelNotFound(player);
                    } else {
                        listeningChannels.remove(channel);
                        channel.removeRecipient(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                } else if(command.startsWith("#")) {
                    // "##{channel}" : Add listening/speaking
                    if(player.isOp()) {
                        String shortName = command.substring(1);
                        Channel channel = this.channels.get(shortName);
                        if(channel == null) {
                            sendChannelNotFound(player);
                        } else {
                            listeningChannels.add(channel);
                            speakingChannels.add(channel);
                            channel.addRecipient(player);
                            sendChannelList(player);
                        }
                    }
                    event.setCancelled(true);
                } else if(command.startsWith("=")) {
                    // "#={channel}" : Remove listening/speaking
                    if(player.isOp()) {
                        String shortName = command.substring(1);
                        Channel channel = this.channels.get(shortName);
                        if(channel == null) {
                            sendChannelNotFound(player);
                        } else {
                            removeFromAllChannel(player);
                            listeningChannels.remove(channel);
                            speakingChannels.remove(channel);
                            channel.addRecipient(player);
                            sendChannelList(player);
                        }
                    }
                    event.setCancelled(true);
                } else {
                    // "#{channel} : Set listening channel"
                    String shortName = command;
                    Channel channel = this.channels.get(shortName);
                    if(channel == null) {
                        sendChannelNotFound(player);
                    } else {
                        removeFromAllChannel(player);
                        listeningChannels.add(channel);
                        speakingChannels.add(channel);
                        channel.addRecipient(player);
                        sendChannelList(player);
                    }
                    event.setCancelled(true);
                }
            } else {
                if(command.startsWith("@")) {
                    // "#@ Message" : Send to all channels
                    if(player.isOp()) {
                        String newMessage = message.substring(prefix.length() + command.length() + 1);
                        event.setMessage(newMessage);
                        plugin.getBot().sendDiscordMessages(this.channels.values().stream().map(c -> c.getDiscordChannel().getId()).collect(Collectors.toList()),
                                c -> new EmbedForwarderMessage(c, new ForwarderMessage(
                                        player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), newMessage))
                        );
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    // "#{channel} Message : Send to specific channel"
                    String shortName = command;
                    Channel channel = this.channels.get(shortName);
                    if(channel == null) {
                        sendChannelNotFound(player);
                        event.setCancelled(true);
                    } else {
                        event.getRecipients().clear();
                        event.getRecipients().addAll(channel.getRecipients());
                        String newMessage = message.substring(prefix.length() + command.length() + 1);
                        event.setMessage(newMessage);
                        plugin.getBot().sendDiscordMessage(new EmbedForwarderMessage(channel.getDiscordChannel(), new ForwarderMessage(
                                player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), newMessage)));
                    }
                }
            }
            return;
        }
        // Default : Send to speaking channels
        List<Player> recipients = speakingChannels.stream().flatMap(c -> c.getRecipients().stream()).distinct().collect(Collectors.toList());
        event.getRecipients().clear();
        event.getRecipients().addAll(recipients);
        plugin.getBot().sendDiscordMessages(speakingChannels.stream().map(c -> c.getDiscordChannel().getId()).collect(Collectors.toList()),
                c -> new EmbedForwarderMessage(c, new ForwarderMessage(
                        player.getName(), IconStorage.getIconFor(player.getUniqueId()), "Minecraft", KubeCityPlayer.checkLinked(player), message))
        );
    }

    public void forwardFromDiscord(Message message) {
        prefix = getConfigurationSection().getString("prefix");

        Channel channel = channels.values().stream()
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
        for(Player recipient : channel.getRecipients()) {
            recipient.sendMessage(String.format(format, minecraftName, text));
        }

    }

    private void sendChannelList(Player player) {
        Set<Channel> speaking = speakingChannels.get(player);
        Set<Channel> listening = listeningChannels.get(player);
        if(listening == null || speaking == null) return;
        player.sendMessage(KubeCityBotPlugin.getInstance().getMessage("channel-forwarder.channel-list", "§fRegistered channels: ")
                + listening.stream().map(c -> String.format("%1$s" + prefix + c.getShortName() + "%1$s", speaking.contains(c) ? "§a" : "§f")).collect(Collectors.joining(", ")));
    }

    private void sendChannelNotFound(Player player) {
        player.sendMessage(KubeCityBotPlugin.getInstance().getMessage("channel-forwarder.channel-not-found", "§eChannel not found."));
    }

    public void newChannelLists(Player player) {
        listeningChannels.put(player, new HashSet<>());
        speakingChannels.put(player, new HashSet<>());
    }

    public void addDefaultChannels(Player player) {
        defaultListeningChannels.forEach(channel -> {
            channel.addRecipient(player);
            listeningChannels.get(player).add(channel);
        });
        defaultSpeakingChannels.forEach(channel -> {
            channel.addRecipient(player);
            speakingChannels.get(player).add(channel);
        });
    }

    public void removeFromAllChannel(Player player) {
        listeningChannels.get(player).clear();
        speakingChannels.get(player).clear();
        channels.values().forEach(channel -> channel.removeRecipient(player));
    }

    public void loadPlayer(Player player) {
        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(kubeCityPlayer != null) {
            List<String> listening = kubeCityPlayer.getListeningChannels();
            List<String> speaking = kubeCityPlayer.getSpeakingChannels();
            if(listening != null && speaking != null) {
                listening.stream().map(this.channels::get).forEach(channel -> channel.addRecipient(player));
                listeningChannels.put(player, listening.stream().map(this.channels::get).collect(Collectors.toSet()));
                speakingChannels.put(player, speaking.stream().map(this.channels::get).collect(Collectors.toSet()));
            } else {
                newChannelLists(player);
                removeFromAllChannel(player);
                addDefaultChannels(player);
            }
        } else {
            newChannelLists(player);
            removeFromAllChannel(player);
            addDefaultChannels(player);
        }
    }

    public void savePlayer(Player player) {
        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(!listeningChannels.containsKey(player) || !speakingChannels.containsKey(player)) return;
        if(kubeCityPlayer != null) {
            kubeCityPlayer.setListeningChannels(listeningChannels.get(player).stream().map(Channel::getShortName).collect(Collectors.toList()));
            kubeCityPlayer.setSpeakingChannels(speakingChannels.get(player).stream().map(Channel::getShortName).collect(Collectors.toList()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayer(event.getPlayer());
        removeFromAllChannel(event.getPlayer());
        listeningChannels.remove(event.getPlayer());
        speakingChannels.remove(event.getPlayer());
    }

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, KubeCityBotPlugin.getInstance());
        channels.clear();
        listeningChannels.clear();
        getConfigurationSection().getStringList("channels").forEach(line -> {
            String[] config = line.split(" ");
            if(config.length >= 2) {
                Channel channel = new Channel(config[1], config[0]);
                if(channel.getDiscordChannel() == null) {
                    Bukkit.getLogger().warning(String.format("ChannelForwarder channels config \"%s\" has been ignored due to invalid id.", line));
                } else {
                    channels.put(config[1], channel);
                }
            } else {
                Bukkit.getLogger().warning(String.format("ChannelForwarder channels config \"%s\" has been ignored due to invalid format.", line));
            }
        });
        getConfigurationSection().getStringList("default-listening-channels").forEach(shortName -> {
            defaultListeningChannels.add(channels.get(shortName));
        });
        getConfigurationSection().getStringList("default-speaking-channels").forEach(shortName -> {
            defaultSpeakingChannels.add(channels.get(shortName));
        });
        plugin.getServer().getOnlinePlayers().forEach(this::loadPlayer);
    }

    @Override
    public void save() {
        Bukkit.getOnlinePlayers().forEach(this::savePlayer);
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("channel-forwarder");
    }

    static class Channel {
        private final String shortName;
        private final TextChannel discordChannel;
        private final Set<Player> recipients = new HashSet<>();

        public Channel(String shortName, String discordChannelId) {
            this.shortName = shortName;
            this.discordChannel = KubeCityBotPlugin.getInstance()
                    .getBot().getGuild().getTextChannelById(discordChannelId);
        }

        public void addRecipient(Player player) {
            this.recipients.add(player);
        }

        public void removeRecipient(Player player) {
            this.recipients.remove(player);
        }

        public String getShortName() {
            return shortName;
        }

        public TextChannel getDiscordChannel() {
            return discordChannel;
        }

        public Set<Player> getRecipients() {
            return recipients;
        }
    }

}
