package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class FoxPathfindGoalBreed extends PathfinderGoal {

    private static final PathfinderTargetCondition d = new PathfinderTargetCondition().a(8.0D).a().b().c();
    protected final EntityTamableFox animal;
    protected final World b;
    private final Class<? extends EntityAnimal> e;
    private final double g;
    protected EntityAnimal partner;
    private int f;

    public FoxPathfindGoalBreed(EntityTamableFox entity, double d0) {
        this(entity, d0, entity.getClass());
    }

    public FoxPathfindGoalBreed(EntityTamableFox entity, double d0, Class<? extends EntityAnimal> oclass) {
        this.animal = entity;
        this.b = entity.world;
        this.e = oclass;
        this.g = d0;
        a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    public boolean a() {
        if (!this.animal.isInLove()) {
            return false;
        }
        this.partner = h();
        return this.partner != null;
    }

    public boolean b() {
        return (this.partner.isAlive()) && (this.partner.isInLove()) && (this.f < 60);
    }

    public void d() {
        this.partner = null;
        this.f = 0;
    }

    public void e() {
        this.animal.getControllerLook().a(this.partner, 10.0F, this.animal.dU());
        this.animal.getNavigation().a(this.partner, this.g);
        this.f += 1;
        if ((this.f >= 60) && (this.animal.h(this.partner) < 9.0D)) {
            g();
        }
    }

    @Nullable
    private EntityAnimal h() {
        List<EntityAnimal> list = this.b.a(this.e, d, this.animal, this.animal.getBoundingBox().g(8.0D));
        double d0 = Double.MAX_VALUE;
        EntityAnimal entityanimal = null;
        for (EntityAnimal entityAnimal : list) {
            if ((this.animal.mate(entityAnimal)) && (this.animal.h(entityAnimal) < d0)) {
                entityanimal = entityAnimal;
                d0 = this.animal.h(entityAnimal);
            }
        }
        return entityanimal;
    }

    protected void g() {
        EntityAgeable entityAgeable = this.animal.createChild(this.animal);
        EntityTamableFox tamableFoxAgeable = (EntityTamableFox) entityAgeable.getBukkitEntity().getHandle();
        if (tamableFoxAgeable != null) {
            EntityPlayer entityplayer = this.animal.getBreedCause();
            if ((entityplayer == null) && (this.partner.getBreedCause() != null)) {
                entityplayer = this.partner.getBreedCause();
            }
            int experience = this.animal.getRandom().nextInt(7) + 1;
            EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(entityAgeable, this.animal, this.partner, entityplayer, this.animal.breedItem, experience);
            if (entityBreedEvent.isCancelled()) {
                return;
            }
            experience = entityBreedEvent.getExperience();
            if (entityplayer != null) {
                entityplayer.a(StatisticList.ANIMALS_BRED);
                CriterionTriggers.o.a(entityplayer, this.animal, this.partner, entityAgeable);
            }
            this.animal.setAgeRaw(6000);
            this.partner.setAgeRaw(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            entityAgeable.setAgeRaw(41536);
            entityAgeable.setPositionRotation(this.animal.locX(), this.animal.locY(), this.animal.locZ(), 0.0F, 0.0F);
            this.b.addEntity(entityAgeable, CreatureSpawnEvent.SpawnReason.BREEDING);
            this.b.broadcastEntityEffect(this.animal, (byte) 18);

            EntityTamableFox tamableFoxBaby = (EntityTamableFox) entityAgeable;
            tamableFoxBaby.setTamed(Boolean.TRUE);
            tamableFoxBaby.setOwner(entityplayer);

            animal.getPlugin().getFoxUUIDs().replace(tamableFoxBaby.getUniqueID(), null, entityplayer.getUniqueID());
            if ((this.b.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) && (experience > 0)) {
                this.b.addEntity(new EntityExperienceOrb(this.b, this.animal.locX(), this.animal.locY(), this.animal.locZ(), experience));
            }
        }
    }

}
