package hillwalk.raidboss.net.task;

import hillwalk.raidboss.net.Main;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractWorld;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BossSpawnTask extends BukkitRunnable {
    private Main plugin;
    private int countdown;

    private Entity activeBoss;

    public BossSpawnTask(Main plugin) {
        this.plugin = plugin;
        this.countdown = 5; // 5 secondes de décompte

        // Ajoutez tous les joueurs en ligne à la BossBar et rendez-la visible
        BossBar bossBar = plugin.getBossBar();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
    }

    @Override
    public void run() {
        // Arrêter la tâche si le décompte est terminé
        if (countdown <= 0) {
            cancel();
            spawnBoss();
            return;
        }

        // Réduire le décompte
        countdown--;

        // Mettre à jour la BossBar pour afficher le décompte
        BossBar bossBar = plugin.getBossBar();
        double progress = ((double) countdown) / 5.0;
        bossBar.setProgress(progress);
        bossBar.setTitle("§6Le boss apparaîtra dans §f" + countdown + " secondes");
    }

    private void spawnBoss() {
        // Récupérez la liste des noms de boss à partir de la configuration
        List<String> bossNames = plugin.getConfig().getStringList("bosses");

        MythicMob mob = null;
        String randomBossName;

        do {
            // Sélectionnez un boss aléatoire
            randomBossName = bossNames.get(new Random().nextInt(bossNames.size()));
            mob = MythicBukkit.inst().getMobManager().getMythicMob(randomBossName).orElse(null);
        } while (mob == null);

        // Cachez la BossBar et retirez tous les joueurs
        BossBar bossBar = plugin.getBossBar();
        bossBar.setVisible(false);
        for (Player player : bossBar.getPlayers()) {
            bossBar.removePlayer(player);
        }

        String worldName = plugin.getConfig().getString("boss_location.world");
        double x = plugin.getConfig().getDouble("boss_location.x");
        double y = plugin.getConfig().getDouble("boss_location.y") + 10;
        double z = plugin.getConfig().getDouble("boss_location.z");

        Location bossLocation = new Location(plugin.getServer().getWorld(worldName), x, y, z);

        // Chargez le chunk contenant la position du boss et forcez son chargement
        Chunk bossChunk = bossLocation.getChunk();
        bossChunk.setForceLoaded(true);

        // Invoquez le boss
        ActiveMob knight = mob.spawn(BukkitAdapter.adapt(bossLocation), 1);
        plugin.getServer().broadcastMessage("Le boss : " + knight.getName() + " vient de faire son apparition !");

        // Suivez le boss pour décharger le chunk lorsque le boss est vaincu
        UUID bossUUID = knight.getEntity().getUniqueId();
        plugin.getRaidManager().trackBoss(bossUUID, bossChunk);

        // Définir le boss actif dans la classe RaidManager
        Entity activeBoss = knight.getEntity().getBukkitEntity();
        plugin.getRaidManager().setActiveBoss(activeBoss);
    }


}
