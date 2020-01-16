package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.function.Predicate;

public class FoxPathfinderGoalLungeUNKNOWN_USE extends PathfinderGoal {
    protected TamableFox tamableFox;

    public FoxPathfinderGoalLungeUNKNOWN_USE(TamableFox tamableFox) {
        this.a(EnumSet.of(net.minecraft.server.v1_14_R1.PathfinderGoal.Type.MOVE, net.minecraft.server.v1_14_R1.PathfinderGoal.Type.LOOK));
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (tamableFox.isSleeping()) {
            return false;
        } else {
            EntityLiving entityliving = tamableFox.getGoalTarget();

            try {
                Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_14_R1.EntityFox");
                Field field = entityFoxClass.getDeclaredField("bG");
                field.setAccessible(true);
                Predicate<Entity> bG = (Predicate<Entity>) field.get(tamableFox);
                field.setAccessible(false);

                return entityliving != null && entityliving.isAlive() && bG.test(entityliving) && tamableFox.h(entityliving) > 36.0D && !tamableFox.isCrouching() && !tamableFox.eg() && !tamableFox.isJumping();
            } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            throw new NullPointerException();
        }
    }

    public void c() {
        tamableFox.setSitting(false);
        try {
            Method method = tamableFox.getClass().getSuperclass().getDeclaredMethod("v", boolean.class);
            method.setAccessible(true);
            method.invoke(tamableFox, false);
            method.setAccessible(false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void d() {
        EntityLiving entityliving = tamableFox.getGoalTarget();
        if (entityliving != null && EntityFox.a((EntityFox)tamableFox, (EntityLiving)entityliving)) {
            tamableFox.u(true);
            tamableFox.setCrouching(true);
            tamableFox.getNavigation().o();
            tamableFox.getControllerLook().a(entityliving, (float)tamableFox.dA(), (float)tamableFox.M());
        } else {
            tamableFox.u(false);
            tamableFox.setCrouching(false);
        }

    }

    public void e() {
        EntityLiving entityliving = tamableFox.getGoalTarget();
        tamableFox.getControllerLook().a(entityliving, (float)tamableFox.dA(), (float)tamableFox.M());
        if (tamableFox.h(entityliving) <= 36.0D) {
            tamableFox.u(true);
            tamableFox.setCrouching(true);
            tamableFox.getNavigation().o();
        } else {
            tamableFox.getNavigation().a(entityliving, 1.5D);
        }

    }
}
