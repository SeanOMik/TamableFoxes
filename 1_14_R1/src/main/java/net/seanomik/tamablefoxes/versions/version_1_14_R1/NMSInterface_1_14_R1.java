package net.seanomik.tamablefoxes.versions.version_1_14_R1;

import net.minecraft.server.v1_14_R1.EntityFox;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.seanomik.tamablefoxes.util.FieldHelper;
import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;

// In IntelliJ, these show up as an error, but it compiles fine.
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.reflect.Field;

public class NMSInterface_1_14_R1 implements NMSInterface {
    @Override
    public void registerCustomFoxEntity() {
        Class<?> clazz = null;
        try {
            // This must be `EntityFox` since after being compiled, the class goes back to `EntityFox` instead of `Fox`
            ClassReader cr = new ClassReader(EntityFox.class.getResourceAsStream("EntityFox.class"));
            ClassNode node = new ClassNode();
            cr.accept(node, 0);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            node.accept(cw);
            clazz = new ClassDefiner(ClassLoader.getSystemClassLoader()).get(node.name.replace("/", "."), cw.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try { // Replace the fox entity
            Field field = EntityTypes.FOX.getClass().getDeclaredField("aZ"); // aZ = factory
            Class<?> finalClazz = clazz;
            FieldHelper.setFieldUsingUnsafe(field, EntityTypes.FOX, (EntityTypes.b<EntityFox>) EntityTamableFox::new);
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnTamableFox(Location loc, FoxType type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.FOX)).getHandle();
        tamableFox.setFoxType((type == FoxType.RED) ? EntityFox.Type.RED : EntityFox.Type.SNOW);
    }

    static class ClassDefiner extends ClassLoader {
        public ClassDefiner(ClassLoader parent) {
            super(parent);
        }

        public Class<?> get(String name, byte[] bytes) {
            Class<?> c = defineClass(name, bytes, 0, bytes.length);
            resolveClass(c);
            return c;
        }
    }
}
