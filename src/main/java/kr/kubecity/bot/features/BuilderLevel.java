package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.discord.BotInstance;
import kr.kubecity.bot.discord.message.EmbedMessage;
import kr.kubecity.bot.minecraft.LevelUpEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class BuilderLevel implements Feature, ContextCalculator<Player> {
    private final BotInstance bot = KubeCityBotPlugin.getInstance().getBot();

    private final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    private final LuckPerms permsApi = provider.getProvider();

    private String requireRoleName;
    private List<String> broadcastChannels;
    private String titleContextKey;

    private int minLevel;
    private int maxLevel;
    private List<Integer> curve;
    private Set<Integer> titles;
    private Map<Integer, Integer> titlesForLevels;

    @Override
    public void load(JavaPlugin plugin) {
        requireRoleName = getConfigurationSection().getString("require-role");
        broadcastChannels = getConfigurationSection().getStringList("broadcast-channels");
        titleContextKey = getConfigurationSection().getString("title-context-key");

        File curveFile = new File(plugin.getDataFolder(), "builder_level_curve.yml");
        if(!curveFile.exists()) plugin.saveResource(curveFile.getName(), false);
        YamlConfiguration curveConfig = YamlConfiguration.loadConfiguration(curveFile);
        minLevel = curveConfig.getInt("min-level");
        maxLevel = curveConfig.getInt("max-level");
        curve = curveConfig.getIntegerList("curve");
        titles = new HashSet<>(curveConfig.getIntegerList("titles"));

        titlesForLevels =  new HashMap<>();
        int title = 0;
        for(int level = minLevel; level <= maxLevel; level++) {
            if(titles.contains(level)) title += 1;
            titlesForLevels.put(level, title);
        }

        KubeCityPlayer.PLAYER_MAP.values().forEach(this::checkLevelRange);

        permsApi.getContextManager().registerCalculator(this);
    }

    @Override
    public void unload(JavaPlugin plugin) {
        permsApi.getContextManager().unregisterCalculator(this);
    }

    @Override
    public void save() {
    }

    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        KubeCityPlayer.of(target).ifPresent(kubeCityPlayer -> {
            int level = kubeCityPlayer.getBuilderLevel();
            int title = titlesForLevels.getOrDefault(level, 0);
            consumer.accept(titleContextKey, String.valueOf(title));
        });
        KubeCityPlayer.of(target).ifPresent(player -> {
            player.setNickname(target.getName());
            player.setDisplayName(target.getDisplayName());
        });
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

        Bukkit.getPluginManager().callEvent(new LevelUpEvent(player, player.getBuilderLevel()));
    }

    public void checkLevelRange(KubeCityPlayer player) {
        if(player.getBuilderLevel() < minLevel) player.setBuilderLevel(minLevel);
        if(player.getBuilderLevel() > maxLevel) player.setBuilderLevel(maxLevel);
    }

    public int getExperienceToNextLevel(int level) {
        return curve.get(level - minLevel);
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("builder-level");
    }
}
