package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class LevelUpEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private KubeCityPlayer player;
    private int level;

    public LevelUpEvent(KubeCityPlayer player, int level) {
        this.player = player;
        this.level = level;
    }

    public KubeCityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(KubeCityPlayer player) {
        this.player = player;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
