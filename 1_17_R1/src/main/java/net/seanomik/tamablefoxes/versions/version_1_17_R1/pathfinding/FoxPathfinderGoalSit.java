package net.seanomik.tamablefoxes.versions.version_1_17_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.level.block.Blocks;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.EntityTamableFox;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class FoxPathfinderGoalSit extends PathfinderGoal {
    private final EntityTamableFox entity;
    private boolean willSit;

    public FoxPathfinderGoalSit(EntityTamableFox tamableFox) {
        this.entity = tamableFox;
        this.a(EnumSet.of(Type.c, Type.a)); // c = JUMP; a = MOVE
    }

    public boolean b() {
        return this.willSit;
    }

    private boolean isInBubbleColumn() {
        return this.entity.getWorld().getType(this.entity.getChunkCoordinates()).a(Blocks.lq);
    }

    public boolean a() {
        if (!this.entity.isTamed()) {
            return this.willSit && this.entity.getGoalTarget() == null;
        } else if (this.entity.aO()) {
            return false;
        } else if (!this.entity.isOnGround()) {
            return false;
        } else {
            EntityLiving entityliving = this.entity.getOwner();
            return entityliving == null || ((!(this.entity.f(entityliving) < 144.0D) || entityliving.getLastDamager() == null) && this.willSit);
        }
    }

    public void c() {
        // For some reason it needs to be ran later to not have the fox slide across the floor.
        Bukkit.getScheduler().runTaskLater(Utils.tamableFoxesPlugin, () -> {
            this.entity.setSitting(true);
            this.entity.getNavigation().o();
        }, 1L);
    }

    public void d() {
        this.entity.setSitting(false);
    }

    public boolean isWillSit() { return this.willSit; }

    public void setSitting(boolean flag) {
        this.willSit = flag;
    }
}
