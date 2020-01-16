package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_14_R1.SoundEffects;
import net.seanomik.tamablefoxes.TamableFox;

public class FoxPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {
    protected TamableFox tamableFox;
    public FoxPathfinderGoalMeleeAttack(TamableFox tamableFox, double d0, boolean flag) {
        super(tamableFox, d0, flag);
        this.tamableFox = tamableFox;
    }

    protected void a(EntityLiving entityliving, double d0) {
        double d1 = this.a(entityliving);
        if (d0 <= d1 && this.b <= 0) {
            this.b = 20;
            this.a.C(entityliving);
            tamableFox.a(SoundEffects.ENTITY_FOX_BITE, 1.0F, 1.0F);
        }

    }

    public void c() {
        tamableFox.u(false);
        super.c();
    }

    public boolean a() {
        return !tamableFox.isSitting() && !tamableFox.isSleeping() && !tamableFox.isCrouching() && !tamableFox.dX() && super.a();
    }
}
