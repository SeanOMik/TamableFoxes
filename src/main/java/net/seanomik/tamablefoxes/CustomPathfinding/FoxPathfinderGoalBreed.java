package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class FoxPathfinderGoalBreed extends PathfinderGoal {
    private static final PathfinderTargetCondition d = (new PathfinderTargetCondition()).a(8.0D).a().b().c();
    protected final TamableFox animal;
    private final Class<? extends EntityAnimal> e;
    protected final World b;
    protected EntityAnimal partner;
    private int f;
    private final double g;

    public FoxPathfinderGoalBreed(TamableFox entityanimal, double d0) {
        this(entityanimal, d0, entityanimal.getClass());
    }

    public FoxPathfinderGoalBreed(TamableFox entityanimal, double d0, Class<? extends EntityAnimal> oclass) {
        this.animal = entityanimal;
        this.b = entityanimal.world;
        this.e = oclass;
        this.g = d0;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
    }

    public boolean a() {
        if (!this.animal.isInLove()) {
            return false;
        } else {
            this.partner = this.h();
            return this.partner != null;
        }
    }

    public boolean b() {
        return this.partner.isAlive() && this.partner.isInLove() && this.f < 60;
    }

    public void d() {
        this.partner = null;
        this.f = 0;
    }

    public void e() {
        this.animal.getControllerLook().a(this.partner, 10.0F, (float)this.animal.M());
        this.animal.getNavigation().a(this.partner, this.g);
        ++this.f;
        if (this.f >= 60 && this.animal.h(this.partner) < 9.0D) {
            this.g();
        }

    }

    @Nullable
    private EntityAnimal h() {
        List<EntityAnimal> list = this.b.a(this.e, d, this.animal, this.animal.getBoundingBox().g(8.0D));
        double d0 = 1.7976931348623157E308D;
        EntityAnimal entityanimal = null;
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            EntityAnimal entityanimal1 = (EntityAnimal)iterator.next();
            if (this.animal.mate(entityanimal1) && this.animal.h(entityanimal1) < d0) {
                entityanimal = entityanimal1;
                d0 = this.animal.h(entityanimal1);
            }
        }

        return entityanimal;
    }

    protected void g() {
        EntityAgeable entityageable = this.animal.createChild(animal);
        TamableFox tamableFoxAgeable = (TamableFox) entityageable.getBukkitEntity().getHandle();

        if (entityageable != null) {
            if (entityageable instanceof EntityTameableAnimal && ((EntityTameableAnimal)entityageable).isTamed()) {
                entityageable.persistent = true;
            }

            EntityPlayer entityplayer = this.animal.getBreedCause();
            if (entityplayer == null && this.partner.getBreedCause() != null) {
                entityplayer = this.partner.getBreedCause();
            }

            int experience = this.animal.getRandom().nextInt(7) + 1;
            EntityBreedEvent entityBreedEvent = CraftEventFactory.callEntityBreedEvent(entityageable, this.animal, this.partner, entityplayer, this.animal.breedItem, experience);
            if (entityBreedEvent.isCancelled()) {
                return;
            }

            experience = entityBreedEvent.getExperience();
            if (entityplayer != null) {
                entityplayer.a(StatisticList.ANIMALS_BRED);
                CriterionTriggers.o.a(entityplayer, this.animal, this.partner, entityageable);
            }

            this.animal.setAgeRaw(6000);
            this.partner.setAgeRaw(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            entityageable.setAgeRaw(-24000);
            entityageable.setPositionRotation(this.animal.locX, this.animal.locY, this.animal.locZ, 0.0F, 0.0F);
            this.b.addEntity(entityageable, CreatureSpawnEvent.SpawnReason.BREEDING);
            this.b.broadcastEntityEffect(this.animal, (byte)18);

            TamableFox tamableFoxBaby = (TamableFox) entityageable;
            tamableFoxBaby.setTamed(true);
            tamableFoxBaby.setOwner(entityplayer);

            // Add fox to foxUUIDs to get their owner and other things
            TamableFoxes.foxUUIDs.replace(tamableFoxBaby.getUniqueID(), null, entityplayer.getUniqueID());

            if (this.b.getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && experience > 0) {
                this.b.addEntity(new EntityExperienceOrb(this.b, this.animal.locX, this.animal.locY, this.animal.locZ, experience));
            }
        }

    }
}
