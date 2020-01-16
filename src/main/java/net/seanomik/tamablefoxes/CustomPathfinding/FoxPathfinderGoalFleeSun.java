package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.PathfinderGoalFleeSun;
import net.minecraft.server.v1_14_R1.WorldServer;
import net.seanomik.tamablefoxes.TamableFox;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfinderGoalFleeSun extends PathfinderGoalFleeSun {
    private int c = 100;
    protected TamableFox tamableFox;
    
    public FoxPathfinderGoalFleeSun(TamableFox tamableFox, double d0) {
        super(tamableFox, d0);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (tamableFox.isTamed()) {
            return false;
        } else if (!tamableFox.isSleeping() && this.a.getGoalTarget() == null) {
            if (tamableFox.world.U()) {
                return true;
            } else if (this.c > 0) {
                --this.c;
                return false;
            } else {
                this.c = 100;
                BlockPosition blockposition = new BlockPosition(this.a);
                return tamableFox.world.J() && tamableFox.world.f(blockposition) && !((WorldServer)tamableFox.world).b_(blockposition) && this.g();
            }
        } else {
            return false;
        }
    }

    public void c() {
        try {
            Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_14_R1.EntityFox");
            Method method = entityFoxClass.getDeclaredMethod("en");
            method.setAccessible(true);
            method.invoke(tamableFox);
            method.setAccessible(false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.c();
    }
}
