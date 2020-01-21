package net.seanomilk.tamablefoxes.pathfinding;

import net.seanomilk.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.PathfinderGoalFloat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfindGoalFloat extends PathfinderGoalFloat {

    protected EntityTamableFox tamableFox;

    public FoxPathfindGoalFloat(EntityTamableFox tamableFox) {
        super(tamableFox);
        this.tamableFox = tamableFox;
    }

    public void c() {
        try {
            super.c();
            Method method = this.tamableFox.getClass().getSuperclass().getDeclaredMethod("eH");
            method.setAccessible(true);
            method.invoke(this.tamableFox);
            method.setAccessible(false);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean a() {
        return this.tamableFox.isInWater() && this.tamableFox.co() > 0.25D || this.tamableFox.aH();
    }

}
