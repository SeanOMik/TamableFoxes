package net.seanomik.tamablefoxes.versions.version_1_14_R1;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import net.seanomik.tamablefoxes.io.sqlite.SQLiteHelper;
import net.seanomik.tamablefoxes.versions.version_1_14_R1.pathfinding.*;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class EntityTamableFox extends EntityFox {

    protected static final DataWatcherObject<Byte> tamed;
    protected static final DataWatcherObject<Optional<UUID>> ownerUUID;
    private static final DataWatcherObject<Byte> bx;
    private static final Predicate<Entity> bD;

    static {
        tamed = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.a);
        ownerUUID = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.o);

        bx = DataWatcher.a(EntityFox.class, DataWatcherRegistry.a);
        bD = (entity) -> !entity.bm() && IEntitySelector.e.test(entity);
    }

    List<PathfinderGoal> untamedGoals;
    private FoxPathfinderGoalSit goalSit;

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    public void initPathfinder() {
        try {
            this.goalSit = new FoxPathfinderGoalSit(this);
            this.goalSelector.a(1, goalSit);

            // Wild animal attacking
            Field landTargetGoal = this.getClass().getSuperclass().getDeclaredField("bH");
            landTargetGoal.setAccessible(true);
            landTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityAnimal.class, 10, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && (entityliving instanceof EntityChicken || entityliving instanceof EntityRabbit);
            }));
            landTargetGoal.setAccessible(false);

            Field turtleEggTargetGoal = this.getClass().getSuperclass().getDeclaredField("bI");
            turtleEggTargetGoal.setAccessible(true);
            turtleEggTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityTurtle.class, 10, false, false, (entityLiving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && EntityTurtle.bz.test((EntityLiving) entityLiving);
            }));
            turtleEggTargetGoal.setAccessible(false);

            Field fishTargetGoal = this.getClass().getSuperclass().getDeclaredField("bJ");
            fishTargetGoal.setAccessible(true);
            fishTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityFish.class, 20, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && entityliving instanceof EntityFishSchool;
            }));
            fishTargetGoal.setAccessible(false);

            this.goalSelector.a(0, getFoxInnerPathfinderGoal("g")); // FoxFloatGoal
            this.goalSelector.a(1, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.goalSelector.a(2, new FoxPathfinderGoalPanic(this, 2.2D));
            this.goalSelector.a(2, new FoxPathfinderGoalRelaxOnOwner(this));
            this.goalSelector.a(3, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // FoxBreedGoal

            this.goalSelector.a(4, new PathfinderGoalAvoidTarget(this, EntityHuman.class, 16.0F, 1.6D, 1.4D, (entityliving) -> {
                return !isTamed() && !((EntityLiving) entityliving).isSneaking() && IEntitySelector.e.test((EntityLiving) entityliving) && !this.isDefending();
            }));
            this.goalSelector.a(4, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !((net.minecraft.server.v1_16_R1.EntityWolf)entityliving).isTamed() && !this.isDefending();
            }));

            this.goalSelector.a(5, getFoxInnerPathfinderGoal("u")); // StalkPreyGoal
            this.goalSelector.a(6, getFoxInnerPathfinderGoal("o")); // FoxPounceGoal
            this.goalSelector.a(7, getFoxInnerPathfinderGoal("l", Arrays.asList(1.2000000476837158D, true), Arrays.asList(double.class, boolean.class))); // FoxMeleeAttackGoal
            this.goalSelector.a(8, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // FoxFollowParentGoal
            this.goalSelector.a(8, new FoxPathfinderGoalSleepWithOwner(this));
            this.goalSelector.a(9, new FoxPathfinderGoalFollowOwner(this, 1.0D, 10.0F, 2.0F));
            this.goalSelector.a(10, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.goalSelector.a(11, new PathfinderGoalRandomStrollLand(this, 1.0D));
            this.goalSelector.a(11, getFoxInnerPathfinderGoal("p")); // FoxSearchForItemsGoal
            this.goalSelector.a(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0F), Arrays.asList(EntityInsentient.class, Class.class, float.class))); // FoxLookAtPlayerGoal

            this.targetSelector.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector.a(3, (new FoxPathfinderGoalHurtByTarget(this)).a(new Class[0]));

            untamedGoals = new ArrayList<>();

            // Sleep
            PathfinderGoal sleep = getFoxInnerPathfinderGoal("t");
            this.goalSelector.a(7, sleep);
            untamedGoals.add(sleep);

            // PerchAndSearch (Random sitting?)
            PathfinderGoal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.goalSelector.a(13, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            // EatBerries (Pick berry bushes)
            PathfinderGoal eatBerries = new f(1.2000000476837158D, 12, 2);
            this.goalSelector.a(10, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            PathfinderGoal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector.a(6, seekShelter); // SeekShelter
            untamedGoals.add(seekShelter);

            PathfinderGoal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector.a(9, strollThroughVillage); // StrollThroughVillage
            untamedGoals.add(strollThroughVillage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.33000001192092896D);
        if (!isTamed()) {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(10.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0D);
        } else {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
        }
    }

    // deobf: getFlag
    private boolean t(int i) {
        return ((Byte)this.datawatcher.get(bx) & i) != 0;
    }

    // deobf: 'isDefending' from 'eF'
    public boolean isDefending() {
        return this.t(128);
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
    @Override
    public void a(NBTTagCompound compound) {
        super.a(compound);
        String ownerUuid = "";

        if (compound.hasKeyOfType("OwnerUUID", 8)) {
            ownerUuid = compound.getString("OwnerUUID");
        }/* else {
            String var2 = compound.getString("Owner");
            ownerUuid = NameReferencingFileConverter.a(this.getMinecraftServer(), var2);
        }*/

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

    // Remove untamed goals if its tamed.
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

                // Heal the fox if its health is below the max.
                if (item.isFood() && item.getFoodInfo().c() && this.getHealth() < this.getMaxHealth()) {
                    // Only remove the item from the player if they're in survival mode.
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        itemstack.subtract(1);
                    }

                    this.heal((float)item.getFoodInfo().getNutrition(), EntityRegainHealthEvent.RegainReason.EATING);
                    return true;
                }

                if (isOwnedBy(entityhuman)) {
                    // This super method checks if the fox can breed or not.
                    boolean flag = super.a(entityhuman, enumhand);

                    // If the player is not sneaking and the fox cannot breed, then make the fox sit.
                    if (!entityhuman.isSneaking() && (!flag || this.isBaby())) {
                        this.goalSit.setSitting(!this.isSitting());
                        return flag;
                    } else if (entityhuman.isSneaking()) { // Swap/Put/Take item from fox.
                        // Ignore buckets since they can be easily duplicated.
                        if (itemstack.getItem() == Items.BUCKET || itemstack.getItem() == Items.LAVA_BUCKET || itemstack.getItem() == Items.WATER_BUCKET) return true;

                        if (!this.getEquipment(EnumItemSlot.MAINHAND).isEmpty()) {
                            getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(this.getEquipment(EnumItemSlot.MAINHAND)));
                            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
                        }

                        // Run this task async to make sure to not slow the server down.
                        // This is needed due to the item being remove as soon as its put in the foxes mouth.
                        Bukkit.getScheduler().runTaskLaterAsynchronously(TamableFoxes.getPlugin(), ()-> {
                            // Put item in mouth
                            if (item != Items.AIR) {
                                ItemStack c = itemstack.cloneItemStack();
                                c.setCount(1);

                                // Only remove the item from the player if they're in survival mode.
                                if (!entityhuman.abilities.canInstantlyBuild) {
                                    itemstack.subtract(1);
                                }

                                this.setSlot(EnumItemSlot.MAINHAND, c);
                            }
                        }, (long) 0.1);

                        return true;
                    }
                }
            } else if (item == Items.CHICKEN) {
                // Check if the player has permissions to tame the fox
                if (Config.canPlayerTameFox((Player) entityhuman.getBukkitEntity())) {
                    // Only remove the item from the player if they're in survival mode.
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        itemstack.subtract(1);
                    }

                    SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance();
                    int maxTameCount = Config.getMaxPlayerFoxTames();
                    if (maxTameCount > 0 && sqLiteHelper.getPlayerFoxAmount(entityhuman.getUniqueID()) >= maxTameCount) {
                        ((Player) entityhuman.getBukkitEntity()).sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getFoxDoesntTrust());

                        return true;
                    }

                    // 0.33% chance to tame the fox, also check if the called tame entity event is cancelled or not.
                    if (this.random.nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
                        this.tame(entityhuman);

                        // Remove all navigation when tamed.
                        this.navigation.o();
                        this.setGoalTarget(null);
                        this.goalSit.setSitting(true);

                        if (maxTameCount > 0) {
                            sqLiteHelper.addPlayerFoxAmount(entityhuman.getUniqueID(), 1);
                        }

                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.HEART, getBukkitEntity().getLocation(), 6, 0.5D, 0.5D, 0.5D);

                        // Give player tamed message.
                        ((Player) entityhuman.getBukkitEntity()).sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamedMessage());

                        // Let the player choose the new fox's name if its enabled in config.
                        if (Config.askForNameAfterTaming()) {
                            Player player = (Player) entityhuman.getBukkitEntity();

                            player.sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getTamingAskingName());
                            new AnvilGUI.Builder()
                                    .onComplete((plr, input) -> { // Called when the inventory output slot is clicked
                                        if (!input.equals("")) {
                                            org.bukkit.entity.Entity tamableFox = this.getBukkitEntity();

                                            // This will auto format the name for config settings.
                                            String foxName = LanguageConfig.getFoxNameFormat(input, player.getDisplayName());

                                            tamableFox.setCustomName(foxName);
                                            tamableFox.setCustomNameVisible(true);
                                            plr.sendMessage(Utils.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamingChosenPerfect(input));
                                        }

                                        return AnvilGUI.Response.close();
                                    })
                                    .text("Fox name")      // Sets the text the GUI should start with
                                    .plugin(TamableFoxes.getPlugin())          // Set the plugin instance
                                    .open(player);         // Opens the GUI for the player provided
                        }
                    } else {
                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, getBukkitEntity().getLocation(), 10, 0.2D, 0.2D, 0.2D, 0.15D);
                    }
                }

                return true;
            }

            return super.a(entityhuman, enumhand);
        }
    }

    @Override
    public EntityTamableFox createChild(EntityAgeable entityageable) {
        EntityTamableFox entityFox = (EntityTamableFox) EntityTypes.FOX.a(this.world);
        entityFox.setFoxType(this.getFoxType());

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
            EntityTamableFox entityFox = (EntityTamableFox) entityanimal;
            return (!entityFox.isSitting() && (this.isInLove() && entityFox.isInLove()));
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

        // Give the player the taming advancement.
        if (owner instanceof EntityPlayer) {
            CriterionTriggers.x.a((EntityPlayer)owner, this);
        }
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

    // Only attack entity if its not attacking owner.
    public boolean c(EntityLiving entity) {
        return !this.isOwnedBy(entity) && super.c(entity);
    }

    public boolean isOwnedBy(EntityLiving entity) {
        return entity == this.getOwner();
    }

    /*
     deobf: wantsToAttack (Copied from EntityWolf)
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

    // Set the scoreboard team to the same as the owner if its tamed.
    public ScoreboardTeamBase getScoreboardTeam() {
        if (this.isTamed()) {
            EntityLiving var0 = this.getOwner();
            if (var0 != null) {
                return var0.getScoreboardTeam();
            }
        }

        return super.getScoreboardTeam();
    }

    // Override isAlliedTo (Entity::r(Entity))
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

    // When the fox dies, show a chat message.
    public void die(DamageSource damageSource) {
        if (!this.world.isClientSide && this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && this.getOwner() instanceof EntityPlayer) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());

            // Remove the amount of foxes the player has tamed if the limit is enabled.
            if (Config.getMaxPlayerFoxTames() > 0) {
                SQLiteHelper sqliteHelper = SQLiteHelper.getInstance();
                sqliteHelper.removePlayerFoxAmount(this.getOwner().getUniqueID(), 1);
            }
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

    public FoxPathfinderGoalSit getGoalSit() {
        return goalSit;
    }
}
