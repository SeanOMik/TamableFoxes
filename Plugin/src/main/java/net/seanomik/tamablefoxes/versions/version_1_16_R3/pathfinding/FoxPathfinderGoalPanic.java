package net.seanomik.tamablefoxes.versions.version_1_16_R3.pathfinding;

import net.minecraft.server.v1_16_R3.PathfinderGoalPanic;
import net.seanomik.tamablefoxes.versions.version_1_16_R3.EntityTamableFox;

public class FoxPathfinderGoalPanic extends PathfinderGoalPanic {
    EntityTamableFox tamableFox;

    public FoxPathfinderGoalPanic(EntityTamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        return !tamableFox.isTamed() && tamableFox.isDefending() && super.a();
    }
}
