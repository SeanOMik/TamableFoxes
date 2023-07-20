package net.seanomik.tamablefoxes.versions.version_1_20_R1.pathfinding;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.seanomik.tamablefoxes.versions.version_1_20_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalHurtByTarget extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public FoxPathfinderGoalHurtByTarget(PathfinderMob entitycreature, Class<?>... aclass) {
        super(entitycreature, true);
        this.toIgnoreDamage = aclass;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity entityliving = this.mob.getLastHurtByMob();
        if (i != this.timestamp && entityliving != null) {
            if (entityliving.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
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

                return this.canAttack(entityliving, HURT_BY_TARGETING);
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

    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob(), TargetReason.TARGET_ATTACKED_ENTITY, true);
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }

        super.start();
    }

    protected void alertOthers() {
        double d0 = this.getFollowDistance();
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0D, d0);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), axisalignedbb, EntitySelector.NO_SPECTATORS);
        Iterator iterator = list.iterator();

        while(true) {
            Mob entityinsentient;
            boolean flag;
            do {
                do {
                    do {
                        do {
                            do {
                                if (!iterator.hasNext()) {
                                    return;
                                }

                                entityinsentient = (Mob)iterator.next();
                            } while(this.mob == entityinsentient);
                        } while(entityinsentient.getTarget() != null);
                    } while(this.mob instanceof EntityTamableFox && ((EntityTamableFox)this.mob).getOwner() != ((EntityTamableFox)entityinsentient).getOwner());
                } while(entityinsentient.isAlliedTo(this.mob.getLastHurtByMob()));

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

            this.alertOther(entityinsentient, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob entityinsentient, LivingEntity entityliving) {
        entityinsentient.setTarget(entityliving, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY, true);
    }
}
