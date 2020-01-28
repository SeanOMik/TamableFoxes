package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_15_R1.EntityLiving;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
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

    public static List<EntityTamableFox> loadFoxesInChunk(Chunk chunk) {
        return Arrays.stream(chunk.getEntities()).filter(Utils::isTamableFox)
                .map(entity -> (EntityTamableFox) ((CraftEntity) entity).getHandle())
                .peek(tamableFox -> {
                    NamespacedKey rootKey = new NamespacedKey(TamableFoxes.getPlugin(), "tamableFoxes");
                    CraftPersistentDataContainer persistentDataContainer = tamableFox.getBukkitEntity().getPersistentDataContainer();
                    if (persistentDataContainer.has(rootKey, PersistentDataType.TAG_CONTAINER)) {
                        PersistentDataContainer tamableFoxesData = persistentDataContainer.get(rootKey, PersistentDataType.TAG_CONTAINER);
                        NamespacedKey ownerKey = new NamespacedKey(TamableFoxes.getPlugin(), "owner");
                        NamespacedKey sittingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sitting");

                        String ownerUUIDString = tamableFoxesData.get(ownerKey, PersistentDataType.STRING);
                        boolean sitting = ((byte) 1) == tamableFoxesData.get(sittingKey, PersistentDataType.BYTE);

                        boolean tamed = false;
                        if (!ownerUUIDString.equals("none")) {
                            tamed = true;
                            OfflinePlayer owner = TamableFoxes.getPlugin().getServer().getOfflinePlayer(UUID.fromString(ownerUUIDString));
                            if (owner.isOnline()) {
                                EntityLiving livingOwner = (EntityLiving) ((CraftEntity) owner).getHandle();
                                tamableFox.setOwner(livingOwner);
                            } else {
                                tamableFox.setOwnerUUID(UUID.fromString(ownerUUIDString));
                            }
                            tamableFox.setTamed(true);
                        }

                        if (sitting && tamed) {
                            tamableFox.setHardSitting(true);
                        } else {
                            tamableFox.setSitting(false);
                            tamableFox.setSleeping(false);
                        }
                    }
                }).collect(Collectors.toList());
    }

}
