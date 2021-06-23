package net.seanomik.tamablefoxes.versions.version_1_17_R1.pathfinding;

import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AxisAlignedBB;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalHurtByTarget extends PathfinderGoalTarget {
    private static final PathfinderTargetCondition a = PathfinderTargetCondition.a().d().e();
    private boolean b;
    private int c;
    private final Class<?>[] d;
    private Class<?>[] i;

    public FoxPathfinderGoalHurtByTarget(EntityTamableFox tamableFox, Class<?>... aclass) {
        super(tamableFox, true);
        this.d = aclass;
        this.a(EnumSet.of(Type.d));
    }

    public boolean a() {
        int i = this.e.dH();
        EntityLiving entityliving = this.e.getLastDamager();
        if (i != this.c && entityliving != null) {
            if (entityliving.getEntityType() == EntityTypes.bi && this.e.getWorld().getGameRules().getBoolean(GameRules.I)) {
                return false;
            } else {
                Class[] aclass = this.d;
                int j = aclass.length;

                for(int k = 0; k < j; ++k) {
                    Class<?> oclass = aclass[k];
                    if (oclass.isAssignableFrom(entityliving.getClass())) {
                        return false;
                    }
                }

                return this.a(entityliving, a);
            }
        } else {
            return false;
        }
    }

    public FoxPathfinderGoalHurtByTarget a(Class<?>... aclass) {
        this.b = true;
        this.i = aclass;
        return this;
    }

    public void c() {
        this.e.setGoalTarget(this.e.getLastDamager(), TargetReason.TARGET_ATTACKED_ENTITY, true);
        this.g = this.e.getGoalTarget();
        this.c = this.e.dH();
        this.h = 300;
        if (this.b) {
            this.g();
        }

        super.c();
    }

    protected void g() {
        double d0 = this.k();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.a(this.e.getPositionVector()).grow(d0, 10.0D, d0);
        List<? extends EntityInsentient> list = this.e.getWorld().a(this.e.getClass(), axisalignedbb, IEntitySelector.f);
        Iterator iterator = list.iterator();

        while(true) {
            EntityInsentient entityinsentient;
            boolean flag;
            do {
                do {
                    do {
                        do {
                            do {
                                if (!iterator.hasNext()) {
                                    return;
                                }

                                entityinsentient = (EntityInsentient)iterator.next();
                            } while(this.e == entityinsentient);
                        } while(entityinsentient.getGoalTarget() != null);
                    } while(this.e instanceof EntityTamableFox && ((EntityTamableFox)this.e).getOwner() != ((EntityTamableFox)entityinsentient).getOwner());
                } while(entityinsentient.r(this.e.getLastDamager()));

                if (this.i == null) {
                    break;
                }

                flag = false;
                Class[] aclass = this.i;
                int i = aclass.length;

                for(int j = 0; j < i; ++j) {
                    Class<?> oclass = aclass[j];
                    if (entityinsentient.getClass() == oclass) {
                        flag = true;
                        break;
                    }
                }
            } while(flag);

            this.a(entityinsentient, this.e.getLastDamager());
        }
    }

    protected void a(EntityInsentient entityinsentient, EntityLiving entityliving) {
        entityinsentient.setGoalTarget(entityliving, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
    }
}