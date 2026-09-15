package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class PlayerAttendEvent extends Event {
    public static final HandlerList handlers = new HandlerList();

    private Player player;
    private KubeCityPlayer kubeCityPlayer;
    private long today;
    private long lastAttendanceDay;

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerAttendEvent(Player player, KubeCityPlayer kubeCityPlayer, long today, long lastAttendanceDay) {
        this.player = player;
        this.kubeCityPlayer = kubeCityPlayer;
        this.today = today;
        this.lastAttendanceDay = lastAttendanceDay;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public KubeCityPlayer getKubeCityPlayer() {
        return kubeCityPlayer;
    }

    public void setKubeCityPlayer(KubeCityPlayer kubeCityPlayer) {
        this.kubeCityPlayer = kubeCityPlayer;
    }

    public long getToday() {
        return today;
    }

    public void setToday(long today) {
        this.today = today;
    }

    public long getLastAttendanceDay() {
        return lastAttendanceDay;
    }

    public void setLastAttendanceDay(long lastAttendanceDay) {
        this.lastAttendanceDay = lastAttendanceDay;
    }
}
