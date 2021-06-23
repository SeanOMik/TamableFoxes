package net.seanomik.tamablefoxes.versions.version_1_16_R1.pathfinding;

import net.minecraft.server.v1_16_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityTameableAnimal;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;
import net.seanomik.tamablefoxes.versions.version_1_16_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final EntityTamableFox a;
    private net.minecraft.server.v1_16_R1.EntityLiving b;
    private int c;

    public FoxPathfinderGoalOwnerHurtTarget(EntityTamableFox tamableFox) {
        super(tamableFox, false);
        this.a = tamableFox;
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    public boolean a() {
        if (this.a.isTamed() && !this.a.isSitting()) { // !this.a.isWillSit()
            net.minecraft.server.v1_16_R1.EntityLiving entityliving = this.a.getOwner();
            if (entityliving == null) {
                return false;
            } else {
                this.b = entityliving.da();
                int i = entityliving.db();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a) && this.a.a(this.b, entityliving);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.db();
        }

        super.c();
    }
}