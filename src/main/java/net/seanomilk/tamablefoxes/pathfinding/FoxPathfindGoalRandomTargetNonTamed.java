package net.seanomilk.tamablefoxes.pathfinding;

import net.seanomilk.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalNearestAttackableTarget;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FoxPathfindGoalRandomTargetNonTamed<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {

    private final EntityTamableFox tamableFox;

    public FoxPathfindGoalRandomTargetNonTamed(EntityTamableFox tamableFox, Class<T> var1, boolean var2, @Nullable Predicate<EntityLiving> var3) {
        super(tamableFox, var1, 10, var2, false, var3);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        return !this.tamableFox.isTamed() && super.a();
    }

    public boolean b() {
        return this.d != null ? this.d.a(this.e, this.c) : super.b();
    }

}
