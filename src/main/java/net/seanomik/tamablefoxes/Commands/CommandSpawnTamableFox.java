package net.seanomik.tamablefoxes.Commands;

import net.minecraft.server.v1_14_R1.EntityFox;
import net.seanomik.tamablefoxes.Reference;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandSpawnTamableFox implements CommandExecutor {

    private TamableFoxes plugin = TamableFoxes.getPlugin(TamableFoxes.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            TamableFoxes plugin = TamableFoxes.getPlugin(TamableFoxes.class);

            if (player.hasPermission("tamableFoxes.spawntamablefox")) {
                if (args.length != 0) {
                    if (args[0].equals("snow")) {
                        plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.SNOW);
                        player.sendMessage("Spawned snow fox.");
                    } else if (args[0].equals("verbose")) {
                        player.sendMessage(TamableFoxes.foxUUIDs.toString());
                    } else if (args[0].equals("inspect")) {
                        ItemStack itemStack = new ItemStack(Material.REDSTONE_TORCH, 1);
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        List<String> lore = new LinkedList<>(Arrays.asList(
                                ChatColor.BLUE + "Tamable Fox Inspector"
                        ));

                        itemMeta.setLore(lore);
                        itemStack.setItemMeta(itemMeta);
                        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            player.getInventory().setItemInMainHand(itemStack);
                            player.sendMessage(Reference.CHAT_PREFIX + ChatColor.GREEN + "Given item.");
                        } else {
                            if (player.getInventory().firstEmpty() == -1) {
                                player.sendMessage(Reference.CHAT_PREFIX + ChatColor.RED + "Inventory is full!");
                            } else {
                                player.sendMessage(Reference.CHAT_PREFIX + ChatColor.GREEN + "Added item to inventory.");
                                player.getInventory().addItem(itemStack);
                            }
                        }

                        //player.sendMessage(TamableFoxes.foxUUIDs.toString());
                    }
                } else {
                    plugin.spawnTamableFox(player.getLocation(), EntityFox.Type.RED);
                    player.sendMessage("Spawned red fox.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have the permission for this command.");
            }
        }

        return true;
    }
}