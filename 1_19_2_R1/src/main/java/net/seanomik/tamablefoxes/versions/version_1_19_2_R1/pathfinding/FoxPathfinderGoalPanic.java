package net.seanomik.tamablefoxes.versions.version_1_19_2_R1.pathfinding;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.seanomik.tamablefoxes.versions.version_1_19_2_R1.EntityTamableFox;

public class FoxPathfinderGoalPanic extends PanicGoal {
    EntityTamableFox tamableFox;

    public FoxPathfinderGoalPanic(EntityTamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    public boolean canUse() {
        if (tamableFox.isTamed()) {
            return tamableFox.getHealth() < 2.0f && super.canUse();
        }

        return tamableFox.isDefending() && super.canUse();
    }
}
