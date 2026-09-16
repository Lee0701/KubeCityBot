package kr.kubecity.bot.features;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import kr.kubecity.bot.Building;
import kr.kubecity.bot.KubeCityBotPlugin;
import org.apache.http.client.utils.URIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class BuildingStorage implements Feature {
    private String apiEndpoint;
    private String categoryName;
    private boolean showHologram;

    private File buildingsDateFile;
    private YamlConfiguration buildingsDataConfiguration;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private Date lastUpdate = new Date();
    private BukkitTask updateCacheTask;
    private HologramManager hologramManager;

    @Override
    public void load(JavaPlugin plugin) {
        apiEndpoint = getConfigurationSection().getString("api-endpoint");
        categoryName = getConfigurationSection().getString("category-name");
        showHologram = getConfigurationSection().getBoolean("show-hologram");

        if(showHologram) {
            hologramManager = FancyHologramsPlugin.get().getHologramManager();
        }

        Building.BUILDINGS.clear();
        buildingsDateFile = new File(plugin.getDataFolder(), "buildings.yml");
        buildingsDataConfiguration = YamlConfiguration.loadConfiguration(buildingsDateFile);
        if(buildingsDataConfiguration.isList("buildings")) buildingsDataConfiguration.getList("buildings");

        Object lastUpdateObj =  buildingsDataConfiguration.get("last-update");
        if(lastUpdateObj instanceof Date) {
            lastUpdate = (Date) lastUpdateObj;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if(Building.BUILDINGS.isEmpty()) cacheBuildings();
            Building.BUILDINGS.values().forEach(Building::spawnHologram);
        });

        if(updateCacheTask != null) updateCacheTask.cancel();
        updateCacheTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            updateCache(null);
            lastUpdate = new Date();
        }, 0L, 15 * 20);
    }

    @Override
    public void unload(JavaPlugin plugin) {
        Building.BUILDINGS.values().forEach(Building::removeHologram);
        this.hologramManager = null;

        if(updateCacheTask != null) updateCacheTask.cancel();
        updateCacheTask = null;
    }

    @Override
    public void save() {
        buildingsDataConfiguration.set("buildings", new ArrayList<>(Building.BUILDINGS.values()));
        buildingsDataConfiguration.set("last-update", lastUpdate);
        try {
            buildingsDataConfiguration.save(buildingsDateFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void purgeCache() {
        KubeCityBotPlugin plugin = KubeCityBotPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Building.BUILDINGS.clear();
            cacheBuildings();
            Building.BUILDINGS.values().forEach(Building::spawnHologram);
        });
    }

    public void cacheBuildings() {
        this.cacheBuildings(-1);
    }

    public void cacheBuildings(int pageId) {
        this.cacheBuildings(pageId, 0);
    }

    public void cacheBuildings(int pageId, int offset) {
        try {
            StringBuilder conditions = new StringBuilder();
            conditions.append(String.format("[[%s]]", categoryName));
            if(pageId >= 0) conditions.append(String.format("[[Page ID::%d]]", pageId));
            String query = String.format("%s|offset=%d|?Name|?Builder|?CompletionDate|?X|?Y|?Z|?World|?Page ID", conditions, offset);

            URI uri = new URIBuilder(apiEndpoint)
                    .setParameter("action", "ask")
                    .setParameter("query", query)
                    .setParameter("format", "json")
                    .setParameter("origin", "*")
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                throw new IOException("HTTP GET request failed with response code " + response.statusCode());
            }

            JSONObject object = new JSONObject(response.body());

            if(object.getJSONObject("query").get("results") instanceof JSONObject results) {
                for(String key : results.keySet()) {
                    JSONObject result = results.getJSONObject(key);
                    parseBuildingResult(result);
                }
            }

            if(object.has("query-continue-offset")) {
                int continueOffset = object.getInt("query-continue-offset");
                cacheBuildings(pageId, continueOffset);
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCache(String rccontinue) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            URIBuilder uriBuilder = new URIBuilder(apiEndpoint)
                    .setParameter("action", "query")
                    .setParameter("list", "recentchanges")
                    .setParameter("rcend", dateFormat.format(lastUpdate))
                    .setParameter("rcprop", "ids")
                    .setParameter("format", "json")
                    .setParameter("origin", "*");
            if(rccontinue != null) uriBuilder.setParameter("rccontinue", rccontinue);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uriBuilder.build())
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                throw new IOException("HTTP GET request failed with response code " + response.statusCode());
            }

            JSONObject object = new JSONObject(response.body());

            JSONArray recentChanges = object.getJSONObject("query").getJSONArray("recentchanges");
            for(int i = 0; i < recentChanges.length(); i++) {
                JSONObject change =  recentChanges.getJSONObject(i);
                int pageId = change.getInt("pageid");
                cacheBuildings(pageId);
                Optional.ofNullable(Building.BUILDINGS.get(pageId)).ifPresent(Building::spawnHologram);
            }

            if(object.has("continue")) {
                updateCache(object.getJSONObject("continue").getString("rccontinue"));
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseBuildingResult(JSONObject object) {
        try {
            JSONObject printouts = object.getJSONObject("printouts");
            Object name = getProperty(printouts, "Name");
            Object builderUuid = getProperty(printouts, "Builder");
            Object completionDate = getProperty(printouts, "CompletionDate");
            Object x = getProperty(printouts, "X");
            Object y = getProperty(printouts, "Y");
            Object z = getProperty(printouts, "Z");
            Object worldName = getProperty(printouts, "World");
            int pageId = printouts.getJSONArray("Page ID").getInt(0);

            Building building = Building.of(pageId);
            if(x instanceof Integer && y instanceof Integer && z instanceof Integer && worldName instanceof String) {
                World world = Bukkit.getWorld((String) worldName);
                building.setLocation(new Location(world, (int) x, (int) y, (int) z));
            }
            if(name instanceof String) {
                building.setName((String) name);
            }
            if(builderUuid instanceof String) {
                building.setBuilderUuid((String) builderUuid);
            }
            if(completionDate instanceof JSONObject obj) {
                try {
                    int date = Integer.parseInt(obj.getString("timestamp"));
                    building.setCompletionDate(new Date(date * 1000L));
                } catch (NumberFormatException _) {}
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Object getProperty(JSONObject object, String key) throws JSONException {
        JSONArray arr = object.getJSONArray(key);
        if(arr.isEmpty()) return null;
        return arr.get(0);
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("building-storage");
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public boolean isShowHologram() {
        return showHologram;
    }
}
