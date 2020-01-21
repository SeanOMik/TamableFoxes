package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FoxPathfindGoalLunge extends PathfinderGoalWaterJumpAbstract {

    protected EntityTamableFox tamableFox;

    public FoxPathfindGoalLunge(EntityTamableFox tamableFox) {
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        if (!this.tamableFox.ez()) {
            return false;
        } else {
            EntityLiving entityliving = this.tamableFox.getGoalTarget();
            if (entityliving != null && entityliving.isAlive()) {
                if (entityliving.getAdjustedDirection() != entityliving.getDirection()) {
                    return false;
                } else {
                    boolean flag = EntityFox.a(this.tamableFox, entityliving);
                    if (!flag) {
                        this.tamableFox.getNavigation().a(entityliving, 0);
                        this.tamableFox.setCrouching(false);
                        this.tamableFox.u(false);
                    }

                    return flag;
                }
            } else {
                return false;
            }
        }
    }

    public boolean b() {
        EntityLiving entityliving = this.tamableFox.getGoalTarget();
        if (entityliving != null && entityliving.isAlive()) {
            double d0 = this.tamableFox.getMot().y;
            return (d0 * d0 >= 0.05000000074505806D || Math.abs(this.tamableFox.pitch) >= 15.0F || !this.tamableFox.onGround) && !this.tamableFox.es();
        } else {
            return false;
        }
    }

    public boolean E_() {
        return false;
    }

    public void c() {
        this.tamableFox.setJumping(true);
        this.tamableFox.s(true);
        this.tamableFox.u(false);
        EntityLiving entityliving = this.tamableFox.getGoalTarget();
        this.tamableFox.getControllerLook().a(entityliving, 60.0F, 30.0F);
        Vec3D vec3d = (new Vec3D(entityliving.locX() - this.tamableFox.locX(),
                entityliving.locY() - this.tamableFox.locY(), entityliving.locZ() - this.tamableFox.locZ())).d();
        this.tamableFox.setMot(this.tamableFox.getMot().add(vec3d.x * 0.8D, 0.9D, vec3d.z * 0.8D));
        this.tamableFox.getNavigation().o();
    }

    public void d() {
        this.tamableFox.setCrouching(false);

        try {
            Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_15_R1.EntityFox");
            Field field = entityFoxClass.getDeclaredField("bK");
            field.setAccessible(true);
            field.set(this.tamableFox, 0.0F);
            field.setAccessible(false);
            field = entityFoxClass.getDeclaredField("bL");
            field.setAccessible(true);
            field.set(this.tamableFox, 0);
            field.setAccessible(false);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException var3) {
            var3.printStackTrace();
        }

        this.tamableFox.u(false);
        this.tamableFox.s(false);
    }

    public void e() {
        EntityLiving entityliving = this.tamableFox.getGoalTarget();
        if (entityliving != null) {
            this.tamableFox.getControllerLook().a(entityliving, 60.0F, 30.0F);
        }

        if (!this.tamableFox.es()) {
            Vec3D vec3d = this.tamableFox.getMot();
            if (vec3d.y * vec3d.y < 0.029999999329447746D && this.tamableFox.pitch != 0.0F) {
                this.tamableFox.pitch = MathHelper.j(this.tamableFox.pitch, 0.0F, 0.2F);
            } else {
                double d0 = Math.sqrt(Entity.b(vec3d));
                double d1 = Math.signum(-vec3d.y) * Math.acos(d0 / vec3d.f()) * 57.2957763671875D;
                this.tamableFox.pitch = (float) d1;
            }
        }

        if (entityliving != null && this.tamableFox.g(entityliving) <= 2.0F) {
            this.tamableFox.B(entityliving);
        } else if (this.tamableFox.pitch > 0.0F && this.tamableFox.onGround && (float) this.tamableFox.getMot().y != 0.0F && this.tamableFox.world.getType(new BlockPosition(this.tamableFox)).getBlock() == Blocks.SNOW) {
            this.tamableFox.pitch = 60.0F;
            this.tamableFox.setGoalTarget(null);

            try {
                Class<?> entityFoxClass = Class.forName("net.minecraft.server.v1_15_R1.EntityFox");
                Method method = entityFoxClass.getDeclaredMethod("v", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(this.tamableFox, true);
                method.setAccessible(false);
            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException var7) {
                var7.printStackTrace();
            }
        }

    }
}

