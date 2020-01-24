package net.seanomik.tamablefoxes.command;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import net.seanomik.tamablefoxes.Config;
import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.minecraft.server.v1_15_R1.EntityFox;
import net.seanomik.tamablefoxes.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
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
            sender.sendMessage("Command can only be run from player state.");
            return true;
        }

        if (!sender.hasPermission("tamablefoxes.spawntamablefox")) {
            sender.sendMessage(ChatColor.RED + "You do not have the permission for this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 0) {
            switch (args[0]) {
                case "red":
                    try {
                        EntityTamableFox fox = plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.RED);
                        //plugin.getSpawnedFoxes().add(fox);
                        plugin.sqLiteSetterGetter.saveFox(fox);

                        player.sendMessage(Utils.getPrefix() + ChatColor.RESET + "Spawned a " + ChatColor.RED + "Red" + ChatColor.WHITE + " fox.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Failed to spawn fox, check console!");
                    }
                    break;
                case "snow":
                    try {
                        EntityTamableFox spawnedFox = plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.SNOW);
                        //plugin.getSpawnedFoxes().add(spawnedFox);
                        plugin.sqLiteSetterGetter.saveFox(spawnedFox);

                        player.sendMessage(Utils.getPrefix() + ChatColor.RESET + "Spawned a " + ChatColor.AQUA + "Snow" + ChatColor.WHITE + " fox.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(Utils.getPrefix() + ChatColor.RED + "Failed to spawn fox, check console!");
                    }
                    break;
                case "reload":
                    plugin.reloadConfig();
                    player.sendMessage(Utils.getPrefix() + ChatColor.GREEN + "Reloaded.");
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
