package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.*;
import kr.kubecity.bot.features.BuilderLevel;
import kr.kubecity.bot.features.BuilderLevelRewards;
import kr.kubecity.bot.features.BuildingStorage;
import kr.kubecity.bot.features.BuildingVotes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.http.client.utils.URIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
import java.util.function.Predicate;

public class BuildingCommandHandler implements TabExecutor {

    private final List<String> completes = new ArrayList<>(List.of("vote", "register"));
    private final List<String> adminCompletes = new ArrayList<>(List.of("vote", "register", "votes", "purge"));

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
            case "approval" -> approvalCommand(sender, subLabel, subArgs);
            case "purge" -> purgeCommand(sender, subLabel, subArgs);
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

        if(!Util.checkPlayer(sender)) return;
        Player player = (Player) sender;

        var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
        if(votes == null) {
            sender.sendMessage(plugin.getMessage("building-votes.not-enabled"));
            return;
        }

        if(!player.hasPermission("kubecitybot.building.vote")) {
            sender.sendMessage(plugin.getMessage("missing-permission"));
            return;
        }

        if(args.length == 1) {
            int buildingId = -1;
            try {
                buildingId = Integer.parseInt(args[0]);
            } catch (NumberFormatException _) {}
            var building = Building.BUILDINGS.get(buildingId);
            if(building == null) {
                player.sendMessage(String.format(plugin.getMessage("building-votes.no-such-building"), buildingId));
                return;
            }

            if(votes.isRequireApproval() && !building.isApproved()) {
                player.sendMessage(plugin.getMessage("building-votes.building-not-approved"));
                return;
            }

            String uuid = player.getUniqueId().toString();
            if(building.getBuilderUuid().equals(uuid)) {
                player.sendMessage(plugin.getMessage("building-votes.no-self-voting"));
                return;
            }

            if(votes.isUseTickets()) {
                KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
                if(kubeCityPlayer == null || kubeCityPlayer.getVoteTickets() < 1) {
                    player.sendMessage(plugin.getMessage("building-votes.not-enough-tickets"));
                    return;
                }
                kubeCityPlayer.setVoteTickets(kubeCityPlayer.getVoteTickets() - 1);
            }

            votes.getDatabase().putVote(new Vote(-1, building.getWikiPageId(), uuid, new Date()));
            player.sendMessage(String.format(plugin.getMessage("building-votes.vote-successful"), building.getName()));
            building.spawnHologram();

            // Give vote reward if enabled
            plugin.getFeature(BuilderLevelRewards.class).ifPresent(builderLevelRewards -> {
                if(!builderLevelRewards.isUseVote()) return;
                BuilderLevel builderLevel = plugin.getFeature(BuilderLevel.class).orElse(null);
                if(builderLevel == null) return;
                KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
                if(kubeCityPlayer == null) return;
                if(!builderLevel.isBuilderLevelEligible(kubeCityPlayer)) return;

                int rewardExp = builderLevelRewards.getVoteReward();

                String format = plugin.getMessage("builder-level-rewards.vote-reward-received");
                player.sendMessage(String.format(format, rewardExp));

                builderLevel.giveExperiencePoint(kubeCityPlayer, rewardExp);
            });

        } else {
            var buildings = Building.BUILDINGS.values().stream().filter(building -> building.getHologram().isViewer(player));
            var lines = new ArrayList<>(buildings.map(building -> {
                String format = plugin.getMessage("building-votes.building-list-item") + "\n";
                TextComponent message = new TextComponent(String.format(format, building.getName()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %d", label, building.getWikiPageId())));
                return message;
            }).toList());
            if(lines.isEmpty()) {
                lines.add(new TextComponent(plugin.getMessage("building-votes.building-list-empty") + "\n"));
            }
            lines.addFirst(new TextComponent(plugin.getMessage("building-votes.building-list-header") + "\n"));
            if(votes.isUseTickets()) {
                KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(player).orElse(null);
                int voteTickets;
                if(kubeCityPlayer == null) voteTickets = 0;
                else voteTickets = kubeCityPlayer.getVoteTickets();
                String format = plugin.getMessage("building-votes.building-list-footer");
                lines.add(new TextComponent(String.format(format, voteTickets, votes.getMaxTickets())));
            }
            player.spigot().sendMessage(lines.toArray(new TextComponent[0]));

        }
    }

    private void votesCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
        if(votes == null) {
            sender.sendMessage(plugin.getMessage("building-votes.not-enabled"));
            return;
        }

        if(!sender.hasPermission("kubecitybot.admin")) {
            sender.sendMessage(plugin.getMessage("missing-permission"));
            return;
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

        if(!Util.checkPlayer(sender)) return;
        Player player = (Player) sender;

        if(!player.hasPermission("kubecitybot.building.register")) {
            sender.sendMessage(plugin.getMessage("missing-permission"));
            return;
        }

        if(args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [name]");
            return;
        }

        TextComponent message = new TextComponent(
                plugin.getMessage("building-storage.click-to-register"));
        String url = plugin.getConfig().getString("building-storage.wiki-url");
        String preload = plugin.getConfig().getString("building-storage.register-preload");
        String name = String.join(" ", args);
        Location location = player.getLocation();
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

    private void approvalCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!Util.checkPlayer(sender)) return;
        Player player = (Player) sender;

        if(!sender.hasPermission("kubecitybot.building.approve")) {
            sender.sendMessage(plugin.getMessage("missing-permission"));
            return;
        }

        if(args.length == 0) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [list|info|approve|lookup]");
            return;
        }

        switch (args[0]) {
            case "list" -> {
                int count = 0;
                if(args.length < 2) {
                    count = 10;
                } else {
                    try {
                        count = Integer.parseInt(args[1]);
                    } catch (NumberFormatException _) {
                    }
                }
                if(count <= 0) return;

                Location location = player.getLocation();
                var notApproved = Building.BUILDINGS.values().stream()
                        .filter(Predicate.not(Building::isApproved))
                        .sorted((a, b) -> (int) (a.getLocation().distance(location) - b.getLocation().distance(location)))
                        .toList();

                if(notApproved.size() > 10) {
                    notApproved = notApproved.subList(0, 10);
                }

                var components = new ArrayList<>(notApproved.stream().map(building -> {
                    String format = plugin.getMessage("building-votes.building-list-item") + "\n";
                    TextComponent component = new TextComponent(String.format(format, building.getName()));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/building approval info %d", building.getWikiPageId())));
                    return component;
                }).toList());
                player.spigot().sendMessage(components.toArray(new TextComponent[0]));
            }
            case "info" -> {
                if(args.length < 2) {
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/" + label + " info [buildingId]");
                    return;
                }

                int buildingId;
                try {
                    buildingId = Integer.parseInt(args[1]);
                } catch (NumberFormatException _) {
                    buildingId = -1;
                }

                Building building = Building.BUILDINGS.get(buildingId);
                if(building == null) {
                    String format = plugin.getMessage("building-votes.no-such-building");
                    player.sendMessage(String.format(format, buildingId));
                    return;
                }

                BuildingVotes buildingVotes = plugin.getFeature(BuildingVotes.class).orElse(null);
                if(buildingVotes == null) return;

                String builderName = plugin.getMessage("building-votes.builder-unknown");
                String uuid = building.getBuilderUuid();
                if(uuid != null) {
                    UUID builderUuid = UUID.fromString(uuid);
                    KubeCityPlayer builder = KubeCityPlayer.of(builderUuid).orElse(null);
                    OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(builderUuid);
                    builderName = (builder != null) ? builder.getNickname() : offlinePlayer.getName();
                }

                Date date = building.getCompletionDate();
                String completionDate = plugin.getMessage("building-votes.completion-date-unknown");
                if(date != null) completionDate = dateFormat.format(date);

                var components = new ArrayList<TextComponent>();
                String format = plugin.getMessage("building-votes.building-info") + "\n";
                components.add(new TextComponent(String.format(format, building.getName(), builderName, completionDate)));

                for(int rating = buildingVotes.getMinRating(); rating <= buildingVotes.getMaxRating(); rating++) {
                    String stars = ChatColor.YELLOW + " ☆ " + ChatColor.WHITE;
                    TextComponent component = new TextComponent(stars);
                    String command = String.format("/building approval approve %d %d", buildingId, rating);
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, List.of(new Text(ChatColor.YELLOW + "★".repeat(rating)))));
                    components.add(component);
                }

                format = plugin.getMessage("building-votes.teleporting-to-building");
                player.sendMessage(String.format(format, building.getName()));
                player.teleport(building.getLocation());
                sender.spigot().sendMessage(components.toArray(new TextComponent[0]));
            }
            case "approve" -> {
                if(args.length < 3) {
                    sender.sendMessage("Usage:");
                    sender.sendMessage("/" + label + " approve [buildingId] [rating]");
                    return;
                }

                int buildingId;
                try {
                    buildingId = Integer.parseInt(args[1]);
                } catch (NumberFormatException _) {
                    buildingId = -1;
                }

                Building building = Building.BUILDINGS.get(buildingId);
                if(building == null) {
                    String format = plugin.getMessage("building-votes.no-such-building");
                    player.sendMessage(String.format(format, buildingId));
                    return;
                }

                BuildingVotes buildingVotes = plugin.getFeature(BuildingVotes.class).orElse(null);
                if(buildingVotes == null) return;

                if(building.isApproved()) {
                    player.sendMessage(plugin.getMessage("building-votes.already-approved"));
                    return;
                }

                int rating;
                try {
                    rating = Integer.parseInt(args[2]);
                } catch (NumberFormatException _) {
                    rating = -1;
                }
                if(!buildingVotes.isValidRating(rating)) return;

                int rewardExp = buildingVotes.getApprovalReward(rating);

                BuildingApproval approval = BuildingApproval.of(buildingId);
                approval.setBuildingId(buildingId);
                approval.setRating(rating);
                approval.setExperience(rewardExp);
                approval.setApproverUuid(player.getUniqueId().toString());
                approval.setDate(new Date());

                sender.sendMessage(plugin.getMessage("building-votes.building-approved"));

                plugin.getFeature(BuilderLevel.class).ifPresent(builderLevel -> {
                    KubeCityPlayer.of(UUID.fromString(building.getBuilderUuid())).ifPresent(builder -> {
                        builderLevel.giveExperiencePoint(builder, rewardExp);
                    });
                });
                Bukkit.getPluginManager().callEvent(new BuildingApproveEvent(approval));
            }
        }
    }

    private void purgeCommand(CommandSender sender, String label, String[] args) {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();

        if(!Util.checkAdmin(sender)) return;

        plugin.getFeature(BuildingStorage.class).ifPresent(BuildingStorage::purgeCache);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        List<String> copied;
        if(sender.hasPermission("kubecitybot.admin")) {
            copied = new ArrayList<>(adminCompletes);
        } else {
            copied = new ArrayList<>(completes);
        }
        if(sender.hasPermission("kubecitybot.building.approve")) {
            copied.add("approval");
        }
        if(args.length == 1) {
            copied.removeIf(it -> !it.startsWith(args[0]));
            return copied;
        } else {
            return Collections.emptyList();
        }
    }
}
