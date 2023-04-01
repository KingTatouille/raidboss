package hillwalk.raidboss.net.listener;


import hillwalk.raidboss.net.group.Group;
import hillwalk.raidboss.net.manager.GroupManager;
import hillwalk.raidboss.net.manager.RaidManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.ItemStack;


public class InventoryListener implements Listener {

    private final GroupManager groupManager;

    private final RaidManager raidManager;

    public InventoryListener(GroupManager groupManager, RaidManager raidManager) {
        this.groupManager = groupManager;
        this.raidManager = raidManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGroupRequestInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!"Invitation au groupe".equals(title)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        Player invitedPlayer = (Player) event.getWhoClicked();
        Player leader = groupManager.getGroupLeaderForInvitedPlayer(invitedPlayer);


        if (clickedItem.getType() == Material.GREEN_WOOL) {
            // Accepter l'invitation
            invitedPlayer.closeInventory();
            groupManager.removeGroupInvitation(invitedPlayer);


            // Vérifier si le joueur invité n'est pas déjà dans un groupe
            if (clickedItem.getType() == Material.GREEN_WOOL) {
                // Accepter l'invitation
                invitedPlayer.closeInventory();
                groupManager.removeGroupInvitation(invitedPlayer);
                Group group = groupManager.getGroupByLeader(leader);

                // Vérifier si le joueur invité n'est pas déjà dans un groupe
                if (!groupManager.isInGroup(invitedPlayer)) {
                    groupManager.addPlayerToGroup(invitedPlayer, group);
                    invitedPlayer.sendMessage(raidManager.getPrefix() + "§aVous avez rejoint le groupe de " + leader.getName() + ".");
                    leader.sendMessage(raidManager.getPrefix() + "§a" + invitedPlayer.getName() + " a rejoint votre groupe.");
                } else {
                    invitedPlayer.sendMessage(raidManager.getPrefix() + "§cVous êtes déjà dans un groupe.");
                }
            }
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            // Refuser l'invitation
            invitedPlayer.closeInventory();
            groupManager.removeGroupInvitation(invitedPlayer);
        } else if (clickedItem.getType() == Material.BARRIER) {
            // Bloquer la personne qui envoie la demande
            invitedPlayer.closeInventory();
            groupManager.blockPlayer(leader, invitedPlayer);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        String title = event.getView().getTitle();
        if (!"§aConfirmer la téléportation".equals(title)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Group group = groupManager.getGroup(player);

        if (group == null) {
            player.closeInventory();
            player.sendMessage(raidManager.getPrefix() + "§cVous n'êtes pas dans un groupe.");
            return;
        }

        if (event.getCurrentItem().getType() == Material.GREEN_WOOL) {
            // Implémentez la logique pour accepter la demande de téléportation
            player.closeInventory();
            groupManager.setTeleportAccepted(player, true);
            Player leader = group.getLeader();
            groupManager.teleportGroup(group, leader);
        } else if (event.getCurrentItem().getType() == Material.RED_WOOL) {
            player.closeInventory();
            player.sendMessage(raidManager.getPrefix() + "§cVous avez refusé la demande de téléportation.");
        }
    }

    @EventHandler
    public void onGroupListShow(InventoryClickEvent event) {

        String title = event.getView().getTitle();
        if (!"§7Membres du groupe".equals(title)) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Group group = groupManager.getGroup(player);

        if (group == null) {
            player.closeInventory();
            player.sendMessage(raidManager.getPrefix() + "§cVous n'êtes pas dans un groupe.");
        }

    }



}
