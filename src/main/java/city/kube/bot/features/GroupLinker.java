package city.kube.bot.features;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupLinker implements Feature, Listener {

    private Map<String, String> discordToMinecraft = new HashMap<>();

    private final LuckPermsApi permsApi = LuckPerms.getApi();

    @Override
    public void reload(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        discordToMinecraft = getConfigurationSection().getStringList("groups").stream()
                .map(it -> it.split(" "))
                .filter(it -> it.length == 2)
                .collect(Collectors.toMap(it -> it[0], it -> it[1]));

        reloadAll();

    }

    public void clearPlayer(Player player) {
        permsApi.getUserManager().loadUser(player.getUniqueId())
                .thenAcceptAsync(user -> {
                    user.getAllNodes().stream()
                            .filter(Node::isGroupNode)
                            .filter(node -> discordToMinecraft.containsValue(node.getGroupName()))
                            .forEach(user::unsetPermission);
                    permsApi.getUserManager().saveUser(user);
                });
    }

    public void reloadPlayer(Player player) {
        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(kubeCityPlayer != null && kubeCityPlayer.getUuid() != null) {
            Member member = KubeCityBotPlugin.getInstance().getBot().getGuild().getMemberById(kubeCityPlayer.getDiscordId());
            if(member == null) {
                clearPlayer(player);
            } else {
                List<String> groups = member.getRoles().stream()
                        .map(Role::getName)
                        .filter(discordToMinecraft::containsKey)
                        .map(discordToMinecraft::get)
                        .collect(Collectors.toList());
                permsApi.getUserManager().loadUser(UUID.fromString(kubeCityPlayer.getUuid()))
                        .thenAcceptAsync(user -> {
                            user.getAllNodes().stream()
                                    .filter(Node::isGroupNode)
                                    .filter(node -> discordToMinecraft.containsValue(node.getGroupName()))
                                    .forEach(user::unsetPermission);
                            groups.stream()
                                    .map(permsApi.getNodeFactory()::makeGroupNode)
                                    .map(Node.Builder::build)
                                    .forEach(user::setPermission);
                            permsApi.getUserManager().saveUser(user);
                        });
            }
        } else {
            clearPlayer(player);
        }
    }

    public void reloadMember(Member member) {
        KubeCityPlayer player = KubeCityPlayer.of(member.getUser().getId());
        if(player.getUuid() != null) {
            List<String> groups = member.getRoles().stream()
                    .map(Role::getName)
                    .filter(discordToMinecraft::containsKey)
                    .map(discordToMinecraft::get)
                    .collect(Collectors.toList());
            permsApi.getUserManager().loadUser(UUID.fromString(player.getUuid()))
                    .thenAcceptAsync(user -> {
                        user.getAllNodes().stream()
                                .filter(Node::isGroupNode)
                                .filter(node -> discordToMinecraft.containsValue(node.getGroupName()))
                                .forEach(user::unsetPermission);
                        groups.stream()
                                .map(permsApi.getNodeFactory()::makeGroupNode)
                                .map(Node.Builder::build)
                                .forEach(user::setPermission);
                        permsApi.getUserManager().saveUser(user);
                    });
        }
    }

    public void reloadAll() {
        List<Member> members = KubeCityBotPlugin.getInstance().getBot().getGuild().getMembers();
        members.forEach(this::reloadMember);
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("group-linker");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        reloadPlayer(event.getPlayer());
    }

}
