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
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BuildingStorage implements Feature {
    private String apiEndpoint;
    private String categoryName;
    private boolean showHologram;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private HologramManager hologramManager;

    @Override
    public void reload(JavaPlugin plugin) {
        apiEndpoint = getConfigurationSection().getString("api-endpoint");
        categoryName = getConfigurationSection().getString("category-name");
        showHologram =  getConfigurationSection().getBoolean("show-hologram");

        if(showHologram) {
            hologramManager = FancyHologramsPlugin.get().getHologramManager();
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            cacheBuildings();
            if(showHologram) {
                Building.BUILDINGS.values().forEach(Building::spawnHologram);
            }
        });
    }

    @Override
    public void save() {

    }

    public void cacheBuildings() {
        this.cacheBuildings(0);
    }

    public void cacheBuildings(int offset) {
        try {
            String query = String.format("[[%s]]|offset=%d|?Name|?Builder|?X|?Y|?Z|?World|?Page ID", categoryName, offset);
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
            JSONObject results = object.getJSONObject("query").getJSONObject("results");
            for(String key : results.keySet()) {
                JSONObject result = results.getJSONObject(key);
                parseBuildingResult(result);
            }

            if(object.has("query-continue-offset")) {
                int continueOffset = object.getInt("query-continue-offset");
                cacheBuildings(continueOffset);
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseBuildingResult(JSONObject object) {
        try {
            JSONObject printouts = object.getJSONObject("printouts");
            String name = printouts.getJSONArray("Name").getString(0);
            String builderUuid = printouts.getJSONArray("Builder").getString(0);
            int x = printouts.getJSONArray("X").getInt(0);
            int y = printouts.getJSONArray("Y").getInt(0);
            int z = printouts.getJSONArray("Z").getInt(0);
            String worldName = printouts.getJSONArray("World").getString(0);
            World world = Bukkit.getWorld(worldName);
            int pageId =  printouts.getJSONArray("Page ID").getInt(0);

            Building building = Building.of(pageId);
            building.setLocation(new Location(world, x, y, z));
            building.setName(name);
            building.setBuilderUuid(builderUuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return KubeCityBotPlugin.getInstance().getConfig().getConfigurationSection("building-storage");
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
