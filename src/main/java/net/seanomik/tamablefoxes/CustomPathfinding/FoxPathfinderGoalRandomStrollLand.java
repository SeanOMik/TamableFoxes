package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.EntityCreature;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_14_R1.RandomPositionGenerator;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.seanomik.tamablefoxes.TamableFox;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class FoxPathfinderGoalRandomStrollLand extends PathfinderGoalRandomStroll {
    protected final float h;
    protected TamableFox tamableFox;
    protected Vec3D vec3D;

    public FoxPathfinderGoalRandomStrollLand(TamableFox var0, double var1) {
        this(var0, var1, 0.001F);
        this.tamableFox = var0;
    }

    public FoxPathfinderGoalRandomStrollLand(TamableFox var0, double var1, float var3) {
        super(var0, var1);
        this.h = var3;
        this.tamableFox = var0;
    }

    @Override
    public boolean a() {
        if (this.a.isVehicle()) {
            return false;
        } else {
            if (!this.g) {
                if (this.a.cw() >= 100) {
                    return false;
                }

                if (this.a.getRandom().nextInt(this.f) != 0) {
                    return false;
                }
            }

            if (!tamableFox.isSitting()) {
                vec3D = this.g();
            }
            if (vec3D == null) {
                return false;
            } else {
                this.b = vec3D.x;
                this.c = vec3D.y;
                this.d = vec3D.z;
                this.g = false;
                return true;
            }
        }
    }

    @Nullable
    protected Vec3D g() {
        if (this.a.av()) {
            Vec3D var0 = RandomPositionGenerator.b(this.a, 15, 7);
            return var0 == null ? super.g() : var0;
        } else {
            return this.a.getRandom().nextFloat() >= this.h ? RandomPositionGenerator.b(this.a, 10, 7) : super.g();
        }
    }

    @Override
    public boolean b() {
        /*Bukkit.broadcastMessage("B: " + !this.a.getNavigation().n());
        if (tamableFox.isSitting()) {

        }

        return false;*/
        return !this.a.getNavigation().n();
    }

    @Override
    public void c() {
        this.a.getNavigation().a(this.b, this.c, this.d, this.e);
    }

    @Override
    public void h() {
        this.g = true;
    }

    @Override
    public void setTimeBetweenMovement(int var0) {
        this.f = var0;
    }

    @Override
    public void e() {
        if (tamableFox.isSitting()) {
            vec3D = null;
        }
    }

    @Override
    public boolean C_() {
        if (tamableFox.isSitting()) {
            vec3D = null;
            return false;
        }
        return true;
    }
}
