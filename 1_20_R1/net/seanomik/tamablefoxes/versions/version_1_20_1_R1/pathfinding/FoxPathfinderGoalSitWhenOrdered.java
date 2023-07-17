package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class FoxPathfinderGoalSitWhenOrdered extends PathfinderGoal {
    private final EntityTamableFox mob;
    private boolean orderedToSit;

    public FoxPathfinderGoalSitWhenOrdered(EntityTamableFox entitytameableanimal) {
        this.mob = entitytameableanimal;
        this.a(EnumSet.of(PathfinderGoal.Type.c, PathfinderGoal.Type.a));
    }

    @Override
    public boolean b() { //canContinueToUse
        return this.orderedToSit;
    }

    @Override
    public boolean a() { //canUse
        if (!this.mob.isTamed()) {
            return this.orderedToSit && this.mob.j() == null;
        } else if (this.mob.aY()) {
            return false;
        } else if (!this.mob.ay()) {
            return false;
        } else {
            EntityLiving entityliving = this.mob.getOwner();
            return entityliving == null || ((!(this.mob.f(entityliving) < 144.0D) || entityliving.ed() == null) && this.mob.isOrderedToSit());
        }
    }

    @Override
    public void c() { //start
        // For some reason it needs to be ran later to not have the fox slide across the floor.
        Bukkit.getScheduler().runTaskLater(Utils.tamableFoxesPlugin, () -> {
            this.mob.J().n();
            this.mob.w(true);
            this.orderedToSit = true;
        }, 1L);
    }

    @Override
    public void d() { //stop
        this.mob.w(false);
        this.orderedToSit = false;
    }

    public boolean isOrderedToSit() { return this.orderedToSit; }

    public void setOrderedToSit(boolean flag) {
        this.orderedToSit = flag;
    }
}
