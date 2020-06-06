package city.kube.bot;

import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IconStorage {
    private static final Map<UUID, Icon> minecraftIcons = new HashMap<>();
    private static final Map<String, Icon> discordIcons = new HashMap<>();

    private IconStorage() {
        throw new UnsupportedOperationException("You cannot instantiate IconStorage");
    }

    public static Icon getIconFor(User user) {
        KubeCityPlayer player = KubeCityPlayer.of(user.getId());
        if(player.getUuid() != null) {
            return getIconFor(UUID.fromString(player.getUuid()));
        } else {
            return discordIcons.computeIfAbsent(user.getAvatarId(), $ -> {
                try (InputStream stream = new URL(user.getAvatarUrl()).openStream()) {
                    return Icon.from(stream);
                } catch (IOException e) {
                    return null;
                }
            });
        }
    }

    public static Icon getIconFor(UUID player) {
        return minecraftIcons.computeIfAbsent(player, uuid -> {
            try (InputStream stream = new URL("https://crafatar.com/avatars/" + uuid + "?overlay=true").openStream()) {
                return Icon.from(stream);
            } catch (IOException e) {
                return null;
            }
        });
    }
}
