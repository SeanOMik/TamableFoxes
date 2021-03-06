package net.seanomik.tamablefoxes.versions.version_1_14_R1.pathfinding;

import net.seanomik.tamablefoxes.versions.version_1_14_R1.EntityTamableFox;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_14_R1.PathfinderTargetCondition;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtByTarget extends PathfinderGoalTarget {
    private final EntityTamableFox a;
    private EntityLiving b;
    private int c;

    public FoxPathfinderGoalOwnerHurtByTarget(EntityTamableFox tamableFox) {
        super(tamableFox, false);
        this.a = tamableFox;
        this.a(EnumSet.of(Type.TARGET));
    }

    public boolean a() {
        if (this.a.isTamed() && !this.a.isSitting()) {
            EntityLiving entityliving = this.a.getOwner();
            if (entityliving == null) {
                return false;
            } else {
                this.b = entityliving.getLastDamager();
                int i = entityliving.ct();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a) && this.a.a(this.b, entityliving);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, TargetReason.TARGET_ATTACKED_OWNER, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.ct();
        }

        super.c();
    }
}
