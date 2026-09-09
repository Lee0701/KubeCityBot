package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.VotesDatabase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public class BuildingVotes implements Feature {
    private VotesDatabase database;

    @Override
    public void reload(JavaPlugin plugin) {
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
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("building-votes");
    }

    public VotesDatabase getDatabase() {
        return database;
    }
}
