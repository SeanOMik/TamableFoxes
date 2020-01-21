package net.seanomilk.tamablefoxes.pathfinding;

import net.seanomilk.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.function.Predicate;

public class FoxPathfindGoalLungeUNKNOWN_USE extends PathfinderGoal {

    protected EntityTamableFox tamableFox;

    public FoxPathfindGoalLungeUNKNOWN_USE(EntityTamableFox tamableFox) {
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (this.tamableFox.isSleeping()) {
            return false;
        } else {
            EntityLiving entityliving = this.tamableFox.getGoalTarget();

            try {
                Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_15_R1.EntityFox");
                Field field = entityFoxClass.getDeclaredField("bD");
                field.setAccessible(true);
                Predicate<Entity> bG = (Predicate<Entity>) field.get(this.tamableFox);
                field.setAccessible(false);
                return entityliving != null && entityliving.isAlive() && bG.test(entityliving) && this.tamableFox.h(entityliving) > 36.0D
                        && !this.tamableFox.isCrouching() && !this.tamableFox.eg() && !this.tamableFox.isJumping();
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException var5) {
                var5.printStackTrace();
                throw new NullPointerException();
            }
        }
    }

    public void c() {
        this.tamableFox.setSitting(false);

        try {
            Method method = this.tamableFox.getClass().getSuperclass().getDeclaredMethod("v", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(this.tamableFox, false);
            method.setAccessible(false);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var2) {
            var2.printStackTrace();
        }

    }

    public void d() {
        EntityLiving entityliving = this.tamableFox.getGoalTarget();
        if (entityliving != null && EntityFox.a(this.tamableFox, entityliving)) {
            this.tamableFox.u(true);
            this.tamableFox.setCrouching(true);
            this.tamableFox.getNavigation().o();
            this.tamableFox.getControllerLook().a(entityliving, (float) this.tamableFox.dV(), (float) this.tamableFox.dU());
        } else {
            this.tamableFox.u(false);
            this.tamableFox.setCrouching(false);
        }

    }

    public void e() {
        EntityLiving entityliving = this.tamableFox.getGoalTarget();
        this.tamableFox.getControllerLook().a(entityliving, (float) this.tamableFox.dV(), (float) this.tamableFox.dU());
        if (this.tamableFox.h(entityliving) <= 36.0D) {
            this.tamableFox.u(true);
            this.tamableFox.setCrouching(true);
            this.tamableFox.getNavigation().o();
        } else {
            this.tamableFox.getNavigation().a(entityliving, 1.5D);
        }
    }

}
