package hillwalk.raidboss.net;

import hillwalk.raidboss.net.config.ConfigsManager;
import hillwalk.raidboss.net.group.Group;
import hillwalk.raidboss.net.group.commands.GroupCommands;
import hillwalk.raidboss.net.listener.*;
import hillwalk.raidboss.net.manager.GroupManager;
import hillwalk.raidboss.net.manager.PortalManager;
import hillwalk.raidboss.net.manager.RaidManager;
import hillwalk.raidboss.net.players.PlayerInfo;
import hillwalk.raidboss.net.raid.commands.PrepareRaidCommand;
import hillwalk.raidboss.net.tabcompleter.GroupTabCompleter;
import hillwalk.raidboss.net.task.BossSpawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin {
    private ConfigsManager configsManager;
    private PortalManager portalManager;
    private GroupManager groupManager;
    private RaidManager raidManager;

    private Map<UUID, Boolean> groupResponses;

    private Map<UUID, PlayerInfo> playerDamageInfo = new HashMap<>();

    private BossBar bossBar;

    @Override
    public void onEnable() {
        // Charger la configuration
        configsManager = new ConfigsManager(this);
        configsManager.loadConfig();


        bossBar = createBossBar();

        groupResponses = new HashMap<>();

        playerDamageInfo = new HashMap<>();

        groupManager = new GroupManager();


        // Créer le gestionnaire de portail et le gestionnaire de raid
        portalManager = new PortalManager(this);
        raidManager = new RaidManager(this);

        //Sauvegarde de la config.
        saveDefaultConfig();

        //Prefix
        raidManager.loadPrefix(getConfig());

        //Appelons la location du boss


        // Lire les intervalles de configuration en heures, minutes et secondes
        int intervalHours = getConfig().getInt("raid_interval_hours", 0);
        int intervalMinutes = getConfig().getInt("raid_interval_minutes", 0);
        int intervalSeconds = getConfig().getInt("raid_interval_seconds", 30);

        // Convertir les intervalles en ticks (20 ticks = 1 seconde)
        long intervalTicks = ((long) intervalHours * 60 * 60
                + (long) intervalMinutes * 60
                + (long) intervalSeconds) * 20;

        // Créez et exécutez la tâche pour le raid du boss
        getServer().getScheduler().runTaskTimer(this, () -> {
            raidManager.startRaid();
        }, intervalTicks, intervalTicks);


        // Enregistrer les écouteurs d'événements
        Location portalLocation = raidManager.getPortalLocation();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BossFightListener(this), this);
        pm.registerEvents(new PortalJoinListener(this, raidManager, portalLocation, groupManager), this);
        pm.registerEvents(new LeaveListener(groupManager, raidManager), this);
        pm.registerEvents(new InventoryListener(groupManager, raidManager), this);

        RaidManager raidManager = new RaidManager(this);
        this.getCommand("group").setExecutor(new GroupCommands(this, raidManager));
        this.getCommand("group").setTabCompleter(new GroupTabCompleter());
        this.getCommand("raid").setExecutor(new PrepareRaidCommand(raidManager));

        // Lancer la tâche pour faire apparaître les boss
        long spawnInterval = configsManager.getBossSpawnInterval();
        new BossSpawnTask(this).runTaskTimer(this, spawnInterval, spawnInterval);
    }

    public ConfigsManager getConfigManager() {
        return configsManager;
    }

    public BossBar createBossBar() {
        BossBar bossBar = Bukkit.createBossBar("§6Le boss apparaîtra bientôt", BarColor.RED, BarStyle.SOLID);
        bossBar.setVisible(false); // Cachez la BossBar au début
        return bossBar;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public Map<UUID, PlayerInfo> getPlayerDamageInfo() {
        return playerDamageInfo;
    }
    public Map<UUID, Boolean> getGroupResponses() {
        return groupResponses;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public PortalManager getPortalManager() {
        return portalManager;
    }


    public RaidManager getRaidManager() {
        return raidManager;
    }
}