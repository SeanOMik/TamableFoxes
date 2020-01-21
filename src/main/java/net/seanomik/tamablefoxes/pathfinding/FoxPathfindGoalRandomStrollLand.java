package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_15_R1.RandomPositionGenerator;
import net.minecraft.server.v1_15_R1.Vec3D;

import javax.annotation.Nullable;

public class FoxPathfindGoalRandomStrollLand extends PathfinderGoalRandomStroll {

    protected final float h;
    protected EntityTamableFox tamableFox;
    protected Vec3D vec3D;

    public FoxPathfindGoalRandomStrollLand(EntityTamableFox var0, double var1) {
        this(var0, var1, 0.001F);
        this.tamableFox = var0;
    }

    public FoxPathfindGoalRandomStrollLand(EntityTamableFox var0, double var1, float var3) {
        super(var0, var1);
        this.h = var3;
        this.tamableFox = var0;
    }

    public boolean a() {
        if (this.a.isVehicle()) {
            return false;
        } else {
            if (!this.g) {
                if (this.a.cL() >= 100) {
                    return false;
                }

                if (this.a.getRandom().nextInt(this.f) != 0) {
                    return false;
                }
            }

            if (!this.tamableFox.isSitting()) {
                this.vec3D = this.g();
            }

            if (this.vec3D == null) {
                return false;
            } else {
                this.b = this.vec3D.x;
                this.c = this.vec3D.y;
                this.d = this.vec3D.z;
                this.g = false;
                return true;
            }
        }
    }

    @Nullable
    protected Vec3D g() {
        if (this.a.az()) {
            Vec3D var0 = RandomPositionGenerator.b(this.a, 15, 7);
            return var0 == null ? super.g() : var0;
        } else {
            return this.a.getRandom().nextFloat() >= this.h ? RandomPositionGenerator.b(this.a, 10, 7) : super.g();
        }
    }

    public boolean b() {
        return !this.a.getNavigation().n();
    }

    public void c() {
        this.a.getNavigation().a(this.b, this.c, this.d, this.e);
    }

    public void h() {
        this.g = true;
    }

    public void setTimeBetweenMovement(int var0) {
        this.f = var0;
    }

    public void e() {
        if (this.tamableFox.isSitting()) {
            this.vec3D = null;
        }
    }

    public boolean E_() {
        if (this.tamableFox.isSitting()) {
            this.vec3D = null;
            return false;
        } else {
            return true;
        }
    }

}
