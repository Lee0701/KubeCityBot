package city.kube.bot.discord;

import city.kube.bot.discord.message.DiscordMessage;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DiscordMessageSender extends BukkitRunnable {
    private final Queue<DiscordMessage> messageQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while(!messageQueue.isEmpty()) {
            DiscordMessage message = messageQueue.poll();
            message.send();
        }
    }

    public void offerMessage(DiscordMessage message) {
        messageQueue.offer(message);
    }

}
