package net.seanomik.tamablefoxes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import net.seanomik.tamablefoxes.command.CommandSpawnTamableFox;
import net.seanomik.tamablefoxes.io.FileManager;
import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.sqlite.SQLiteHandler;
import net.seanomik.tamablefoxes.sqlite.SQLiteSetterGetter;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

// @TODO: Add language.yml

public class TamableFoxes extends JavaPlugin implements Listener {

    public static final String ITEM_INSPECTOR_LORE = ChatColor.BLUE + "Tamable Fox Inspector";
    public static final String FOX_REGISTER_NAME = "tameablefox";

    private FileManager fileManager = new FileManager(this);

    private Map<UUID, UUID> foxUUIDs = Maps.newHashMap(); // FoxUUID, OwnerUUID
    private EntityTypes customType;
    private Map<UUID, Entity> lookupCache = Maps.newHashMap();
    private List<EntityTamableFox> spawnedFoxes = new ArrayList<>();
    private FileManager.Config config;//, configFoxes;
    public static SQLiteHandler sqLiteHandler = new SQLiteHandler();
    public static SQLiteSetterGetter sqLiteSetterGetter = new SQLiteSetterGetter();

    @Override
    public void onEnable() {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        if (!version.equals("org.bukkit.craftbukkit.v1_15_R1")) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "This plugin version only supports 1.15.1!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.config = fileManager.getConfig("config.yml");
        this.config.copyDefaults(true).save();

        sqLiteHandler.connect();
        sqLiteSetterGetter.createTablesIfNotExist();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        final Map<String, Type<?>> types = (Map<String, Type<?>>) DataConverterRegistry.a()
                .getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion()))
                .findChoiceType(DataConverterTypes.ENTITY).types();
        types.put("minecraft:" + FOX_REGISTER_NAME, types.get("minecraft:fox"));

        EntityTypes.a<net.minecraft.server.v1_15_R1.Entity> a = EntityTypes.a.a((entityTypes, world) ->
                new EntityTamableFox(this, entityTypes, world), EnumCreatureType.AMBIENT);
        customType = IRegistry.a(IRegistry.ENTITY_TYPE, FOX_REGISTER_NAME, a.a(FOX_REGISTER_NAME));

        this.replaceFoxesOnLoad();
    }

    @Override
    public void onDisable() {
        try {
            for (EntityTamableFox fox : spawnedFoxes) {
                sqLiteSetterGetter.saveFox(fox);

                Location loc = new Location(fox.getWorld().getWorld(), fox.locX(), fox.locY(), fox.locZ());
                loc.getChunk().load();
                fox.die();
            }

            getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.GREEN + "Saved all foxes successfully!");
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.RED + "Failed to save foxes!");
        }
    }

    private class SaveFoxRunnable extends BukkitRunnable {
        private final TamableFoxes plugin;

        SaveFoxRunnable(TamableFoxes plugin) {
            this.plugin = plugin;
        }

        public void run() {
            try {
                sqLiteSetterGetter.saveFoxes(spawnedFoxes);

                getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.GREEN + "Saved all foxes successfully!");
            } catch (Exception e) {
                getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.RED + "Failed to save foxes!");
            }
        }
    }

    private void replaceFoxesOnLoad() {
        //spawnedFoxes = sqLiteSetterGetter.spawnFoxes();
    }

    public net.minecraft.server.v1_15_R1.Entity spawnTamableFox(Location location, net.minecraft.server.v1_15_R1.EntityFox.Type type) {
        if (location.getChunk() != null) {
            location.getChunk().load();
        }

        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityTamableFox spawnedFox = (EntityTamableFox) customType.b(world, null, null, null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()), null, false, false);

        if (!world.addEntity(spawnedFox)) { // Show an error if the fox failed to spawn
            getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.RED + "Failed to spawn fox!");
        }

        spawnedFox.setFoxType(type);

        return spawnedFox;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) return;

        // Remove vanilla foxes
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Fox && !this.isTamableFox(entity)) {
                entity.remove();
            }
        }

        // Spawn saved foxes
        List<EntityTamableFox> newFoxes = sqLiteSetterGetter.spawnFoxesInChunk(event.getChunk());
        if (newFoxes != null && newFoxes.size() != 0) {
            spawnedFoxes.addAll(newFoxes);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (isTamableFox(entity)) {
                EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

                sqLiteSetterGetter.saveFox(tamableFox);
                tamableFox.die();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        getFoxesOf(player).forEach(uuid -> {
            EntityTamableFox tamableFox = getSpawnedFox(uuid);
            tamableFox.setOwner(((CraftPlayer) player).getHandle());
            tamableFox.setTamed(true);
            foxUUIDs.replace(tamableFox.getUniqueID(), player.getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) return;

        final ItemStack playerHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = playerHand.getItemMeta();

        if (itemMeta != null && playerHand.getType() == Material.REDSTONE_TORCH
                && itemMeta.hasLore() && itemMeta.getLore().contains(ITEM_INSPECTOR_LORE)) {
            List<String> lore;

            if (!this.isTamableFox(entity)) {
                lore = Arrays.asList(ITEM_INSPECTOR_LORE,
                        "UUID: " + entity.getUniqueId(),
                        "Entity ID: " + entity.getEntityId(),
                        "Entity type: " + ((CraftEntity) entity).getHandle());
            } else {
                EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

                lore = Arrays.asList(ITEM_INSPECTOR_LORE,
                        "UUID: " + entity.getUniqueId(),
                        "Entity ID: " + entity.getEntityId(),
                        "Tamable",
                        "Owner: " + ((tamableFox.getOwner() == null) ? "none" : tamableFox.getOwner().getName()));
            }

            // Update item
            itemMeta.setLore(lore);
            playerHand.setItemMeta(itemMeta);
            player.getInventory().setItemInMainHand(playerHand);

            event.setCancelled(true);
            player.sendMessage(getPrefix() + "Inspected Entity.");
            return;
        }

        if (this.isTamableFox(entity)) {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

            if (tamableFox.isTamed() && tamableFox.getOwner() != null &&
                    playerHand.getType() != Material.SWEET_BERRIES) {
                // if this fox is theirs
                if (foxUUIDs.get(entity.getUniqueId()).equals(player.getUniqueId())) {
                    if (player.isSneaking()) {
                        net.minecraft.server.v1_15_R1.ItemStack wolfHolding = tamableFox.getEquipment(EnumItemSlot.MAINHAND);
                        net.minecraft.server.v1_15_R1.ItemStack playerItemInHandNMS;
                        if (wolfHolding.isEmpty()) {
                            if (playerHand.getType() == Material.AIR) {
                                return;
                            }

                            playerItemInHandNMS = CraftItemStack.asNMSCopy(playerHand);
                            playerItemInHandNMS.setCount(1);
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, playerItemInHandNMS);
                            playerHand.setAmount(playerHand.getAmount() - 1);
                            player.getInventory().setItemInMainHand(playerHand);
                        } else if (playerHand.getType() == Material.AIR) {
                            entity.getWorld().dropItem(tamableFox.getBukkitEntity().getLocation().add(0.0D, 0.2D, 0.0D),
                                    CraftItemStack.asBukkitCopy(wolfHolding));
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));
                        } else {
                            playerItemInHandNMS = CraftItemStack.asNMSCopy(playerHand);
                            entity.getWorld().dropItem(tamableFox.getBukkitEntity().getLocation().add(0.0D, 0.2D, 0.0D),
                                    CraftItemStack.asBukkitCopy(wolfHolding));
                            playerItemInHandNMS.setCount(1);
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, playerItemInHandNMS);
                            playerHand.setAmount(playerHand.getAmount() - 1);
                            player.getInventory().setItemInMainHand(playerHand);
                        }
                    }  else if (playerHand.getType() == Material.NAME_TAG) {
                        ItemMeta handMeta = playerHand.getItemMeta();
                        tamableFox.setChosenName(handMeta.getDisplayName());
                    } else {
                        tamableFox.toggleSitting();
                    }

                    event.setCancelled(true);
                }
            } else if (playerHand.getType() == Material.CHICKEN) {
                if (Math.random() < 0.33D) {
                    tamableFox.setTamed(true);
                    tamableFox.setOwner(((CraftPlayer) player).getHandle());
                    foxUUIDs.replace(tamableFox.getUniqueID(), null, player.getUniqueId());
                    player.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 6, 0.5D, 0.5D, 0.5D);

                    // Name process
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You just tamed a wild fox!");
                    player.sendMessage(ChatColor.RED + "What do you want to call it?");
                    tamableFox.setChosenName("???");

                    event.setCancelled(true);
                    new AnvilGUI.Builder()
                            .onComplete((plr, text) -> { // Called when the inventory output slot is clicked
                                if(!text.equals("")) {
                                    tamableFox.setChosenName(text);
                                    plr.sendMessage(getPrefix() + ChatColor.GREEN + text + " is perfect!");
                                }

                                return AnvilGUI.Response.close();
                            })
                            //.preventClose()        // Prevents the inventory from being closed
                            .text("Fox name")      // Sets the text the GUI should start with
                            .plugin(this)          // Set the plugin instance
                            .open(player);         // Opens the GUI for the player provided

                } else { // Failed to tame
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 10, 0.3D, 0.3D, 0.3D, 0.15D);
                }

                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    playerHand.setAmount(playerHand.getAmount() - 1);
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        getFoxesOf(player).forEach(uuid -> {
            EntityTamableFox tamableFox = getSpawnedFox(uuid);
            if (player.getWorld().getTime() > 12541L && player.getWorld().getTime() < 23460L) {
                tamableFox.setSleeping(true);
            } else return; // Stop the loop, no point of iterating through all the foxes if the condition will be the same.
        });
    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();

        getFoxesOf(player).forEach(uuid -> {
            EntityTamableFox tamableFox = getSpawnedFox(uuid);
            tamableFox.setSleeping(false);
        });
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        org.bukkit.entity.Entity entity = event.getEntity();
        if (entity instanceof Fox && !this.isTamableFox(entity)) {
            net.minecraft.server.v1_15_R1.EntityFox.Type foxType = ((EntityFox) ((CraftEntity) entity).getHandle()).getFoxType();
            spawnTamableFox(entity.getLocation(), foxType);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!this.isTamableFox(entity)) return; // Is the entity a tamable fox?

        // Remove the fox from storage
        lookupCache.remove(entity.getUniqueId());
        foxUUIDs.remove(entity.getUniqueId());
        spawnedFoxes.remove(entity.getUniqueId());

        // Notify the owner
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();
        if (tamableFox.getOwner() != null) {
            Player owner = ((EntityPlayer)tamableFox.getOwner()).getBukkitEntity();
            owner.sendMessage(getPrefix() + ChatColor.RED + tamableFox.getChosenName() + " was killed!");
        }

        // Remove the fox from database
        sqLiteSetterGetter.removeFox(tamableFox);
    }

    public EntityTamableFox getSpawnedFox(UUID uniqueId) {
        for (EntityTamableFox fox : spawnedFoxes) {
            if (fox.getUniqueID() == uniqueId) {
                return fox;
            }
        }

        return null;
    }

    public boolean isTamableFox(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getClass().getName().contains("TamableFox") || ((CraftEntity) entity).getHandle() instanceof EntityTamableFox;
    }

    public List<UUID> getFoxesOf(Player player) {
        return foxUUIDs.entrySet().stream().filter(foxPlayer -> foxPlayer.getValue() != null && foxPlayer.getValue().equals(player.getUniqueId())).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public FileManager.Config getMainConfig() {
        return config;
    }

    public EntityTypes getCustomType() {
        return customType;
    }

    public Map<UUID, UUID> getFoxUUIDs() {
        return foxUUIDs;
    }

    public List<EntityTamableFox> getSpawnedFoxes() {
        return spawnedFoxes;
    }

    public static String getPrefix() {
        //return ChatColor.translateAlternateColorCodes('&', (String) config.get("prefix"));
        return ChatColor.RED + "[Tamable Foxes] ";
    }

    public boolean isShowOwnerFoxName() {
        return (boolean) config.get("show-owner-in-fox-name");
    }

    public boolean isShowNameTags() {
        return (boolean) config.get("show-nametags");
    }

    public boolean isTamedAttackRabbitChicken() {
        return (boolean) config.get("tamed-behavior.attack-chicken-rabbit");
    }

}
