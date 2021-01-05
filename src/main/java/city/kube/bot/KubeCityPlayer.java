package city.kube.bot;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

public class KubeCityPlayer implements ConfigurationSerializable {
    public static final Map<String, KubeCityPlayer> PLAYER_MAP = new HashMap<>();
    public static final Set<Registration> REGISTRATIONS = new HashSet<>();

    private String nickname;
    private String discordId;
    private String uuid;
    private String chatFormat;
    private List<String> listeningChannels;
    private List<String> speakingChannels;

    public KubeCityPlayer(String discordId) {
        this.discordId = discordId;
    }

    public static Optional<KubeCityPlayer> of(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return PLAYER_MAP.values().stream().filter(e -> uuid.toString().equals(e.uuid)).findFirst();
    }

    public static Optional<KubeCityPlayer> of(Player player) {
        Objects.requireNonNull(player, "player");
        return PLAYER_MAP.values().stream().filter(e -> player.getUniqueId().toString().equals(e.uuid)).findFirst();
    }

    public static KubeCityPlayer of(String discordId) {
        Objects.requireNonNull(discordId, "discordId");
        return PLAYER_MAP.computeIfAbsent(discordId, KubeCityPlayer::new);
    }

    public static boolean checkLinked(Player player) {
        return of(player).map(KubeCityPlayer::isLinked).orElse(false);
    }

    public static boolean checkLinked(String discordId) {
        return of(discordId).isLinked();
    }

    public static KubeCityPlayer deserialize(Map<String, Object> args) {
        KubeCityPlayer result = KubeCityPlayer.of((String) args.get("discordId"));
        Object chatFormat = args.get("chat-format");
        if (chatFormat instanceof String) {
            result.chatFormat = (String) chatFormat;
        }
        Object uuid = args.get("uuid");
        if (uuid instanceof String) {
            result.uuid = (String) uuid;
        }
        Object nickname = args.get("nickname");
        if (nickname instanceof String) {
            result.nickname = (String) nickname;
        }
        Object listeningChannels = args.get("listening-channels");
        if(listeningChannels instanceof List) {
            result.listeningChannels = (List<String>) listeningChannels;
        }
        Object speakingChannels = args.get("speaking-channels");
        if(speakingChannels instanceof List) {
            result.speakingChannels = (List<String>) speakingChannels;
        }
        return result;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("discordId", discordId);
        if (uuid != null) {
            result.put("uuid", uuid);
        }
        if (nickname != null) {
            result.put("nickname", nickname);
        }
        if (chatFormat != null) {
            result.put("chat-format", chatFormat);
        }
        if(listeningChannels != null) {
            result.put("listening-channels", listeningChannels);
        }
        if(speakingChannels != null) {
            result.put("speaking-channels", speakingChannels);
        }
        return result;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLinked() {
        return discordId != null && uuid != null;
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public void setChatFormat(String chatFormat) {
        this.chatFormat = chatFormat;
    }

    public List<String> getListeningChannels() {
        return listeningChannels;
    }

    public void setListeningChannels(List<String> listeningChannels) {
        this.listeningChannels = listeningChannels;
    }

    public List<String> getSpeakingChannels() {
        return speakingChannels;
    }

    public void setSpeakingChannels(List<String> speakingChannels) {
        this.speakingChannels = speakingChannels;
    }
}
