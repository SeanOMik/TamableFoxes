package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_15_R1.SoundEffects;

public class FoxPathfindGoalMeleeAttack extends PathfinderGoalMeleeAttack {

    protected EntityTamableFox tamableFox;

    public FoxPathfindGoalMeleeAttack(EntityTamableFox tamableFox, double d0, boolean flag) {
        super(tamableFox, d0, flag);
        this.tamableFox = tamableFox;
    }

    protected void a(EntityLiving entityliving, double d0) {
        double d1 = this.a(entityliving);
        if (d0 <= d1 && this.b <= 0) {
            this.b = 20;
            this.a.B(entityliving);
            this.tamableFox.a(SoundEffects.ENTITY_FOX_BITE, 1.0F, 1.0F);
        }

    }

    public void c() {
        this.tamableFox.u(false);
        super.c();
    }

    public boolean a() {
        return !this.tamableFox.isSitting() && !this.tamableFox.isSleeping() && !this.tamableFox.isCrouching() && !this.tamableFox.es() && super.a();
    }

}