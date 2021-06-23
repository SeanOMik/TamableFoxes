package net.seanomik.tamablefoxes.versions.version_1_17_R1;

import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.monster.EntityGhast;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerAbilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemMonsterEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.scores.ScoreboardTeamBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.animal.EntityFox;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import net.seanomik.tamablefoxes.util.io.sqlite.SQLiteHelper;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.pathfinding.*;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

public class EntityTamableFox extends EntityFox {

    protected static final DataWatcherObject<Byte> tamed;
    protected static final DataWatcherObject<Optional<UUID>> ownerUUID;

    private static final DataWatcherObject<Byte> bw; // DATA_FLAGS_ID
    private static final Predicate<Entity> bC; // AVOID_PLAYERS

    static {
        tamed = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.a);
        ownerUUID = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.o);

        bw = DataWatcher.a(EntityFox.class, DataWatcherRegistry.a);
        bC = (entity) -> !entity.isSneaking() && IEntitySelector.e.test(entity);
    }

    List<PathfinderGoal> untamedGoals;
    private FoxPathfinderGoalSit goalSit;

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytypes, World world) {
        super(entitytypes, world);

        System.out.println("Set default attributes");
        /*this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.33000001192092896D);
        if (isTamed()) {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
            this.setHealth(this.getMaxHealth());
        } else {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(10.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0D);
        }*/
    }

    @Override
    public void initPathfinder() {
        try {
            this.goalSit = new FoxPathfinderGoalSit(this);
            this.bO.a(1, goalSit);

            // Wild animal attacking
            Field landTargetGoal = this.getClass().getSuperclass().getDeclaredField("cj"); // landTargetGoal
            landTargetGoal.setAccessible(true);
            landTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityAnimal.class, 10, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && (entityliving instanceof EntityChicken || entityliving instanceof EntityRabbit);
            }));

            Field turtleEggTargetGoal = this.getClass().getSuperclass().getDeclaredField("ck"); // turtleEggTargetGoal
            turtleEggTargetGoal.setAccessible(true);
            turtleEggTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityTurtle.class, 10, false, false, EntityTurtle.bT));

            Field fishTargetGoal = this.getClass().getSuperclass().getDeclaredField("cl"); // fishTargetGoal
            fishTargetGoal.setAccessible(true);
            fishTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityFish.class, 20, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && entityliving instanceof EntityFishSchool;
            }));

            this.goalSelector().a(0, getFoxInnerPathfinderGoal("g")); // FoxFloatGoal
            this.goalSelector().a(1, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.goalSelector().a(2, new FoxPathfinderGoalPanic(this, 2.2D));
            this.goalSelector().a(2, new FoxPathfinderGoalSleepWithOwner(this));
            this.goalSelector().a(3, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // FoxBreedGoal

            this.goalSelector().a(4, new PathfinderGoalAvoidTarget(this, EntityHuman.class, 16.0F, 1.6D, 1.4D, (entityliving) -> {
                return !isTamed() && bC.test((EntityLiving) entityliving) && !this.isDefending();
            }));
            this.goalSelector().a(4, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !((EntityWolf)entityliving).isTamed() && !this.isDefending();
            }));
            this.goalSelector().a(4, new PathfinderGoalAvoidTarget(this, EntityPolarBear.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !this.isDefending();
            }));

            this.goalSelector().a(5, getFoxInnerPathfinderGoal("u")); // StalkPreyGoal
            this.goalSelector().a(6, new o()); // FoxPounceGoal
            this.goalSelector().a(7, getFoxInnerPathfinderGoal("l", Arrays.asList(1.2000000476837158D, true), Arrays.asList(double.class, boolean.class))); // FoxMeleeAttackGoal
            this.goalSelector().a(8, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // FoxFollowParentGoal
            this.goalSelector().a(8, new FoxPathfinderGoalSleepWithOwner(this));
            this.goalSelector().a(9, new FoxPathfinderGoalFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
            this.goalSelector().a(10, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.goalSelector().a(11, new PathfinderGoalRandomStrollLand(this, 1.0D));
            this.goalSelector().a(11, getFoxInnerPathfinderGoal("p")); // FoxSearchForItemsGoal
            this.goalSelector().a(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0f),
                        Arrays.asList(EntityInsentient.class, Class.class, float.class))); // LookAtPlayer

            this.targetSelector().a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector().a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector().a(3, (new FoxPathfinderGoalHurtByTarget(this)).a(new Class[0]));

            // Assign all the untamed goals that will later be removed.
            untamedGoals = new ArrayList<>();

            // SleepGoal
            PathfinderGoal sleep = getFoxInnerPathfinderGoal("t");
            this.goalSelector().a(7, sleep);
            untamedGoals.add(sleep);

            // PerchAndSearch (Random sitting?)
            PathfinderGoal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.goalSelector().a(13, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            // FoxEatBerriesGoal (Pick berry bushes)
            PathfinderGoal eatBerries = new f(1.2000000476837158D, 12, 2);
            this.goalSelector().a(11, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            // SeekShelterGoal
            PathfinderGoal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector().a(6, seekShelter);
            untamedGoals.add(seekShelter);

            // StrollThroughVillage
            PathfinderGoal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector().a(9, strollThroughVillage);
            untamedGoals.add(strollThroughVillage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataWatcher datawatcher() {
        return this.Y;
    }

    private PathfinderGoalSelector goalSelector() {
        return this.bO;
    }

    private PathfinderGoalSelector targetSelector() {
        return this.bP;
    }

    private Random random() {
        return this.Q;
    }

    // deobf: 'getFlag'
    private boolean u(int i) {
        return ((Byte)datawatcher().get(bw) & i) != 0;
    }
    
    // deobf: 'isDefending' from 'fI'
    public boolean isDefending() {
        return this.u(128);
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
        this.datawatcher().register(tamed, (byte) 0);
        this.datawatcher().register(ownerUUID, Optional.empty());
    }

    @Override
    public void saveData(NBTTagCompound compound) {
        super.saveData(compound);
        if (this.getOwnerUUID() == null) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.getOwnerUUID().toString());
        }

        compound.setBoolean("Sitting", this.isSitting());
    }

    @Override
    public void loadData(NBTTagCompound compound) {
        super.loadData(compound);
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
        return ((Byte) this.datawatcher().get(tamed) & 4) != 0;
    }

    public void setTamed(boolean tamed_) {
        byte isTamed = this.datawatcher().get(tamed);
        if (tamed_) {
            this.datawatcher().set(tamed, (byte) (isTamed | 4));
        } else {
            this.datawatcher().set(tamed, (byte) (isTamed & -5));
        }
        this.reassessTameGoals();

        System.out.println("setTamed set attributes");
        /*if (tamed_) {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
            this.setHealth(this.getMaxHealth());
        } else {
            this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(10.0D);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0D);
        }*/
    }

    // Remove untamed goals if its tamed.
    private void reassessTameGoals() {
        if (!isTamed()) return;

        for (PathfinderGoal untamedGoal : untamedGoals) {
            this.goalSelector().a(untamedGoal);
        }
    }

    // mobInteract
    @Override
    public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.b(enumhand);
        Item item = itemstack.getItem();

        if (itemstack.getItem() instanceof ItemMonsterEgg) {
            return super.b(entityhuman, enumhand);
        } else {
            if (this.isTamed()) {

                // Heal the fox if its health is below the max.
                if (item.isFood() && item.getFoodInfo().c() && this.getHealth() < this.getMaxHealth()) {
                    // Only remove the item from the player if they're in survival mode.
                    Player player = (Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.subtract(1);
                    }

                    this.heal((float)item.getFoodInfo().getNutrition(), EntityRegainHealthEvent.RegainReason.EATING);
                    return EnumInteractionResult.b; // CONSUME
                }

                if (isOwnedBy(entityhuman)) {
                    // This super method checks if the fox can breed or not.
                    EnumInteractionResult flag = super.b(entityhuman, enumhand);

                    // If the player is not sneaking and the fox cannot breed, then make the fox sit.
                    // @TODO: Do I need to use this.eQ() instead of flag != EnumInteractionResult.SUCCESS?
                    // EnumInteractionResult.a = EnumInteractionResult.SUCCESS
                    if (!entityhuman.isSneaking() && (flag != EnumInteractionResult.a || this.isBaby())) {
                        this.setSleeping(false);
                        this.goalSit.setSitting(!this.isSitting());
                        return flag;
                    } else if (entityhuman.isSneaking() && enumhand == EnumHand.a) { // EnumHand.a = EnumHand.MAIN_HAND; Swap/Put/Take item from fox.
                        // Ignore buckets since they can be easily duplicated.
                        // nW = BUCKET; nX = WATER_BUCKET; nY = LAVA_BUCKET
                        if (itemstack.getItem() == Items.nW || itemstack.getItem() == Items.nX || itemstack.getItem() == Items.nY) {
                            return EnumInteractionResult.c; // EnumInteractionResult.c = EnumInteractionResult.PASS
                        }

                        // Check if the player has something in their main hand.
                        // EnumItemSlot.MAINHAND = EnumItemSlot.a
                        if (!this.getEquipment(EnumItemSlot.a).isEmpty()) {
                            getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(this.getEquipment(EnumItemSlot.a)));
                            this.setSlot(EnumItemSlot.a, new ItemStack(Items.a)); // Items.a = AIR
                        }

                        // Run this task async to make sure to not slow the server down.
                        // This is needed due to the item being remove as soon as its put in the foxes mouth.
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Utils.tamableFoxesPlugin, ()-> {
                            // Put item in mouth
                            if (item != Items.a) { // Items.a = AIR
                                ItemStack c = itemstack.cloneItemStack();
                                c.setCount(1);

                                // Only remove the item from the player if they're in survival mode.
                                Player player = (Player) entityhuman.getBukkitEntity();
                                if (player.getGameMode() != GameMode.CREATIVE ) {
                                    itemstack.subtract(1);
                                }

                                this.setSlot(EnumItemSlot.a, c);
                            }
                            // If the player doesn't have anything in their hand, make the fox sleep or wakeup.
                            else {
                                this.goalSit.setSitting(false);
                                this.setSleeping(!this.isSleeping());
                            }
                        }, (long) 0.1);

                        return EnumInteractionResult.a;
                        //return true;
                    }
                }
            } else if (item == Items.px) { // px = CHICKEN
                // Check if the player has permissions to tame the fox
                if (Config.canPlayerTameFox((Player) entityhuman.getBukkitEntity())) {
                    // Only remove the item from the player if they're in survival mode.
                    Player player = (Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.subtract(1);
                    }

                    SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
                    int maxTameCount = Config.getMaxPlayerFoxTames();
                    if ( !((Player) entityhuman.getBukkitEntity()).hasPermission("tamablefoxes.tame.unlimited") && maxTameCount > 0 && sqLiteHelper.getPlayerFoxAmount(entityhuman.getUniqueID()) >= maxTameCount) {
                        ((Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFoxDoesntTrust());

                        return EnumInteractionResult.a;
                    }

                    // 0.33% chance to tame the fox, also check if the called tame entity event is cancelled or not.
                    if (this.getRandom().nextInt(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
                        this.tame(entityhuman);

                        // Remove all navigation when tamed.
                        this.bN.o(); // bN = navigation
                        this.setGoalTarget(null);
                        this.goalSit.setSitting(true);

                        if (maxTameCount > 0) {
                            sqLiteHelper.addPlayerFoxAmount(entityhuman.getUniqueID(), 1);
                        }

                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.HEART, getBukkitEntity().getLocation(), 6, 0.5D, 0.5D, 0.5D);

                        // Give player tamed message.
                        ((Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamedMessage());

                        // Let the player choose the new fox's name if its enabled in config.
                        if (Config.askForNameAfterTaming()) {
                            player.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getTamingAskingName());
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
                                    .text("Fox name")      // Sets the text the GUI should start with
                                    .plugin(Utils.tamableFoxesPlugin)          // Set the plugin instance
                                    .open(player);         // Opens the GUI for the player provided
                        }
                    } else {
                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, getBukkitEntity().getLocation(), 10, 0.2D, 0.2D, 0.2D, 0.15D);
                    }
                }

                return EnumInteractionResult.a;
            }

            return super.b(entityhuman, enumhand);
        }
    }

    @Override
    public EntityTamableFox createChild(WorldServer worldserver, EntityAgeable entityageable) {
        EntityTamableFox entityfox = (EntityTamableFox) EntityTypes.E.a(worldserver); // EntityTypes.E = EntityTypes.FOX
        entityfox.setFoxType(this.getRandom().nextBoolean() ? this.getFoxType() : ((EntityFox)entityageable).getFoxType());

        UUID uuid = this.getOwnerUUID();
        if (uuid != null) {
            entityfox.setOwnerUUID(uuid);
            entityfox.setTamed(true);
        }

        return entityfox;
    }

    @Override
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
        return (UUID) ((Optional) this.datawatcher().get(ownerUUID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID ownerUuid) {
        this.datawatcher().set(ownerUUID, Optional.ofNullable(ownerUuid));
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
            return ownerUuid == null ? null : this.getWorld().b(ownerUuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    // Only attack entity if its not attacking owner.
    // canAttack
    @Override
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
    public boolean wantsToAttack(EntityLiving entityliving, EntityLiving entityliving1) {
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
        // getWorld().y = getWorld().isClientSide; GameRules.m = GameRules.SHOW_DEATH_MESSAGES
        if (!this.getWorld().y && this.getWorld().getGameRules().getBoolean(GameRules.m) && this.getOwner() instanceof EntityPlayer) {
            this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage(), getOwnerUUID());

            // Remove the amount of foxes the player has tamed if the limit is enabled.
            if (Config.getMaxPlayerFoxTames() > 0) {
                SQLiteHelper sqliteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
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
        Set<?> goalSet = (Set<?>) getPrivateField("d", PathfinderGoalSelector.class, goalSelector());
        Set<?> targetSet = (Set<?>) getPrivateField("d", PathfinderGoalSelector.class, targetSelector());
        goalSet.clear();
        targetSet.clear();

        Map<?, ?> goalMap = (Map<?, ?>) getPrivateField("c", PathfinderGoalSelector.class, goalSelector());
        Map<?, ?> targetMap = (Map<?, ?>) getPrivateField("c", PathfinderGoalSelector.class, targetSelector());
        goalMap.clear();
        targetMap.clear();

        EnumSet<?> goalEnumSet = (EnumSet<?>) getPrivateField("f", PathfinderGoalSelector.class, goalSelector());
        EnumSet<?> targetEnumSet = (EnumSet<?>) getPrivateField("f", PathfinderGoalSelector.class, targetSelector());
        goalEnumSet.clear();
        targetEnumSet.clear();
    }
}
