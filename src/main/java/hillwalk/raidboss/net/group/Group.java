package hillwalk.raidboss.net.group;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private final Player leader;
    private final Set<Player> members;
    private final String name;

    public Group(Player leader, String groupName) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.name = groupName;
        this.members.add(leader);
    }

    public Player getLeader() {
        return leader;
    }

    public Set<Player> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public void addMember(Player player) {
//        System.out.println("[Debug] Adding member to the group: " + player.getName());
        members.add(player);
    }

    public void removeMember(Player player) {
        members.remove(player);
    }

    public boolean isMember(Player player) {
        return members.contains(player);
    }
}
