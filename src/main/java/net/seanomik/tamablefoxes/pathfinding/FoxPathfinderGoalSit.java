package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoal;

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
        } else if (this.entity.az()) {
            return false;
        } else if (!this.entity.onGround) {
            return false;
        } else {
            EntityLiving entityliving = this.entity.getOwner();
            return entityliving == null || ((!(this.entity.h(entityliving) < 144.0D) || entityliving.getLastDamager() == null) && this.willSit);
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
