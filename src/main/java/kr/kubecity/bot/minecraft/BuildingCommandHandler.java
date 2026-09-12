package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.Building;
import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.Util;
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

public class BuildingCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(List.of("vote", "register"));
    private final List<String> adminCompletes = new ArrayList<>(List.of("vote", "register", "votes"));

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if(args.length < 1) {
            usage(sender, label);
            return true;
        }
        var subLabel = label + ' ' + args[0];
        var subArgs = Arrays.copyOfRange(args, 1, args.length);
        switch(args[0]) {
            case "vote" -> voteCommand(sender, subLabel, subArgs);
            case "votes" -> votesCommand(sender, subLabel, subArgs);
            case "register" -> registerCommand(sender, subLabel, subArgs);
            default -> usage(sender, label);
        }
        return true;
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage("Usage:");
        sender.sendMessage("/" + label + " [vote|register]");
    }

    private void voteCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage(
                    "not-player",
                    "You must be a player to use this command."
            ));
            return;
        }
        var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
        if(votes == null) {
            sender.sendMessage(plugin.getMessage(
                    "building-votes.not-enabled",
                    "Building votes feature ins not enabled in config."
            ));
            return;
        }

        if(!player.hasPermission("kubecitybot.building.vote")) {
            sender.sendMessage(plugin.getMessage(
                    "missing-permission",
                    "You don't have permission to use this command."
            ));
        }

        if(args.length == 1) {
            int buildingId = -1;
            try {
                buildingId = Integer.parseInt(args[0]);
            } catch (NumberFormatException _) {}
            var building = Building.BUILDINGS.get(buildingId);
            if(building == null) {
                player.sendMessage(String.format(plugin.getMessage(
                        "building-votes.no-such-building",
                        "No such building with id %1$d"
                ), buildingId));
                return;
            }
            String uuid = player.getUniqueId().toString();
            if(building.getBuilderUuid().equals(uuid)) {
                player.sendMessage(plugin.getMessage(
                        "building-votes.no-self-voting",
                        "You cannot vote to your building."
                ));
                return;
            }
            votes.getDatabase().putVote(new Vote(-1, building.getWikiPageId(), uuid, new Date()));
            player.sendMessage("Successfully voted to " + building.getName());
            building.spawnHologram();

        } else {
            var buildings = Building.BUILDINGS.values().stream().filter(building -> building.getHologram().isViewer(player));
            var lines = new ArrayList<>(buildings.map(building -> {
                TextComponent message = new TextComponent(
                        String.format(plugin.getMessage("building-votes.building-list-item", "- %1$s\n"), building.getName()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %d", label, building.getWikiPageId())));
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
    }

    private void votesCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
        if(votes == null) {
            sender.sendMessage(plugin.getMessage(
                    "building-votes.not-enabled",
                    "Building votes feature ins not enabled in config."
            ));
            return;
        }

        if(!sender.hasPermission("kubecitybot.admin")) {
            sender.sendMessage(plugin.getMessage(
                    "missing-permission",
                    "You don't have permission to use this command."
            ));
        }

        if(args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [lookup|delete|clear]");
            return;
        }

        switch(args[0]) {
            case "lookup" -> {
                if(args.length < 3) {
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/" + label + " lookup [player] [count]");
                    return;
                }

                var player = Util.findOfflinePlayer(args[1]);
                var uuid = (player == null) ? null : player.getUniqueId().toString();

                int count = 0;
                try {
                    count = Integer.parseInt(args[2]);
                }  catch (NumberFormatException _) {}
                if(count <= 0) return;

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                var list = votes.getDatabase().getVotes(uuid);
                list.subList(Math.max(list.size() - count, 0), list.size()).forEach(vote -> {
                    int id = vote.id();
                    var building = Building.BUILDINGS.get(vote.buildingId());
                    var buildingName = (building == null) ? null : building.getName();
                    var date = format.format(vote.date());
                    sender.sendMessage(String.format("- %06d: %s <%s>", id, date, buildingName));
                });
            }
            case "clear" -> {
                if(args.length < 2) {
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/" + label + " clear [player]");
                    return;
                }

                var player = Util.findOfflinePlayer(args[1]);
                var uuid = (player == null) ? null : player.getUniqueId().toString();

                int deleted = votes.getDatabase().clearVotes(uuid);
                sender.sendMessage(String.format("Deleted %d votes", deleted));

                Building.BUILDINGS.values().forEach(Building::spawnHologram);
            }
            case "delete" -> {
                if(args.length < 2) {
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/" + label + " delete [id]");
                    return;
                }

                int id = -1;
                try {
                    id = Integer.parseInt(args[1]);
                }  catch (NumberFormatException _) {}
                if(id == -1) return;

                int deleted = votes.getDatabase().deleteVote(id);
                sender.sendMessage(String.format("Deleted %d votes", deleted));

                Building.BUILDINGS.values().forEach(Building::spawnHologram);
            }
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

        if(!player.hasPermission("kubecitybot.building.register")) {
            sender.sendMessage(plugin.getMessage(
                    "missing-permission",
                    "You don't have permission to use this command."
            ));
        }

        if(args.length < 1) {
            sender.sendMessage(plugin.getMessage(
                    "building-storage.missing-building-name",
                    "Usage: /" + label + " <name>"
            ));
            return;
        }

        TextComponent message = new TextComponent(
                plugin.getMessage("building-storage.click-to-register", "Click on this text to register a building on this location."));
        String url = plugin.getConfig().getString("building-storage.wiki-url");
        String preload = plugin.getConfig().getString("building-storage.register-preload");
        String name = String.join(" ", args);
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
