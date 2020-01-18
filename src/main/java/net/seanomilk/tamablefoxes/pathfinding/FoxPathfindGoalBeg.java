package net.seanomilk.tamablefoxes.pathfinding;

import net.seanomilk.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;

import java.util.EnumSet;

public class FoxPathfindGoalBeg extends PathfinderGoal {

    private final EntityFox a;
    private final EntityTamableFox z;
    private final World c;
    private final float d;
    private final PathfinderTargetCondition f;
    private EntityHuman b;
    private int e;

    public FoxPathfindGoalBeg(EntityTamableFox tamableFox, float var1) {
        this.a = tamableFox;
        this.c = tamableFox.world;
        this.d = var1;
        this.f = (new PathfinderTargetCondition()).a((double) var1).a().b().d();
        this.a(EnumSet.of(PathfinderGoal.Type.LOOK));
        this.z = tamableFox;
    }

    public boolean a() {
        this.b = this.c.a(this.f, this.a);
        return this.b != null && this.a(this.b);
    }

    public boolean b() {
        if (!this.b.isAlive()) {
            return false;
        } else if (this.a.h(this.b) > (double) (this.d * this.d)) {
            return false;
        } else {
            return this.e > 0 && this.a(this.b);
        }
    }

    public void e() {
        this.a.getControllerLook().a(this.b.locX(), this.b.locY() + (double) this.b.getHeadHeight(), this.b.locZ(), 10.0F, (float) this.a.dU());
        --this.e;
    }

    private boolean a(EntityHuman var0) {
        EnumHand[] var2 = EnumHand.values();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            if (this.z.isTamed() && var0.getItemInMainHand().getItem() == Items.SWEET_BERRIES) {
                return true;
            }

            if (this.a.i(var0.getItemInMainHand())) {
                return true;
            }
        }

        return false;
    }

}
