package city.kube.bot;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IconStorage {
    private static final Map<UUID, PlayerIcon> minecraftIcons = new HashMap<>();
    private static final Map<String, PlayerIcon> discordIcons = new HashMap<>();

    private IconStorage() {
        throw new UnsupportedOperationException("You cannot instantiate IconStorage");
    }

    public static PlayerIcon getIconFor(User user) {
        KubeCityPlayer player = KubeCityPlayer.of(user.getId());
        if(player.getUuid() != null) {
            return getIconFor(UUID.fromString(player.getUuid()));
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

    public static PlayerIcon getIconFor(UUID player) {
        if(!KubeCityBotPlugin.getInstance().isIconStorageEnabled()) return null;
        return minecraftIcons.computeIfAbsent(player, uuid -> {
            String url = "https://crafatar.com/avatars/" + uuid + "?overlay=true";
            try (InputStream stream = new URL(url).openStream()) {
                return new PlayerIcon(url, Icon.from(stream));
            } catch (IOException e) {
                return null;
            }
        });
    }
}
