package hillwalk.raidboss.net.manager;

import hillwalk.raidboss.net.group.Group;
import jdk.internal.net.http.common.Pair;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GroupManager {
    private Map<UUID, UUID> groupInvitations = new HashMap<>();
    private Set<Pair<UUID, UUID>> blockedPlayers = new HashSet<>();

    private final Map<Player, Boolean> teleportAcceptanceMap = new HashMap<>();

    private final Map<UUID, Group> playerGroupMap;

    public GroupManager(){
        playerGroupMap = new HashMap<>();
    }

    public Group createGroup(Player leader, String name) {
        Group group = new Group(leader, name);
        playerGroupMap.put(leader.getUniqueId(), group);
        return group;
    }

    public Group getGroupByLeader(Player leader) {
        return playerGroupMap.get(leader.getUniqueId());
    }



    public void removeGroupInvitation(Player invitedPlayer) {
        groupInvitations.remove(invitedPlayer.getUniqueId());
    }



    public Player getGroupLeaderForInvitedPlayer(Player invitedPlayer) {
        UUID leaderUUID = groupInvitations.get(invitedPlayer.getUniqueId());
        if (leaderUUID != null) {
            return Bukkit.getPlayer(leaderUUID);
        }
        return null;
    }

    public boolean isInGroup(Player player) {
        for (Group group : playerGroupMap.values()) {
            if (group.isMember(player)) {
                return true;
            }
        }
        return false;
    }

    public Group getGroup(Player player) {
        return playerGroupMap.get(player.getUniqueId());
    }

    public void addPlayerToGroup(Player player, Group group) {
        group.addMember(player);
        playerGroupMap.put(player.getUniqueId(), group);
    }

    public void removePlayerFromGroup(Player player) {
        playerGroupMap.remove(player.getUniqueId());
    }


    public void openGroupRequestInventory(Player invitedPlayer, Player leader) {
        int size = 27;
        String title = "Invitation au groupe";
        Inventory groupRequestInventory = Bukkit.createInventory(null, size, title);

        // Item pour accepter l'invitation
        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Accepter");
        acceptItem.setItemMeta(acceptMeta);
        groupRequestInventory.setItem(11, acceptItem);

        // Item pour refuser l'invitation
        ItemStack declineItem = new ItemStack(Material.RED_WOOL);
        ItemMeta declineMeta = declineItem.getItemMeta();
        declineMeta.setDisplayName(ChatColor.RED + "Refuser");
        declineItem.setItemMeta(declineMeta);
        groupRequestInventory.setItem(15, declineItem);

        // Item pour bloquer la personne qui envoie la demande
        ItemStack blockItem = new ItemStack(Material.BARRIER);
        ItemMeta blockMeta = blockItem.getItemMeta();
        blockMeta.setDisplayName(ChatColor.DARK_RED + "Bloquer");
        blockItem.setItemMeta(blockMeta);
        groupRequestInventory.setItem(22, blockItem);

        invitedPlayer.openInventory(groupRequestInventory);
        groupInvitations.put(invitedPlayer.getUniqueId(), leader.getUniqueId());
    }

    public void blockPlayer(Player playerToBlock, Player playerBlocking) {
        blockedPlayers.add(new Pair<>(playerBlocking.getUniqueId(), playerToBlock.getUniqueId()));
    }

    public boolean isPlayerBlocked(Player playerToCheck, Player playerBlocking) {
        return blockedPlayers.contains(new Pair<>(playerBlocking.getUniqueId(), playerToCheck.getUniqueId()));
    }

    public void setTeleportAccepted(Player player, boolean accepted) {
        teleportAcceptanceMap.put(player, accepted);
    }

    public void teleportGroup(Group group, Player leader) {
        for (Player member : group.getMembers()) {
            if (!member.equals(leader) && teleportAcceptanceMap.getOrDefault(member, false)) {
                member.teleport(leader.getLocation());
                member.sendMessage(ChatColor.GREEN + "Vous avez été téléporté avec succès vers " + leader.getName() + " !");
            }
        }
        teleportAcceptanceMap.clear();
    }


    public void sendTeleportTitle(Player player) {
        String title = "§aTéléporté";
        String subtitle = "§bVous avez été téléporté avec succès!";
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void sendClickableTeleportMessage(Player sender, Group group) {
        TextComponent message = new TextComponent("§a" + sender.getName() + " a demandé une téléportation. ");
        TextComponent clickableText = new TextComponent("§6[Cliquer ici pour confirmer]");
        clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group confirmteleport " + sender.getName()));
        message.addExtra(clickableText);

        for (Player member : group.getMembers()) {
            if (!member.equals(sender)) {
                member.spigot().sendMessage(message);
            }
        }
    }



    public void openTeleportRequestInventory(Player player, Group group) {
        Inventory teleportRequestInventory = Bukkit.createInventory(null, 9, "§aConfirmer la téléportation");

        ItemStack acceptTeleportItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptTeleportMeta = acceptTeleportItem.getItemMeta();
        acceptTeleportMeta.setDisplayName("§aAccepter la téléportation");
        acceptTeleportItem.setItemMeta(acceptTeleportMeta);

        ItemStack refuseTeleportItem = new ItemStack(Material.RED_WOOL);
        ItemMeta refuseTeleportMeta = acceptTeleportItem.getItemMeta();
        refuseTeleportMeta.setDisplayName("§aRefuser la téléportation");
        refuseTeleportItem.setItemMeta(refuseTeleportMeta);

        teleportRequestInventory.setItem(2, acceptTeleportItem);
        teleportRequestInventory.setItem(4, refuseTeleportItem);
        player.openInventory(teleportRequestInventory);
    }



}
