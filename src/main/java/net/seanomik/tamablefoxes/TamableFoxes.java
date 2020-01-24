package net.seanomik.tamablefoxes;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.seanomik.tamablefoxes.command.CommandSpawnTamableFox;
import net.seanomik.tamablefoxes.sqlite.SQLiteHandler;
import net.seanomik.tamablefoxes.sqlite.SQLiteSetterGetter;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// @TODO: Add language.yml
//        Foxes will be loaded in as sitting, even if they weren't when the server was shutdown
//         Add fox sleeping when the player sleeps
public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;
    public List<EntityTamableFox> spawnedFoxes = new ArrayList<>();

    public SQLiteSetterGetter sqLiteSetterGetter = new SQLiteSetterGetter();
    public SQLiteHandler sqLiteHandler = new SQLiteHandler();

    @Override
    public void onLoad() {
        String version = Bukkit.getServer().getClass().getPackage().getName();

        if (!version.equals("org.bukkit.craftbukkit.v1_15_R1")) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + "This plugin version only supports 1.15.1! Not registering entity!");
            return;
        }

        try { // Replace the fox entity
            Field field = EntityTypes.FOX.getClass().getDeclaredField("ba");
            field.setAccessible(true);

            // Remove the final modifier from the "ba" variable
            Field fieldMutable = field.getClass().getDeclaredField("modifiers");
            fieldMutable.setAccessible(true);
            fieldMutable.set(field, fieldMutable.getInt(field) & ~Modifier.FINAL);
            fieldMutable.setAccessible(false);

            field.set(EntityTypes.FOX, (EntityTypes.b<EntityFox>) (type, world) -> new EntityTamableFox(type, world));

            field.setAccessible(false);

            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.GREEN + "Replaced tamable fox entity!");
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + "Failed to replace tamable fox entity!");
        }

    }

    @Override
    public void onEnable() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        if (!version.equals("org.bukkit.craftbukkit.v1_15_R1")) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + "This plugin version only supports 1.15.1! Disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        sqLiteSetterGetter.createTablesIfNotExist();
        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        spawnedFoxes = sqLiteSetterGetter.loadFoxes();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + "Saving foxes.");
        sqLiteSetterGetter.saveFoxes(spawnedFoxes);
    }

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        sqLiteSetterGetter.saveFoxes(spawnedFoxes);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (EntityTamableFox fox : getFoxesOf(player)) {
            fox.setOwner((EntityLiving) ((CraftEntity) player).getHandle());
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (Utils.isTamableFox(entity)) {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

            spawnedFoxes.add(tamableFox);
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack itemHand = player.getInventory().getItemInMainHand();
        ItemMeta handMeta =  itemHand.getItemMeta();

        // Checks if the entity is EntityTamableFox and that the player is allowed to tame foxes
        if (Utils.isTamableFox(entity)) {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

            // Check if its tamed but ignore it if the player is holding sweet berries for breeding
            if (tamableFox.isTamed() && tamableFox.getOwner() != null && itemHand.getType() != Material.SWEET_BERRIES) {
                if (tamableFox.getOwner().getUniqueID() == player.getUniqueId()) {
                    if (player.isSneaking()) {
                        net.minecraft.server.v1_15_R1.ItemStack foxMouth = tamableFox.getEquipment(EnumItemSlot.MAINHAND);

                        if (foxMouth.isEmpty() && itemHand.getType() != Material.AIR) { // Giving an item
                            tamableFox.setMouthItem(itemHand);
                            itemHand.setAmount(itemHand.getAmount() - 1);
                        } else if (!foxMouth.isEmpty() && itemHand.getType() == Material.AIR) { // Taking the item
                            tamableFox.dropMouthItem();
                        } else if (!foxMouth.isEmpty() && itemHand.getType() != Material.AIR){ // Swapping items
                            // Drop item
                            tamableFox.dropMouthItem();

                            // Give item and take one away from player
                            tamableFox.setMouthItem(itemHand);
                            itemHand.setAmount(itemHand.getAmount() - 1);
                        }
                    } else if (itemHand.getType() == Material.NAME_TAG) {
                        tamableFox.setChosenName(handMeta.getDisplayName());
                    } else {
                        tamableFox.toggleSitting();
                    }

                    event.setCancelled(true);
                }
            } else if (itemHand.getType() == Material.CHICKEN && Config.canPlayerTameFox(player)) {
                if (Math.random() < 0.33D) { // tamed
                    tamableFox.setTamed(true);
                    tamableFox.setOwner(((CraftPlayer) player).getHandle());
                    // store uuid
                    player.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 6, 0.5D, 0.5D, 0.5D);

                    // Name fox
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You just tamed a wild fox!");
                    player.sendMessage(ChatColor.RED + "What do you want to call it?");
                    tamableFox.setChosenName("???");

                    //TamableFoxes.getPlugin().sqLiteSetterGetter.saveFox(tamableFox);

                    event.setCancelled(true);
                    new AnvilGUI.Builder()
                            .onComplete((plr, text) -> { // Called when the inventory output slot is clicked
                                if(!text.equals("")) {
                                    tamableFox.setChosenName(text);
                                    plr.sendMessage(Utils.getPrefix() + ChatColor.GREEN + text + " is perfect!");

                                    TamableFoxes.getPlugin().sqLiteSetterGetter.saveFox(tamableFox);
                                }

                                return AnvilGUI.Response.close();
                            })
                            //.preventClose()      // Prevents the inventory from being closed
                            .text("Fox name")      // Sets the text the GUI should start with
                            .plugin(this)          // Set the plugin instance
                            .open(player);         // Opens the GUI for the player provided
                } else { // Tame failed
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 10, 0.3D, 0.3D, 0.3D, 0.15D);
                }

                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    itemHand.setAmount(itemHand.getAmount() - 1);
                }

                event.setCancelled(true);
            }
        }
    }

    public EntityTamableFox spawnTamableFox(Location loc, EntityFox.Type type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType(type);

        return tamableFox;
    }

    public List<EntityTamableFox> getFoxesOf(Player player) {
        return spawnedFoxes.stream().filter(fox -> fox.getOwnerUUID() != null && fox.getOwnerUUID().equals(player.getUniqueId())).collect(Collectors.toList());
    }

    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
