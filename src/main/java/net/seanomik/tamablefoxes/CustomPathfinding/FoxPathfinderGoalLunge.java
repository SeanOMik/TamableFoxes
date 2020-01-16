package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfinderGoalLunge extends PathfinderGoalWaterJumpAbstract {
    protected TamableFox tamableFox;
    
    public FoxPathfinderGoalLunge(TamableFox tamableFox) {
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (!tamableFox.ee()) {
            return false;
        } else {
            EntityLiving entityliving = tamableFox.getGoalTarget();
            if (entityliving != null && entityliving.isAlive()) {
                if (entityliving.getAdjustedDirection() != entityliving.getDirection()) {
                    return false;
                } else {
                    boolean flag = EntityFox.a((EntityFox)tamableFox, (EntityLiving)entityliving);
                    if (!flag) {
                        tamableFox.getNavigation().a(entityliving, 0);
                        tamableFox.setCrouching(false);
                        tamableFox.u(false);
                    }

                    return flag;
                }
            } else {
                return false;
            }
        }
    }

    public boolean b() {
        EntityLiving entityliving = tamableFox.getGoalTarget();
        if (entityliving != null && entityliving.isAlive()) {
            double d0 = tamableFox.getMot().y;
            return (d0 * d0 >= 0.05000000074505806D || Math.abs(tamableFox.pitch) >= 15.0F || !tamableFox.onGround) && !tamableFox.dX();
        } else {
            return false;
        }
    }

    public boolean C_() {
        return false;
    }

    public void c() {
        tamableFox.setJumping(true);
        tamableFox.s(true);
        tamableFox.u(false);
        EntityLiving entityliving = tamableFox.getGoalTarget();
        tamableFox.getControllerLook().a(entityliving, 60.0F, 30.0F);
        Vec3D vec3d = (new Vec3D(entityliving.locX - tamableFox.locX, entityliving.locY - tamableFox.locY, entityliving.locZ - tamableFox.locZ)).d();
        tamableFox.setMot(tamableFox.getMot().add(vec3d.x * 0.8D, 0.9D, vec3d.z * 0.8D));
        tamableFox.getNavigation().o();
    }

    public void d() {
        tamableFox.setCrouching(false);
        try {
            Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_14_R1.EntityFox");
            Field field = entityFoxClass.getDeclaredField("bN");
            field.setAccessible(true);
            field.set(tamableFox, 0.0F);
            field.setAccessible(false);

            field = entityFoxClass.getDeclaredField("bO");
            field.setAccessible(true);
            field.set(tamableFox, 0);
            field.setAccessible(false);
                /*tamableFox.bN = 0.0F;
                tamableFox.bO = 0.0F;*/
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        tamableFox.u(false);
        tamableFox.s(false);
    }

    public void e() {
        EntityLiving entityliving = tamableFox.getGoalTarget();
        if (entityliving != null) {
            tamableFox.getControllerLook().a(entityliving, 60.0F, 30.0F);
        }

        if (!tamableFox.dX()) {
            Vec3D vec3d = tamableFox.getMot();
            if (vec3d.y * vec3d.y < 0.029999999329447746D && tamableFox.pitch != 0.0F) {
                tamableFox.pitch = this.a(tamableFox.pitch, 0.0F, 0.2F);
            } else {
                double d0 = Math.sqrt(Entity.b(vec3d));
                double d1 = Math.signum(-vec3d.y) * Math.acos(d0 / vec3d.f()) * 57.2957763671875D;
                tamableFox.pitch = (float)d1;
            }
        }

        if (entityliving != null && tamableFox.g(entityliving) <= 2.0F) {
            tamableFox.C(entityliving);
        } else if (tamableFox.pitch > 0.0F && tamableFox.onGround && (float)tamableFox.getMot().y != 0.0F && tamableFox.world.getType(new BlockPosition(tamableFox)).getBlock() == Blocks.SNOW) {
            tamableFox.pitch = 60.0F;
            tamableFox.setGoalTarget((EntityLiving)null);

            try {
                Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_14_R1.EntityFox");
                Method method = entityFoxClass.getDeclaredMethod("v");
                method.setAccessible(true);
                method.invoke(tamableFox, true);
                method.setAccessible(false);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            //tamableFox.v(true);
        }

    }
}
