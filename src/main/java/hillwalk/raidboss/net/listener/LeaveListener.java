package hillwalk.raidboss.net.listener;

import hillwalk.raidboss.net.group.Group;
import hillwalk.raidboss.net.manager.GroupManager;
import hillwalk.raidboss.net.manager.RaidManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private GroupManager groupManager;
    private RaidManager raidManager;

    public LeaveListener(GroupManager groupManager, RaidManager raidManager) {
        this.groupManager = groupManager;
        this.raidManager = raidManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Group group = groupManager.getGroup(player);

        if (group != null) {
            if (group.getLeader().equals(player)) {
                // Si le leader se déconnecte, détruire le groupe et informer les membres
                for (Player member : group.getMembers()) {
                    if (!member.equals(player)) {
                        member.sendMessage(raidManager.getPrefix() + "§cLe leader du groupe " + player.getName() + " s'est déconnecté, le groupe a été dissous.");
                    }
                }
                groupManager.removePlayerFromGroup(player);
                groupManager.removeGroupInvitation(player);
            } else {
                // Si un membre se déconnecte, l'enlever du groupe et informer les autres membres
                group.removeMember(player);
                for (Player member : group.getMembers()) {
                    member.sendMessage(raidManager.getPrefix() + "§c" + player.getName() + " a quitté le groupe.");
                }
                groupManager.removePlayerFromGroup(player);
            }
        }
    }

}
