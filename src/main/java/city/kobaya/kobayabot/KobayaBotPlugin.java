package city.kobaya.kobayabot;

import city.kobaya.kobayabot.discord.BotInstance;
import city.kobaya.kobayabot.features.*;
import city.kobaya.kobayabot.minecraft.DiscordCommandHandler;
import city.kobaya.kobayabot.minecraft.KobayaBotCommandHandler;
import city.kobaya.kobayabot.minecraft.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class KobayaBotPlugin extends JavaPlugin {

    public static KobayaBotPlugin getInstance() {
        return getPlugin(KobayaBotPlugin.class);
    }

    private BotInstance bot = new BotInstance();
    private String serverId;

    private final File dataFile = new File(getDataFolder(), "data.yml");
    private YamlConfiguration dataConfiguration;

    private final List<Feature> features = new ArrayList<>();

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();

        reload();

        getCommand("kobayabot").setExecutor(new KobayaBotCommandHandler());
        getCommand("discord").setExecutor(new DiscordCommandHandler());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    public void reload() {
        reloadConfig();

        FileConfiguration config = getConfig();
        String botToken = config.getString("bot-token");
        serverId = config.getString("server-id");

        if(botToken != null) {
            bot.launch(botToken);
        } else {
            getLogger().warning("Discord bot token is not set. Disabling Discord bot.");
        }

        KobayaPlayer.PLAYER_MAP.clear();
        dataConfiguration = YamlConfiguration.loadConfiguration(dataFile);
        if(dataConfiguration.isList("players")) dataConfiguration.getList("players");

        features.clear();
        if(config.getConfigurationSection("broadcaster").getBoolean("use")) features.add(new Broadcaster());
        if(config.getConfigurationSection("simple-forwarder").getBoolean("use")) features.add(new SimpleForwarder());
        if(config.getConfigurationSection("translator-forwarder").getBoolean("use")) features.add(new TranslatorForwarder());
        if(config.getConfigurationSection("group-linker").getBoolean("use")) features.add(new GroupLinker());

        for(Feature feature : features) {
            feature.reload(this);
        }

    }

    @Override
    public void saveConfig() {
        super.saveConfig();

        for(Feature feature : features) {
            feature.save();
        }
        dataConfiguration.set("players", new ArrayList<>(KobayaPlayer.PLAYER_MAP.values()));
        try {
            dataConfiguration.save(dataFile);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public <T extends Feature> Optional<T> getFeature(Class<T> type) {
        return (Optional<T>) features.stream().filter(feature -> feature.getClass().equals(type)).findAny();
    }

    public BotInstance getBot() {
        return bot;
    }

    public String getServerId() {
        return serverId;
    }

}
