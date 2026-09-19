package kr.kubecity.bot;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

public class KubeCityPlayer implements ConfigurationSerializable {
    public static final Map<String, KubeCityPlayer> PLAYER_MAP = new HashMap<>();
    public static final Set<Registration> REGISTRATIONS = new HashSet<>();

    private String nickname;
    private String displayName;
    private String discordId;
    private String uuid;
    private String chatFormat;
    private List<String> listeningChannels;
    private List<String> speakingChannels;

    private int experiencePoint;
    private int builderLevel;

    private Date lastAttendance;
    private int attendanceDays;

    private int chatMessagesToday;
    private int chatExperienceToday;

    private int voteTickets;

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

    public static Optional<KubeCityPlayer> of(OfflinePlayer offlinePlayer) {
        Objects.requireNonNull(offlinePlayer, "offlinePlayer");
        return PLAYER_MAP.values().stream().filter(e -> offlinePlayer.getUniqueId().toString().equals(e.uuid)).findFirst();
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
        Object displayName = args.get("display-name");
        if (displayName instanceof String) {
            result.displayName = (String) displayName;
        }
        Object listeningChannels = args.get("listening-channels");
        if(listeningChannels instanceof List) {
            result.listeningChannels = (List<String>) listeningChannels;
        }
        Object speakingChannels = args.get("speaking-channels");
        if(speakingChannels instanceof List) {
            result.speakingChannels = (List<String>) speakingChannels;
        }
        Object experiencePoint = args.get("experience-point");
        if (experiencePoint instanceof Integer) {
            result.experiencePoint = (Integer) experiencePoint;
        }
        Object builderLevel = args.get("builder-level");
        if (builderLevel instanceof Integer) {
            result.builderLevel = (Integer) builderLevel;
        }
        Object lastAttendance = args.get("last-attendance");
        if (lastAttendance instanceof Date) {
            result.lastAttendance = (Date) lastAttendance;
        }
        Object attendanceDays = args.get("attendance-days");
        if (attendanceDays instanceof Integer) {
            result.attendanceDays = (Integer) attendanceDays;
        }
        Object chatMessagesToday = args.get("chat-messages-today");
        if (chatMessagesToday instanceof Integer) {
            result.chatMessagesToday = (Integer) chatMessagesToday;
        }
        Object chatExperienceToday = args.get("chat-experience-today");
        if (chatExperienceToday instanceof Integer) {
            result.chatExperienceToday = (Integer) chatExperienceToday;
        }
        Object voteTickets = args.get("vote-tickets");
        if (voteTickets instanceof Integer) {
            result.voteTickets = (Integer) voteTickets;
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
        if (displayName != null) {
            result.put("display-name", displayName);
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
        result.put("experience-point", experiencePoint);
        result.put("builder-level", builderLevel);
        result.put("last-attendance", lastAttendance);
        result.put("attendance-days", attendanceDays);
        result.put("chat-messages-today", chatMessagesToday);
        result.put("chat-experience-today", chatExperienceToday);
        result.put("vote-tickets", voteTickets);
        return result;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public int getExperiencePoint() {
        return experiencePoint;
    }

    public void setExperiencePoint(int experiencePoint) {
        this.experiencePoint = experiencePoint;
    }

    public int getBuilderLevel() {
        return builderLevel;
    }

    public void setBuilderLevel(int builderLevel) {
        this.builderLevel = builderLevel;
    }

    public Date getLastAttendance() {
        return lastAttendance;
    }

    public void setLastAttendance(Date lastAttendance) {
        this.lastAttendance = lastAttendance;
    }

    public int getAttendanceDays() {
        return attendanceDays;
    }

    public void setAttendanceDays(int attendanceDays) {
        this.attendanceDays = attendanceDays;
    }

    public int getChatMessagesToday() {
        return chatMessagesToday;
    }

    public void setChatMessagesToday(int chatMessagesToday) {
        this.chatMessagesToday = chatMessagesToday;
    }

    public int getChatExperienceToday() {
        return chatExperienceToday;
    }

    public void setChatExperienceToday(int chatExperienceToday) {
        this.chatExperienceToday = chatExperienceToday;
    }

    public int getVoteTickets() {
        return voteTickets;
    }

    public void setVoteTickets(int voteTickets) {
        this.voteTickets = voteTickets;
    }
}
