package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.PathfinderGoalNearestAttackableTarget;
import net.seanomik.tamablefoxes.TamableFox;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FoxPathfinderGoalRandomTargetNonTamed<T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
    private final TamableFox tamableFox;

    public FoxPathfinderGoalRandomTargetNonTamed(TamableFox tamableFox, Class<T> var1, boolean var2, @Nullable Predicate<EntityLiving> var3) {
        super(tamableFox, var1, 10, var2, false, var3);
        this.tamableFox = tamableFox;
    }

    public boolean a() {
        return !tamableFox.isTamed() && super.a();
    }

    public boolean b() {
        return this.d != null ? this.d.a(this.e, this.c) : super.b();
    }
}