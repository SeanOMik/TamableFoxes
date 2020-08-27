package net.seanomik.tamablefoxes.versions.version_1_16_R2.pathfinding;

import net.minecraft.server.v1_16_R2.*;
import net.seanomik.tamablefoxes.versions.version_1_16_R2.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalHurtByTarget extends PathfinderGoalTarget {
    private static final PathfinderTargetCondition a = (new PathfinderTargetCondition()).c().e();
    private boolean b;
    private int c;
    private final Class<?>[] d;
    private Class<?>[] i;

    public FoxPathfinderGoalHurtByTarget(EntityTamableFox tamableFox, Class<?>... aclass) {
        super(tamableFox, true);
        this.d = aclass;
        this.a(EnumSet.of(Type.TARGET));
    }

    public boolean a() {
        int i = this.e.cZ();
        EntityLiving entityliving = this.e.getLastDamager();
        if (i != this.c && entityliving != null) {
            if (entityliving.getEntityType() == EntityTypes.PLAYER && this.e.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
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
        this.c = this.e.cZ();
        this.h = 300;
        if (this.b) {
            this.g();
        }

        super.c();
    }

    protected void g() {
        double d0 = this.k();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.a(this.e.getPositionVector()).grow(d0, 10.0D, d0);
        List<EntityInsentient> list = this.e.world.b(this.e.getClass(), axisalignedbb);
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
                    } while(this.e instanceof EntityTameableAnimal && ((EntityTameableAnimal)this.e).getOwner() != ((EntityTameableAnimal)entityinsentient).getOwner());
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