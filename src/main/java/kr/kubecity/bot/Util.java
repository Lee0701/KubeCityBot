package kr.kubecity.bot;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
}
