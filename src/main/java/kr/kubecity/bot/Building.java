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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class Building implements ConfigurationSerializable {
    public static final String HOLOGRAM_PREFIX = "KubeCity_Building_";

    public static final Map<Integer, Building> BUILDINGS = new HashMap<>();

    private final int wikiPageId;
    private String fullUrl;
    private String name;
    private Location location;
    private String builderUuid;
    private Date completionDate;

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
        TextHologramData hologramData = new TextHologramData(HOLOGRAM_PREFIX + wikiPageId, location);
        hologramData.setText(new ArrayList<>());

        hologramData.addLine(String.format(plugin.getMessage("building-storage.hologram-name", "%1$s"), this.name));

        if(builderUuid != null) {
            UUID builderUuid = UUID.fromString(this.builderUuid);
            KubeCityPlayer kubeCityPlayer = KubeCityPlayer.of(builderUuid).orElse(null);
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(builderUuid);
            String name = (kubeCityPlayer != null) ? kubeCityPlayer.getNickname() : offlinePlayer.getName();
            if(name != null) {
                String format = plugin.getMessage("building-storage.hologram-builder", "Builder: %1$s");
                hologramData.addLine(String.format(format, name));
            }
        }

        plugin.getFeature(BuildingVotes.class).ifPresent(votes -> {
            if(votes.isRequireApproval() && !isApproved()) return;
            int totalVotes = votes.getDatabase().getVotes(this).size();
            hologramData.addLine(String.format(plugin.getMessage("building-storage.hologram-votes-total"), totalVotes));
            Date monthStart = Date.from(YearMonth.now(plugin.getTimezone()).atDay(1).atStartOfDay().atZone(plugin.getTimezone()).toInstant());
            Date now = new Date();
            int votesThisMonth = votes.getDatabase().getVotes(this, monthStart, now).size();
            hologramData.addLine(String.format(plugin.getMessage("building-storage.hologram-votes-month"), votesThisMonth));
        });

        if(this.hologram != null) {
            manager.removeHologram(hologram);
        }
        this.hologram = manager.create(hologramData);
        manager.addHologram(this.hologram);
    }

    public void removeHologram() {
        if(this.hologram != null) {
            KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
            BuildingStorage feature = plugin.getFeature(BuildingStorage.class).orElse(null);
            if(feature == null || !feature.isShowHologram()) return;
            HologramManager manager = feature.getHologramManager();
            manager.removeHologram(this.hologram);
        }
    }

    public String getBuilderName() {
        String builderName = KubeCityBotPlugin.getInstance().getMessage("building-storage.builder-unknown");
        String uuid = getBuilderUuid();
        if(uuid != null) {
            UUID builderUuid = UUID.fromString(uuid);
            KubeCityPlayer builder = KubeCityPlayer.of(builderUuid).orElse(null);
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(builderUuid);
            builderName = (builder != null) ? builder.getNickname() : offlinePlayer.getName();
        }
        return builderName;
    }

    public boolean isApproved() {
        return BuildingApproval.BUILDING_APPROVALS.containsKey(wikiPageId);
    }

    public int getWikiPageId() {
        return wikiPageId;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
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

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public static Building deserialize(Map<String, Object> map) {
        Building result = Building.of((int) map.get("wiki-page-id"));
        Object fullUrl =  map.get("full-url");
        if(fullUrl instanceof String) {
            result.fullUrl = (String) fullUrl;
        }
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
        Object completionDate = map.get("completion-date");
        if(completionDate instanceof Date) {
            result.completionDate = (Date) completionDate;
        }
        return result;
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("wiki-page-id", wikiPageId);
        result.put("full-url", fullUrl);
        result.put("name", name);
        result.put("location", location);
        result.put("builder-uuid", builderUuid);
        result.put("completion-date", completionDate);
        return result;
    }
}
