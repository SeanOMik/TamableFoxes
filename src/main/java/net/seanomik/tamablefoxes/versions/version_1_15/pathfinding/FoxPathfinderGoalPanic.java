package net.seanomik.tamablefoxes.versions.version_1_15.pathfinding;

import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.PathfinderGoalPanic;
import net.seanomik.tamablefoxes.EntityTamableFox;

import java.lang.reflect.Method;

public class FoxPathfinderGoalPanic extends PathfinderGoalPanic {
    EntityTamableFox tamableFox;

    public FoxPathfinderGoalPanic(EntityTamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        try {
            Method eFMethod = EntityFox.class.getDeclaredMethod("eF");
            eFMethod.setAccessible(true);
            boolean eF = (boolean) eFMethod.invoke(tamableFox);
            eFMethod.setAccessible(false);

            return !tamableFox.isTamed() && !eF && super.a();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
