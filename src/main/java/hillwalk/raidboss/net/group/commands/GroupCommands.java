package hillwalk.raidboss.net.group.commands;

import hillwalk.raidboss.net.Main;
import hillwalk.raidboss.net.manager.GroupManager;
import hillwalk.raidboss.net.manager.RaidManager;
import hillwalk.raidboss.net.group.Group;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GroupCommands implements CommandExecutor {

    private final RaidManager raidManager;
    private final GroupManager groupManager;

    private final Main plugin;


    public GroupCommands(Main plugin, RaidManager raidManager) {
        this.raidManager = raidManager;
        this.groupManager = plugin.getGroupManager();
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(raidManager.getPrefix() + "Cette commande ne peut être utilisée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            return false;
        }

        String action = args[0].toLowerCase();

        if ("create".equals(action)) {
            if (args.length < 2) {
                player.sendMessage(raidManager.getPrefix() + "§cVeuillez spécifier un nom pour votre groupe.");
                return true;
            }

            String groupName = args[1];

            if (groupManager.isInGroup(player)) {
                player.sendMessage(raidManager.getPrefix() + "§cVous êtes déjà dans un groupe.");
                return true;
            }

            groupManager.createGroup(player, groupName);
            player.sendMessage(raidManager.getPrefix() + "§aVous avez créé un groupe avec le nom \"" + groupName + "\".");

        } else if ("invite".equals(action)) {


            if (args.length < 2) {
                player.sendMessage(raidManager.getPrefix() + "§cVeuillez spécifier le joueur à inviter.");
                return true;
            }

            if (!groupManager.isInGroup(player)) {
                player.sendMessage(raidManager.getPrefix() + "§cVous devez être dans un groupe pour inviter des joueurs.");
                return true;
            }

            Group group = groupManager.getGroup(player);
            if (!player.equals(group.getLeader())) {
                player.sendMessage(raidManager.getPrefix() + "§cSeul le leader du groupe peut inviter des joueurs.");
                return true;
            }

            String invitedPlayerName = args[1];
            Player invitedPlayer = Bukkit.getPlayer(invitedPlayerName);


            if (invitedPlayer == null) {
                player.sendMessage(raidManager.getPrefix() + "§cLe joueur spécifié est introuvable ou hors ligne.");
                return true;
            }

            if (groupManager.isInGroup(invitedPlayer)) {
                player.sendMessage(raidManager.getPrefix() + "§cLe joueur spécifié est déjà dans un groupe.");
                return true;
            }

            player.sendMessage(raidManager.getPrefix() + "§aVous avez invité " + invitedPlayerName + " à rejoindre votre groupe.");

            // Créer le message de texte JSON avec l'action clickEvent
            TextComponent openInvitationMessage = new TextComponent("§a" + player.getName() + " vous a invité à rejoindre son groupe. ");
            TextComponent openInvitationLink = new TextComponent("§6[Ouvrir l'invitation]");
            openInvitationLink.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group openinvitation " + player.getName()));
            openInvitationMessage.addExtra(openInvitationLink);

            // Envoyer le message de texte JSON au joueur invité
            invitedPlayer.spigot().sendMessage(openInvitationMessage);


        } else if ("openinvitation".equals(action)) {
            if (args.length < 2) {
                player.sendMessage(raidManager.getPrefix() + "§cErreur : Utilisation incorrecte de la commande.");
                return true;
            }

            String leaderName = args[1];
            Player leader = Bukkit.getPlayer(leaderName);

            if (leader == null) {
                player.sendMessage(raidManager.getPrefix() + "§cLe joueur qui vous a invité n'est plus en ligne.");
                return true;
            }


            Group group = groupManager.getGroup(leader);
            if (group == null) {
                player.sendMessage(raidManager.getPrefix() + "§cLe groupe n'existe plus.");
                return true;
            }

            groupManager.openGroupRequestInventory(player, leader);

        } else if ("join".equals(action)) {
            if (args.length < 2) {
                player.sendMessage(raidManager.getPrefix() + "§cVeuillez spécifier le nom du leader du groupe.");
                return true;
            }

            Player leader = Bukkit.getPlayer(args[1]);
            if (leader == null) {
                player.sendMessage(raidManager.getPrefix() + "§cLe leader spécifié est introuvable ou hors ligne.");
                return true;
            }

            Group group = groupManager.getGroup(leader);
            if (group != null) {

                groupManager.addPlayerToGroup(player, group);
//                System.out.println(player.getName());
//                System.out.println(group.getLeader().getName());
//                System.out.println("[Debug] Player joined group using /group join"); // Ajouter ce message de débogage
                player.sendMessage(raidManager.getPrefix() + "§aVous avez rejoint le groupe de " + leader.getName() + ".");
                leader.sendMessage(raidManager.getPrefix() + "§a" + player.getName() + " a rejoint votre groupe.");
            } else {
                player.sendMessage(raidManager.getPrefix() + "§cLe groupe spécifié n'existe plus.");
            }
        } else if ("leave".equals(action)) {
            Group group = groupManager.getGroup(player);
            if (group == null) {
                player.sendMessage(raidManager.getPrefix() + "§cVous n'êtes actuellement dans aucun groupe.");
                return true;
            }

            // Si le joueur est le leader du groupe, le groupe est dissous et tous les membres sont informés.
            if (group.getLeader().equals(player)) {
                for (Player member : group.getMembers()) {
                    if (!member.equals(player)) {
                        member.sendMessage(raidManager.getPrefix() + "§cLe leader a quitté le groupe. Le groupe a été dissous.");
                        groupManager.removePlayerFromGroup(member);
                    }
                }
                player.sendMessage(raidManager.getPrefix() + "§aVous avez quitté le groupe et dissous le groupe.");
            } else {
                // Si le joueur est un membre du groupe, il quitte simplement le groupe.
                group.removeMember(player);
                groupManager.removePlayerFromGroup(player);
                player.sendMessage(raidManager.getPrefix() + "§aVous avez quitté le groupe.");
                group.getLeader().sendMessage(raidManager.getPrefix() + "§c" + player.getName() + " a quitté le groupe.");
            }
        } else if ("disband".equals(action)) {

            Group group = groupManager.getGroup(player);
            if (group == null) {
                player.sendMessage(raidManager.getPrefix() + "§cVous n'êtes actuellement dans aucun groupe.");
                return true;
            }

            if (!group.getLeader().equals(player)) {
                player.sendMessage(raidManager.getPrefix() + "§cSeul le leader du groupe peut dissoudre le groupe.");
                return true;
            }

            for (Player member : group.getMembers()) {
                if (!member.equals(player)) {
                    member.sendMessage(raidManager.getPrefix() + "§cLe leader a dissous le groupe.");
                    groupManager.removePlayerFromGroup(member);
                }
            }
            player.sendMessage(raidManager.getPrefix() + "§aVous avez dissous le groupe.");
            groupManager.removePlayerFromGroup(player);

        } else if ("list".equals(action)) {
            Group group = groupManager.getGroup(player);
            if (group == null) {
                player.sendMessage(raidManager.getPrefix() + "Vous n'êtes pas dans un groupe.");
                return true;
            }

            // Créer l'inventaire pour la liste des membres du groupe
            int inventorySize = 9 * ((group.getMembers().size() + 8) / 9); // La taille de l'inventaire doit être un multiple de 9
            Inventory groupMembersInventory = Bukkit.createInventory(null, inventorySize, "§7Membres du groupe");

            // Ajouter la tête de chaque membre du groupe à l'inventaire
            for (Player member : group.getMembers()) {
                ItemStack memberHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta memberHeadMeta = (SkullMeta) memberHead.getItemMeta();
                memberHeadMeta.setOwningPlayer(member);
                memberHeadMeta.setDisplayName("§a" + member.getName());
                memberHead.setItemMeta(memberHeadMeta);

                groupMembersInventory.addItem(memberHead);
            }

            // Ouvrir l'inventaire pour le joueur
            player.openInventory(groupMembersInventory);
        } else if ("requestteleport".equals(action)) {
            // Obtient le groupe dont le joueur est leader
            Group group = groupManager.getGroupByLeader(player);

            // Si le groupe existe (c'est-à-dire que le joueur est le leader d'un groupe)
            if (group != null) {
                // Envoyer la demande de téléportation aux membres du groupe
                groupManager.sendClickableTeleportMessage(player, group);
                player.sendMessage(raidManager.getPrefix() + "§aDemande de téléportation envoyée à tous les membres du groupe.");
            } else {
                // Si le groupe n'existe pas (c'est-à-dire que le joueur n'est pas le leader d'un groupe)
                player.sendMessage(raidManager.getPrefix() + "§cVous devez être le leader d'un groupe pour envoyer une demande de téléportation.");
            }
        } else if ("confirmteleport".equals(action)) {
            Group group = groupManager.getGroup(player);
            if (group == null) {
                player.sendMessage(raidManager.getPrefix() + "§cVous devez être membre d'un groupe pour confirmer une téléportation.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(raidManager.getPrefix() + "§cErreur : Utilisation incorrecte de la commande.");
                return true;
            }

            String leaderName = args[1];
            Player leader = Bukkit.getPlayer(leaderName);

            if (leader == null) {
                player.sendMessage(raidManager.getPrefix() + "§cLe joueur qui a demandé la téléportation n'est plus en ligne.");
                return true;
            }

            groupManager.openTeleportRequestInventory(player, group);
        } else if ("reload".equals(action)) {
            reloadPluginConfiguration(sender);
            return true;
        } else {
            return false;
        }

        return true;
    }

    private void reloadPluginConfiguration(CommandSender sender) {
        try {
            // Rechargez la configuration
            plugin.reloadConfig();
            plugin.saveDefaultConfig();

            // Envoyez un message à l'expéditeur pour confirmer que la configuration a été rechargée
            sender.sendMessage("§aLa configuration du plugin a été rechargée avec succès.");
        } catch (Exception e) {
            // Si une erreur se produit lors du rechargement, informez l'expéditeur
            sender.sendMessage("§cErreur lors du rechargement de la configuration du plugin.");
            e.printStackTrace();
        }
    }


}
