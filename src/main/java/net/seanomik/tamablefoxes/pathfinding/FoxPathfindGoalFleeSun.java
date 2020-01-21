package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PathfinderGoalFleeSun;
import net.minecraft.server.v1_15_R1.WorldServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfindGoalFleeSun extends PathfinderGoalFleeSun {

    protected EntityTamableFox tamableFox;
    private int c = 100;

    public FoxPathfindGoalFleeSun(EntityTamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (this.tamableFox.isTamed()) {
            return false;
        } else if (!this.tamableFox.isSleeping() && this.a.getGoalTarget() == null) {
            if (this.tamableFox.world.U()) {
                return true;
            } else if (this.c > 0) {
                --this.c;
                return false;
            } else {
                this.c = 100;
                BlockPosition blockposition = new BlockPosition(this.a);
                return this.tamableFox.world.isDay() && this.tamableFox.world.f(blockposition) && !((WorldServer) this.tamableFox.world).b_(blockposition)
                        && this.g();
            }
        } else {
            return false;
        }
    }

    public void c() {
        try {
            Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_15_R1.EntityFox");
            Method method = entityFoxClass.getDeclaredMethod("eH");
            method.setAccessible(true);
            method.invoke(this.tamableFox);
            method.setAccessible(false);
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException var3) {
            var3.printStackTrace();
        }

        super.c();
    }

}
