package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.KobayaBotPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TranslatorForwarder extends Forwarder {

    private List<String> languages = new ArrayList<>();

    @Override
    public void reload(JavaPlugin plugin) {
        super.reload(plugin);
        languages = getConfigurationSection().getStringList("languages");
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("translator-forwarder");
    }

    @Override
    public void forwardFromMinecraft(Player player, String message) {
        super.forwardFromMinecraft(player, message);
    }

    public List<String> getLanguages() {
        return languages;
    }
}
