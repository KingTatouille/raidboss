package hillwalk.raidboss.net.tabcompleter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
            "create", "invite", "openinvitation", "join",
            "leave", "disband", "list", "requestteleport", "confirmteleport"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        if (args.length == 1) {
            String action = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            for (String cmd : COMMANDS) {
                if (cmd.startsWith(action)) {
                    suggestions.add(cmd);
                }
            }

            return suggestions;
        }

        return null;
    }
}
