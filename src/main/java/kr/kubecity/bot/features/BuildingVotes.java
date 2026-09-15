package kr.kubecity.bot.features;

import kr.kubecity.bot.*;
import kr.kubecity.bot.minecraft.BuildingApproveEvent;
import kr.kubecity.bot.minecraft.PlayerAttendEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuildingVotes implements Feature, Listener {
    private VotesDatabase database;

    private File approvalDataFile;
    private YamlConfiguration approvalDataConfiguration;

    private boolean useTickets;
    private int maxTickets;
    private int attendanceRewardTickets;
    private int buildingApprovalRewardTickets;

    private boolean requireApproval;
    private List<Integer> approvalRewards;

    @Override
    public void load(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        String databasePath = new File(
                plugin.getDataFolder(),
                getConfigurationSection().getString("database-path", "votes.db")
        ).getPath();

        try {
            if(this.database != null) this.database.close();
            this.database = new VotesDatabase(databasePath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        useTickets = getConfigurationSection().getBoolean("vote-tickets.use");
        maxTickets = getConfigurationSection().getInt("vote-tickets.max");
        attendanceRewardTickets = getConfigurationSection().getInt("vote-tickets.attendance-reward");
        buildingApprovalRewardTickets =  getConfigurationSection().getInt("vote-tickets.building-approval-reward");

        requireApproval = getConfigurationSection().getBoolean("require-approval");
        approvalRewards = getConfigurationSection().getIntegerList("approval-rewards");

        BuildingApproval.BUILDING_APPROVALS.clear();
        approvalDataFile = new File(plugin.getDataFolder(), "building_approvals.yml");
        approvalDataConfiguration = YamlConfiguration.loadConfiguration(approvalDataFile);
        if(approvalDataConfiguration.isList("approvals")) approvalDataConfiguration.getList("approvals");
    }

    @Override
    public void unload(JavaPlugin plugin) {
        HandlerList.unregisterAll(this);
        if(this.database != null) {
            try {
                this.database.close();
                this.database = null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void save() {
        approvalDataConfiguration.set("approvals", new ArrayList<>(BuildingApproval.BUILDING_APPROVALS.values()));
        try {
            approvalDataConfiguration.save(approvalDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("building-votes");
    }

    @EventHandler
    public void onPlayerAttend(PlayerAttendEvent event) {
        if(!useTickets) return;
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        KubeCityPlayer player = event.getKubeCityPlayer();
        if(player.getVoteTickets() >= maxTickets) {
            event.getPlayer().sendMessage(plugin.getMessage("building-votes.vote-tickets-full"));
            return;
        }
        player.setVoteTickets(Math.min(player.getVoteTickets() + attendanceRewardTickets, maxTickets));
        event.getPlayer().sendMessage(String.format(plugin.getMessage("building-votes.attendance-reward-given"), attendanceRewardTickets));
    }

    @EventHandler
    public void onBuildingApprove(BuildingApproveEvent event) {
        if(!useTickets) return;
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        Building building = Building.BUILDINGS.get(event.getApproval().getBuildingId());
        if(building == null) return;
        UUID uuid = UUID.fromString(building.getBuilderUuid());
        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(uuid).orElse(null);
        if(kubeCityPlayer == null) return;
        kubeCityPlayer.setVoteTickets(Math.min(kubeCityPlayer.getVoteTickets() + buildingApprovalRewardTickets, maxTickets));
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        player.sendMessage(String.format(plugin.getMessage("building-votes.building-approval-reward-given"), buildingApprovalRewardTickets));
    }

    public int getApprovalReward(int rating) {
        return approvalRewards.get(rating - 1);
    }

    public int getMinRating() {
        return 1;
    }

    public int getMaxRating() {
        return approvalRewards.size();
    }

    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <= approvalRewards.size();
    }

    public VotesDatabase getDatabase() {
        return database;
    }

    public boolean isRequireApproval() {
        return requireApproval;
    }

    public boolean isUseTickets() {
        return useTickets;
    }

    public int getMaxTickets() {
        return maxTickets;
    }
}
