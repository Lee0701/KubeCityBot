package kr.kubecity.bot;

import kr.kubecity.bot.discord.BotInstance;
import kr.kubecity.bot.features.*;
import kr.kubecity.bot.minecraft.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class KubeCityBotPlugin extends JavaPlugin {

    public static KubeCityBotPlugin getInstance() {
        return getPlugin(KubeCityBotPlugin.class);
    }

    private BotInstance bot = new BotInstance();
    private String serverId;
    private ZoneId timezone;

    private final File dataFile = new File(getDataFolder(), "data.yml");
    private YamlConfiguration dataConfiguration;

    private final File messagesFile = new File(getDataFolder(), "messages.yml");
    private YamlConfiguration messagesConfiguration;
    private YamlConfiguration defaultMessagesConfiguration;

    private final List<Feature> features = new ArrayList<>();

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        if(!messagesFile.exists()) saveResource(messagesFile.getName(), false);

        ConfigurationSerialization.registerClass(KubeCityPlayer.class);
        ConfigurationSerialization.registerClass(Building.class);
        ConfigurationSerialization.registerClass(BuildingApproval.class);

        reload();

        getCommand("kubecitybot").setExecutor(new KubeCityBotCommandHandler());
        getCommand("discord").setExecutor(new DiscordCommandHandler());
        getCommand("building").setExecutor(new BuildingCommandHandler());
        getCommand("level").setExecutor(new LevelCommandHandler());

        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), this);

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new KubeCityPlaceholderExpansion().register();
        }

    }

    @Override
    public void onDisable() {
        saveData();
        bot.shutdown();
    }

    public void reload() {
        for(Feature feature : features) {
            feature.unload(this);
        }

        reloadConfig();

        FileConfiguration config = getConfig();
        String botToken = config.getString("bot-token");
        serverId = config.getString("server-id");
        timezone = ZoneId.of(config.getString("timezone"));

        if(botToken != null) {
            bot.launch(botToken);
        } else {
            getLogger().warning("Discord bot token is not set. Disabling Discord bot.");
        }

        KubeCityPlayer.PLAYER_MAP.clear();
        dataConfiguration = YamlConfiguration.loadConfiguration(dataFile);
        if(dataConfiguration.isList("players")) dataConfiguration.getList("players");

        messagesConfiguration = YamlConfiguration.loadConfiguration(messagesFile);
        defaultMessagesConfiguration = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource(messagesFile.getName())));

        features.clear();
        if(config.getBoolean("icon-storage.use")) features.add(new IconStorage());
        if(config.getBoolean("broadcaster.use")) features.add(new Broadcaster());
        if(config.getBoolean("status-updater.use")) features.add(new StatusUpdater());
        if(config.getBoolean("simple-forwarder.use")) features.add(new SimpleForwarder());
        if(config.getBoolean("translator-forwarder.use")) features.add(new TranslatorForwarder());
        if(config.getBoolean("channel-forwarder.use")) features.add(new ChannelForwarder());
        if(config.getBoolean("group-linker.use")) features.add(new GroupLinker());
        if(config.getBoolean("building-storage.use")) features.add(new BuildingStorage());
        if(config.getBoolean("building-votes.use")) features.add(new BuildingVotes());
        if(config.getBoolean("builder-level.use")) features.add(new BuilderLevel());
        if(config.getBoolean("builder-level-rewards.use")) features.add(new BuilderLevelRewards());

    }

    public void saveData() {
        for(Feature feature : features) {
            feature.save();
        }

        dataConfiguration.set("players", new ArrayList<>(KubeCityPlayer.PLAYER_MAP.values()));
        try {
            dataConfiguration.save(dataFile);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadFeatures() {
        // This is done later when discord bot is logged in.
        for(Feature feature : features) {
            feature.load(this);
        }
    }

    public <T extends Feature> Optional<T> getFeature(Class<T> type) {
        return (Optional<T>) features.stream().filter(feature -> feature.getClass().equals(type)).findAny();
    }

    public String getMessage(String key) {
        return getMessage(key, null);
    }

    public String getMessage(String key, String def) {
        return Optional.ofNullable(messagesConfiguration.getString(key))
                .orElse(defaultMessagesConfiguration.getString(key, def));
    }

    public BotInstance getBot() {
        return bot;
    }

    public String getServerId() {
        return serverId;
    }

    public ZoneId getTimezone() {
        return timezone;
    }
}
