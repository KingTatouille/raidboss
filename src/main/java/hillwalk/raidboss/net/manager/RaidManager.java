package hillwalk.raidboss.net.manager;


import hillwalk.raidboss.net.Main;
import hillwalk.raidboss.net.players.PlayerInfo;
import hillwalk.raidboss.net.task.BossSpawnTask;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RaidManager {
    private Main plugin;
    private Map<UUID, RaidManager> raids;

    private final Map<UUID, UUID> pendingInvitations;

    private String prefix;

    private Location bossLocation;
    private boolean raidBossActive;

    private Entity activeBoss;

    private boolean isRaidInProgress;

    private Map<UUID, Chunk> trackedBosses = new HashMap<>();


    public RaidManager(Main plugin) {
        this.plugin = plugin;
        this.pendingInvitations = new HashMap<>();
        this.raids = new HashMap<>();
        this.raidBossActive = false;
        this.isRaidInProgress = false;
    }

    // Les autres méthodes de RaidManager


    public void loadPrefix(FileConfiguration config) {
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix"));
    }

    public String getPrefix() {
        String prefix = plugin.getConfig().getString("prefix");
        if (prefix == null) {
            return ChatColor.GREEN + "[RaidBoss] " + ChatColor.RESET;
        }
        return ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.RESET;
    }


    public Location getPortalLocation() {
        // Récupérer les coordonnées et le nom du monde depuis la configuration
        String worldName = plugin.getConfig().getString("portal_location.world");
        double x = plugin.getConfig().getDouble("portal_location.x");
        double y = plugin.getConfig().getDouble("portal_location.y");
        double z = plugin.getConfig().getDouble("portal_location.z");

        // Créer et retourner un objet Location à partir des coordonnées et du nom du monde récupérés
        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }


    public void loadBossLocation(FileConfiguration config) {
        String worldName = config.getString("boss_location.world");
        World world = Bukkit.getWorld(worldName);

        double x = config.getDouble("boss_location.x");
        double y = config.getDouble("boss_location.y");
        double z = config.getDouble("boss_location.z");

        this.bossLocation = new Location(world, x, y, z);
    }

    public Location getDestinationRaid(FileConfiguration config) {
        String worldName = config.getString("destination.world");
        World world = Bukkit.getWorld(worldName);

        double x = config.getDouble("destination.x");
        double y = config.getDouble("destination.y");
        double z = config.getDouble("destination.z");

        return new Location(world, x, y, z);
    }

    public void trackBoss(UUID bossUUID, Chunk bossChunk) {
        // Ajoutez le boss et le chunk associé à la map des boss suivis
        trackedBosses.put(bossUUID, bossChunk);
    }

    public void giveRewards(List<Player> players, Map<UUID, Double> damagePercentages) {
        List<Map<?, ?>> rewardsList = plugin.getConfig().getMapList("rewards");

        for (Player player : players) {
            double damagePercentage = damagePercentages.get(player.getUniqueId());

            for (Map<?, ?> rewardMap : rewardsList) {
                double minDamage = (double) rewardMap.get("min_damage");
                if (damagePercentage >= minDamage) {
                    double dropChance = (double) rewardMap.get("drop_chance");
                    double randomValue = Math.random(); // Génère un nombre aléatoire entre 0 et 1

                    if (randomValue <= dropChance) {
                        Material itemMaterial = Material.getMaterial((String) rewardMap.get("item"));
                        int maxAmount = (int) rewardMap.get("max_amount");

                        int amount = (int) (damagePercentage / minDamage);
                        if (amount > maxAmount) {
                            amount = maxAmount;
                        }

                        ItemStack item = new ItemStack(itemMaterial, amount);
                        player.getInventory().addItem(item);
                    }
                }
            }
        }
    }


    private Map<UUID, PlayerInfo> playerDamageInfo = new HashMap<>();

    public void registerPlayerDamage(Player player, double damage) {
        UUID playerUUID = player.getUniqueId();
        PlayerInfo info = playerDamageInfo.get(playerUUID);

        if (info == null) {
            info = new PlayerInfo(playerUUID);
            playerDamageInfo.put(playerUUID, info);
        }

        info.addDamageDealt(damage);
    }

    public List<Player> getPlayersWhoHitBoss() {
        return playerDamageInfo.values().stream()
                .map(info -> Bukkit.getPlayer(info.getPlayerUUID()))
                .collect(Collectors.toList());
    }

    public Map<UUID, Double> getPlayerDamagePercentages() {
        double totalDamage = playerDamageInfo.values().stream()
                .mapToDouble(PlayerInfo::getDamageDealt)
                .sum();

        Map<UUID, Double> damagePercentages = new HashMap<>();
        for (PlayerInfo info : playerDamageInfo.values()) {
            double percentage = info.getDamageDealt() / totalDamage;
            damagePercentages.put(info.getPlayerUUID(), percentage);
        }

        return damagePercentages;
    }



    public void onBossDefeated() {
        // Effectuez des actions liées à la défaite du boss, par exemple distribuer des récompenses aux joueurs

        Player topPlayer = null;
        double maxDamage = 0;
        Map<UUID, Double> damagePercentages = new HashMap<>();

        // Calculez le total des dégâts pour tous les joueurs
        double totalDamage = 0;
        for (PlayerInfo info : plugin.getPlayerDamageInfo().values()) {
            totalDamage += info.getDamageDealt();
        }

        // Calculez les pourcentages de dégâts pour chaque joueur
        for (PlayerInfo info : plugin.getPlayerDamageInfo().values()) {
            Player player = Bukkit.getPlayer(info.getPlayerUUID());
            if (player != null && player.isOnline()) {
                double damageDealt = info.getDamageDealt();
                double damagePercentage = damageDealt / totalDamage;

                // Trouvez le joueur avec le plus de dégâts
                if (damageDealt > maxDamage) {
                    maxDamage = damageDealt;
                    topPlayer = player;
                }

                damagePercentages.put(player.getUniqueId(), damagePercentage);
            }
        }

        // Distribuez les récompenses en fonction des pourcentages de dégâts infligés
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        giveRewards(players, damagePercentages);

        if (topPlayer != null) {
            // Diffusez un message pour féliciter le joueur ou le groupe ayant infligé le plus de dégâts
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "Félicitations à " + topPlayer.getName() + " pour avoir infligé le plus de dégâts au boss !");
        }

        // Supprimez les informations sur les joueurs
        playerDamageInfo.clear();

        // Autres actions pour gérer la défaite du boss (par exemple, décharger les chunks)
    }



    public void startRaid() {
        if (!isRaidInProgress) {
            isRaidInProgress = true;
            plugin.getServer().broadcastMessage(getPrefix() + "Un boss apparaîtra dans 5 minutes. Préparez-vous et rejoignez le portail.");
            plugin.getPortalManager().createPortal(plugin.getRaidManager().getPortalLocation());

            // Créer et exécuter la tâche BossSpawnTask avec un délai initial de 6000 ticks (5 minutes) et une période de 20 ticks
            BossSpawnTask bossSpawnTask = new BossSpawnTask(plugin);
            bossSpawnTask.runTaskTimer(plugin, 6000, 20);
        } else {
            plugin.getLogger().info("Un raid est déjà en cours. Veuillez attendre qu'il se termine avant d'en démarrer un nouveau.");
        }
    }


    public void removeActiveBoss() {
        this.isRaidInProgress = false;
    }

    public boolean isRaidBossActive(){
        return isRaidInProgress;
    }

    public void setActiveBoss(Entity boss) {
        this.activeBoss = boss;
    }

    public Entity getActiveBoss() {
        return activeBoss;
    }



}

