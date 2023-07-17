package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalTarget;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AxisAlignedBB;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class FoxPathfinderGoalHurtByTarget extends PathfinderGoalTarget {
    private static final PathfinderTargetCondition HURT_BY_TARGETING = PathfinderTargetCondition.a().d().e();
	private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public FoxPathfinderGoalHurtByTarget(EntityCreature entitycreature, Class<?>... aclass) {
        super(entitycreature, true);
        this.toIgnoreDamage = aclass;
        this.a(EnumSet.of(Type.d));
    }

    @SuppressWarnings("resource")
	@Override
    public boolean a() { //canUse
        int i = this.e.ee();
        EntityLiving entityliving = this.e.ed();
        if (i != this.timestamp && entityliving != null) {
            if (entityliving.ae() == EntityTypes.bt && this.e.dI().X().b(GameRules.K)) {
                return false;
            } else {
                Class[] aclass = this.toIgnoreDamage;
                int j = aclass.length;

                for(int k = 0; k < j; ++k) {
                    Class<?> oclass = aclass[k];
                    if (oclass.isAssignableFrom(entityliving.getClass())) {
                        return false;
                    }
                }

                return this.a(entityliving, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    public FoxPathfinderGoalHurtByTarget setAlertOthers(Class<?>... aclass) {
        this.alertSameType = true;
        this.toIgnoreAlert = aclass;
        return this;
    }

    @Override
    public void c() { //start
        this.e.setTarget(this.e.ed(), TargetReason.TARGET_ATTACKED_ENTITY, true);
        this.g = this.e.j();
        this.timestamp = this.e.ee();
        this.h = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.c();
    }

    @SuppressWarnings("resource")
	protected void alertOthers() {
        double d0 = this.l();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.a(this.e.dg()).c(d0, 10.0D, d0);
        //List<? extends EntityInsentient> list = this.e.dI().getEntitiesOfClass(this.e.getClass(), axisalignedbb, EntitySelector.NO_SPECTATORS);
        List<? extends EntityInsentient> list = this.e.dI().a(this.e.getClass(), axisalignedbb);
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
                        } while(entityinsentient.j() != null);
                    } while(this.e instanceof EntityTamableFox && ((EntityTamableFox)this.e).getOwner() != ((EntityTamableFox)entityinsentient).getOwner());
                } while(entityinsentient.p(this.e.ed()));

                if (this.toIgnoreAlert == null) {
                    break;
                }

                flag = false;
                Class[] aclass = this.toIgnoreAlert;
                int i = aclass.length;

                for(int j = 0; j < i; ++j) {
                    Class<?> oclass = aclass[j];
                    if (entityinsentient.getClass() == oclass) {
                        flag = true;
                        break;
                    }
                }
            } while(flag);

            this.alertOther(entityinsentient, this.e.ed());
        }
    }

    protected void alertOther(EntityInsentient entityinsentient, EntityLiving entityliving) {
        entityinsentient.setTarget(entityliving, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
    }
}
