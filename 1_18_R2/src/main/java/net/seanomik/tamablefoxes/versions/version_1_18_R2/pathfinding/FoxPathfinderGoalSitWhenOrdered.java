package net.seanomik.tamablefoxes.versions.version_1_18_R2.pathfinding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.versions.version_1_18_R2.EntityTamableFox;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class FoxPathfinderGoalSitWhenOrdered extends Goal {
    private final EntityTamableFox mob;
    private boolean orderedToSit;

    public FoxPathfinderGoalSitWhenOrdered(EntityTamableFox entitytameableanimal) {
        this.mob = entitytameableanimal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canContinueToUse() {
        return this.orderedToSit;
    }

    public boolean canUse() {
        if (!this.mob.isTamed()) {
            return this.orderedToSit && this.mob.getTarget() == null;
        } else if (this.mob.isInWaterOrBubble()) {
            return false;
        } else if (!this.mob.isOnGround()) {
            return false;
        } else {
            LivingEntity entityliving = this.mob.getOwner();
            return entityliving == null || ((!(this.mob.distanceToSqr(entityliving) < 144.0D) || entityliving.getLastHurtByMob() == null) && this.mob.isOrderedToSit());
        }
    }

    public void start() {
        // For some reason it needs to be ran later to not have the fox slide across the floor.
        Bukkit.getScheduler().runTaskLater(Utils.tamableFoxesPlugin, () -> {
            this.mob.getNavigation().stop();
            this.mob.setSitting(true);
            this.orderedToSit = true;
        }, 1L);
    }

    public void stop() {
        this.mob.setSitting(false);
        this.orderedToSit = false;
    }

    public boolean isOrderedToSit() { return this.orderedToSit; }

    public void setOrderedToSit(boolean flag) {
        this.orderedToSit = flag;
    }
}
