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
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftFox;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// @TODO: Add language.yml

public class TamableFoxes extends JavaPlugin implements Listener {

    public static final String ITEM_INSPECTOR_LORE = ChatColor.BLUE + "Tamable Fox Inspector";
    public static final String TAG_TAME_FOX = "tameablefox";

    private FileManager fileManager = new FileManager(this);

    private Map<UUID, UUID> foxUUIDs = Maps.newHashMap(); // FoxUUID, OwnerUUID
    private EntityTypes customType;
    private boolean isOnLoad = true;
    private Map<UUID, Entity> lookupCache = Maps.newHashMap();
    private List<EntityTamableFox> spawnedFoxes;
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

        sqLiteHandler.connect();
        sqLiteSetterGetter.createTablesIfNotExist();

        this.config = fileManager.getConfig("config.yml");
        this.config.copyDefaults(true).save();
        /*this.configFoxes = fileManager.getConfig("foxes.yml");
        this.configFoxes.copyDefaults(true).save();*/

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        final Map<String, Type<?>> types = (Map<String, Type<?>>) DataConverterRegistry.a()
                .getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion()))
                .findChoiceType(DataConverterTypes.ENTITY).types();
        types.put("minecraft:" + TAG_TAME_FOX, types.get("minecraft:fox"));

        EntityTypes.a<net.minecraft.server.v1_15_R1.Entity> a = EntityTypes.a.a((entityTypes, world) ->
                new EntityTamableFox(this, entityTypes, world), EnumCreatureType.AMBIENT);
        customType = IRegistry.a(IRegistry.ENTITY_TYPE, "tameablefox", a.a("tameablefox"));

        this.replaceFoxesOnLoad();
    }

    @Override
    public void onDisable() {
        try {
            for (EntityTamableFox fox : spawnedFoxes) {
                sqLiteSetterGetter.saveFox(fox);

                fox.getBukkitEntity().remove();
            }

            getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.GREEN + "Saved all foxes successfully!");
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage(getPrefix() + ChatColor.RED + "Failed to save foxes!");
        }
    }

    private void replaceFoxesOnLoad() {
        /*int amountReplaced = 0;

        for (World world : Bukkit.getWorlds()) {
            Chunk[] loadedChunks = world.getLoadedChunks();
            for (Chunk chunk : loadedChunks) {
                Entity[] entities = chunk.getEntities();
                for (Entity entity : entities) {
                    if (!(entity instanceof Fox))
                        continue;
                    if (this.isTamableFox(entity))
                        continue;
                    EntityTamableFox tamableFox = (EntityTamableFox) spawnTamableFox(entity.getLocation(), ((CraftFox) entity).getHandle().getFoxType());

                    //final YamlConfiguration configuration = configFoxes.get();
                    // get living entity data
                    if (configuration.isConfigurationSection("Foxes." + entity.getUniqueId())) {
                        String owner = configuration.getString("Foxes." + entity.getUniqueId() + ".owner");

                        // make new data
                        if (owner.equals("none")) {
                            foxUUIDs.replace(tamableFox.getUniqueID(), null);
                            configuration.set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");
                        } else {
                            foxUUIDs.replace(tamableFox.getUniqueID(), UUID.fromString(owner));
                            tamableFox.setTamed(true);
                            configuration.set("Foxes." + tamableFox.getUniqueID() + ".owner", owner);
                        }

                        // set name
                        if (configuration.isSet("Foxes." + entity.getUniqueId() + ".name")) {
                            final String name = configuration.getString("Foxes." + entity.getUniqueId() + ".name");
                            configuration.set("Foxes." + tamableFox.getUniqueID() + ".name", name);
                            tamableFox.setChosenName(name);
                        }

                        // delete old data
                        configuration.set("Foxes." + entity.getUniqueId(), null);

                        tamableFox.setSitting(((EntityFox) ((CraftEntity) entity).getHandle()).isSitting());
                        tamableFox.updateFox();
                        tamableFox.setAge(((CraftFox) entity).getAge());
                        ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        entityMouthItem.setAmount(1);

                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(entityMouthItem));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));
                        }

                    } else {
                        configuration.set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");

                        tamableFox.setAge(((CraftFox) entity).getAge());
                        ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        entityMouthItem.setAmount(1);

                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(entityMouthItem));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));
                        }
                    }

                    entity.remove();
                    ++amountReplaced;
                }
            }
        }

        configFoxes.save();*/

        spawnedFoxes = sqLiteSetterGetter.spawnFoxes();
        this.isOnLoad = false;
    }

    public net.minecraft.server.v1_15_R1.Entity spawnTamableFox(Location location, net.minecraft.server.v1_15_R1.EntityFox.Type type) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        net.minecraft.server.v1_15_R1.Entity spawnedEntity = customType.b(world, null, null, null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()), null, false, false);

        world.addEntity(spawnedEntity);
        EntityFox fox = (EntityFox) spawnedEntity;
        fox.setFoxType(type);

        /*configFoxes.get().set("Foxes." + spawnedEntity.getUniqueID() + ".owner", "none");
        fileManager.saveConfig("foxes.yml");*/

        return fox;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        /*if (isOnLoad)
            return;
        Chunk chunk = event.getChunk();
        Entity[] entities = chunk.getEntities();

        for (Entity entity : entities) {
            if (entity instanceof Fox && !this.isTamableFox(entity)) {
                EntityTamableFox tamableFox = (EntityTamableFox) spawnTamableFox(entity.getLocation(), ((CraftFox) entity).getHandle().getFoxType());
                final YamlConfiguration configuration = configFoxes.get();
                // if has data
                if (configuration.isConfigurationSection("Foxes." + entity.getUniqueId())) {
                    String owner = configuration.getString("Foxes." + entity.getUniqueId() + ".owner", "none");

                    // if has owner
                    if (!owner.equals("none")) {
                        foxUUIDs.replace(tamableFox.getUniqueID(), UUID.fromString(owner));
                        configuration.set("Foxes." + tamableFox.getUniqueID() + ".owner", owner);

                        // set name
                        if (configuration.isSet("Foxes." + entity.getUniqueId() + ".name")) {
                            String name = configuration.getString("Foxes." + entity.getUniqueId() + ".name");

                            configuration.set("Foxes." + tamableFox.getUniqueID() + ".name", name);
                            tamableFox.setChosenName(name);
                        }

                        // remove old data
                        configuration.set("Foxes." + entity.getUniqueId(), null);

                        tamableFox.setTamed(true);
                        tamableFox.setSitting(((EntityFox) ((CraftEntity) entity).getHandle()).isSitting());
                        tamableFox.updateFox();
                        tamableFox.setAge(((CraftFox) entity).getAge());
                        ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(entityMouthItem.getType(), 1)));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));
                        }
                    }
                } else {
                    configuration.set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");

                    tamableFox.setAge(((CraftFox) entity).getAge());
                    ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                    if (entityMouthItem.getType() != Material.AIR) {
                        tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(entityMouthItem.getType(), 1)));
                    } else {
                        tamableFox.setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));
                    }
                }

                entity.remove();
            }
        }*/

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        getFoxesOf(player).forEach(uuid -> {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) this.getEntityByUniqueId(uuid)).getHandle();
            tamableFox.setOwner(((CraftPlayer) player).getHandle());
            tamableFox.setTamed(true);
            foxUUIDs.replace(tamableFox.getUniqueID(), player.getUniqueId());
        });
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        final ItemStack playerHand = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = playerHand.getItemMeta();

        if (itemMeta != null && playerHand.getType() == Material.REDSTONE_TORCH
                && itemMeta.hasLore() && itemMeta.getLore().contains(ITEM_INSPECTOR_LORE)) {
            List<String> lore;

            if (!this.isTamableFox(entity)) {
                lore = Arrays.asList(ITEM_INSPECTOR_LORE,
                        "UUID: " + entity.getUniqueId(),
                        "Entity ID: " + entity.getEntityId());
            } else {
                EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

                if (tamableFox.getOwner() == null) {
                    lore = Arrays.asList(ITEM_INSPECTOR_LORE,
                            "UUID: " + entity.getUniqueId(),
                            "Entity ID: " + entity.getEntityId(),
                            "Tamable",
                            "Owner: None");
                } else {
                    lore = Arrays.asList(ITEM_INSPECTOR_LORE,
                            "UUID: " + entity.getUniqueId(),
                            "Entity ID: " + entity.getEntityId(),
                            "Tamable",
                            "Owner: " + tamableFox.getOwner().getName());
                }
            }

            // update item
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

                } else {
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
        List<UUID> dogsToSleep = getFoxesOf(player);

        for (UUID uuid : dogsToSleep) {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftFox) this.getEntityByUniqueId(uuid)).getHandle();
            if (player.getWorld().getTime() > 12541L && player.getWorld().getTime() < 23460L) {
                tamableFox.setSleeping(true);
            }
        }

    }

    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        final List<UUID> foxesOf = getFoxesOf(player);

        for (UUID uuid : foxesOf) {
            EntityTamableFox tamableFox = (EntityTamableFox) ((CraftFox) this.getEntityByUniqueId(uuid)).getHandle();
            tamableFox.setSleeping(false);
        }

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
        if (!this.isTamableFox(entity)) {

            return;
        }

        // Remove the fox from storage
        lookupCache.remove(entity.getUniqueId());
        foxUUIDs.remove(entity.getUniqueId());
        spawnedFoxes.remove(entity.getUniqueId());

        //sqLiteSetterGetter.removeFox();


        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();

        if (tamableFox.getOwner() != null) {
            Player owner = ((EntityPlayer)tamableFox.getOwner()).getBukkitEntity();
            owner.sendMessage(getPrefix() + ChatColor.RED + tamableFox.getChosenName() + " was killed!");
        }

        sqLiteSetterGetter.removeFox(tamableFox);

        /*if (configFoxes.get().getConfigurationSection("Foxes").contains(entity.getUniqueId().toString())) {
            *//*EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) entity).getHandle();
            if (tamableFox.getOwner() != null && tamableFox.getOwner() instanceof Player) {
                Player owner = (Player) tamableFox.getOwner();
                owner.sendMessage(getPrefix() + ChatColor.RED + tamableFox.getChosenName() + " was killed!");
            }*//*

            configFoxes.get().set("Foxes." + entity.getUniqueId(), null);
            fileManager.saveConfig("foxes.yml");
        }*/
    }

    public Entity getEntityByUniqueId(UUID uniqueId) {
        final Entity cacheEntity = lookupCache.get(uniqueId);
        if (cacheEntity != null) {
            if (cacheEntity.isDead())
                lookupCache.remove(uniqueId);
            else return cacheEntity;
        }

        for (World world : Bukkit.getWorlds()) {
            for (Chunk loadedChunk : world.getLoadedChunks()) {
                for (Entity entity : loadedChunk.getEntities()) {
                    if (entity.getUniqueId().equals(uniqueId)) {
                        this.lookupCache.put(uniqueId, entity);
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    public boolean isTamableFox(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getClass().getName().contains("TamableFox")
                || ((CraftEntity) entity).getHandle() instanceof EntityTamableFox;
    }

    public List<UUID> getFoxesOf(Player player) {
        return foxUUIDs.entrySet().stream().filter(foxPlayer -> foxPlayer.getValue() != null && foxPlayer.getValue().equals(player.getUniqueId()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public FileManager.Config getMainConfig() {
        return config;
    }

    /*public FileManager.Config getConfigFoxes() {
        return configFoxes;
    }*/

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
        return (boolean) config.get("tamed-behavior.no-attack-chicken-rabbit");
    }

}
