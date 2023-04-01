package hillwalk.raidboss.net.raid.commands;

import hillwalk.raidboss.net.manager.RaidManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrepareRaidCommand implements CommandExecutor {
    private RaidManager raidManager;

    public PrepareRaidCommand(RaidManager raidManager) {
        this.raidManager = raidManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(raidManager.getPrefix() + "Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        // Vérifiez si le joueur a la permission d'exécuter la commande
        if (!player.hasPermission("bossraid.prepare")) {
            player.sendMessage(raidManager.getPrefix() + "§cVous n'avez pas la permission d'exécuter cette commande.");
            return true;
        }

        // Lancez la phase de préparation
        raidManager.startRaid();

        return true;
    }
}
