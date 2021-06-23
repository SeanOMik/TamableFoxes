package net.seanomik.tamablefoxes.versions.version_1_17_R1.pathfinding;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final EntityTamableFox a;
    private EntityLiving b;
    private int c;

    public FoxPathfinderGoalOwnerHurtTarget(EntityTamableFox tamableFox) {
        super(tamableFox, false);
        this.a = tamableFox;
        this.a(EnumSet.of(Type.d));
    }

    public boolean a() {
        if (this.a.isTamed() && !this.a.isSitting()) { // !this.a.isWillSit()
            EntityLiving entityliving = this.a.getOwner();
            if (entityliving == null) {
                return false;
            } else {
                this.b = entityliving.dI();
                int i = entityliving.dJ();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a) && this.a.wantsToAttack(this.b, entityliving);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.dJ();
        }

        super.c();
    }
}