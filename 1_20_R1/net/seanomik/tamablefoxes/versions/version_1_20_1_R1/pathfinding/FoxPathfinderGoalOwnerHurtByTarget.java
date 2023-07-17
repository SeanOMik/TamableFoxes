package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {
    private final EntityTamableFox tameAnimal;
    private EntityLiving ownerLastHurtBy;
    private int timestamp;

    public FoxPathfinderGoalOwnerHurtByTarget(EntityTamableFox entitytameableanimal) {
        super(entitytameableanimal, false);
        this.tameAnimal = entitytameableanimal;
        this.a(EnumSet.of(PathfinderGoal.Type.d));
    }

    @Override
    public boolean a() { //canUse
        if( this.tameAnimal.isTamed() && !this.tameAnimal.isOrderedToSit() && !this.tameAnimal.isOrderedToSleep() )
        {
            EntityLiving entityliving = this.tameAnimal.getOwner();
            if( entityliving == null )
                return false;
            else
            {
                this.ownerLastHurtBy = entityliving.ed();
                int i = entityliving.ee();
                return i != this.timestamp && this.a(this.ownerLastHurtBy, PathfinderTargetCondition.a) && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, entityliving);
            }
        }
        else
            return false;
    }

    @Override
    public void c() { //start
        this.e.setTarget(this.ownerLastHurtBy, TargetReason.TARGET_ATTACKED_OWNER, true);
        EntityLiving entityliving = this.tameAnimal.getOwner();
        if (entityliving != null) {
            this.timestamp = entityliving.ee();
        }

        tameAnimal.setDefending(false);

        super.c();
    }

    @Override
    public void d() { //stop
        tameAnimal.setDefending(false);

        super.d();
    }
}
