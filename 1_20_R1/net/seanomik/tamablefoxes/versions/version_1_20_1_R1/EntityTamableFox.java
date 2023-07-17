package net.seanomik.tamablefoxes.versions.version_1_20_1_R1;

import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.monster.EntityGhast;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.EntityFox;
import net.minecraft.world.level.World;
import net.minecraft.world.scores.ScoreboardTeamBase;
import net.seanomik.tamablefoxes.util.ConversationUtil;
import net.seanomik.tamablefoxes.util.ConversationUtil.ConversationListener;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import net.seanomik.tamablefoxes.util.io.sqlite.SQLiteHelper;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import eu.d0by.utils.Common;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

public class EntityTamableFox extends EntityFox {

    protected static final DataWatcherObject<Boolean> tamed;
    protected static final DataWatcherObject<Optional<UUID>> ownerUUID;

    //private static final DataWatcherObject<Byte> bw; // DATA_FLAGS_ID
    private static final Predicate<Entity> AVOID_PLAYERS; // AVOID_PLAYERS

    static {
        tamed = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.k);
        ownerUUID = DataWatcher.a(EntityTamableFox.class, DataWatcherRegistry.q);

        AVOID_PLAYERS = (entity) -> !entity.bU();// && EntitySelector.test(entity);
    }

    List<PathfinderGoal> untamedGoals;
    private FoxPathfinderGoalSitWhenOrdered goalSitWhenOrdered;
    private FoxPathfinderGoalSleepWhenOrdered goalSleepWhenOrdered;

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytype, World world) {
        super(entitytype, world);

        this.a(GenericAttributes.d).a(0.33000001192092896D); // Set movement speed
        if (isTamed()) {
            this.a(GenericAttributes.a).a(24.0D);
            this.a(GenericAttributes.f).a(3.0D);
            this.t(this.eI());
        } else {
            this.a(GenericAttributes.a).a(10.0D);
            this.a(GenericAttributes.f).a(2.0D);
        }

        this.setTamed(false);
    }

    @Override
    public void x() { //registerGoals
        try {
            this.goalSitWhenOrdered = new FoxPathfinderGoalSitWhenOrdered(this);
            this.bO.a(1, goalSitWhenOrdered);
            this.goalSleepWhenOrdered = new FoxPathfinderGoalSleepWhenOrdered(this);
            this.bO.a(1, goalSleepWhenOrdered);

            // For reflection, we must use the non remapped names, since this is done at runtime
            // and the user will be using a normal spigot jar.

            // Wild animal attacking
            Field landTargetGoal = this.getClass().getSuperclass().getDeclaredField("ck"); // landTargetGoal kk
            landTargetGoal.setAccessible(true);
            landTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityAnimal.class, 10, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && (entityliving instanceof EntityChicken || entityliving instanceof EntityRabbit);
            }));

            Field turtleEggTargetGoal = this.getClass().getSuperclass().getDeclaredField("cl"); // turtleEggTargetGoal kk
            turtleEggTargetGoal.setAccessible(true);
            turtleEggTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityTurtle.class, 10, false, false, EntityTurtle.bU));

            Field fishTargetGoal = this.getClass().getSuperclass().getDeclaredField("cm"); // fishTargetGoal kk
            fishTargetGoal.setAccessible(true);
            fishTargetGoal.set(this, new PathfinderGoalNearestAttackableTarget(this, EntityFish.class, 20, false, false, (entityliving) -> {
                return (!isTamed() || (Config.doesTamedAttackWildAnimals() && isTamed())) && entityliving instanceof EntityFishSchool;
            }));

            this.bO.a(0, getFoxInnerPathfinderGoal("g")); // FoxFloatGoal
            this.bO.a(1, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.bO.a(2, new FoxPathfinderGoalPanic(this, 2.2D));
            this.bO.a(2, new FoxPathfinderGoalSleepWithOwner(this));
            this.bO.a(3, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // FoxBreedGoal

            this.bO.a(4, new PathfinderGoalAvoidTarget(this, EntityHuman.class, 16.0F, 1.6D, 1.4D, (entityliving) -> {
                return !isTamed() && AVOID_PLAYERS.test((EntityLiving) entityliving) && !this.isDefending();
            }));
            this.bO.a(4, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !((EntityWolf)entityliving).q() && !this.isDefending();
            }));
            this.bO.a(4, new PathfinderGoalAvoidTarget(this, EntityPolarBear.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
                return !this.isDefending();
            }));

            this.bO.a(5, getFoxInnerPathfinderGoal("u")); // StalkPreyGoal
            this.bO.a(6, new o());
            this.bO.a(7, getFoxInnerPathfinderGoal("l", Arrays.asList(1.2000000476837158D, true), Arrays.asList(double.class, boolean.class))); // FoxMeleeAttackGoal
            this.bO.a(8, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // FoxFollowParentGoal
            //this.bO.a(8, new FoxPathfinderGoalSleepWithOwner(this));
            this.bO.a(9, new FoxPathfinderGoalFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
            this.bO.a(10, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.bO.a(11, new PathfinderGoalRandomStroll(this, 1.0D));
            this.bO.a(11, getFoxInnerPathfinderGoal("p")); // FoxSearchForItemsGoal
            this.bO.a(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0f),
                        Arrays.asList(EntityInsentient.class, Class.class, float.class))); // LookAtPlayer

            this.bP.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.bP.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.bP.a(3, (new FoxPathfinderGoalHurtByTarget(this)).setAlertOthers(new Class[0]));

            // Assign all the untamed goals that will later be removed.
            untamedGoals = new ArrayList<>();

            // SleepGoal
            PathfinderGoal sleep = getFoxInnerPathfinderGoal("t");
            this.bO.a(7, sleep);
            untamedGoals.add(sleep);

            // PerchAndSearchGoal
            PathfinderGoal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.bO.a(13, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            PathfinderGoal eatBerries = new f(1.2000000476837158D, 12, 2);
            this.bO.a(11, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            // SeekShelterGoal
            PathfinderGoal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.bO.a(6, seekShelter);
            untamedGoals.add(seekShelter);

            // FoxStrollThroughVillageGoal
            PathfinderGoal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.bO.a(9, strollThroughVillage);
            untamedGoals.add(strollThroughVillage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected DataWatcherObject<Byte> getDataFlagsId() throws NoSuchFieldException, IllegalAccessException {
        Field dataFlagsField = EntityFox.class.getDeclaredField("bY"); // DATA_FLAGS_ID kk
        dataFlagsField.setAccessible(true);
        DataWatcherObject<Byte> dataFlagsId = (DataWatcherObject<Byte>) dataFlagsField.get(null);
        dataFlagsField.setAccessible(false);

        return dataFlagsId;
    }

    protected boolean getFlag(int i) {
        try {
            DataWatcherObject<Byte> dataFlagsId = getDataFlagsId();

            return ((Byte)super.am.b(dataFlagsId) & i) != 0;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return false;
    }

    protected void setFlag(int i, boolean flag) {
        try {
            DataWatcherObject<Byte> dataFlagsId = getDataFlagsId();

            if (flag) {
                this.am.b(dataFlagsId, (byte)((Byte)this.am.b(dataFlagsId) | i));
            } else {
                this.am.b(dataFlagsId, (byte)((Byte)this.am.b(dataFlagsId) & ~i));
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public boolean isDefending() {
        return getFlag(128);
    }

    public void setDefending(boolean defending) {
        setFlag(128, defending);
    }

    @Override
    protected void a_() { //defineSynchedData
        super.a_();
        this.am.a(tamed, false);
        this.am.a(ownerUUID, Optional.empty());
    }

    @Override
    public void b(NBTTagCompound compound) { //addAdditionalSaveData
        super.b(compound);
        if (this.getOwnerUUID() == null) {
            compound.a("OwnerUUID", new UUID(0L, 0L));
        } else {
            compound.a("OwnerUUID", this.getOwnerUUID());
        }

        compound.a("Sitting", this.goalSitWhenOrdered.isOrderedToSit());
        compound.a("Sleeping", this.goalSleepWhenOrdered.isOrderedToSleep());
    }

    @Override
    public void a(NBTTagCompound compound) { //readAdditionalSaveData
        super.a(compound);
        UUID ownerUuid = null;

        if (compound.e("OwnerUUID")) {
            try {
                ownerUuid = compound.a("OwnerUUID");
            } catch (IllegalArgumentException e) {
                String uuidStr = compound.l("OwnerUUID");
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
            this.goalSitWhenOrdered.setOrderedToSit(compound.q("Sitting"));
        }

        if (this.goalSleepWhenOrdered != null) {
            this.goalSleepWhenOrdered.setOrderedToSleep(compound.q("Sleeping"));
        }

        if (!this.isTamed()) {
            goalSitWhenOrdered.setOrderedToSit(false);
            goalSleepWhenOrdered.setOrderedToSleep(false);
        }
    }

    public boolean isTamed() {
        UUID ownerUuid = getOwnerUUID();
        return this.am.b(tamed) && (ownerUuid != null && !ownerUuid.equals(new UUID(0, 0)));
    }

    public void setTamed(boolean tamed) {
        this.am.b(EntityTamableFox.tamed, tamed);
        this.reassessTameGoals();

        if (tamed) {
            this.a(GenericAttributes.a).a(24.0D);
            this.a(GenericAttributes.f).a(3.0D);
        } else {
            this.a(GenericAttributes.a).a(10.0D);
            this.a(GenericAttributes.f).a(2.0D);
        }
        this.t(this.eI());
    }

    // Remove untamed goals if its tamed.
    private void reassessTameGoals() {
        if (!isTamed()) return;

        for (PathfinderGoal untamedGoal : untamedGoals) {
            this.bO.a(untamedGoal);
        }
    }

    public void rename(org.bukkit.entity.Player player) {
        ConversationUtil.getInstance().createConversation( player, new ConversationListener()
        {
            public boolean onMessage( String input )
            {
            	if( !input.isBlank() )
            	{
                    org.bukkit.entity.Entity tamableFox = getBukkitEntity();

                    // This will auto format the name for config settings.
                    String foxName = LanguageConfig.getFoxNameFormat( input, player.getDisplayName() );

                    tamableFox.setCustomName( foxName );
                    tamableFox.setCustomNameVisible( true );
                    if( !LanguageConfig.getTamingChosenPerfect( input ).equalsIgnoreCase("disabled") )
                    	player.sendMessage( Common.colorize( Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamingChosenPerfect( input ) ) );
                    
                    return false;
                }
            	else
            		return true;
            }

            public void onExit()
            {
            }
        } );
    	/*
        new AnvilGUI.Builder()
                .onComplete((plr, input) -> { // Called when the inventory output slot is clicked
                    if (!input.equals("")) {
                        org.bukkit.entity.Entity tamableFox = this.getBukkitEntity();

                        // This will auto format the name for config settings.
                        String foxName = LanguageConfig.getFoxNameFormat(input, player.getDisplayName());

                        tamableFox.setCustomName(foxName);
                        tamableFox.setCustomNameVisible(true);
                        if (!LanguageConfig.getTamingChosenPerfect(input).equalsIgnoreCase("disabled")) {
                            plr.sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamingChosenPerfect(input));
                        }
                    }

                    //return AnvilGUI.Response.close();
                    return Arrays.asList(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("Fox name")      // Sets the text the GUI should start with
                .plugin(Utils.tamableFoxesPlugin)          // Set the plugin instance
                .open(player);         // Opens the GUI for the player provided
        */
    }

    @Override
    public EnumInteractionResult b(EntityHuman entityhuman, EnumHand enumhand) { //mobInteract
        ItemStack itemstack = entityhuman.b(enumhand);
        Item item = itemstack.d();

        if (itemstack.d() instanceof ItemMonsterEgg) {
            return super.b(entityhuman, enumhand);
        } else {
            if (this.isTamed()) {

                // Heal the fox if its health is below the max.
                if (item.u() && item.v().c() && this.er() < this.eI()) {
                    // Only remove the item from the player if they're in survival mode.
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.h(1);
                    }

                    this.heal((float)item.v().a(), EntityRegainHealthEvent.RegainReason.EATING);
                    return EnumInteractionResult.b;
                }

                if (isOwnedBy(entityhuman) && enumhand == EnumHand.a) {
                    // This super method checks if the fox can breed or not.
                    EnumInteractionResult flag = super.b(entityhuman, enumhand);

                    // If the player is not sneaking and the fox cannot breed, then make the fox sit.
                    // @TODO: Do I need to use this.eQ() instead of flag != EnumInteractionResult.SUCCESS?
                    if (!entityhuman.bU() && (flag != EnumInteractionResult.a || this.h_())) {
                        // Show the rename menu again when trying to use a nametag on the fox.
                        if (itemstack.d() instanceof ItemNameTag) {
                            org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                            rename(player);
                            return EnumInteractionResult.d;
                        }

                        this.goalSleepWhenOrdered.setOrderedToSleep(false);
                        this.goalSitWhenOrdered.setOrderedToSit(!this.isOrderedToSit());
                        return EnumInteractionResult.a;
                    } else if (entityhuman.bU()) { // Swap/Put/Take item from fox.
                        // Ignore buckets since they can be easily duplicated.
                        if (itemstack.d() instanceof ItemBucket) {
                            return EnumInteractionResult.d;
                        }

                        // If the fox has something in its mouth and the player has something in its hand, empty it.
                        if (this.b(EnumItemSlot.a)) {
                            getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(this.c(EnumItemSlot.a)));
                            this.setItemSlot(EnumItemSlot.a, new ItemStack(Items.a), false);
                        } // Check if the player's hand is empty and if it is, make the fox sleep.
                          // The reason its here is to make sure that we don't take the item
                          // from its mouth and make it sleep in a single click.
                        else if (!entityhuman.b(EnumItemSlot.a)) {
                            this.goalSitWhenOrdered.setOrderedToSit(false);
                            this.goalSleepWhenOrdered.setOrderedToSleep(!this.goalSleepWhenOrdered.isOrderedToSleep());
                        }

                        // Run this task async to make sure to not slow the server down.
                        // This is needed due to the item being removed as soon as its put in the foxes mouth.
                        Bukkit.getScheduler().runTaskLaterAsynchronously(Utils.tamableFoxesPlugin, ()-> {
                            // Put item in mouth
                            if (entityhuman.b(EnumItemSlot.a)) {
                                ItemStack c = itemstack.p();
                                c.f(1);

                                // Only remove the item from the player if they're in survival mode.
                                org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                                if (player.getGameMode() != GameMode.CREATIVE ) {
                                    itemstack.h(1);
                                }

                                this.setItemSlot(EnumItemSlot.a, c, false);
                            }
                        }, 1L);

                        return EnumInteractionResult.a;
                    }
                }
            } else if (item == Items.rn) {
                // Check if the player has permissions to tame the fox
                if (Config.canPlayerTameFox((org.bukkit.entity.Player) entityhuman.getBukkitEntity())) {
                    // Only remove the item from the player if they're in survival mode.
                    org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
                    if (player.getGameMode() != GameMode.CREATIVE ) {
                        itemstack.h(1);
                    }

                    SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
                    int maxTameCount = Config.getMaxPlayerFoxTames();
                    if ( !((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).hasPermission("tamablefoxes.tame.unlimited") && maxTameCount > 0 && sqLiteHelper.getPlayerFoxAmount(entityhuman.ct()) >= maxTameCount) {
                        if (!LanguageConfig.getFoxDoesntTrust().equalsIgnoreCase("disabled")) {
                            ((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getFoxDoesntTrust());
                        }

                        return EnumInteractionResult.a;
                    }

                    // 0.33% chance to tame the fox, also check if the called tame entity event is cancelled or not.
                    if (this.ec().a(3) == 0 && !CraftEventFactory.callEntityTameEvent(this, entityhuman).isCancelled()) {
                        this.tame(entityhuman);

                        this.bN.n();
                        this.goalSitWhenOrdered.setOrderedToSit(true);

                        if (maxTameCount > 0) {
                            sqLiteHelper.addPlayerFoxAmount(entityhuman.ct(), 1);
                        }

                        getBukkitEntity().getWorld().spawnParticle(org.bukkit.Particle.HEART, getBukkitEntity().getLocation(), 6, 0.5D, 0.5D, 0.5D);

                        // Give player tamed message.
                        if (!LanguageConfig.getTamedMessage().equalsIgnoreCase("disabled")) {
                            ((org.bukkit.entity.Player) entityhuman.getBukkitEntity()).sendMessage(Config.getPrefix() + ChatColor.GREEN + LanguageConfig.getTamedMessage());
                        }

                        // Let the player choose the new fox's name if its enabled in config.
                        if (Config.askForNameAfterTaming()) {
                            if (!LanguageConfig.getTamingAskingName().equalsIgnoreCase("disabled")) {
                                player.sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getTamingAskingName());
                            }
                            rename(player);
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
    public EntityTamableFox b(WorldServer worldserver, EntityAgeable entityageable) { //getBreedOffspring
        EntityTamableFox entityfox = (EntityTamableFox) EntityTypes.N.a(worldserver);
        entityfox.a(this.ec().h() ? this.r() : ((EntityFox)entityageable).r());

        UUID uuid = this.getOwnerUUID();
        if (uuid != null) {
            entityfox.setOwnerUUID(uuid);
            entityfox.setTamed(true);
        }

        return entityfox;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return (UUID) ((Optional) this.am.b(ownerUUID)).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID ownerUuid) {
        this.am.b(ownerUUID, Optional.ofNullable(ownerUuid));
    }

    public void tame(EntityHuman owner) {
        this.setTamed(true);
        this.setOwnerUUID(owner.ct());

        // Give the player the taming advancement.
        if (owner instanceof EntityPlayer) {
            CriterionTriggers.x.a((EntityPlayer) owner, this);
        }
    }

    @SuppressWarnings("resource")
	@Nullable
    public EntityLiving getOwner() {
        try {
            UUID ownerUuid = this.getOwnerUUID();
            return ownerUuid == null ? null : this.cH().b(ownerUuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    // Only attack entity if its not attacking owner.
    @Override
    public boolean c(EntityLiving entity) { //canAttack
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
                        || !((EntityHorseAbstract) entityliving).gn()) && (!(entityliving instanceof EntityTameableAnimal)
                        || !((EntityTameableAnimal) entityliving).q()));
            }
        } else {
            return false;
        }
    }

    // Set the scoreboard team to the same as the owner if its tamed.
    @Override
    public ScoreboardTeamBase cd() { //getTeam
        if (this.isTamed()) {
            EntityLiving var0 = this.getOwner();
            if (var0 != null) {
                return var0.cd();
            }
        }

        return super.cd();
    }

    // Override isAlliedTo (Entity::r(Entity))
    @Override
    public boolean p(Entity entity) { //isAlliedTo
        if (this.isTamed()) {
            EntityLiving entityOwner = this.getOwner();
            if (entity == entityOwner) {
                return true;
            }

            if (entityOwner != null) {
                return entityOwner.p(entity);
            }
        }
        return super.p(entity);
    }

    // When the fox dies, show a chat message, and remove the player's stored tamed foxed.
    @SuppressWarnings("resource")
	@Override
    public void a(DamageSource damageSource) { //die
        if (!this.cH().B && this.cH().X().b(GameRules.m) && this.getOwner() instanceof EntityPlayer) {
            //this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage(), getOwnerUUID());
            this.getOwner().a(this.eG().a());
        }

        // Remove the amount of foxes the player has tamed if the limit is enabled.
        if (Config.getMaxPlayerFoxTames() > 0) {
            SQLiteHelper sqliteHelper = SQLiteHelper.getInstance(Utils.tamableFoxesPlugin);
            sqliteHelper.removePlayerFoxAmount(this.getOwner().ct(), 1);
        }

        super.a(damageSource);
    }


    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName, List<Object> args, List<Class<?>> argTypes) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, args, argTypes);
    }

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, Arrays.asList(), Arrays.asList());
    }

    public boolean isOrderedToSit() { return this.goalSitWhenOrdered.isOrderedToSit(); }

    public void setOrderedToSit(boolean flag) { this.goalSitWhenOrdered.setOrderedToSit(flag); }

    public boolean isOrderedToSleep() { return this.goalSleepWhenOrdered.isOrderedToSleep(); }

    public void setOrderedToSleep(boolean flag) { this.goalSleepWhenOrdered.setOrderedToSleep(flag); }
}
