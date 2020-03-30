package net.seanomik.tamablefoxes;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.versions.version_1_15.pathfinding.*;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class EntityTamableFox extends EntityFox {

    protected static final DataWatcherObject<Byte> tamed;
    protected static final DataWatcherObject<Optional<UUID>> ownerUUID;
    private static final Predicate<Entity> bD;

    static {
        tamed = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.a);
        ownerUUID = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.o);

        bD = (entity) -> !entity.bm() && IEntitySelector.e.test(entity);
    }

    List<PathfinderGoal> untamedGoals;
    private FoxPathfinderGoalSit goalSit;

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytypes, World world) {
        super(entitytypes, world);
       // clearPathFinderGoals();
        //initPathfinderGoals();
    }

    @Override
    public void initPathfinder() {
        try {
            this.goalSelector.a(0, getFoxInnerPathfinderGoal("g")); // FloatGoal

            this.goalSit = new FoxPathfinderGoalSit(this);
            this.goalSelector.a(1, goalSit);

            this.goalSelector.a(2, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.goalSelector.a(3, new FoxPathfinderGoalPanic(this, 2.2D)); // PanicGoal
            this.goalSelector.a(4, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // BreedGoal

            // Avoid human only if not tamed
            this.goalSelector.a(5, new PathfinderGoalAvoidTarget(this, EntityHuman.class, 16.0F, 1.6D, 1.4D, (entityliving) -> {
                return !isTamed() && bD.test((EntityLiving) entityliving);
            }));

            // Avoid wolf if it is not tamed
            this.goalSelector.a(5, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                try {
                    Method eFMethod = EntityFox.class.getDeclaredMethod("eF");
                    eFMethod.setAccessible(true);
                    boolean eF = (boolean) eFMethod.invoke(this);
                    eFMethod.setAccessible(false);

                    return !((EntityWolf) entityliving).isTamed() && !eF;
                } catch (Exception e) {
                    return !((EntityWolf) entityliving).isTamed();
                }
            }));

            this.goalSelector.a(8, new FoxPathfinderGoalMeleeAttack(this, 1.2000000476837158D, true));
            this.goalSelector.a(9, new FoxPathfinderGoalFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
            this.goalSelector.a(6, getFoxInnerPathfinderGoal("u")); // StalkPrey
            this.goalSelector.a(7, new o()); // Pounce

            this.goalSelector.a(9, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // FollowParent

            this.goalSelector.a(11, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.goalSelector.a(12, new PathfinderGoalRandomStrollLand(this, 1.15D));

            this.goalSelector.a(12, getFoxInnerPathfinderGoal("p")); // SearchForItems
            this.goalSelector.a(13, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0f), Arrays.asList(EntityInsentient.class, Class.class, float.class))); // LookAtPlayer

            this.targetSelector.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector.a(3, (new FoxPathfinderGoalHurtByTarget(this, new Class[0])).a(new Class[0]));

            // Wild animal attacking
            Field bE = this.getClass().getSuperclass().getDeclaredField("bE");
            bE.setAccessible(true);
            bE.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityAnimal.class, 10, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && (entityliving instanceof EntityChicken || entityliving instanceof EntityRabbit);
            }));

            Field bF = this.getClass().getSuperclass().getDeclaredField("bF");
            bF.setAccessible(true);
            bF.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityTurtle.class, 10, false, false, (entityLiving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && EntityTurtle.bw.test((EntityLiving) entityLiving);
            }));

            Field bG = this.getClass().getSuperclass().getDeclaredField("bG");
            bG.setAccessible(true);
            bG.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityFish.class, 20, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && entityliving instanceof EntityFishSchool;
            }));

            untamedGoals = new ArrayList<>();

            // Sleep
            PathfinderGoal sleep = getFoxInnerPathfinderGoal("t");
            this.goalSelector.a(8, sleep);
            untamedGoals.add(sleep);

            // PerchAndSearch (Random sitting?)
            PathfinderGoal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.goalSelector.a(14, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            // EatBerries (Pick berry bushes)
            PathfinderGoal eatBerries = new f(1.2000000476837158D, 12, 2);
            this.goalSelector.a(11, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            PathfinderGoal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector.a(7, seekShelter); // SeekShelter
            untamedGoals.add(seekShelter);

            PathfinderGoal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector.a(10, strollThroughVillage); // StrollThroughVillage
            untamedGoals.add(strollThroughVillage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initAttributes() {
        this.getAttributeMap().b(GenericAttributes.MAX_HEALTH);
        this.getAttributeMap().b(GenericAttributes.KNOCKBACK_RESISTANCE);
        this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED);
        this.getAttributeMap().b(GenericAttributes.ARMOR);
        this.getAttributeMap().b(GenericAttributes.ARMOR_TOUGHNESS);

        // Default value is 32, might want to make this configurable in the future
        this.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE).setValue(16.0D);

        this.getAttributeMap().b(GenericAttributes.ATTACK_KNOCKBACK);

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D);

        if (!isTamed()) {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(10.0D);
            this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(2.0D);
        } else {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
        }
    }

    public static Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object o = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    protected void initDatawatcher() {
        super.initDatawatcher();
        this.datawatcher.register(tamed, (byte) 0);
        this.datawatcher.register(ownerUUID, Optional.empty());
    }

    // addAdditionalSaveData
    public void b(NBTTagCompound compound) {
        super.b(compound);
        if (this.getOwnerUUID() == null) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.getOwnerUUID().toString());
        }

        compound.setBoolean("Sitting", this.isSitting());
    }

    // readAdditionalSaveData
    public void a(NBTTagCompound compound) {
        super.a(compound);
        String ownerUuid;
        if (compound.hasKeyOfType("OwnerUUID", 8)) {
            ownerUuid = compound.getString("OwnerUUID");
        } else {
            String var2 = compound.getString("Owner");
            ownerUuid = NameReferencingFileConverter.a(this.getMinecraftServer(), var2);
        }
        if (!ownerUuid.isEmpty()) {
            try {
                this.setOwnerUUID(UUID.fromString(ownerUuid));
                this.setTamed(true);
            } catch (Throwable throwable) {
                this.setTamed(false);
            }
        }
        if (this.goalSit != null) {
            this.goalSit.setSitting(compound.getBoolean("Sitting"));
        }
        this.setSitting(compound.getBoolean("Sitting"));
    }

    public boolean isTamed() {
        return ((Byte) this.datawatcher.get(tamed) & 4) != 0;
    }

    public void setTamed(boolean tamed_) {
        byte isTamed = this.datawatcher.get(tamed);
        if (tamed_) {
            this.datawatcher.set(tamed, (byte) (isTamed | 4));
        } else {
            this.datawatcher.set(tamed, (byte) (isTamed & -5));
        }
        this.reassessTameGoals();

        if (tamed_) {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
            this.setHealth(this.getMaxHealth());
        } else {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(10.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0D);
        }
    }

    private void reassessTameGoals() {
        if (!isTamed()) return;
        for (PathfinderGoal untamedGoal : untamedGoals) {
            this.goalSelector.a(untamedGoal);
        }
    }

    // deobf: mobInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);
        Item item = itemstack.getItem();
        if (itemstack.getItem() instanceof ItemMonsterEgg) {
            return super.a(entityhuman, enumhand);
        } else {
            if (this.isTamed()) {
                if (item.isFood() && item.getFoodInfo().c() && this.getHealth() < this.getMaxHealth()) {
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        itemstack.subtract(1);
                    }
                    this.heal((float)item.getFoodInfo().getNutrition(), EntityRegainHealthEvent.RegainReason.EATING);
                    return true;
                }

                if (isOwnedBy(entityhuman)) {
                    boolean flag = super.a(entityhuman, enumhand);
                    if (!entityhuman.isSneaking() && (!flag || this.isBaby())) {
                        this.goalSit.setSitting(!this.isSitting());
                        return flag;
                    } else if (entityhuman.isSneaking()) {
                        if (!this.getEquipment(EnumItemSlot.MAINHAND).isEmpty()) {
                            getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(this.getEquipment(EnumItemSlot.MAINHAND)));
                            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                        }
                        if (item != Items.AIR) {
                            ItemStack c = itemstack.cloneItemStack();
                            c.setCount(1);
                            itemstack.subtract(1);
                            this.setSlot(EnumItemSlot.MAINHAND, c);
                        }
                    }
                }
                // TODO: take/give items
            } else if (item == Items.SWEET_BERRIES) {
                if (!entityhuman.abilities.canInstantlyBuild) {
                    itemstack.subtract(1);
                }
                if (this.random.nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
                    this.tame(entityhuman);
                    this.navigation.o();
                    this.setGoalTarget(null);
                    this.goalSit.setSitting(true);
                    getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.HEART, getBukkitEntity().getLocation(), 6, 0.5D, 0.5D, 0.5D);
                } else {
                    getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, getBukkitEntity().getLocation(), 10, 0.2D, 0.2D, 0.2D, 0.15D);
                }
                return true;
            }

            return super.a(entityhuman, enumhand);
        }
    }

    // deobf: isFood (used for breeding)
    public boolean i(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item.isFood() && item.getFoodInfo().c();
    }

    public EntityTamableFox createChild(EntityAgeable entityageable) {
        EntityTamableFox entityFox = (EntityTamableFox) EntityTypes.FOX.a(this.world);
        UUID uuid = this.getOwnerUUID();
        if (uuid != null) {
            entityFox.setOwnerUUID(uuid);
            entityFox.setTamed(true);
        }

        return entityFox;
    }

    public boolean mate(EntityAnimal entityanimal) {
        if (entityanimal == this) {
            return false;
        } else if (!(entityanimal instanceof EntityTamableFox)) {
            return false;
        } else {
            EntityTamableFox entitywolf = (EntityTamableFox)entityanimal;
            return (!entitywolf.isSitting() && (this.isInLove() && entitywolf.isInLove()));
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.datawatcher.get(ownerUUID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID ownerUuid) {
        this.datawatcher.set(ownerUUID, Optional.ofNullable(ownerUuid));
    }

    public void tame(EntityHuman owner) {
        this.setTamed(true);
        this.setOwnerUUID(owner.getUniqueID());
        /*
        * The following code appears to be for the taming advancement, will investigate how to change that in the future
        if (owner instanceof EntityPlayer) {
            CriterionTriggers.x.a((EntityPlayer)owner, this);
        }
        */
    }

    @Nullable
    public EntityLiving getOwner() {
        try {
            UUID ownerUuid = this.getOwnerUUID();
            return ownerUuid == null ? null : this.world.b(ownerUuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    // deobf: canAttack
    public boolean c(EntityLiving entity) {
        return !this.isOwnedBy(entity) && super.c(entity);
    }

    public boolean isOwnedBy(EntityLiving entity) {
        return entity == this.getOwner();
    }

    /*
     deobf: wantsToAttack (copied from EntityWolf)
     This code being from EntityWolf also means that wolves will want to attack foxes
     Our life would be so much easier if we could extend both EntityFox and EntityTameableAnimal
    */
    public boolean a(EntityLiving entityliving, EntityLiving entityliving1) {
        if (!(entityliving instanceof EntityCreeper) && !(entityliving instanceof EntityGhast)) {
            if (entityliving instanceof EntityTamableFox) {
                EntityTamableFox entityFox = (EntityTamableFox) entityliving;
                return !entityFox.isTamed() || entityFox.getOwner() != entityliving1;
            } else {
                return (!(entityliving instanceof EntityHuman)
                        || !(entityliving1 instanceof EntityHuman) ||
                        ((EntityHuman) entityliving1).a((EntityHuman) entityliving)) && ((!(entityliving instanceof EntityHorseAbstract)
                        || !((EntityHorseAbstract) entityliving).isTamed()) && (!(entityliving instanceof EntityTameableAnimal)
                        || !((EntityTameableAnimal) entityliving).isTamed()));
            }
        } else {
            return false;
        }
    }

    public ScoreboardTeamBase getScoreboardTeam() {
        if (this.isTamed()) {
            EntityLiving var0 = this.getOwner();
            if (var0 != null) {
                return var0.getScoreboardTeam();
            }
        }

        return super.getScoreboardTeam();
    }

    // override isAlliedTo
    public boolean r(Entity entity) {
        if (this.isTamed()) {
            EntityLiving entityOwner = this.getOwner();
            if (entity == entityOwner) {
                return true;
            }
            if (entityOwner != null) {
                return entityOwner.r(entity);
            }
        }
        return super.r(entity);
    }

    public void die(DamageSource damageSource) {
        if (!this.world.isClientSide && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof EntityPlayer) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(damageSource);
    }


    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName, List<Object> args, List<Class<?>> argTypes) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, args, argTypes);
    }

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, Arrays.asList(), Arrays.asList());
    }

    private void clearPathFinderGoals() {
        Set<?> goalSet = (Set<?>) getPrivateField("d", PathfinderGoalSelector.class, goalSelector);
        Set<?> targetSet = (Set<?>) getPrivateField("d", PathfinderGoalSelector.class, targetSelector);
        goalSet.clear();
        targetSet.clear();

        Map<?, ?> goalMap = (Map<?, ?>) getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
        Map<?, ?> targetMap = (Map<?, ?>) getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
        goalMap.clear();
        targetMap.clear();

        EnumSet<?> goalEnumSet = (EnumSet<?>) getPrivateField("f", PathfinderGoalSelector.class, goalSelector);
        EnumSet<?> targetEnumSet = (EnumSet<?>) getPrivateField("f", PathfinderGoalSelector.class, targetSelector);
        goalEnumSet.clear();
        targetEnumSet.clear();
    }

}
