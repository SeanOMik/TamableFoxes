package net.seanomik.tamablefoxes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.sun.istack.internal.NotNull;
import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.Commands.CommandSpawnTamableFox;
import net.seanomik.tamablefoxes.Utils.FileManager;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftFox;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public final class TamableFoxes extends JavaPlugin implements Listener {

    public static Map<UUID, UUID> foxUUIDs = new HashMap<>(); // FoxUUID, OwnerUUID
    public static EntityTypes customType;
    public static JavaPlugin plugin;
    private boolean isOnLoad = true;
    private File dataFolder = null;

    public static FileManager fileManager;

    // TODO:
    //      Fix the fox moving when you make it sit while it was moving.

    @Override
    public void onEnable() {
        if (!Bukkit.getVersion().contains("1.14.4")) {
            Bukkit.getConsoleSender().sendMessage(Reference.CHAT_PREFIX + ChatColor.RED + "THIS PLUGIN WILL ONLY RUN ON MC SPIGOT 1.14.4!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        fileManager = new FileManager(this);
        fileManager.getConfig("foxes.yml").copyDefaults(true).save();
        fileManager.getConfig("config.yml").copyDefaults(true).save();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox());

        plugin = getPlugin(this.getClass());
        dataFolder = getDataFolder();

        // Registering Fox
        Map<String, Type<?>> types = (Map<String, Type<?>>) DataConverterRegistry.a().getSchema(DataFixUtils.makeKey(SharedConstants.a().getWorldVersion())).findChoiceType(DataConverterTypes.ENTITY).types();
        types.put("minecraft:" + Reference.CUSTOM_FOX_REGISTER_NAME, types.get("minecraft:fox"));
        EntityTypes.a<net.minecraft.server.v1_14_R1.Entity> a = EntityTypes.a.a(TamableFox::new, EnumCreatureType.AMBIENT);
        customType = IRegistry.a(IRegistry.ENTITY_TYPE, Reference.CUSTOM_FOX_REGISTER_NAME, a.a(Reference.CUSTOM_FOX_REGISTER_NAME));

        // Spawn all foxes
        replaceFoxesOnLoad();
    }

    private void replaceFoxesOnLoad() {
        int amountReplaced = 0;
        World world = Bukkit.getWorlds().get(0);
        for (Chunk chunk : world.getLoadedChunks()) {
            for (Entity entity : chunk.getEntities()) {
                if(entity instanceof Fox) {
                    if (isTamableFox(entity)) {
                        continue;
                    }
                    TamableFox tamableFox = (TamableFox) spawnTamableFox(entity.getLocation(), ((CraftFox) entity).getHandle().getFoxType());

                    if (fileManager.getConfig("foxes.yml").get().getString("Foxes." + entity.getUniqueId()) != null) {
                        Bukkit.broadcastMessage("NOT NULL");
                        String owner = fileManager.getConfig("foxes.yml").get().getString("Foxes." + entity.getUniqueId() + ".owner");

                        fileManager.getConfig("foxes.yml").get().set("Foxes." + entity.getUniqueId(), null);
                        if (owner.equals("none")) {
                            foxUUIDs.replace(tamableFox.getUniqueID(), null);
                            fileManager.getConfig("foxes.yml").get().set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");
                        } else {
                            foxUUIDs.replace(tamableFox.getUniqueID(), UUID.fromString(owner));
                            tamableFox.setTamed(true);
                            fileManager.getConfig("foxes.yml").get().set("Foxes." + tamableFox.getUniqueID() + ".owner", owner);
                        }

                        tamableFox.setSitting(((EntityFox) ((CraftEntity) entity).getHandle()).isSitting());
                        tamableFox.updateFox();

                        tamableFox.setAge(((CraftFox) entity).getAge());

                        org.bukkit.inventory.ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entityMouthItem.getType(), 1)));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                        }
                    } else {
                        fileManager.getConfig("foxes.yml").get().set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");
                        tamableFox.setAge(((CraftFox) entity).getAge());

                        org.bukkit.inventory.ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entityMouthItem.getType(), 1)));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                        }
                    }

                    entity.remove();
                    amountReplaced++;
                }
            }
        }

        //Bukkit.getConsoleSender().sendMessage(Reference.CHAT_PREFIX + "Respawned " + amountReplaced + " foxes.");
        fileManager.saveConfig("foxes.yml");
        isOnLoad = false;
    }

    public static net.minecraft.server.v1_14_R1.Entity spawnTamableFox(Location location, EntityFox.Type type) {
        WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        net.minecraft.server.v1_14_R1.Entity spawnedEntity = customType.b(world,
                null,
                null,
                null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()),
                null, false, false);
        world.addEntity(spawnedEntity);

        EntityFox fox = (EntityFox) spawnedEntity;
        fox.setFoxType(type);
        fileManager.getConfig("foxes.yml").get().set("Foxes." + spawnedEntity.getUniqueID() + ".owner", "none");
        fileManager.saveConfig("foxes.yml");
        return fox;
    }

    @EventHandler
    public void onChunkLoad (ChunkLoadEvent event) {
        if (!isOnLoad) {
            Chunk chunk = event.getChunk();
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Fox) {
                    if (isTamableFox(entity)) {
                        continue;
                    }
                    TamableFox tamableFox = (TamableFox) spawnTamableFox(entity.getLocation(), ((CraftFox) entity).getHandle().getFoxType());

                    if (fileManager.getConfig("foxes.yml").get().getString("Foxes." + entity.getUniqueId()) != null) {
                        String owner = fileManager.getConfig("foxes.yml").get().getString("Foxes." + entity.getUniqueId() + ".owner");
                        if (!owner.equals("none")) {
                            foxUUIDs.replace(tamableFox.getUniqueID(), UUID.fromString(owner));
                            fileManager.getConfig("foxes.yml").get().set("Foxes." + tamableFox.getUniqueID() + ".owner", owner);
                            fileManager.getConfig("foxes.yml").get().set("Foxes." + entity.getUniqueId(), null);
                            tamableFox.setTamed(true);
                            tamableFox.setSitting(((EntityFox) ((CraftEntity) entity).getHandle()).isSitting());
                            tamableFox.updateFox();

                            tamableFox.setAge(((CraftFox) entity).getAge());

                            org.bukkit.inventory.ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                            if (entityMouthItem.getType() != Material.AIR) {
                                tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entityMouthItem.getType(), 1)));
                            } else {
                                tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                            }
                        }
                    } else {
                        fileManager.getConfig("foxes.yml").get().set("Foxes." + tamableFox.getUniqueID() + ".owner", "none");
                        tamableFox.setAge(((CraftFox) entity).getAge());

                        org.bukkit.inventory.ItemStack entityMouthItem = ((CraftFox) entity).getEquipment().getItemInMainHand();
                        if (entityMouthItem.getType() != Material.AIR) {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(entityMouthItem.getType(), 1)));
                        } else {
                            tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                        }
                    }

                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (foxUUIDs.containsValue(player.getUniqueId())) {
            for (Map.Entry<UUID, UUID> entry : foxUUIDs.entrySet()) {
                if (entry.getValue() != null && entry.getValue().equals(player.getUniqueId())) {
                    TamableFox tamableFox = (TamableFox) ((CraftEntity) getEntityByUniqueId(entry.getKey())).getHandle();
                    tamableFox.setOwner(((CraftPlayer) player).getHandle());
                    tamableFox.setTamed(true);
                    foxUUIDs.replace(tamableFox.getUniqueID(), player.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (event.getHand().equals(EquipmentSlot.HAND)) {
            ItemMeta itemMeta = player.getInventory().getItemInMainHand().getItemMeta();
            if (player.getInventory().getItemInMainHand().getType() == Material.REDSTONE_TORCH && !isTamableFox(entity) && itemMeta.getLore().contains(ChatColor.BLUE + "Tamable Fox Inspector")) {
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta itemMeta1 = item.getItemMeta();
                List<String> lore = new LinkedList<>(Arrays.asList(
                        ChatColor.BLUE + "Tamable Fox Inspector",
                        "UUID: " + entity.getUniqueId(),
                        "Entity ID: " + entity.getEntityId()
                ));
                itemMeta1.setLore(lore);
                item.setItemMeta(itemMeta1);
                player.sendMessage("Inspected Entity.");
            }

            if (isTamableFox(entity)) {
                TamableFox tamableFox = (TamableFox) ((CraftEntity)entity).getHandle();
                if (player.getInventory().getItemInMainHand().getType() == Material.REDSTONE_TORCH && itemMeta.getLore().contains(ChatColor.BLUE + "Tamable Fox Inspector")) {
                    org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                    List<String> lore = new LinkedList<>();
                    if (tamableFox.getOwner() == null) {
                        lore = new LinkedList<>(Arrays.asList(
                                ChatColor.BLUE + "Tamable Fox Inspector",
                                "UUID: " + entity.getUniqueId(),
                                "Entity ID: " + entity.getEntityId(),
                                "Tamable",
                                "Owner: None"
                        ));
                    } else {
                        lore = new LinkedList<>(Arrays.asList(
                                ChatColor.BLUE + "Tamable Fox Inspector",
                                "UUID: " + entity.getUniqueId(),
                                "Entity ID: " + entity.getEntityId(),
                                "Tamable",
                                "Owner: " + tamableFox.getOwner().getName()
                        ));
                    }

                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);

                    event.setCancelled(true);
                    return;
                }

                if (tamableFox.isTamed() && tamableFox.getOwner() != null && player.getInventory().getItemInMainHand().getType() != Material.SWEET_BERRIES) {
                    if (player.getUniqueId() == foxUUIDs.get(entity.getUniqueId())) {
                        if (player.isSneaking()) { // Shift right click to add items
                            ItemStack itemstack = tamableFox.getEquipment(EnumItemSlot.MAINHAND);
                            if (itemstack.isEmpty()) {
                                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                    return;
                                }
                                net.minecraft.server.v1_14_R1.ItemStack playerItemInHandNMS = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());

                                playerItemInHandNMS.setCount(1);

                                // Set foxes mouth
                                tamableFox.setSlot(EnumItemSlot.MAINHAND, playerItemInHandNMS);

                                // Take item from player
                                org.bukkit.inventory.ItemStack playerItemInHand = player.getInventory().getItemInMainHand();
                                playerItemInHand.setAmount(playerItemInHand.getAmount() - 1);
                                player.getInventory().setItemInMainHand(playerItemInHand);
                            } else {
                                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) { //
                                    // Drop the item in the foxes mouth on the floor
                                    World world = Bukkit.getWorlds().get(0);
                                    world.dropItem(tamableFox.getBukkitEntity().getLocation().add(0D, 0.2D, 0D), CraftItemStack.asBukkitCopy(itemstack));

                                    // Remove the item from mouth
                                    tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                                } else { // Replace items
                                    net.minecraft.server.v1_14_R1.ItemStack playerItemInHandNMS = CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand());

                                    // Drop the item in the foxes mouth on the floor
                                    World world = Bukkit.getWorlds().get(0);
                                    world.dropItem(tamableFox.getBukkitEntity().getLocation().add(0D, 0.2D, 0D), CraftItemStack.asBukkitCopy(itemstack));

                                    playerItemInHandNMS.setCount(1);

                                    // Set foxes mouth
                                    tamableFox.setSlot(EnumItemSlot.MAINHAND, playerItemInHandNMS);

                                    // Take item from player
                                    org.bukkit.inventory.ItemStack playerItemInHand = player.getInventory().getItemInMainHand();
                                    playerItemInHand.setAmount(playerItemInHand.getAmount() - 1);
                                    player.getInventory().setItemInMainHand(playerItemInHand);
                                }
                            }
                        } else {
                            tamableFox.toggleSitting();
                        }
                        event.setCancelled(true);
                    }
                } else if (player.getInventory().getItemInMainHand().getType() == Material.CHICKEN) {
                    if (Math.random() < 0.33) {
                        tamableFox.setTamed(true);
                        tamableFox.setOwner(((CraftPlayer) player).getHandle());

                        // Add fox to foxUUIDs to get their owner and other things
                        TamableFoxes.foxUUIDs.replace(tamableFox.getUniqueID(), null, player.getUniqueId());

                        // Indicate that it was tamed
                        player.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 6, 0.5D, 0.5D, 0.5D);
                    } else {
                        player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 10, 0.3D, 0.3D, 0.3D, 0.15D);
                    }

                    if (!player.getGameMode().equals(GameMode.CREATIVE)) { // Remove chicken from inventory unless in creative
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    // Make it so when player sleeps, fox does too
    @EventHandler
    public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (foxUUIDs.containsValue(player.getUniqueId())) {
            List<UUID> listOfUUIDs = new ArrayList<>();

            for (Map.Entry<UUID, UUID> entry : foxUUIDs.entrySet()) {
                if (entry.getValue().equals(player.getUniqueId())) {
                    listOfUUIDs.add(entry.getKey());
                }
            }

            for (UUID uuid : listOfUUIDs) {
                TamableFox tamableFox = ((TamableFox)((CraftFox)getEntityByUniqueId(uuid)).getHandle());
                if (player.getWorld().getTime() > 12541 && player.getWorld().getTime() < 23460) {
                    tamableFox.setSleeping(true);
                }
            }
        }
    }

    // Wake the fox up when the player wakes up
    @EventHandler
    public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        if (foxUUIDs.containsValue(player.getUniqueId())) {
            List<UUID> listOfUUIDs = new ArrayList<>();

            for (Map.Entry<UUID, UUID> entry : foxUUIDs.entrySet()) {
                if (entry.getValue().equals(event.getPlayer().getUniqueId())) {
                    listOfUUIDs.add(entry.getKey());
                }
            }

            for (UUID uuid : listOfUUIDs) {
                TamableFox tamableFox = ((TamableFox)((CraftFox)getEntityByUniqueId(uuid)).getHandle());
                tamableFox.setSleeping(false);
            }
        }
    }

    // Replace all spawned foxes with the TamableFox
    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Fox && !(isTamableFox(entity))) {
            EntityFox.Type foxType = ((EntityFox) ((CraftEntity)entity).getHandle()).getFoxType();
            spawnTamableFox(entity.getLocation(), foxType);
            //Bukkit.getConsoleSender().sendMessage(Reference.CHAT_PREFIX + "Replaced vanilla fox");
            event.setCancelled(true);
        }
    }

    public Entity getEntityByUniqueId(UUID uniqueId){
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getUniqueId().equals(uniqueId)) {
                        return entity;
                    }
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onEntityDeathEvent (EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (isTamableFox(entity)) {
            foxUUIDs.remove(entity.getUniqueId());
            if (fileManager.getConfig("foxes.yml").get().getConfigurationSection("Foxes").contains(entity.getUniqueId() + "")) {
                fileManager.getConfig("foxes.yml").get().set("Foxes." + entity.getUniqueId(), null);
                fileManager.saveConfig("foxes.yml");
            }
        }
    }

    public boolean isTamableFox(Entity entity) {
        return ((CraftEntity)entity).getHandle().getClass().getName().contains("TamableFox") || ((CraftEntity)entity).getHandle() instanceof TamableFox;
    }

    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) {
        File file = getFile();
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
            } else {
                File outFile = new File(this.dataFolder, resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    //if (outFile.exists() && !replace) {
                    if (!outFile.exists() && replace) {
                        //this.logger.log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    } /*else {

                    }*/
                } catch (IOException var10) {
                    Bukkit.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }
            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
