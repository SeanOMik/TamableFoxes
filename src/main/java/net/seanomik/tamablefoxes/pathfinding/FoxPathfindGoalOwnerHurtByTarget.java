package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfindGoalOwnerHurtByTarget extends PathfinderGoalTarget {

    private final EntityTamableFox a;
    private EntityLiving b;
    private int c;

    public FoxPathfindGoalOwnerHurtByTarget(EntityTamableFox tamableFox) {
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
                int i = entityliving.cI();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, TargetReason.TARGET_ATTACKED_OWNER, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.cI();
        }

        super.c();
    }

}
