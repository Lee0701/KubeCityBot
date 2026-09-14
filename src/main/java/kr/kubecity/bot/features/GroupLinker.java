package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GroupLinker implements Feature, ContextCalculator<Player> {

    private String contextKey;

    private final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    private final LuckPerms permsApi = provider.getProvider();

    @Override
    public void load(JavaPlugin plugin) {
        contextKey = getConfigurationSection().getString("context-key");

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
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("group-linker");
    }

    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        var guild = KubeCityBotPlugin.getInstance().getBot().getGuild();
        if(guild == null) return;
        var member = KubeCityPlayer.of(target)
                .map(KubeCityPlayer::getDiscordId)
                .map(guild::getMemberById)
                .orElse(null);
        if(member == null) return;
        member.getRoles().stream()
                .map(Role::getName)
                .forEach(role -> consumer.accept(contextKey, role));
        KubeCityPlayer.of(target).ifPresent(player -> {
            player.setNickname(target.getName());
            player.setDisplayName(target.getDisplayName());
        });
    }

    public void reloadPlayer(Player player) {
        permsApi.getContextManager().signalContextUpdate(player);
    }

    public void reloadMember(Member member) {
        String uuid = KubeCityPlayer.of(member.getId()).getUuid();
        if(uuid == null) return;
        Player player = KubeCityBotPlugin.getInstance().getServer().getPlayer(UUID.fromString(uuid));
        if(player == null) return;
        permsApi.getContextManager().signalContextUpdate(player);
    }
}
