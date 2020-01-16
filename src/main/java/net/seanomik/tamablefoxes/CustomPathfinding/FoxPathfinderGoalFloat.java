package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.EntityFox;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.seanomik.tamablefoxes.TamableFox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfinderGoalFloat extends PathfinderGoalFloat {
    protected TamableFox tamableFox;
    public FoxPathfinderGoalFloat(TamableFox tamableFox) {
        super(tamableFox);
        this.tamableFox = tamableFox;
    }

    public void c() {
        try {
            super.c();
            Method method = tamableFox.getClass().getSuperclass().getDeclaredMethod("en");
            method.setAccessible(true);
            method.invoke(tamableFox);
            method.setAccessible(false);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        //tamableFox.en();
    }

    public boolean a() {
        return tamableFox.isInWater() && tamableFox.cf() > 0.25D || tamableFox.aD();
    }
}
