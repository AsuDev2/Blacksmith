package asudev.blacksmith;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Commands implements Listener, CommandExecutor {

    private Blacksmith plugin = Blacksmith.getPlugin(Blacksmith.class);

    private Manager manager = Manager.getInstance();

    public String blacksmithmain = "blacksmith";

    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command cmd, @NonNull String label, @NonNull String[] args) {
        if (cmd.getName().equalsIgnoreCase(blacksmithmain)) {
            if(sender.hasPermission("blacksmith.admin")) {
                if (args.length == 0) {
                    manager.send(sender, "&8&m-----------------------------------------------------\n" +
                            "&7 \n" +
                            "&a&l&nBlacksmith Admin Commands\n" +
                            "&7 \n" +
                            "&b/blacksmith reload &8> &7Reloads all the mmoitems for blacksmith.\n" +
                            "&b/blacksmith open <player> &8> &7Opens blacksmith shop for a player\n" +
                            "&7 \n" +
                            "&8&m-----------------------------------------------------");
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    Long timeNow = System.currentTimeMillis() / 1000;
                    plugin.reloadCustomConfig();
                    plugin.reloadCustomConfig2();
                    manager.setupItems();
                    Number timeAfter = (System.currentTimeMillis() / 1000) - timeNow;
                    manager.send(sender, "&7Reloaded &a&l&nBlacksmith &7in &b" + timeAfter + "ms&7.");
                } else if (args[0].equalsIgnoreCase("open")) {
                    if (args.length > 1) {
                        Player target = Bukkit.getServer().getPlayer(args[1]);
                        if (target != null) {
                            manager.openBlacksmithGui(target);
                            return true;
                        } else {
                            manager.send(sender, "&cThe player you specified is not online.");
                            return true;
                        }
                    } else {
                        manager.send(sender, "&cYou must specify a player to open the shop to!");
                        return true;
                    }
                }
            } else {
                manager.send(sender, "&cYou do not have permission to use this command.");
                return true;
            }
        } else {
            return false;
        }
        return false;
    }
}
