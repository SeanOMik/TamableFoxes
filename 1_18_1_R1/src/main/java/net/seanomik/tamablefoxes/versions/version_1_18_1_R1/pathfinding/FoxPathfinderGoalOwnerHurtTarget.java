package net.seanomik.tamablefoxes.versions.version_1_18_1_R1.pathfinding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.seanomik.tamablefoxes.versions.version_1_18_1_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends TargetGoal {
    private final EntityTamableFox tameAnimal;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public FoxPathfinderGoalOwnerHurtTarget(EntityTamableFox entitytameableanimal) {
        super(entitytameableanimal, false);
        this.tameAnimal = entitytameableanimal;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        if (this.tameAnimal.isTamed() && !this.tameAnimal.isOrderedToSit() && !this.tameAnimal.isOrderedToSleep()) {
            LivingEntity entityliving = this.tameAnimal.getOwner();
            if (entityliving == null) {
                return false;
            } else {
                this.ownerLastHurt = entityliving.getLastHurtMob();
                int i = entityliving.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurt, entityliving);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurt, TargetReason.OWNER_ATTACKED_TARGET, true);
        LivingEntity entityliving = this.tameAnimal.getOwner();
        if (entityliving != null) {
            this.timestamp = entityliving.getLastHurtMobTimestamp();
        }

        tameAnimal.setDefending(true);

        super.start();
    }


    @Override
    public void stop() {
        tameAnimal.setDefending(false);

        super.stop();
    }
}