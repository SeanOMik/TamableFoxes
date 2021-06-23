package net.seanomik.tamablefoxes;

import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandSpawnTamableFox implements TabExecutor {

    private final TamableFoxes plugin;

    public CommandSpawnTamableFox(TamableFoxes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getOnlyRunPlayer());
            return true;
        }

        if (!sender.hasPermission("tamablefoxes.spawntamablefox")) {
            sender.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getNoPermMessage());
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 0) {
            switch (args[0]) {
                case "red":
                    try {
                        plugin.nmsInterface.spawnTamableFox(player.getLocation(), NMSInterface.FoxType.RED);
                        player.sendMessage(Config.getPrefix() + ChatColor.RESET + LanguageConfig.getSpawnedFoxMessage(NMSInterface.FoxType.RED));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFailureSpawn());
                    }
                    break;
                case "snow":
                    try {
                        plugin.nmsInterface.spawnTamableFox(player.getLocation(), NMSInterface.FoxType.SNOW);
                        player.sendMessage(Config.getPrefix() + ChatColor.RESET + LanguageConfig.getSpawnedFoxMessage(NMSInterface.FoxType.SNOW));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFailureSpawn());
                    }
                    break;
                case "reload":
                    plugin.reloadConfig();
                    LanguageConfig.getConfig(plugin).reloadConfig();
                    player.sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getReloadMessage());
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "/spawntamablefox " + ChatColor.GRAY + "[red | snow | reload]");
            }
        } else {
            player.sendMessage(ChatColor.RED + "/spawntamablefox " + ChatColor.GRAY + "[red | snow | reload]");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new LinkedList<>(Arrays.asList(
                "red",
                "snow",
                "reload"
        ));
    }
}
