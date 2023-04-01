package hillwalk.raidboss.net.listener;

import hillwalk.raidboss.net.Main;
import hillwalk.raidboss.net.players.PlayerInfo;
import hillwalk.raidboss.net.task.BossSpawnTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BossFightListener implements Listener {

    private final Main plugin;
    private BossSpawnTask bossSpawnTask;


    public BossFightListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && isBoss(event.getEntity())) {
            Player player = (Player) event.getDamager();
            UUID playerUUID = player.getUniqueId();
            double damage = event.getFinalDamage();

            PlayerInfo info = plugin.getPlayerDamageInfo().get(playerUUID);
            if (info == null) {
                info = new PlayerInfo(playerUUID);
                plugin.getPlayerDamageInfo().put(playerUUID, info);
            }

            info.addDamageDealt(damage);
        }
    }



    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (isBoss(event.getEntity())) {
            plugin.getRaidManager().onBossDefeated();
            plugin.getRaidManager().removeActiveBoss();

            // Distribuer les récompenses
            List<Player> players = plugin.getRaidManager().getPlayersWhoHitBoss();
            Map<UUID, Double> damagePercentages = plugin.getRaidManager().getPlayerDamagePercentages();
            plugin.getRaidManager().giveRewards(players, damagePercentages);
        }
    }



    private boolean isBoss(Entity entity) {
        Entity activeBoss = plugin.getRaidManager().getActiveBoss();
        return entity.equals(activeBoss);
    }

}
