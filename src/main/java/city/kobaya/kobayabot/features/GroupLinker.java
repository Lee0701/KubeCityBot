package city.kobaya.kobayabot.features;

import city.kobaya.kobayabot.KobayaBotPlugin;
import city.kobaya.kobayabot.KobayaPlayer;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupLinker implements Feature {

    private Map<String, String> discordToMinecraft = new HashMap<>();

    private LuckPermsApi permsApi = LuckPerms.getApi();

    @Override
    public void reload(JavaPlugin plugin) {

        discordToMinecraft = getConfigurationSection().getStringList("groups").stream()
                .map(it -> it.split(" "))
                .filter(it -> it.length == 2)
                .collect(Collectors.toMap(it -> it[0], it -> it[1]));

        reloadAll();

    }

    public void reloadPlayer(Player player) {
        KobayaPlayer kobayaPlayer = KobayaPlayer.of(player).orElse(null);
        if(kobayaPlayer != null && kobayaPlayer.getUuid() != null) {
            Member member = KobayaBotPlugin.getInstance().getBot().getGuild().getMemberById(kobayaPlayer.getDiscordId());
            List<String> groups = member.getRoles().stream()
                    .map(Role::getName)
                    .filter(discordToMinecraft::containsKey)
                    .map(discordToMinecraft::get)
                    .collect(Collectors.toList());
            permsApi.getUserManager().loadUser(UUID.fromString(kobayaPlayer.getUuid()))
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

    public void reloadMember(Member member) {
        KobayaPlayer player = KobayaPlayer.of(member.getUser().getId());
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
        List<Member> members = KobayaBotPlugin.getInstance().getBot().getGuild().getMembers();
        members.forEach(this::reloadMember);
    }

    @Override
    public void save() {

    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KobayaBotPlugin.getInstance().getConfig().getConfigurationSection("group-linker");
    }
}
