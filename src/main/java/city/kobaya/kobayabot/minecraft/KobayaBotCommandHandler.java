package city.kobaya.kobayabot.minecraft;

import city.kobaya.kobayabot.KobayaBotPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;

public class KobayaBotCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(Arrays.asList("reload"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            if(sender.isOp()) {
                sender.sendMessage("Usage:");
                sender.sendMessage("/" + label + " reload");
            }
            return true;
        }
        if(args[0].equals("reload")) {
            if(sender.isOp()) {
                sender.sendMessage(ChatColor.GRAY + "Reloading config...");
                KobayaBotPlugin.getInstance().reload();
                sender.sendMessage(ChatColor.GREEN + "Reload complete!");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> copied;
        if(sender.isOp()) {
            copied = new ArrayList<>(completes);
        } else {
            copied = new ArrayList<>();
        }
        if(args.length == 1) {
            copied.removeIf(it -> !it.startsWith(args[0]));
            return copied;
        } else {
            return Collections.emptyList();
        }
    }
}
