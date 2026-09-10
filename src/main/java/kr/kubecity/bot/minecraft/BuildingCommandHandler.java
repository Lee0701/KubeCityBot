package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.Building;
import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.Vote;
import kr.kubecity.bot.features.BuildingVotes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.http.client.utils.URIBuilder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BuildingCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(List.of("vote"));
    private final List<String> adminCompletes = new ArrayList<>(List.of("vote"));

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if(args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [vote|register]");
            return true;
        }
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        if(args[0].equals("vote")) {
            if(!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage(
                        "missing-permission",
                        "You must be a player to use this command."
                ));
                return true;
            }
            var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
            if(votes == null) {
                sender.sendMessage(plugin.getMessage(
                        "building-votes.not-enabled",
                        "Building votes feature ins not enabled in config."
                ));
                return true;
            }

            if(!player.hasPermission("kubecitybot.building.vote")) {
                sender.sendMessage(plugin.getMessage(
                        "missing-permission",
                        "You don't have permission to use this command."
                ));
            }

            if(args.length == 2) {
                int buildingId = -1;
                try {
                    buildingId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {}
                var building = Building.BUILDINGS.get(buildingId);
                if(building == null) {
                    player.sendMessage(String.format(plugin.getMessage(
                            "building-votes.no-such-building",
                            "No such building with id %1$d"
                    ), buildingId));
                    return true;
                }
                String uuid = player.getUniqueId().toString();
                if(building.getBuilderUuid().equals(uuid)) {
                    player.sendMessage(plugin.getMessage(
                            "building-votes.no-self-voting",
                            "You cannot vote to your building."
                    ));
                    return true;
                }
                votes.getDatabase().putVote(new Vote(building.getWikiPageId(), uuid, new Date()));
                player.sendMessage("Successfully voted to " + building.getName());
                building.spawnHologram();

            } else {
                var buildings = Building.BUILDINGS.values().stream().filter(building -> building.getHologram().isViewer(player));
                var lines = new ArrayList<>(buildings.map(building -> {
                    TextComponent message = new TextComponent(
                            String.format(plugin.getMessage("building-votes.building-list-item", "- %1$s\n"), building.getName()));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/building vote %d", building.getWikiPageId())));
                    return message;
                }).toList());
                if(lines.isEmpty()) {
                    lines.add(new TextComponent(
                            plugin.getMessage("building-votes.building-list-empty", "There are no buildings nearby.") + "\n"));
                }
                lines.addFirst(new TextComponent(
                        plugin.getMessage("building-votes.building-list-header", "Buildings nearby:") + "\n"));
                player.spigot().sendMessage(lines.toArray(new TextComponent[0]));

            }
            return true;
        }
        if(args[0].equals("register")) {
            if(!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessage(
                        "missing-permission",
                        "You must be a player to use this command."
                ));
                return true;
            }

            if(!player.hasPermission("kubecitybot.building.register")) {
                sender.sendMessage(plugin.getMessage(
                        "missing-permission",
                        "You don't have permission to use this command."
                ));
            }

            if(args.length < 2) {
                sender.sendMessage(plugin.getMessage(
                        "missing-building-name",
                        "Usage: /building register <name>"
                ));
                return true;
            }

            TextComponent message = new TextComponent(
                    plugin.getMessage("building-votes.click-to-register", "Click on this text to register a building on this location."));
            String url = plugin.getMessage("building-votes.wiki-url", "https://example.com/");
            String preload = plugin.getMessage("building-votes.register-preload", "");
            String name = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
            Location location = player.getLocation();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                URI uri = new URIBuilder(url)
                        .setPath("/wiki/" + name)
                        .setParameter("action", "edit")
                        .setParameter("preload", preload)
                        .addParameter("preloadparams[]", Integer.toString((int) location.getX()))
                        .addParameter("preloadparams[]", Integer.toString((int) location.getY()))
                        .addParameter("preloadparams[]", Integer.toString((int) location.getZ()))
                        .addParameter("preloadparams[]", location.getWorld().getName())
                        .addParameter("preloadparams[]", player.getUniqueId().toString())
                        .addParameter("preloadparams[]", player.getName())
                        .addParameter("preloadparams[]", dateFormat.format(new Date()))
                        .build();
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, uri.toASCIIString()));
                player.spigot().sendMessage(message);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
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
