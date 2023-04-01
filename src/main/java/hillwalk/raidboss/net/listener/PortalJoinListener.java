package hillwalk.raidboss.net.listener;

import hillwalk.raidboss.net.Main;
import hillwalk.raidboss.net.group.Group;
import hillwalk.raidboss.net.manager.GroupManager;
import hillwalk.raidboss.net.manager.RaidManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import org.bukkit.inventory.ItemStack;


public class PortalJoinListener implements Listener {
    private Main plugin;
    private final RaidManager raidManager;
    private final Location portalLocation;
    private final GroupManager groupManager;


    public PortalJoinListener(Main plugin, RaidManager raidManager, Location portalLocation, GroupManager groupManager) {
        this.plugin = plugin;
        this.raidManager = raidManager;
        this.portalLocation = portalLocation;
        this.groupManager = groupManager;
    }



    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerPortalEvent.TeleportCause.NETHER_PORTAL) {
            // Vérifiez si le joueur est à proximité de l'emplacement du portail personnalisé
            Location portalLocation = plugin.getConfigManager().getPortalLocation();

            // Vérifiez si le joueur et le portail personnalisé sont dans le même monde
            if (!event.getFrom().getWorld().equals(portalLocation.getWorld())) {
                return;
            }

            double distanceSquared = event.getFrom().getBlock().getLocation().distanceSquared(portalLocation);

            // Utilisez une valeur de distance au carré pour éviter d'utiliser la racine carrée (plus coûteuse en calcul)
            double maxDistanceSquared = 9; // Correspond à une distance maximale de 3 blocs

            if (distanceSquared <= maxDistanceSquared) {
                // Vérifiez si le raid boss est actif
                if (plugin.getRaidManager().isRaidBossActive()) {
                    event.setCancelled(true); // Annule l'événement du portail pour empêcher le joueur d'être téléporté dans le Nether
                    Location destination = raidManager.getDestinationRaid(plugin.getConfig());

                    event.getPlayer().teleport(destination);
                    groupManager.sendTeleportTitle(event.getPlayer());
                } else {
                    // Si le raid boss n'est pas actif, annulez la téléportation
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Le raid boss n'est pas actif. Vous ne pouvez pas être téléporté.");
                }
            } else {
                event.setCancelled(true); // Annule la téléportation vers le Nether si le joueur n'est pas à proximité du portail personnalisé
            }
        }
    }
}