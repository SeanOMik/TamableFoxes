package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_15_R1.EntityLiving;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils {

    public static boolean isTamableFox(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle().getClass().getName().contains("TamableFox") || ((CraftEntity) entity).getHandle() instanceof EntityTamableFox;
    }

    public static String getPrefix() {
        return ChatColor.RED + "[Tamable Foxes] ";
    }

    public static Object getPrivateFieldValue(Class c, String field, Object instance) {
        Object value = null;
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            value = f.get(instance);
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public static void setPrivateFieldValue(Class c, String field, Object instance, Object value) {
        try {
            Field f = c.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendConsoleMessage(String message) {
        TamableFoxes.getPlugin().getServer().getConsoleSender().sendMessage(message);
    }

    public static Class<?> getPrivateInnerClass(Class outer, String innerName) {
        for (Class<?> declaredClass : outer.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals(innerName)) return declaredClass;
        }

        return null;
    }

    public static Object instantiatePrivateInnerClass(Class outer, String innerName, Object outerObject, List<Object> args, List<Class<?>> argTypes) {
        try {
            Class<?> innerClass = getPrivateInnerClass(outer, innerName);

            Object[] argObjects = new Object[args.size() + 1];
            Class<?>[] argClasses = new Class<?>[argTypes.size() + 1];

            // Needed due to how List#toArray() converts the classes to objects
            for (int i = 0; i < argClasses.length; i++) {
                if (i == argClasses.length - 1) continue;
                argObjects[i + 1] = args.get(i);
                argClasses[i + 1] = argTypes.get(i);
            }
            argObjects[0] = outerObject;
            argClasses[0] = outer;

            Constructor<?> innerConstructor = innerClass.getDeclaredConstructor(argClasses);
            innerConstructor.setAccessible(true);

            Object instantiatedClass = innerConstructor.newInstance(argObjects);

            innerConstructor.setAccessible(false);

            return instantiatedClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Entity getEntity(UUID uuid) {
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uuid)) {
                    return entity;
                }
            }
        }

        return null;
    }

    public static Entity getEntity(Chunk chunk, UUID uuid) {
        for (Entity entity : chunk.getEntities()) {
            if (entity.getUniqueId().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

    public static List<EntityTamableFox> loadFoxesInChunk(Chunk chunk) {
        return Arrays.stream(chunk.getEntities()).filter(Utils::isTamableFox)
                .map(entity -> (EntityTamableFox) ((CraftEntity) entity).getHandle())
                .peek(tamableFox -> {
                    NamespacedKey rootKey = new NamespacedKey(TamableFoxes.getPlugin(), "tamableFoxes");
                    CraftPersistentDataContainer persistentDataContainer = tamableFox.getBukkitEntity().getPersistentDataContainer();
                    if (persistentDataContainer.has(rootKey, PersistentDataType.TAG_CONTAINER)) {
                        PersistentDataContainer tamableFoxesData = persistentDataContainer.get(rootKey, PersistentDataType.TAG_CONTAINER);
                        NamespacedKey ownerKey = new NamespacedKey(TamableFoxes.getPlugin(), "owner");
                        NamespacedKey chosenNameKey = new NamespacedKey(TamableFoxes.getPlugin(), "chosenName");
                        NamespacedKey sittingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sitting");
                        NamespacedKey sleepingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sleeping");

                        String ownerUUIDString = tamableFoxesData.get(ownerKey, PersistentDataType.STRING);
                        String chosenName = tamableFoxesData.get(chosenNameKey, PersistentDataType.STRING);
                        boolean sitting = ((byte) 1) == tamableFoxesData.get(sittingKey, PersistentDataType.BYTE);
                        boolean sleeping = ((byte) 1) == tamableFoxesData.get(sleepingKey, PersistentDataType.BYTE);

                        boolean tamed = false;
                        if (!ownerUUIDString.equals("none")) {
                            tamed = true;

                            OfflinePlayer owner = TamableFoxes.getPlugin().getServer().getOfflinePlayer(UUID.fromString(ownerUUIDString));
                            if (owner.isOnline()) {
                                EntityLiving livingOwner = (EntityLiving) ((CraftEntity) owner).getHandle();
                                tamableFox.setOwner(livingOwner);
                            }

                            tamableFox.setOwnerUUID(owner.getUniqueId());
                            tamableFox.setTamed(true);
                            tamableFox.setChosenName(chosenName);
                        }

                        if (sitting && tamed) {
                            tamableFox.setHardSitting(true);
                        } else if (sleeping) {
                            tamableFox.setSleeping(true);
                        } else { // Avoid the foxes getting stuck sitting down.
                            tamableFox.setSitting(false);
                            tamableFox.setSleeping(false);
                        }
                    }
                }).collect(Collectors.toList());
    }

}
