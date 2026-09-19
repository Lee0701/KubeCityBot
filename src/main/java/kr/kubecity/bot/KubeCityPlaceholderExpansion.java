package kr.kubecity.bot;

import kr.kubecity.bot.features.BuilderLevel;
import kr.kubecity.bot.features.BuilderLevelRewards;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KubeCityPlaceholderExpansion extends PlaceholderExpansion {
    @NotNull
    @Override
    public String getIdentifier() {
        return "kubecity";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Lee0701";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Nullable
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(kubeCityPlayer == null) return null;
        switch(params) {
            case "linked" -> {
                return String.valueOf(kubeCityPlayer.isLinked());
            }
            case "discord_id" -> {
                return kubeCityPlayer.getDiscordId();
            }
            case "discord_name" -> {
                String discordId = kubeCityPlayer.getDiscordId();
                if(discordId == null) return null;
                var member = plugin.getBot().getGuild().getMemberById(discordId);
                if(member == null) return null;
                return member.getEffectiveName();
            }
        }

        BuilderLevel builderLevel = plugin.getFeature(BuilderLevel.class).orElse(null);
        if(builderLevel == null || !builderLevel.isBuilderLevelEligible(kubeCityPlayer)) return null;
        switch(params) {
            case "experience_point" -> {
                return String.valueOf(kubeCityPlayer.getExperiencePoint());
            }
            case "experience_point_next" -> {
                return String.valueOf(builderLevel.getExperienceToNextLevel(kubeCityPlayer.getBuilderLevel()));
            }
            case "builder_level" -> {
                return String.valueOf(kubeCityPlayer.getBuilderLevel());
            }
            case "attendance_days" -> {
                return String.valueOf(kubeCityPlayer.getAttendanceDays() + 1);
            }
            case "attendance_days_max" -> {
                BuilderLevelRewards rewards = plugin.getFeature(BuilderLevelRewards.class).orElse(null);
                if(rewards == null) return null;
                return String.valueOf(rewards.getAttendanceRewards().size());
            }
            case "vote_tickets" -> {
                return String.valueOf(kubeCityPlayer.getVoteTickets());
            }
        }
        if(params.startsWith("attendance_days_")) {
            try {
                int i = Integer.parseInt(params.replace("attendance_days_", ""));
                return String.valueOf((kubeCityPlayer.getAttendanceDays() + 1) >= i);
            } catch(NullPointerException _) {
                return null;
            }
        }
        if(params.startsWith("attendance_day_")) {
            try {
                int i = Integer.parseInt(params.replace("attendance_day_", ""));
                return String.valueOf((kubeCityPlayer.getAttendanceDays() + 1) == i);
            } catch(NullPointerException _) {
                return null;
            }
        }
        return null;
    }
}
