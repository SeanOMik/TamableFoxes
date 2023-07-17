package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;

import java.util.EnumSet;

public class FoxPathfinderGoalSleepWhenOrdered extends PathfinderGoal {
    private final EntityTamableFox mob;
    private boolean orderedToSleep;

    public FoxPathfinderGoalSleepWhenOrdered(EntityTamableFox entitytameableanimal) {
        this.mob = entitytameableanimal;
        this.a(EnumSet.of(Type.c, Type.a));
    }

    public boolean canContinueToUse() {
        return this.orderedToSleep;
    }

    @Override
    public boolean a() { //canUse
        if (!this.mob.isTamed()) {
            return this.orderedToSleep && this.mob.j() == null;
        } else if (this.mob.aY()) {
            return false;
        } else if (!this.mob.ay()) {
            return false;
        } else {
            EntityLiving entityliving = this.mob.getOwner();
            return entityliving == null || ((!(this.mob.f(entityliving) < 144.0D) || entityliving.ed() == null) && this.mob.isOrderedToSleep());
        }
    }

    @Override
    public void c() {
        this.mob.J().n();
        this.mob.C(true);
        this.orderedToSleep = true;
    }

    @Override
    public void d() {
        this.mob.C(false);
        this.orderedToSleep = false;
    }

    public boolean isOrderedToSleep() { return this.orderedToSleep; }

    public void setOrderedToSleep(boolean flag) {
        this.orderedToSleep = flag;
    }
}
