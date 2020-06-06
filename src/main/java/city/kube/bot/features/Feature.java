package city.kube.bot.features;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public interface Feature {

    void reload(JavaPlugin plugin);

    void save();

    ConfigurationSection getConfigurationSection();

}
