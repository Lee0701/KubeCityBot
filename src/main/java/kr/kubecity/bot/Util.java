package kr.kubecity.bot;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class Util {
    public static OfflinePlayer findOfflinePlayer(String arg) {
        OfflinePlayer offlinePlayer;
        try {
            UUID uuid = UUID.fromString(arg);
            offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        } catch(IllegalArgumentException ex) {
            offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> Objects.equals(p.getName(), arg))
                    .findAny()
                    .orElse(null);
        }
        return offlinePlayer;
    }

    public static boolean checkAdmin(CommandSender sender) {
        if(!sender.hasPermission("kubecitybot.admin")) {
            sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                    "missing-permission",
                    "You must be a player to use this command."
            ));
            return false;
        }
        return true;
    }

    public static boolean checkPlayer(CommandSender sender) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                    "not-player",
                    "You must be a player to use this command."
            ));
            return false;
        }
        return true;
    }
}
