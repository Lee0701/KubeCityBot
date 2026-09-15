package kr.kubecity.bot.features;

import kr.kubecity.bot.BuildingApproval;
import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.VotesDatabase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BuildingVotes implements Feature {
    private VotesDatabase database;

    private File approvalDataFile;
    private YamlConfiguration approvalDataConfiguration;

    private int maxTickets;

    private boolean requireApproval;
    private List<Integer> approvalRewards;

    @Override
    public void load(JavaPlugin plugin) {
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

        maxTickets = getConfigurationSection().getInt("max-tickets");

        requireApproval = getConfigurationSection().getBoolean("require-approval");
        approvalRewards = getConfigurationSection().getIntegerList("approval-rewards");

        BuildingApproval.BUILDING_APPROVALS.clear();
        approvalDataFile = new File(plugin.getDataFolder(), "building_approvals.yml");
        approvalDataConfiguration = YamlConfiguration.loadConfiguration(approvalDataFile);
        if(approvalDataConfiguration.isList("approvals")) approvalDataConfiguration.getList("approvals");
    }

    @Override
    public void unload(JavaPlugin plugin) {
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

    public void giveVoteTickets(KubeCityPlayer player, int amount) {
        int tickets = player.getVoteTickets();
        tickets += amount;
        if(tickets < 0) tickets = 0;
        if(tickets > maxTickets) tickets = maxTickets;
        player.setVoteTickets(tickets);
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
}
