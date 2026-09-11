package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.KubeCityPlayer;
import kr.kubecity.bot.Registration;
import kr.kubecity.bot.Util;
import kr.kubecity.bot.features.GroupLinker;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class DiscordCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(Arrays.asList("register", "unregister", "whois"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            usage(sender, label);
            return true;
        }
        var subLabel = label + ' ' + args[0];
        var subArgs = Arrays.copyOfRange(args, 1, args.length);
        switch(args[0]) {
            case "whois" -> whoisCommand(sender, subLabel, subArgs);
            case "register" -> registerCommand(sender, subLabel, subArgs);
            case "unregister" -> unregisterCommand(sender, subLabel, subArgs);
            default -> usage(sender, label);
        }
        return true;
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/" + label + " [register|unregister|whois]");
    }

    private void whoisCommand(CommandSender sender, String label, String[] args) {
        KubeCityPlayer kubeCityPlayer = null;

        if(args.length >= 1) {
            OfflinePlayer offlinePlayer = Util.findOfflinePlayer(args[0]);
            if(offlinePlayer != null) kubeCityPlayer = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
        } else if(sender instanceof Player) {
            kubeCityPlayer = KubeCityPlayer.of((Player) sender).orElse(null);
        }

        if(kubeCityPlayer != null) {
            String uuid = kubeCityPlayer.getUuid();
            String minecraftName = kubeCityPlayer.getNickname();
            String discordName = KubeCityBotPlugin.getInstance().getBot().getGuild()
                    .getMemberById(kubeCityPlayer.getDiscordId()).getEffectiveName();
            sender.sendMessage(String.format(KubeCityBotPlugin.getInstance().getMessage(
                    "registration.player-info",
                    ChatColor.GREEN + "Player info:\n" + ChatColor.WHITE +
                            " - UUID: %1$s\n" +
                            " - Minecraft Name: %2$s\n" +
                            " - Discord Name: %3$s"
            ), uuid, minecraftName, discordName));
        } else {
            sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                    "player-not-found",
                    ChatColor.YELLOW + "Player not found."));
        }
    }

    private void registerCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage(
                    "not-player",
                    "You must be a player to use this command."
            ));
            return;
        }

        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(kubeCityPlayer == null || !kubeCityPlayer.isLinked()) {
            Registration registration = new Registration(player);
            KubeCityPlayer.REGISTRATIONS.add(registration);

            String registerCommand = "/register " + registration.getKey();
            String url = "http:register/" + registration.getKey();
            TextComponent register = new TextComponent(registerCommand);
            register.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(plugin.getMessage(
                    "registration.alt-command-info", "or click to copy an alternative command"))));
            register.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            register.setColor(ChatColor.AQUA.asBungee());
            String[] msg = String.format(plugin.getMessage(
                    "registration.type-in-discord",
                    "Type \"%1$s\" in Discord chat to complete."), "<COMMAND>").split("<COMMAND>");
            TextComponent root = new TextComponent(
                    new TextComponent(msg[0]), register, new TextComponent(msg.length > 1 ?msg[1] : ""));
            sender.spigot().sendMessage(root);
        } else {
            sender.sendMessage(plugin.getMessage(
                    "registration.already-registered",
                    ChatColor.YELLOW + "You are already registered! use /discord unregister to unregister first."
            ));
        }
    }

    private void unregisterCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(args.length >= 1) {
            if(!sender.hasPermission("kubecitybot.admin")) {
                sender.sendMessage(plugin.getMessage(
                        "missing-permission",
                        "You must be a player to use this command."
                ));
                return;
            }
            OfflinePlayer offlinePlayer = Util.findOfflinePlayer(args[0]);
            if(offlinePlayer == null) {
                sender.sendMessage(String.format(plugin.getMessage(
                        "registration.player-is-not-registered", ChatColor.YELLOW + "Player %1$s is not registered!"
                ), args[0]));
                return;
            }
            KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(offlinePlayer.getUniqueId()).orElse(null);
            if(kubeCityPlayer == null) {
                sender.sendMessage(String.format(plugin.getMessage(
                        "registration.player-is-not-registered", ChatColor.YELLOW + "Player %1$s is not registered!"
                ), offlinePlayer.getName()));
                return;
            }
            KubeCityPlayer.PLAYER_MAP.remove(kubeCityPlayer.getDiscordId());
            sender.sendMessage(String.format(plugin.getMessage(
                    "registration.unregistered-player", ChatColor.GREEN + "Unregistered player %1$s!"
            ), kubeCityPlayer.getNickname()));
            return;
        }

        if(!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage(
                    "not-player",
                    "You must be a player to use this command."
            ));
            return;
        }

        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
        if(kubeCityPlayer != null) {
            KubeCityPlayer.PLAYER_MAP.remove(kubeCityPlayer.getDiscordId());
            sender.sendMessage(plugin.getMessage(
                    "registration.unregister-complete", ChatColor.GREEN + "You are now unregistered."));

            plugin.getFeature(GroupLinker.class).ifPresent(linker -> linker.reloadPlayer(player));

        } else {
            sender.sendMessage(plugin.getMessage(
                    "registration.already-unregistered", ChatColor.YELLOW + "You are already unregistered!"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> copied;
        copied = new ArrayList<>(completes);
        if(args.length == 1) {
            copied.removeIf(it -> !it.startsWith(args[0]));
            return copied;
        } else {
            return Collections.emptyList();
        }
    }
}
