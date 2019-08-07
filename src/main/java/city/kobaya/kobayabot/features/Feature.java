package city.kobaya.kobayabot.features;

import org.bukkit.plugin.java.JavaPlugin;

public interface Feature {

    void reload(JavaPlugin plugin);

    void save();

}
