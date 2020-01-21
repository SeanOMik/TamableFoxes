package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfindGoalOwnerHurtTarget extends PathfinderGoalTarget {

    private final EntityTamableFox a;
    private EntityLiving b;
    private int c;

    public FoxPathfindGoalOwnerHurtTarget(EntityTamableFox tamableFox) {
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
                this.b = entityliving.cJ();
                int i = entityliving.cK();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.cK();
        }

        super.c();
    }
}