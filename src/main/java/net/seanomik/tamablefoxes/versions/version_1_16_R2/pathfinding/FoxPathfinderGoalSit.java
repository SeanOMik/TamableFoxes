package net.seanomik.tamablefoxes.versions.version_1_16_R2.pathfinding;

import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.PathfinderGoal;
import net.seanomik.tamablefoxes.versions.version_1_16_R2.EntityTamableFox;

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
    } // return this.entity.isWillSit();

    public boolean a() {
        if (!this.entity.isTamed()) {
            return this.willSit && this.entity.getGoalTarget() == null; // this.entity.isWillSit()
        } else if (this.entity.aG()) {
            return false;
        } else if (!this.entity.isOnGround()) {
            return false;
        } else {
            EntityLiving entityliving = this.entity.getOwner();
            return entityliving == null ? true : (this.entity.h(entityliving) < 144.0D && entityliving.getLastDamager() != null ? false : this.willSit); // this.entity.isWillSit()
        }
    }

    public void c() {
        this.entity.getNavigation().o();
        this.entity.setSitting(true);
    }

    public void d() {
        this.entity.setSitting(false);
    }

    public void setSitting(boolean flag) {
        this.willSit = flag;
    }
}
