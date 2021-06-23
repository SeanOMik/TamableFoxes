package net.seanomik.tamablefoxes.versions.version_1_17_R1;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.EntityFox;
import net.seanomik.tamablefoxes.util.FieldHelper;
import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NMSInterface_1_17_R1 implements NMSInterface {
    @Override
    public void registerCustomFoxEntity() {
        try { // Replace the fox entity
            Field field = EntityTypes.E.getClass().getDeclaredField("bm");

            //FOX = a("fox", EntityTypes.Builder.a(EntityFox::new, EnumCreatureType.CREATURE).a(0.6F, 0.7F).trackingRange(8).a(Blocks.SWEET_BERRY_BUSH));
            /*Method method = EntityTypes.class.getDeclaredMethod("a", String.class, EntityTypes.Builder.class);
            method.setAccessible(true);
            EntityTypes<EntityFox> type = method.invoke(null, "fox", EntityTypes.Builder.a(EntityTamableFox::new, EnumCreatureType.C))*/

            FieldHelper.setField(field, EntityTypes.E, (EntityTypes.b<EntityFox>) EntityTamableFox::new);
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnTamableFox(Location loc, FoxType type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType((type == FoxType.RED) ? EntityFox.Type.a : EntityFox.Type.b);
    }
}
