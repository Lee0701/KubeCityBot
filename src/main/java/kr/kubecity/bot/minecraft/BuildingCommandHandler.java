package kr.kubecity.bot.minecraft;

import kr.kubecity.bot.Building;
import kr.kubecity.bot.KubeCityBotPlugin;
import kr.kubecity.bot.Vote;
import kr.kubecity.bot.VotesDatabase;
import kr.kubecity.bot.features.BuildingVotes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuildingCommandHandler implements TabExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if(args.length < 1) {
            sender.sendMessage("Usage:");
            sender.sendMessage("/" + label + " [register|unregister|whois]");
            return true;
        }
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        if(args[0].equals("vote")) {
            if(!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to use this command.");
                return true;
            }
            var votes = plugin.getFeature(BuildingVotes.class).orElse(null);
            if(votes == null) {
                sender.sendMessage("Building Votes feature ins not enabled in config.");
                return true;
            }
            Player player = (Player) sender;
            if(args.length == 2) {
                int buildingId = -1;
                try {
                    buildingId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {}
                var building = Building.BUILDINGS.get(buildingId);
                if(building == null) {
                    player.sendMessage("No such building with id " + buildingId);
                    return true;
                }
                String uuid = player.getUniqueId().toString();
                if(building.getBuilderUuid().equals(uuid)) {
                    player.sendMessage("You cannot vote to your building.");
                    return true;
                }
                votes.getDatabase().putVote(new Vote(building.getWikiPageId(), uuid, new Date()));
                player.sendMessage("Successfully voted to " + building.getName());
                building.spawnHologram();
                return true;

            } else {
                var buildings = Building.BUILDINGS.values().stream().filter(building -> building.getHologram().isViewer(player));
                var lines = new ArrayList<>(buildings.map(building -> {
                    TextComponent message = new TextComponent(String.format("- %s\n", building.getName()));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/building vote %d", building.getWikiPageId())));
                    return message;
                }).toList());
                if(lines.isEmpty()) {
                    lines.add(new TextComponent(plugin.getMessage("building-votes.building-list-empty", "There are no buildings nearby.") + "\n"));
                }
                lines.addFirst(new TextComponent(plugin.getMessage("building-votes.building-list-header", "Buildings nearby:") + "\n"));
                player.spigot().sendMessage(lines.toArray(new TextComponent[0]));
                return true;

            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        return List.of();
    }
}
