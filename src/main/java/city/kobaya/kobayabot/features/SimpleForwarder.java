package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.KobayaBotPlugin;
import org.bukkit.configuration.ConfigurationSection;

public class SimpleForwarder extends Forwarder {
    @Override
    public ConfigurationSection getConfigurationSection() {
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("simple-forwarder");
    }
}
