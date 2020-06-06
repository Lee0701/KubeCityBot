package city.kube.bot.minecraft;

import city.kube.bot.KubeCityBotPlugin;
import city.kube.bot.KubeCityPlayer;
import city.kube.bot.Registration;
import city.kube.bot.features.GroupLinker;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(Arrays.asList("register", "unregister"));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [register|unregister]");
            return true;
        }
        if(args[0].equals("register")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                Registration registration = new Registration(player);
                KubeCityPlayer.REGISTRATIONS.add(registration);

                String registerCommand = "/register " + registration.getKey();
                String url = "http:register/" + registration.getKey();
                TextComponent register = new TextComponent(registerCommand);
                register.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[] {new TextComponent(KubeCityBotPlugin.getInstance().getMessage(
                                "registration.alt-command-info", "or click to copy an alternative command"))}
                ));
                register.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                register.setColor(ChatColor.AQUA.asBungee());
                String[] msg = String.format(KubeCityBotPlugin.getInstance().getMessage(
                        "registration.type-in-discord",
                        "Type \"%1$s\" in Discord chat to complete."), "<COMMAND>").split("<COMMAND>");
                TextComponent root = new TextComponent(
                        new TextComponent(msg[0]), register, new TextComponent(msg.length > 1 ?msg[1] : ""));
                sender.spigot().sendMessage(root);
            }
            return true;
        }
        if(args[0].equals("unregister")) {
            if(args.length >= 2) {
                if(sender.isOp()) {
                    KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(KubeCityBotPlugin.getInstance().getServer().getPlayer(args[1])).orElse(null);
                    if(kubeCityPlayer != null) {
                        KubeCityPlayer.PLAYER_MAP.remove(kubeCityPlayer.getDiscordId());
                        sender.sendMessage(String.format(KubeCityBotPlugin.getInstance().getMessage(
                                "registration.unregistered-player", ChatColor.GREEN + "Unregistered player %1$s!"
                        ), kubeCityPlayer.getNickname()));
                    } else {
                        sender.sendMessage(String.format(KubeCityBotPlugin.getInstance().getMessage(
                                "registration.player-is-not-registered", ChatColor.YELLOW + "Player %1$s is not registered!"
                        ), args[1]));
                    }
                    return true;
                }
            }
            if(sender instanceof Player) {
                Player player = (Player) sender;
                KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
                if(kubeCityPlayer != null) {
                    KubeCityPlayer.PLAYER_MAP.remove(kubeCityPlayer.getDiscordId());
                    sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                            "registration.unregister-complete", ChatColor.GREEN + "You are now unregistered."));

                    KubeCityBotPlugin.getInstance().getFeature(GroupLinker.class).ifPresent(linker -> linker.clearPlayer(player));

                } else {
                    sender.sendMessage(KubeCityBotPlugin.getInstance().getMessage(
                            "registration.already-unregistered", ChatColor.YELLOW + "You are already unregistered!"));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> copied;
        if(sender.isOp()) {
            copied = new ArrayList<>(completes);
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
