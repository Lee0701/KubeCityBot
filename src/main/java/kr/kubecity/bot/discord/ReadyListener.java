package kr.kubecity.bot.discord;

import kr.kubecity.bot.KubeCityBotPlugin;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReadyListener extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        KubeCityBotPlugin.getInstance().reloadFeatures();
    }
}
