package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;

import java.util.EnumSet;

public class FoxPathfinderGoalBeg extends PathfinderGoal {
    private final EntityFox a;
    private final TamableFox z;
    private EntityHuman b;
    private final World c;
    private final float d;
    private int e;
    private final PathfinderTargetCondition f;

    public FoxPathfinderGoalBeg(TamableFox tamableFox, float var1) {
        this.a = tamableFox;
        this.c = tamableFox.world;
        this.d = var1;
        this.f = (new PathfinderTargetCondition()).a((double)var1).a().b().d();
        this.a(EnumSet.of(Type.LOOK));
        this.z = tamableFox;
    }

    public boolean a() {
        this.b = this.c.a(this.f, this.a);
        return this.b == null ? false : this.a(this.b);
    }

    public boolean b() {
        if (!this.b.isAlive()) {
            return false;
        } else if (this.a.h(this.b) > (double)(this.d * this.d)) {
            return false;
        } else {
            return this.e > 0 && this.a(this.b);
        }
    }

    /*public void c() {
        ((EntityFox)this.a).v(true);
        this.e = 40 + this.a.getRandom().nextInt(40);
    }

    public void d() {
        this.a.v(false);
        this.b = null;
    }*/

    public void e() {
        this.a.getControllerLook().a(this.b.locX, this.b.locY + (double)this.b.getHeadHeight(), this.b.locZ, 10.0F, (float)this.a.M());
        --this.e;
    }

    private boolean a(EntityHuman var0) {
        EnumHand[] var2 = EnumHand.values();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            if (this.z.isTamed() && var0.getItemInMainHand().getItem() == Items.SWEET_BERRIES) { //var5.getItem() == Items.SWEET_BERRIES) {
                return true;
            }

            if (this.a.i(var0.getItemInMainHand())) {
                return true;
            }
        }

        return false;
    }
}
