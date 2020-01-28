package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.versions.version_1_15.command.CommandSpawnTamableFox;
import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// @TODO:

/* @CHANGELOG (1.5.2):
 *    Removed debug messages
 */
public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;
    public List<EntityTamableFox> spawnedFoxes = new ArrayList<>();

    private boolean versionSupported = true;

    @Override
    public void onLoad() {
        plugin = this;

        LanguageConfig.getConfig().saveDefault();

        String version = Bukkit.getServer().getClass().getPackage().getName();
        if (!version.equals("org.bukkit.craftbukkit.v1_15_R1")) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
            versionSupported = false;
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

            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
        }

    }

    @Override
    public void onEnable() {
        if (!versionSupported) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionDisable());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + LanguageConfig.getSavingFoxMessage());
        spawnedFoxes.forEach(EntityTamableFox::saveNbt);
    }

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        spawnedFoxes.forEach(EntityTamableFox::saveNbt);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, ()-> {
            spawnedFoxes.addAll(Utils.loadFoxesInChunk(event.getChunk()));
        }, 5L);
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

            // Check if its tamed but ignore it if the player is holding sweet berries for breeding or nametag for renaming
            if (tamableFox.isTamed() && tamableFox.getOwner() != null && itemHand.getType() != Material.SWEET_BERRIES && itemHand.getType() != Material.NAME_TAG) {
                if (tamableFox.getOwner().getUniqueID() == player.getUniqueId()) {
                    event.setCancelled(true);
                    if (player.isSneaking()) {
                        net.minecraft.server.v1_15_R1.ItemStack foxMouth = tamableFox.getEquipment(EnumItemSlot.MAINHAND);
                        if (!foxMouth.isEmpty()) tamableFox.dropMouthItem();
                        if (itemHand.getType() != Material.AIR) {
                            tamableFox.setMouthItem(itemHand);
                            if (itemHand.getAmount() == 1) player.getInventory().removeItem(itemHand);
                            else itemHand.setAmount(itemHand.getAmount() - 1);
                        }
                    } else {
                        tamableFox.toggleSitting();
                    }
                }
            } else if (itemHand.getType() == Material.CHICKEN && Config.canPlayerTameFox(player)) {
                if (Math.random() < 0.33D) { // tamed
                    tamableFox.setTamed(true);
                    tamableFox.setOwner(((CraftPlayer) player).getHandle());

                    player.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 6, 0.5D, 0.5D, 0.5D);


                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + LanguageConfig.getTamedMessage());

                    if (Config.askForNameAfterTaming()) {
                        player.sendMessage(ChatColor.RED + LanguageConfig.getTamingAskingName());
                        new AnvilGUI.Builder()
                                .onComplete((plr, text) -> { // Called when the inventory output slot is clicked
                                    if (!text.equals("")) {
                                        tamableFox.getBukkitEntity().setCustomName(text);
                                        tamableFox.setCustomNameVisible(true);
                                        plr.sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamingChosenPerfect(text));
                                        tamableFox.saveNbt();
                                    }

                                    return AnvilGUI.Response.close();
                                })
                                //.preventClose()      // Prevents the inventory from being closed
                                .text("Fox name")      // Sets the text the GUI should start with
                                .plugin(this)          // Set the plugin instance
                                .open(player);         // Opens the GUI for the player provided
                    }
                } else { // Tame failed
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 10, 0.3D, 0.3D, 0.3D, 0.15D);
                }

                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    if (itemHand.getAmount() == 1) player.getInventory().removeItem(itemHand);
                    else itemHand.setAmount(itemHand.getAmount() - 1);
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        List<EntityTamableFox> foxesOf = getFoxesOf(player);

        for (EntityTamableFox tamableFox : foxesOf) {
            if (player.getWorld().getTime() > 12541L && player.getWorld().getTime() < 23460L) {
                tamableFox.setSleeping(true);
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        List<EntityTamableFox> foxesOf = getFoxesOf(player);

        for (EntityTamableFox tamableFox : foxesOf) {
            tamableFox.setSleeping(false);
            if (tamableFox.isSitting()) {
                tamableFox.setSitting(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!Utils.isTamableFox(entity)) return; // Is the entity a tamable fox?
        // Remove the fox from storage
        spawnedFoxes.remove(entity);
        // Notify the owner
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();
        if (tamableFox.getOwner() != null) {
            Player owner = ((EntityPlayer) tamableFox.getOwner()).getBukkitEntity();
            owner.sendMessage(Utils.getPrefix() + ChatColor.RED + (tamableFox.hasCustomName() ? tamableFox.getBukkitEntity().getCustomName() : "Your fox") + " was killed!");
        }
    }

    public EntityTamableFox spawnTamableFox(Location loc, EntityFox.Type type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType(type);

        return tamableFox;
    }

    public List<EntityTamableFox> getFoxesOf(Player player) {
        return spawnedFoxes.stream().filter(fox -> fox.getOwner() != null && fox.getOwner().getUniqueID().equals(player.getUniqueId())).collect(Collectors.toList());
    }

    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
