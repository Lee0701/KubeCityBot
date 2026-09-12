package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.Util;
import kr.kubecity.bot.features.BuilderLevel;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelCommandHandler implements TabExecutor {
    private final List<String> completes = new ArrayList<>(List.of("status"));
    private final List<String> adminCompletes = new ArrayList<>(List.of("status", "giveexp", "setlevel"));

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if(args.length < 1) {
            usage(sender, label);
            return true;
        }
        var subLabel = label + ' ' + args[0];
        var subArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "status" -> statusCommand(sender, subLabel, subArgs);
            case "giveexp" -> giveExpCommand(sender, subLabel, subArgs);
            case "setlevel" -> setLevelCommand(sender, subLabel, subArgs);
            default -> usage(sender, label);
        }
        return true;
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/" + label + " [status]");
    }

    private void statusCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        KubeCityPlayer player;
        if(args.length < 1) {
            if(!Util.checkPlayer(sender)) return;
            player = KubeCityPlayer.of((Player) sender).orElse(null);

            if(player == null || !player.isLinked()) {
                sender.sendMessage(plugin.getMessage("player-not-linked"));
                return;
            }

        } else {
            OfflinePlayer offlinePlayer = Util.findOfflinePlayer(args[0]);
            player = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
            if(player == null) {
                sender.sendMessage(plugin.getMessage("player-not-found"));
                return;
            }
        }

        var feature = plugin.getFeature(BuilderLevel.class).orElse(null);
        if(feature == null) return;

        String format = plugin.getMessage("builder-level.status");
        sender.sendMessage(String.format(format,
                player.getDisplayName(),
                player.getBuilderLevel(),
                player.getExperiencePoint(),
                feature.getExperienceToNextLevel(player.getBuilderLevel())
        ));
    }

    private void giveExpCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!Util.checkAdmin(sender)) return;

        if(args.length < 2) {
            giveExpUsage(sender, label);
            return;
        }

        OfflinePlayer offlinePlayer = Util.findOfflinePlayer(args[0]);
        if(offlinePlayer != null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }
        KubeCityPlayer player = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
        if(player == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            giveExpUsage(sender, label);
            return;
        }

        BuilderLevel feature = plugin.getFeature(BuilderLevel.class).orElse(null);
        if(feature == null) {
            sender.sendMessage(plugin.getMessage("builder-level.not-enabled"));
            return;
        }

        boolean result = feature.giveExperiencePoint(player, amount);

        if(result) {
            String format = plugin.getMessage("builder-level.experience-point-given");
            sender.sendMessage(String.format(format, player.getNickname(), amount));
        } else {
            String format = plugin.getMessage("builder-level.experience-point-not-given");
            sender.sendMessage(String.format(format, player.getNickname()));
        }
    }

    private void giveExpUsage(CommandSender sender, String label) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/" + label + " [player] [amount]");
    }

    private void setLevelCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!Util.checkAdmin(sender)) return;

        if(args.length < 2) {
            setLevelUsage(sender, label);
            return;
        }

        OfflinePlayer offlinePlayer = Util.findOfflinePlayer(args[0]);
        if(offlinePlayer != null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }
        KubeCityPlayer player = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
        if(player == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            setLevelUsage(sender, label);
            return;
        }

        BuilderLevel feature =  plugin.getFeature(BuilderLevel.class).orElse(null);
        if(feature == null) {
            sender.sendMessage(plugin.getMessage("builder-level.not-enabled"));
            return;
        }

        boolean result = feature.setLevel(player, level);

        if(result) {
            String format = plugin.getMessage("builder-level.level-set");
            sender.sendMessage(String.format(format, player.getNickname(), player.getBuilderLevel()));
        } else {
            String format = plugin.getMessage("builder-level.level-not-set");
            sender.sendMessage(String.format(format, player.getNickname()));
        }
    }

    private void setLevelUsage(CommandSender sender, String label) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/" + label + " [player] [level]");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> copied;
        if(sender.hasPermission("kubecitybot.admin")) {
            copied = new ArrayList<>(adminCompletes);
        } else {
            copied = new ArrayList<>(completes);
        }
        if(args.length == 1) {
            copied.removeIf(it -> !it.startsWith(args[0]));
            return copied;
        } else {
            return Collections.emptyList();
        }
    }
}
