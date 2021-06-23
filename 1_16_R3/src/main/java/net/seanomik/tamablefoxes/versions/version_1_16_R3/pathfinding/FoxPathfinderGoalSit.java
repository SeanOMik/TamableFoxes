package net.seanomik.tamablefoxes.versions.version_1_16_R3.pathfinding;

import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.versions.version_1_16_R3.EntityTamableFox;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class FoxPathfinderGoalSit extends PathfinderGoal {
    private final EntityTamableFox entity;
    private boolean willSit;

    public FoxPathfinderGoalSit(EntityTamableFox tamableFox) {
        this.entity = tamableFox;
        this.a(EnumSet.of(Type.JUMP, Type.MOVE));
    }

    public boolean b() {
        return this.willSit;
    }

    public boolean a() {
        if (!this.entity.isTamed()) {
            return this.willSit && this.entity.getGoalTarget() == null;
        } else if (this.entity.aG()) {
            return false;
        } else if (!this.entity.isOnGround()) {
            return false;
        } else {
            EntityLiving entityliving = this.entity.getOwner();
            return entityliving == null || ((!(this.entity.h(entityliving) < 144.0D) || entityliving.getLastDamager() == null) && this.willSit);
        }
    }

    public void c() {
        this.entity.getNavigation().o();
        this.entity.setGoalTarget(null);

        // For some reason it needs to be ran later.
        Bukkit.getScheduler().runTaskLater(Utils.tamableFoxesPlugin, () -> {
            this.entity.setSitting(true);
        }, 1L);
    }

    public void d() {
        this.entity.setSitting(false);
    }

    public void setSitting(boolean flag) {
        this.willSit = flag;
    }
}
