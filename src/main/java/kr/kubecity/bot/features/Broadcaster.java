package kr.kubecity.bot.features;

import kr.kubecity.bot.Building;
import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.discord.BotInstance;
import kr.kubecity.bot.discord.message.EmbedMessage;
import kr.kubecity.bot.discord.message.SimpleMessage;
import kr.kubecity.bot.minecraft.BuildingApproveEvent;
import kr.kubecity.bot.minecraft.LevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Broadcaster implements Feature, Listener {
    private final BotInstance bot = KubeCityBotPlugin.getInstance().getBot();

    private List<String> joinQuitChannels = new ArrayList<>();
    private List<String> levelUpChannels = new ArrayList<>();
    private List<String> buildingApprovalChannels = new ArrayList<>();

    @Override
    public void load(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        joinQuitChannels = getConfigurationSection().getStringList("join-quit");
        levelUpChannels = getConfigurationSection().getStringList("level-up");
        buildingApprovalChannels = getConfigurationSection().getStringList("building-approval");
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
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("broadcaster");
    }

    public void broadcastJoinQuit(String message) {
        bot.sendDiscordMessages(joinQuitChannels, channel -> new SimpleMessage(channel, message));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        broadcastJoinQuit(String.format(KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-join", "%1$s joined."), event.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        broadcastJoinQuit(String.format(KubeCityBotPlugin.getInstance().getMessage("broadcaster.player-quit", "%1$s left."), event.getPlayer().getName()));
    }

    @EventHandler
    public void onBuildingApproved(BuildingApproveEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        Building building = Building.of(event.getApproval().getBuildingId());
        String name = building.getName();
        String url = building.getFullUrl();
        String builder = building.getBuilderName();
        String rating = "★".repeat(event.getApproval().getRating());

        String format = plugin.getMessage("broadcaster.building-approved-content");
        String content = String.format(format, name, url, builder, rating);

        bot.sendDiscordMessages(buildingApprovalChannels, channel -> new SimpleMessage(channel, content));
    }

    @EventHandler
    public void onPlayerLevelUp(LevelUpEvent event) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        KubeCityPlayer player = event.getPlayer();

        String title = plugin.getMessage("broadcaster.level-up-title");
        String content = String.format(
                plugin.getMessage("broadcaster.level-up-content"),
                player.getNickname(), player.getBuilderLevel()
        );

        bot.sendDiscordMessages(levelUpChannels, channel -> {
            EmbedMessage message = new EmbedMessage(channel, title, content);
            message.setNickname(player.getNickname());
            message.setAvatar(IconStorage.getIconFor(UUID.fromString(player.getUuid())));
            return message;
        });

        Player bukkitPlayer = Bukkit.getPlayer(UUID.fromString(player.getUuid()));
        if(bukkitPlayer != null) bukkitPlayer.sendMessage(String.format(
                plugin.getMessage("broadcaster.level-up-personal"),
                player.getBuilderLevel()
        ));
    }
}
