package net.seanomik.tamablefoxes.versions.version_1_16_R2;

import net.minecraft.server.v1_16_R2.EntityFox;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.seanomik.tamablefoxes.util.FieldHelper;
import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;

public class NMSInterface_1_16_R2 implements NMSInterface {

    @Override
    public void registerCustomFoxEntity() {
        try { // Replace the fox entity
            Field field = EntityTypes.FOX.getClass().getDeclaredField("bf");
            FieldHelper.setField(field, EntityTypes.FOX, (EntityTypes.b<EntityFox>) EntityTamableFox::new);
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnTamableFox(Location loc, FoxType type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, EntityType.FOX)).getHandle();
        tamableFox.setFoxType((type == FoxType.RED) ? EntityFox.Type.RED : EntityFox.Type.SNOW);
    }
}
