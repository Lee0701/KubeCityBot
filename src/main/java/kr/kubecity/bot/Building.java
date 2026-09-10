package kr.kubecity.bot;

import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import kr.kubecity.bot.features.BuildingStorage;
import kr.kubecity.bot.features.BuildingVotes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class Building implements ConfigurationSerializable {
    public static final Map<Integer, Building> BUILDINGS = new HashMap<>();

    private final int wikiPageId;
    private String name;
    private Location location;
    private String builderUuid;

    private Hologram hologram;

    public Building(int wikiPageId) {
        this.wikiPageId = wikiPageId;
    }

    public static Building of(int wikiPageId) {
        return BUILDINGS.computeIfAbsent(wikiPageId, Building::new);
    }

    public void spawnHologram() {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        BuildingStorage feature = plugin.getFeature(BuildingStorage.class).orElse(null);
        if(feature == null || !feature.isShowHologram()) return;
        HologramManager manager = feature.getHologramManager();

        Location location = this.location.clone();
        location.add(0.5, 1, 0.5);
        TextHologramData hologramData = new TextHologramData("KubeCity_Building_" + wikiPageId, location);
        hologramData.setText(new ArrayList<>());

        hologramData.addLine(String.format(plugin.getMessage("building-storage.hologram-name", "%1$s"), this.name));

        KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(UUID.fromString(builderUuid)).orElse(null);
        OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(builderUuid));
        String name = null;
        if(kubeCityPlayer != null) name = kubeCityPlayer.getNickname();
        else name = offlinePlayer.getName();
        if(name != null) {
            String format = plugin.getMessage("building-storage.hologram-builder", "Builder: %1$s");
            hologramData.addLine(String.format(format, name));
        }

        plugin.getFeature(BuildingVotes.class).ifPresent(votes -> {
            String format = plugin.getMessage("building-storage.hologram-votes-total", "Total votes: %1$d");
            int totalVotes = votes.getDatabase().getVotes(this).size();
            hologramData.addLine(String.format(format, totalVotes));
        });

        if(this.hologram != null) {
            manager.removeHologram(hologram);
        }
        this.hologram = manager.create(hologramData);
        manager.addHologram(this.hologram);
    }

    public int getWikiPageId() {
        return wikiPageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getBuilderUuid() {
        return builderUuid;
    }

    public void setBuilderUuid(String builderUuid) {
        this.builderUuid = builderUuid;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public static void spawnHolograms() {
        BuildingStorage feature = KubeCityBotPlugin.getInstance().getFeature(BuildingStorage.class).orElse(null);
        if(feature == null || !feature.isShowHologram()) return;
        BUILDINGS.values().forEach(Building::spawnHologram);
    }

    public static Building deserialize(Map<String, Object> map) {
        Building result = Building.of((int) map.get("wiki-page-id"));
        Object name =  map.get("name");
        if(name instanceof String) {
            result.name = (String) name;
        }
        Object location = map.get("location");
        if(location instanceof Location) {
            result.location = (Location) location;
        }
        Object builderUuid = map.get("builder-uuid");
        if(builderUuid instanceof String) {
            result.builderUuid = (String) builderUuid;
        }
        return result;
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("wiki-page-id", wikiPageId);
        result.put("name", name);
        result.put("location", location);
        result.put("builder-uuid", builderUuid);
        return result;
    }
}
