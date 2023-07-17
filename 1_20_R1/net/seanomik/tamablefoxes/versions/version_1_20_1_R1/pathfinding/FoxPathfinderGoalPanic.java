package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;

public class FoxPathfinderGoalPanic extends PathfinderGoalPanic {
    EntityTamableFox tamableFox;

    public FoxPathfinderGoalPanic(EntityTamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    @Override
    public boolean a() { //canUse
        if (tamableFox.isTamed()) {
            return tamableFox.er() < 2.0f && super.a();
        }

        return tamableFox.isDefending() && super.a();
    }
}
