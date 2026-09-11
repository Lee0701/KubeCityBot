package kr.kubecity.bot.features;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public interface Feature {

    void load(JavaPlugin plugin);

    void unload(JavaPlugin plugin);

    void save();

    ConfigurationSection getConfigurationSection();

}
