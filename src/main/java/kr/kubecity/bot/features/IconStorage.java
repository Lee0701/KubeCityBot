package kr.kubecity.bot.features;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.PlayerIcon;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IconStorage implements Feature {
    private final Map<UUID, PlayerIcon> minecraftIcons = new HashMap<>();
    private final Map<String, PlayerIcon> discordIcons = new HashMap<>();

    private String url;

    @Override
    public void load(JavaPlugin plugin) {
        url = getConfigurationSection().getString("url");
    }

    @Override
    public void unload(JavaPlugin plugin) {
    }

    @Override
    public void save() {
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("icon-storage");
    }

    public PlayerIcon getIcon(User user) {
        KubeCityPlayer player = KubeCityPlayer.of(user.getId());
        if(player.getUuid() != null) {
            return this.getIconFor(UUID.fromString(player.getUuid()));
        } else {
            return discordIcons.computeIfAbsent(user.getAvatarId(), $ -> {
                String url = user.getAvatarUrl();
                try (InputStream stream = new URL(url).openStream()) {
                    return new PlayerIcon(url, Icon.from(stream));
                } catch (IOException e) {
                    return new PlayerIcon(url, null);
                }
            });
        }
    }

    public PlayerIcon getIcon(UUID player) {
        return minecraftIcons.computeIfAbsent(player, uuid -> {
            String url = String.format(this.url, uuid);
            try (InputStream stream = new URL(url).openStream()) {
                return new PlayerIcon(url, Icon.from(stream));
            } catch (IOException e) {
                return null;
            }
        });
    }

    public static PlayerIcon getIconFor(User user) {
        IconStorage iconStorage = KubeCityBotPlugin.getInstance().getFeature(IconStorage.class).orElse(null);
        if(iconStorage == null) return null;
        return iconStorage.getIcon(user);
    }

    public static PlayerIcon getIconFor(UUID player) {
        IconStorage iconStorage = KubeCityBotPlugin.getInstance().getFeature(IconStorage.class).orElse(null);
        if(iconStorage == null) return null;
        return iconStorage.getIcon(player);
    }

}
