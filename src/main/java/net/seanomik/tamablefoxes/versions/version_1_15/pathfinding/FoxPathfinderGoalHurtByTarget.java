package net.seanomik.tamablefoxes.versions.version_1_15.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalHurtByTarget extends PathfinderGoalTarget {

    private static final PathfinderTargetCondition a = (new PathfinderTargetCondition()).c().e();
    private final Class<?>[] d;
    private boolean b;
    private int c;
    private Class<?>[] i;

    public FoxPathfinderGoalHurtByTarget(EntityTamableFox entitycreature, Class<?>... aclass) {
        super(entitycreature, true);
        this.d = aclass;
        this.a(EnumSet.of(Type.TARGET));
    }

    public boolean a() {
        int i = this.e.cI();
        EntityLiving entityliving = this.e.getLastDamager();
        if (i != this.c && entityliving != null) {
            Class[] aclass = this.d;
            int j = aclass.length;

            for (Class<?> oclass : aclass) {
                if (oclass.isAssignableFrom(entityliving.getClass())) {
                    return false;
                }
            }

            return this.a(entityliving, a);
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
        if (!(this.e instanceof EntityTamableFox) || ((EntityTamableFox) this.e).getOwner() != this.e.getLastDamager()) {
            this.e.setGoalTarget(this.e.getLastDamager(), TargetReason.TARGET_ATTACKED_ENTITY, true);
        }

        this.g = this.e.getGoalTarget();
        this.c = this.e.cI();
        this.h = 300;
        if (this.b) {
            this.g();
        }

        super.c();
    }

    protected void g() {
        double d0 = this.k();
        List<EntityInsentient> list = this.e.world.b(this.e.getClass(),
                new AxisAlignedBB(this.e.locX(), this.e.locY(), this.e.locZ(), this.e.locX() + 1.0D, this.e.locY() + 1.0D, this.e.locZ() + 1.0D)
                        .grow(d0, 10.0D, d0));
        Iterator iterator = list.iterator();

        // kekw
        while (true) {
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

                                entityinsentient = (EntityInsentient) iterator.next();
                            } while (this.e == entityinsentient);
                        } while (entityinsentient.getGoalTarget() != null);
                    } while (this.e instanceof EntityTamableFox && ((EntityTamableFox) this.e).getOwner() != ((EntityTamableFox) entityinsentient).getOwner());
                } while (entityinsentient.r(this.e.getLastDamager()));

                if (this.i == null) {
                    break;
                }

                flag = false;
                Class[] aclass = this.i;
                int i = aclass.length;

                for (Class<?> oclass : aclass) {
                    if (entityinsentient.getClass() == oclass) {
                        flag = true;
                        break;
                    }
                }
            } while (flag);

            this.a(entityinsentient, this.e.getLastDamager());
        }
    }

    protected void a(EntityInsentient entityinsentient, EntityLiving entityliving) {
        if (!(entityinsentient instanceof EntityTamableFox) || ((EntityTamableFox) entityinsentient).getOwner() != entityliving) {
            entityinsentient.setGoalTarget(entityliving, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
        }
    }

}