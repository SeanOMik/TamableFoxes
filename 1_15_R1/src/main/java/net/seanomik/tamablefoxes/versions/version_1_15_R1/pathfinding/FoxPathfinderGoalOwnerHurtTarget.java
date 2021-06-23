package net.seanomik.tamablefoxes.versions.version_1_15_R1.pathfinding;

import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;
import net.seanomik.tamablefoxes.versions.version_1_15_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;

public class FoxPathfinderGoalOwnerHurtTarget extends PathfinderGoalTarget {
    private final EntityTamableFox fox;
    private EntityLiving hitEntity;
    private int c;

    public FoxPathfinderGoalOwnerHurtTarget(EntityTamableFox entityTamableFox) {
        super(entityTamableFox, false);
        this.fox = entityTamableFox;
        this.a(EnumSet.of(Type.TARGET));
    }

    public boolean a() {
        if (fox.isTamed() && !fox.isSitting()) {
            EntityLiving entityliving = fox.getOwner();
            if (entityliving == null) {
                e.setGoalTarget(null);
                return false;
            } else {
                hitEntity = entityliving.cJ();
                int i = entityliving.cK();
                return i != this.c && this.a(hitEntity, PathfinderTargetCondition.a) && fox.a(hitEntity, entityliving);
            }
        } else {
            return false;
        }
    }

    public void c() {
        this.e.setGoalTarget(hitEntity, TargetReason.OWNER_ATTACKED_TARGET, true);
        EntityLiving entityliving = fox.getOwner();
        if (entityliving != null) {
            this.c = entityliving.cK();
        }

        super.c();
    }
}