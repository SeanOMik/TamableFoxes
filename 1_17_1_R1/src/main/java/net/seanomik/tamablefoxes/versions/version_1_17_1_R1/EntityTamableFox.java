package net.seanomik.tamablefoxes.versions.version_1_17_1_R1;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import net.seanomik.tamablefoxes.util.io.sqlite.SQLiteHelper;
import net.seanomik.tamablefoxes.versions.version_1_17_1_R1.pathfinding.*;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class EntityTamableFox extends Fox {

    protected static final EntityDataAccessor<Boolean> tamed;
    protected static final EntityDataAccessor<Optional<UUID>> ownerUUID;

    //private static final EntityDataAccessor<Byte> bw; // DATA_FLAGS_ID
    private static final Predicate<Entity> AVOID_PLAYERS; // AVOID_PLAYERS

    static {
        tamed = SynchedEntityData.defineId(EntityTamableFox.class, EntityDataSerializers.BOOLEAN);
        ownerUUID = SynchedEntityData.defineId(EntityTamableFox.class, EntityDataSerializers.OPTIONAL_UUID);

        AVOID_PLAYERS = (entity) -> !entity.isCrouching();// && EntitySelector.test(entity);
    }

    List<Goal> untamedGoals;
    private FoxPathfinderGoalSitWhenOrdered goalSitWhenOrdered;
    private FoxPathfinderGoalSleepWhenOrdered goalSleepWhenOrdered;

    public EntityTamableFox(EntityType<? extends Fox> entitytype, Level world) {
        super(entitytype, world);

        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.33000001192092896D); // Set movement speed
        if (isTamed()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
            this.setHealth(this.getMaxHealth());
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        }

        this.setTamed(false);
    }

    @Override
    public void registerGoals() {
        try {
            this.goalSitWhenOrdered = new FoxPathfinderGoalSitWhenOrdered(this);
            this.goalSelector.addGoal(1, goalSitWhenOrdered);
            this.goalSleepWhenOrdered = new FoxPathfinderGoalSleepWhenOrdered(this);
            this.goalSelector.addGoal(1, goalSleepWhenOrdered);

            // Wild animal attacking
            Field landTargetGoal = this.getClass().getSuperclass().getDeclaredField("ck"); // landTargetGoal
            landTargetGoal.setAccessible(true);
            landTargetGoal.set(this, new NearestAttackableTargetGoal(this, Animal.class, 10, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && (entityliving instanceof Chicken || entityliving instanceof Rabbit);
            }));

            Field turtleEggTargetGoal = this.getClass().getSuperclass().getDeclaredField("cl"); // turtleEggTargetGoal
            turtleEggTargetGoal.setAccessible(true);
            turtleEggTargetGoal.set(this, new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));

            Field fishTargetGoal = this.getClass().getSuperclass().getDeclaredField("cm"); // fishTargetGoal
            fishTargetGoal.setAccessible(true);
            fishTargetGoal.set(this, new NearestAttackableTargetGoal(this, AbstractFish.class, 20, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && entityliving instanceof AbstractSchoolingFish;
            }));

            this.goalSelector.addGoal(0, getFoxInnerPathfinderGoal("g")); // FoxFloatGoal
            this.goalSelector.addGoal(1, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.goalSelector.addGoal(2, new FoxPathfinderGoalPanic(this, 2.2D));
            this.goalSelector.addGoal(2, new FoxPathfinderGoalSleepWithOwner(this));
            this.goalSelector.addGoal(3, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // FoxBreedGoal

            this.goalSelector.addGoal(4, new AvoidEntityGoal(this, Player.class, 16.0F, 1.6D, 1.4D, (entityliving) -> {
                return !isTamed() && AVOID_PLAYERS.test((LivingEntity) entityliving) && !this.isDefending();
            }));
            this.goalSelector.addGoal(4, new AvoidEntityGoal(this, Wolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !((Wolf)entityliving).isTame() && !this.isDefending();
            }));
            this.goalSelector.addGoal(4, new AvoidEntityGoal(this, PolarBear.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !this.isDefending();
            }));

            this.goalSelector.addGoal(5, getFoxInnerPathfinderGoal("u")); // StalkPreyGoal
            this.goalSelector.addGoal(6, new FoxPounceGoal());
            this.goalSelector.addGoal(7, getFoxInnerPathfinderGoal("l", Arrays.asList(1.2000000476837158D, true), Arrays.asList(double.class, boolean.class))); // FoxMeleeAttackGoal
            this.goalSelector.addGoal(8, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(Fox.class, double.class))); // FoxFollowParentGoal
            //this.goalSelector.addGoal(8, new FoxPathfinderGoalSleepWithOwner(this));
            this.goalSelector.addGoal(9, new FoxPathfinderGoalFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
            this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
            this.goalSelector.addGoal(11, new RandomStrollGoal(this, 1.0D));
            this.goalSelector.addGoal(11, getFoxInnerPathfinderGoal("p")); // FoxSearchForItemsGoal
            this.goalSelector.addGoal(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, Player.class, 24.0f),
                        Arrays.asList(Mob.class, Class.class, float.class))); // FoxLookAtPlayerGoal

            this.targetSelector.addGoal(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector.addGoal(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector.addGoal(3, (new FoxPathfinderGoalHurtByTarget(this)).setAlertOthers(new Class[0]));

            // Assign all the untamed goals that will later be removed.
            untamedGoals = new ArrayList<>();

            // SleepGoal
            Goal sleep = getFoxInnerPathfinderGoal("t");
            this.goalSelector.addGoal(7, sleep);
            untamedGoals.add(sleep);

            // PerchAndSearchGoal
            Goal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.goalSelector.addGoal(13, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            Goal eatBerries = new FoxEatBerriesGoal(1.2000000476837158D, 12, 2);
            this.goalSelector.addGoal(11, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            // SeekShelterGoal
            Goal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector.addGoal(6, seekShelter);
            untamedGoals.add(seekShelter);

            // FoxStrollThroughVillageGoal
            Goal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector.addGoal(9, strollThroughVillage);
            untamedGoals.add(strollThroughVillage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDefending() {
        try {
            Method method = Fox.class.getDeclaredMethod("fJ"); // isDefending
            method.setAccessible(true);
            boolean defending = (boolean) method.invoke((Fox) this);
            method.setAccessible(false);
            return defending;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setDefending(boolean defending) {
        try {
            Method method = Fox.class.getDeclaredMethod("A", boolean.class); // setDefending
            method.setAccessible(true);
            method.invoke((Fox) this, defending);
            method.setAccessible(false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(tamed, false);
        this.entityData.define(ownerUUID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID() == null) {
            compound.putUUID("OwnerUUID", new UUID(0L, 0L));
        } else {
            compound.putUUID("OwnerUUID", this.getOwnerUUID());
        }

        compound.putBoolean("Sitting", this.goalSitWhenOrdered.isOrderedToSit());
        compound.putBoolean("Sleeping", this.goalSleepWhenOrdered.isOrderedToSleep());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        UUID ownerUuid = null;

        if (compound.contains("OwnerUUID")) {
            try {
                ownerUuid = compound.getUUID("OwnerUUID");
            } catch (IllegalArgumentException e) {
                String uuidStr = compound.getString("OwnerUUID");
                if (!uuidStr.isEmpty()) {
                    ownerUuid = UUID.fromString(uuidStr);
                } else {
                    ownerUuid = null;
                }
            }
        }

        if (ownerUuid != null && !ownerUuid.equals(new UUID(0, 0))) {
            this.setOwnerUUID(ownerUuid);
            this.setTamed(true);
        } else {
            this.setTamed(false);
        }

        if (this.goalSitWhenOrdered != null) {
            this.goalSitWhenOrdered.setOrderedToSit(compound.getBoolean("Sitting"));
        }

        if (this.goalSleepWhenOrdered != null) {
            this.goalSleepWhenOrdered.setOrderedToSleep(compound.getBoolean("Sleeping"));
        }

        if (!this.isTamed()) {
            goalSitWhenOrdered.setOrderedToSit(false);
            goalSleepWhenOrdered.setOrderedToSleep(false);
        }
    }

    public boolean isTamed() {
        UUID ownerUuid = getOwnerUUID();
        return this.entityData.get(tamed) && (ownerUuid != null && !ownerUuid.equals(new UUID(0, 0)));
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(EntityTamableFox.tamed, tamed);
        this.reassessTameGoals();

        if (tamed) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        }
        this.setHealth(this.getMaxHealth());
    }

    // Remove untamed goals if its tamed.
    private void reassessTameGoals() {
        if (!isTamed()) return;

        for (Goal untamedGoal : untamedGoals) {
            this.goalSelector.removeGoal(untamedGoal);
        }
    }

    public void rename(org.bukkit.entity.Player player) {
        new AnvilGUI.Builder()
                .onComplete((plr, input) -> { // Called when the inventory output slot is clicked
                    if (!input.equals("")) {
                        org.bukkit.entity.Entity tamableFox = this.getBukkitEntity();

                        // This will auto format the name for config settings.
                        String foxName = LanguageConfig.getFoxNameFormat(input, player.getDisplayName());

                        tamableFox.setCustomName(foxName);
                        tamableFox.setCustomNameVisible(true);
                        plr.sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamingChosenPerfect(input));
                    }

                    return AnvilGUI.Response.close();
                })
                .preventClose()
                .text("Fox name")      // Sets the text the GUI should start with
                .plugin(Utils.tamableFoxesPlugin)          // Set the plugin instance
                .open(player);         // Opens the GUI for the player provided
    }

    @Override
    public InteractionResult mobInteract(Player entityhuman, InteractionHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Item item = itemstack.getItem();

        if (itemstack.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(entityhuman, enumhand);
        } else {
            if (this.isTamed()) {

                // Heal the fox if its health is below the max.
                if (item.isEdible() && item.getFoodProperties().isMeat() && this.getHealth() < this.getMaxHealth()) {
                    // Only remove the item from the player if they're in survival mode.
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.shrink(1);
                    }

                    this.heal((float)item.getFoodProperties().getNutrition(), EntityRegainHealthEvent.RegainReason.EATING);
                    return InteractionResult.CONSUME;
                }

                if (isOwnedBy(entityhuman) && enumhand == InteractionHand.MAIN_HAND) {
                    // This super method checks if the fox can breed or not.
                    InteractionResult flag = super.mobInteract(entityhuman, enumhand);

                    // If the player is not sneaking and the fox cannot breed, then make the fox sit.
                    // @TODO: Do I need to use this.eQ() instead of flag != EnumInteractionResult.SUCCESS?
                    if (!entityhuman.isCrouching() && (flag != InteractionResult.SUCCESS || this.isBaby())) {
                        // Show the rename menu again when trying to use a nametag on the fox.
                        if (itemstack.getItem() instanceof NameTagItem) {
                            org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                            rename(player);
                            return InteractionResult.PASS;
                        }

                        this.goalSleepWhenOrdered.setOrderedToSleep(false);
                        this.goalSitWhenOrdered.setOrderedToSit(!this.isOrderedToSit());
                        return InteractionResult.SUCCESS;
                    } else if (entityhuman.isCrouching()) { // Swap/Put/Take item from fox.
                        // Ignore buckets since they can be easily duplicated.
                        if (itemstack.getItem() instanceof BucketItem) {
                            return InteractionResult.PASS;
                        }

                        // If the fox has something in its mouth and the player has something in its hand, empty it.
                        if (this.hasItemInSlot(EquipmentSlot.MAINHAND)) {
                            getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(this.getItemBySlot(EquipmentSlot.MAINHAND)));
                            this.setSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.AIR), false);
                        } // Check if the player's hand is empty and if it is, make the fox sleep.
                          // The reason its here is to make sure that we don't take the item
                          // from its mouth and make it sleep in a single click.
                        else if (!entityhuman.hasItemInSlot(EquipmentSlot.MAINHAND)) {
                            this.goalSitWhenOrdered.setOrderedToSit(false);
                            this.goalSleepWhenOrdered.setOrderedToSleep(!this.goalSleepWhenOrdered.isOrderedToSleep());
                        }

                        // Run this task async to make sure to not slow the server down.
                        // This is needed due to the item being removed as soon as its put in the foxes mouth.
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Utils.tamableFoxesPlugin, ()-> {
                            // Put item in mouth
                            if (entityhuman.hasItemInSlot(EquipmentSlot.MAINHAND)) {
                                ItemStack c = itemstack.copy();
                                c.setCount(1);

                                // Only remove the item from the player if they're in survival mode.
                                org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                                if (player.getGameMode() != GameMode.CREATIVE ) {
                                    itemstack.shrink(1);
                                }

                                this.setSlot(EquipmentSlot.MAINHAND, c, false);
                            }
                        }, 1L);

                        return InteractionResult.SUCCESS;
                    }
                }
            } else if (item == Items.CHICKEN) {
                // Check if the player has permissions to tame the fox
                if (Config.canPlayerTameFox((org.bukkit.entity.Player) entityhuman.getBukkitEntity())) {
                    // Only remove the item from the player if they're in survival mode.
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.shrink(1);
                    }

                    SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
                    int maxTameCount = Config.getMaxPlayerFoxTames();
                    if ( !((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).hasPermission("tamablefoxes.tame.unlimited") && maxTameCount > 0 && sqLiteHelper.getPlayerFoxAmount(entityhuman.getUUID()) >= maxTameCount) {
                        ((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFoxDoesntTrust());

                        return InteractionResult.SUCCESS;
                    }

                    // 0.33% chance to tame the fox, also check if the called tame entity event is cancelled or not.
                    if (this.getRandom().nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
                        this.tame(entityhuman);

                        this.navigation.stop();
                        this.goalSitWhenOrdered.setOrderedToSit(true);

                        if (maxTameCount > 0) {
                            sqLiteHelper.addPlayerFoxAmount(entityhuman.getUUID(), 1);
                        }

                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.HEART, getBukkitEntity().getLocation(), 6, 0.5D, 0.5D, 0.5D);

                        // Give player tamed message.
                        ((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamedMessage());

                        // Let the player choose the new fox's name if its enabled in config.
                        if (Config.askForNameAfterTaming()) {
                            player.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getTamingAskingName());
                            rename(player);
                        }
                    } else {
                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, getBukkitEntity().getLocation(), 10, 0.2D, 0.2D, 0.2D, 0.15D);
                    }
                }

                return InteractionResult.SUCCESS;
            }

            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    public EntityTamableFox createChild(ServerLevel worldserver, AgeableMob entityageable) {
        EntityTamableFox entityfox = (EntityTamableFox) EntityType.FOX.create(worldserver);
        entityfox.setFoxType(this.getRandom().nextBoolean() ? this.getFoxType() : ((Fox)entityageable).getFoxType());

        UUID uuid = this.getOwnerUUID();
        if (uuid != null) {
            entityfox.setOwnerUUID(uuid);
            entityfox.setTamed(true);
        }

        return entityfox;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.entityData.get(ownerUUID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID ownerUuid) {
        this.entityData.set(ownerUUID, Optional.ofNullable(ownerUuid));
    }

    public void tame(Player owner) {
        this.setTamed(true);
        this.setOwnerUUID(owner.getUUID());

        // Give the player the taming advancement.
        if (owner instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) owner, this);
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID ownerUuid = this.getOwnerUUID();
            return ownerUuid == null ? null : this.getCommandSenderWorld().getPlayerByUUID(ownerUuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    // Only attack entity if its not attacking owner.
    @Override
    public boolean canAttack(LivingEntity entity) {
        return !this.isOwnedBy(entity) && super.canAttack(entity);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    /*
     deobf: wantsToAttack (Copied from EntityWolf)
     This code being from EntityWolf also means that wolves will want to attack foxes
     Our life would be so much easier if we could extend both EntityFox and EntityTameableAnimal
    */
    public boolean wantsToAttack(LivingEntity entityliving, LivingEntity entityliving1) {
        if (!(entityliving instanceof Creeper) && !(entityliving instanceof Ghast)) {
            if (entityliving instanceof EntityTamableFox) {
                EntityTamableFox entityFox = (EntityTamableFox) entityliving;
                return !entityFox.isTamed() || entityFox.getOwner() != entityliving1;
            } else {
                return (!(entityliving instanceof Player)
                        || !(entityliving1 instanceof Player) ||
                        ((Player) entityliving1).canHarmPlayer((Player) entityliving)) && ((!(entityliving instanceof AbstractHorse)
                        || !((AbstractHorse) entityliving).isTamed()) && (!(entityliving instanceof TamableAnimal)
                        || !((TamableAnimal) entityliving).isTame()));
            }
        } else {
            return false;
        }
    }

    // Set the scoreboard team to the same as the owner if its tamed.
    @Override
    public Team getTeam() {
        if (this.isTamed()) {
            LivingEntity var0 = this.getOwner();
            if (var0 != null) {
                return var0.getTeam();
            }
        }

        return super.getTeam();
    }

    // Override isAlliedTo (Entity::r(Entity))
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.isTamed()) {
            LivingEntity entityOwner = this.getOwner();
            if (entity == entityOwner) {
                return true;
            }

            if (entityOwner != null) {
                return entityOwner.isAlliedTo(entity);
            }
        }
        return super.isAlliedTo(entity);
    }

    // When the fox dies, show a chat message.
    @Override
    public void die(DamageSource damageSource) {
        if (!this.getCommandSenderWorld().isClientSide && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage(), getOwnerUUID());

            // Remove the amount of foxes the player has tamed if the limit is enabled.
            if (Config.getMaxPlayerFoxTames() > 0) {
                SQLiteHelper sqliteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
                sqliteHelper.removePlayerFoxAmount(this.getOwner().getUUID(), 1);
            }
        }

        super.die(damageSource);
    }


    private Goal getFoxInnerPathfinderGoal(String innerName, List<Object> args, List<Class<?>> argTypes) {
        return (Goal) Utils.instantiatePrivateInnerClass(Fox.class, innerName, this, args, argTypes);
    }

    private Goal getFoxInnerPathfinderGoal(String innerName) {
        return (Goal) Utils.instantiatePrivateInnerClass(Fox.class, innerName, this, Arrays.asList(), Arrays.asList());
    }

    public boolean isOrderedToSit() { return this.goalSitWhenOrdered.isOrderedToSit(); }

    public void setOrderedToSit(boolean flag) { this.goalSitWhenOrdered.setOrderedToSit(flag); }

    public boolean isOrderedToSleep() { return this.goalSleepWhenOrdered.isOrderedToSleep(); }

    public void setOrderedToSleep(boolean flag) { this.goalSleepWhenOrdered.setOrderedToSleep(flag); }
}
