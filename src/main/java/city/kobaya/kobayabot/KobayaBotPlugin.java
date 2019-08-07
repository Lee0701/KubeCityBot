package city.kobaya.kobayabot;

import city.kobaya.kobayabot.discord.BotInstance;
import city.kobaya.kobayabot.features.Broadcaster;
import city.kobaya.kobayabot.features.Feature;
import city.kobaya.kobayabot.features.Translator;
import city.kobaya.kobayabot.minecraft.CommandHandler;
import city.kobaya.kobayabot.minecraft.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        getCommand("kobayabot").setExecutor(new CommandHandler());
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);

    }

    @Override
    public void onDisable() {
    }

    private void reload() {
        reloadConfig();

        FileConfiguration config = getConfig();
        String botToken = config.getString("bot-token");
        serverId = config.getString("server-id");

        if(botToken != null) {
            bot.launch(botToken);
        } else {
            getLogger().warning("Discord bot token is not set. Disabling Discord bot.");
        }

        dataConfiguration = YamlConfiguration.loadConfiguration(dataFile);

        features.clear();

        if(config.getConfigurationSection("broadcaster").getBoolean("use")) features.add(new Broadcaster());
        if(config.getConfigurationSection("translator").getBoolean("use")) features.add(new Translator());

        for(Feature feature : features) {
            feature.reload(this);
        }

    }

    public Feature getFeature(Class<? extends Feature> type) {
        return features.stream().filter(feature -> feature.getClass().equals(type)).findAny().orElse(null);
    }

    public BotInstance getBot() {
        return bot;
    }

    public String getServerId() {
        return serverId;
    }

}
