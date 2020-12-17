package city.kube.bot.discord;

import city.kube.bot.KubeCityBotPlugin;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        KubeCityBotPlugin.getInstance().reloadFeatures();
    }
}
