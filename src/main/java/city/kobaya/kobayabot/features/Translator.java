package city.kobaya.kobayabot.features;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Translator implements Feature {

    private List<String> channels = new ArrayList<>();
    private List<String> languages = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        channels = plugin.getConfig().getConfigurationSection("translator").getStringList("channels");
        languages = plugin.getConfig().getConfigurationSection("translator").getStringList("languages");
    }

    @Override
    public void save() {

    }

    public List<String> getChannels() {
        return channels;
    }

    public List<String> getLanguages() {
        return languages;
    }
}
