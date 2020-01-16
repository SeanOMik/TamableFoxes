package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalHurtByTarget extends PathfinderGoalTarget {
    private static final PathfinderTargetCondition a = (new PathfinderTargetCondition()).c().e();
    private boolean b;
    private int c;
    private final Class<?>[] d;
    private Class<?>[] i;

    public FoxPathfinderGoalHurtByTarget(TamableFox entitycreature, Class<?>... aclass) {
        super(entitycreature, true);
        this.d = aclass;
        this.a(EnumSet.of(Type.TARGET));
    }

    public boolean a() {
        int i = this.e.ct();
        EntityLiving entityliving = this.e.getLastDamager();
        if (i != this.c && entityliving != null) {
            Class[] aclass = this.d;
            int j = aclass.length;

            for(int k = 0; k < j; ++k) {
                Class<?> oclass = aclass[k];
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
        if ( !(e instanceof TamableFox && ((TamableFox) e).getOwner() == e.getLastDamager()) ) {
            this.e.setGoalTarget(this.e.getLastDamager(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
        }

        this.g = this.e.getGoalTarget();
        this.c = this.e.ct();
        this.h = 300;
        if (this.b) {
            this.g();
        }

        super.c();
    }

    protected void g() {
        double d0 = this.k();
        List<EntityInsentient> list = this.e.world.b(this.e.getClass(), (new AxisAlignedBB(this.e.locX, this.e.locY, this.e.locZ, this.e.locX + 1.0D, this.e.locY + 1.0D, this.e.locZ + 1.0D)).grow(d0, 10.0D, d0));
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
                            } while (this.e == entityinsentient);
                        } while (entityinsentient.getGoalTarget() != null);

                        //Bukkit.broadcastMessage(String.valueOf(this.e instanceof TamableFox && ((TamableFox)this.e).getOwner() != ((TamableFox) entityinsentient).getOwner()));

                    } while (this.e instanceof TamableFox && ((TamableFox)this.e).getOwner() != ((TamableFox) entityinsentient).getOwner());
                } while (entityinsentient.r(this.e.getLastDamager()));

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
        if (entityinsentient instanceof TamableFox && ((TamableFox) entityinsentient).getOwner() == entityliving) {
            return;
        }

        entityinsentient.setGoalTarget(entityliving, EntityTargetEvent.TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
    }
}
