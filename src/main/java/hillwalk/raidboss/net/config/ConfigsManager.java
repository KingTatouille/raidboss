package hillwalk.raidboss.net.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigsManager {
    private JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigsManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }
    }

    public long getBossSpawnInterval() {
        return config.getLong("boss-spawn-interval", 72000); // Valeur par défaut : 1 heure (en ticks)
    }

    public Location getBossSpawnLocation() {
        // À compléter : charger les coordonnées depuis la configuration
        Location spawnLocation = new Location(plugin.getServer().getWorld(plugin.getConfig().getString("boss_location.world")), plugin.getConfig().getDouble("boss_location.x"), plugin.getConfig().getDouble("boss_location.y"), plugin.getConfig().getDouble("boss_location.z"));

        return spawnLocation;
    }

    public Location getDestinationRaidBoss() {
        // À compléter : charger les coordonnées depuis la configuration
        Location destination = new Location(plugin.getServer().getWorld(plugin.getConfig().getString("destination.world")), plugin.getConfig().getDouble("destination.x"), plugin.getConfig().getDouble("destination.y"), plugin.getConfig().getDouble("destination.z"));

        return destination;
    }

    public Location getPortalLocation() {

        Location portalLocation = new Location(plugin.getServer().getWorld(plugin.getConfig().getString("portal_location.world")), plugin.getConfig().getDouble("portal_location.x"), plugin.getConfig().getDouble("portal_location.y"), plugin.getConfig().getDouble("portal_location.z"));

        return portalLocation;
    }
}