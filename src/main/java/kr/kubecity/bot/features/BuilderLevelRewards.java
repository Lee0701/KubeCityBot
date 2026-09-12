package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class BuilderLevelRewards implements Feature, Listener {
    private boolean useAttendance;
    private List<Integer> attendanceRewards;

    @Override
    public void load(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, KubeCityBotPlugin.getInstance());

        useAttendance = getConfigurationSection().getBoolean("attendance.use");
        attendanceRewards = getConfigurationSection().getIntegerList("attendance.rewards");
    }

    @Override
    public void unload(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("builder-level-rewards");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if(!useAttendance) return;

        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        BuilderLevel builderLevel = plugin.getFeature(BuilderLevel.class).orElse(null);
        if (builderLevel == null) return;

        KubeCityPlayer player = KubeCityPlayer.of(event.getPlayer()).orElse(null);
        if(player == null) return;
        if(!builderLevel.isBuilderLevelEligible(player)) return;

        Date lastAttendance = player.getLastAttendance();
        if(lastAttendance == null) lastAttendance = new Date(0L);
        long today = LocalDate.now(plugin.getTimezone()).toEpochDay();
        long lastAttendanceDay = LocalDate.ofInstant(lastAttendance.toInstant(), plugin.getTimezone()).toEpochDay();

        player.setLastAttendance(new Date());
        if(today <= lastAttendanceDay) return;

        int attendanceDays = player.getAttendanceDays();
        if(attendanceDays < 0) attendanceDays = 0;
        if(attendanceDays >= attendanceRewards.size()) attendanceDays = attendanceRewards.size() - 1;

        // Continuous attendance
        if(today - lastAttendanceDay == 1) {
            attendanceDays += 1;
        } else {
            attendanceDays = 0;
        }
        // reset days on complete
        if(attendanceDays >= attendanceRewards.size()) attendanceDays = 0;

        player.setAttendanceDays(attendanceDays);

        int rewardExp = attendanceRewards.get(attendanceDays);

        String format = attendanceDays == 0 ?
                plugin.getMessage("builder-level-rewards.attendance-reward-received") :
                plugin.getMessage("builder-level-rewards.attendance-reward-received-continuous");
        String message = attendanceDays == 0 ?
                String.format(format, rewardExp) :
                String.format(format, rewardExp, attendanceDays + 1);
        event.getPlayer().sendMessage(message);

        builderLevel.giveExperiencePoint(player, rewardExp);
    }
}
