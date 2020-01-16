package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final TamableFox a;
    private EntityLiving b;
    private int c;

    public FoxPathfinderGoalOwnerHurtTarget(TamableFox tamableFox) {
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
                this.b = entityliving.cu();
                int i = entityliving.cv();
                return i != this.c && this.a(this.b, PathfinderTargetCondition.a);// && this.a.a(this.b, entityliving);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(this.b, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving != null) {
            this.c = entityliving.cv();
        }

        super.c();
    }
}