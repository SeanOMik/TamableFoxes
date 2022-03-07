package net.seanomik.tamablefoxes.versions.version_1_18_R2;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Fox;
import net.seanomik.tamablefoxes.util.FieldHelper;
import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class NMSInterface_1_18_R2 implements NMSInterface {
    @Override
    public void registerCustomFoxEntity() {
        try { // Replace the fox entity
            Field field = EntityType.FOX.getClass().getDeclaredField("bn"); // bn = factory
            FieldHelper.setFieldUsingUnsafe(field, EntityType.FOX, (EntityType.EntityFactory<Fox>) EntityTamableFox::new);
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getSuccessReplaced());
        } catch (Exception e) {
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFailureReplace());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnTamableFox(Location loc, FoxType type) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.FOX)).getHandle();
        tamableFox.setFoxType((type == FoxType.RED) ? Fox.Type.RED : Fox.Type.SNOW);
    }

    @Override
    public void changeFoxOwner(org.bukkit.entity.Fox fox, Player newOwner) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) fox).getHandle();
        tamableFox.setOwnerUUID(newOwner.getUniqueId());
    }

    @Override
    public UUID getFoxOwner(org.bukkit.entity.Fox fox) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) fox).getHandle();
        return tamableFox.getOwnerUUID();
    }

    @Override
    public void renameFox(org.bukkit.entity.Fox fox, Player player) {
        EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) fox).getHandle();
        tamableFox.rename(player);
    }
}
