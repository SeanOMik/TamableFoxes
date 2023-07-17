package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final EntityTamableFox tameAnimal;
    private EntityLiving ownerLastHurt;
    private int timestamp;

    public FoxPathfinderGoalOwnerHurtTarget(EntityTamableFox entitytameableanimal) {
        super(entitytameableanimal, false);
        this.tameAnimal = entitytameableanimal;
        this.a(EnumSet.of(PathfinderGoal.Type.d));
    }

    @Override
    public boolean a() { //canUse
        if (this.tameAnimal.isTamed() && !this.tameAnimal.isOrderedToSit() && !this.tameAnimal.isOrderedToSleep()) {
            EntityLiving entityliving = this.tameAnimal.getOwner();
            if (entityliving == null) {
                return false;
            } else {
                this.ownerLastHurt = entityliving.ef();
                int i = entityliving.eg();
                return i != this.timestamp && this.a(this.ownerLastHurt, PathfinderTargetCondition.a) && this.tameAnimal.wantsToAttack(this.ownerLastHurt, entityliving);
            }
        } else {
            return false;
        }
    }

    @Override
    public void c() {//start
        this.e.setTarget(this.ownerLastHurt, TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = this.tameAnimal.getOwner();
        if (entityliving != null) {
            this.timestamp = entityliving.eg();
        }

        tameAnimal.setDefending(true);

        super.c();
    }


    @Override
    public void d() { //stop
        tameAnimal.setDefending(false);

        super.d();
    }
}
