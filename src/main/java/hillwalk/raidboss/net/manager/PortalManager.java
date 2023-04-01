package hillwalk.raidboss.net.manager;

import hillwalk.raidboss.net.Main;
import hillwalk.raidboss.net.group.Group;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;


public class PortalManager {
    private Main plugin;

    public PortalManager(Main plugin) {
        this.plugin = plugin;
    }

    public void createPortal(Location location) {
        // Créez un cadre de portail en obsidienne
        World world = location.getWorld();
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                if (x == -1 || x == 2 || y == -1 || y == 2) {
                    world.getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ()).setType(Material.OBSIDIAN);
                } else {
                    world.getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ()).setType(Material.AIR);
                }
            }
        }

        // Ouvrir le portail en plaçant les blocs de portail
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                Block portalBlock = world.getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ());
                portalBlock.setType(Material.NETHER_PORTAL);
                BlockData blockData = portalBlock.getBlockData();
                if (blockData instanceof Orientable) {
                    Orientable orientable = (Orientable) blockData;
                    orientable.setAxis(Axis.X); // Utilisez Axis.Z pour une orientation est-ouest
                    portalBlock.setBlockData(orientable);
                } else {
                    // Gérez le cas où le bloc de données n'est pas une instance d'Orientable
                }
            }
        }
    }
}
