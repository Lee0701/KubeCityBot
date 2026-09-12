package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.discord.BotInstance;
import kr.kubecity.bot.discord.message.EmbedMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class BuilderLevel implements Feature {
    private final BotInstance bot = KubeCityBotPlugin.getInstance().getBot();

    private String requireRoleName;
    private List<String> broadcastChannels;

    private int minLevel;
    private int maxLevel;
    private List<Integer> curve;

    @Override
    public void load(JavaPlugin plugin) {
        requireRoleName = getConfigurationSection().getString("require-role");
        broadcastChannels = getConfigurationSection().getStringList("broadcast-channels");

        File curveFile = new File(plugin.getDataFolder(), "builder_level_curve.yml");
        if(!curveFile.exists()) plugin.saveResource(curveFile.getName(), false);
        YamlConfiguration curveConfig = YamlConfiguration.loadConfiguration(curveFile);
        minLevel = curveConfig.getInt("min-level");
        maxLevel = curveConfig.getInt("max-level");
        curve = curveConfig.getIntegerList("curve");

        KubeCityPlayer.PLAYER_MAP.values().forEach(player -> {
            if(player.getBuilderLevel() < minLevel) player.setBuilderLevel(minLevel);
            if(player.getBuilderLevel() > maxLevel) player.setBuilderLevel(maxLevel);
        });
    }

    @Override
    public void unload(JavaPlugin plugin) {
    }

    @Override
    public void save() {
    }

    public boolean isBuilderLevelEligible(KubeCityPlayer player) {
        if(requireRoleName == null) return true;
        Member member = bot.getGuild().getMemberById(player.getDiscordId());
        if(member == null) return false;
        if(!player.isLinked()) return false;
        return member.getRoles().stream().map(Role::getName).anyMatch(name -> name.equals(requireRoleName));
    }

    public boolean setLevel(KubeCityPlayer player, int level) {
        if(!isBuilderLevelEligible(player)) return false;
        if(level < minLevel) level = minLevel;
        if(level > maxLevel) level = maxLevel;
        player.setBuilderLevel(level);
        player.setExperiencePoint(0);
        return true;
    }

    public boolean giveExperiencePoint(KubeCityPlayer player, int point) {
        if(!isBuilderLevelEligible(player)) return false;
        player.setExperiencePoint(player.getExperiencePoint() + point);
        maybeLevelUp(player);
        return true;
    }

    public void maybeLevelUp(KubeCityPlayer player) {
        while(player.getExperiencePoint() >= getExperienceToNextLevel(player.getBuilderLevel())) {
            if(player.getBuilderLevel() < maxLevel) {
                levelUp(player);
            } else {
                break;
            }
        }
    }

    public void levelUp(KubeCityPlayer player) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!isBuilderLevelEligible(player)) return;
        if(player.getBuilderLevel() >= maxLevel) return;

        int subtractExp = getExperienceToNextLevel(player.getBuilderLevel());
        player.setBuilderLevel(player.getBuilderLevel() + 1);
        player.setExperiencePoint(player.getExperiencePoint() - subtractExp);

        bot.sendDiscordMessages(broadcastChannels, channel -> {
            String title = plugin.getMessage("builder-level.level-up-broadcast-title");
            String content = String.format(
                    plugin.getMessage("builder-level.level-up-broadcast-content"),
                    player.getNickname(), player.getBuilderLevel()
            );
            EmbedMessage message = new EmbedMessage(channel, title, content);
            message.setNickname(player.getNickname());
            message.setAvatar(IconStorage.getIconFor(UUID.fromString(player.getUuid())));
            return message;
        });

        Player bukkitPlayer = Bukkit.getPlayer(UUID.fromString(player.getUuid()));
        if(bukkitPlayer != null) bukkitPlayer.sendMessage(String.format(
                plugin.getMessage("builder-level.level-up-message"),
                player.getBuilderLevel()
        ));
    }

    public int getExperienceToNextLevel(int level) {
        return curve.get(level - minLevel);
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("builder-level");
    }
}
