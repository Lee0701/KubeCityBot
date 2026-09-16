package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDate;
import java.util.Date;

public class PlayerJoinEventListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        KubeCityPlayer player = KubeCityPlayer.of(event.getPlayer()).orElse(null);
        if(player == null) return;

        Date lastAttendance = player.getLastAttendance();
        if(lastAttendance == null) lastAttendance = new Date(0L);
        long today = LocalDate.now(plugin.getTimezone()).toEpochDay();
        long lastAttendanceDay = LocalDate.ofInstant(lastAttendance.toInstant(), plugin.getTimezone()).toEpochDay();

        player.setLastAttendance(new Date());
        if(today <= lastAttendanceDay) return;

        PlayerAttendEvent playerAttendEvent =  new PlayerAttendEvent(
                event.getPlayer(),
                player,
                lastAttendanceDay,
                today
        );
        Bukkit.getPluginManager().callEvent(playerAttendEvent);
    }
}
