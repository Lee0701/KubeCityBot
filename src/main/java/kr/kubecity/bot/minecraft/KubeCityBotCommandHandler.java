package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityBotPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class KubeCityBotCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(Arrays.asList("reload", "save"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("kubecitybot.admin")) {
            sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                    "missing-permission",
                    "You don't have permission to use this command."
            ));
            return true;
        }
        if(args.length < 1) {
            String commands = completes.stream().collect(Collectors.joining("|"));
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " (" + commands + ")");
            return true;
        }
        if(args[0].equals("reload")) {
            sender.sendMessage(ChatColor.GRAY + "Reloading KubeCityBot...");
            KubeCityBotPlugin.getInstance().saveData();
            KubeCityBotPlugin.getInstance().reload();
            sender.sendMessage(ChatColor.GREEN + "Reload complete!");
            return true;
        } else if(args[0].equals("forcereload")) {
            sender.sendMessage(ChatColor.GRAY + "Reloading KubeCityBot without saving...");
            KubeCityBotPlugin.getInstance().reload();
            sender.sendMessage(ChatColor.GREEN + "Force reload complete!");
            return true;
        } else if(args[0].equals("save")) {
            sender.sendMessage(ChatColor.GRAY + "Saving data...");
            KubeCityBotPlugin.getInstance().saveConfig();
            KubeCityBotPlugin.getInstance().saveData();
            sender.sendMessage(ChatColor.GREEN + "Save complete!");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> copied;
        if(sender.hasPermission("kubecitybot.admin")) {
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
