package net.seanomik.tamablefoxes.versions.version_1_16_R1;

import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EntityFox;
import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import net.seanomik.tamablefoxes.versions.FieldHelper;
import net.seanomik.tamablefoxes.versions.NMSInterface;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class NMSInterface_1_16_R1 implements NMSInterface {

    @Override
    public void registerCustomFoxEntity() {
        try { // Replace the fox entity
            Field field = EntityTypes.FOX.getClass().getDeclaredField("be");
            FieldHelper.setField(field, EntityTypes.FOX, (EntityTypes.b<EntityFox>) EntityTamableFox::new);
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnTamableFox(Location loc, FoxType type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType((type == FoxType.RED) ? EntityFox.Type.RED : EntityFox.Type.SNOW);
    }
}
