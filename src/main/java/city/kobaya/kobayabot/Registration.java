package city.kobaya.kobayabot;

import org.bukkit.entity.Player;

import java.util.Random;

public class Registration {
    private final String key;
    private final Player player;

    public Registration(Player player) {
        this.key = getRandomString(5);
        this.player = player;
    }

    public String getKey() {
        return key;
    }

    public boolean isCommandMatches(String command) {
        return ("http:register/" + key).equals(command) || ("/register " + key).equals(command);
    }

    public Player getPlayer() {
        return player;
    }

    private static String getRandomString(int length) {
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(26) + 0x41;
            if (random.nextBoolean()) { num += 0x20; }
            result.append((char) num);
        }
        return result.toString();
    }

}
