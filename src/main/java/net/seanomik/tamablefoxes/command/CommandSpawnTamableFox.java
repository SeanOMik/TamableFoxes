package net.seanomik.tamablefoxes.command;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.minecraft.server.v1_15_R1.EntityFox;
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
                        EntityTamableFox fox = (EntityTamableFox) plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.RED);
                        plugin.getSpawnedFoxes().add(fox);
                        plugin.sqLiteSetterGetter.saveFox(fox);

                        player.sendMessage(plugin.getPrefix() + ChatColor.RESET + "Spawned a " + ChatColor.RED + "Red" + ChatColor.WHITE + " fox.");
                    } catch (Exception e) {
                        player.sendMessage(plugin.getPrefix() + ChatColor.RED + "Failed to spawn fox, check console!");
                    }
                    break;
                case "snow":
                    try {
                        EntityTamableFox spawnedFox = (EntityTamableFox) plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.SNOW);
                        plugin.getSpawnedFoxes().add(spawnedFox);
                        plugin.sqLiteSetterGetter.saveFox(spawnedFox);

                        player.sendMessage(plugin.getPrefix() + ChatColor.RESET + "Spawned a " + ChatColor.AQUA + "Snow" + ChatColor.WHITE + " fox.");
                    } catch (Exception e) {
                        player.sendMessage(plugin.getPrefix() + ChatColor.RED + "Failed to spawn fox, check console!");
                    }
                    break;
                case "verbose":
                    player.sendMessage(plugin.getFoxUUIDs().toString());
                    break;
                case "inspect":
                    ItemStack itemStack = new ItemStack(Material.REDSTONE_TORCH, 1);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    List<String> lore = Collections.singletonList(TamableFoxes.ITEM_INSPECTOR_LORE);
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);

                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        player.getInventory().setItemInMainHand(itemStack);
                        player.sendMessage(plugin.getPrefix() + ChatColor.GREEN + "Gave Inspector item.");
                    } else if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(plugin.getPrefix() + ChatColor.RED + "Your inventory is full!");
                    } else {
                        player.sendMessage(plugin.getPrefix() + ChatColor.GREEN + "Added item to inventory.");
                        player.getInventory().addItem(itemStack);
                    }
                    break;
                case "reload":
                    plugin.getMainConfig().reload();
                    //plugin.getConfigFoxes().reload();
                    player.sendMessage(plugin.getPrefix() + ChatColor.GREEN + "Reloaded.");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "/spawntamablefox " + ChatColor.GRAY + "[red | snow | verbose | inspect | reload]");
            }
        } else {
            player.sendMessage(ChatColor.RED + "/spawntamablefox " + ChatColor.GRAY + "[red | snow | verbose | inspect | reload]");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new LinkedList<>(Arrays.asList(
                "red",
                "snow",
                "verbose",
                "inspect",
                "reload"
        ));
    }
}
