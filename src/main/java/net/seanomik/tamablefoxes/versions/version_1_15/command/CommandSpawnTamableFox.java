package net.seanomik.tamablefoxes.versions.version_1_15.command;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.minecraft.server.v1_15_R1.EntityFox;
import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.LanguageConfig;
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
            sender.sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getOnlyRunPlayer());
            return true;
        }

        if (!sender.hasPermission("tamablefoxes.spawntamablefox")) {
            sender.sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getNoPermMessage());
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 0) {
            switch (args[0]) {
                case "red":
                    try {
                        EntityTamableFox fox = plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.RED);
                        fox.saveNbt();

                        player.sendMessage(Utils.getPrefix() + ChatColor.RESET + LanguageConfig.getSpawnedFoxMessage(EntityFox.Type.RED));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureSpawn());
                    }
                    break;
                case "snow":
                    try {
                        EntityTamableFox spawnedFox = plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.SNOW);
                        spawnedFox.saveNbt();

                        player.sendMessage(Utils.getPrefix() + ChatColor.RESET + LanguageConfig.getSpawnedFoxMessage(EntityFox.Type.SNOW));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureSpawn());
                    }
                    break;
                case "reload":
                    plugin.reloadConfig();
                    LanguageConfig.getConfig().reloadConfig();
                    player.sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getReloadMessage());
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
