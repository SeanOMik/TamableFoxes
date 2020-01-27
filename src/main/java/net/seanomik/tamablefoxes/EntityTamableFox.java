package net.seanomik.tamablefoxes;

import com.mojang.datafixers.Dynamic;
import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.versions.version_1_15.pathfinding.*;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EntityTamableFox extends EntityFox {

    private boolean tamed;
    private boolean sitting;
    private boolean sleeping;
    private String chosenName;
    private EntityLiving owner;
    private UUID ownerUUID;

    private FoxPathfinderGoalSit goalSit;
    private PathfinderGoal goalRandomSitting;
    private PathfinderGoal goalBerryPicking;
    private PathfinderGoal goalFleeSun;
    private PathfinderGoal goalNearestVillage;

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytypes, World world) {
        super(entitytypes, world);
    }

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName, List<Object> args, List<Class<?>> argTypes) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, args, argTypes);
    }

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, Arrays.asList(), Arrays.asList());
    }

    @Override
    protected void initPathfinder() {
        try {
            this.goalSit = new FoxPathfinderGoalSit(this);
            this.goalSelector.a(0, getFoxInnerPathfinderGoal("g")); // Swim
            this.goalSelector.a(1, this.goalSit);
            this.goalSelector.a(1, getFoxInnerPathfinderGoal("b")); // Unknown

            // Panic
            this.goalSelector.a(2, new FoxPathfinderGoalPanic(this, 2.2D));

            // Breed
            this.goalSelector.a(3, getFoxInnerPathfinderGoal("n", Arrays.asList(1.0D), Arrays.asList(double.class)));

            // Avoid human only if not tamed
            this.goalSelector.a(4, new PathfinderGoalAvoidTarget(this, EntityHuman.class, 16.0F, 1.6D, 1.4D, (entityliving) -> !tamed));

            // Avoid wolf if it is not tamed
            this.goalSelector.a(4, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
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
            this.goalSelector.a(4, new FoxPathfinderGoalMeleeAttack(this, 1.2000000476837158D, true));
            this.goalSelector.a(5, new FoxPathfinderGoalFollowOwner(this, 1.3D, 10.0F, 2.0F, false));
            this.goalSelector.a(6, getFoxInnerPathfinderGoal("u")); // Lunge shake
            this.goalSelector.a(7, new EntityFox.o()); // Lunge

            // Flee sun
            goalFleeSun = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector.a(7, goalFleeSun);

            this.goalSelector.a(8, getFoxInnerPathfinderGoal("t")); // Sleeping under trees
            this.goalSelector.a(9, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // Follow parent

            // Nearest village
            goalNearestVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector.a(9, goalNearestVillage);

            // Pick berry bushes
            goalBerryPicking = new EntityFox.f(1.2000000476837158D, 12, 2);
            this.goalSelector.a(10, goalBerryPicking);

            this.goalSelector.a(10, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.goalSelector.a(11, new PathfinderGoalRandomStrollLand(this, 1.15D));

            this.goalSelector.a(11, getFoxInnerPathfinderGoal("p")); // If a item is on the ground, go to it and take it
            this.goalSelector.a(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0f), Arrays.asList(EntityInsentient.class, Class.class, float.class))); // Look at player

            // The random sitting(?)
            this.goalRandomSitting = getFoxInnerPathfinderGoal("r");
            this.goalSelector.a(13, goalRandomSitting);

            this.targetSelector.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector.a(3, (new FoxPathfinderGoalHurtByTarget(this, new Class[0])).a(new Class[0]));

            // Wild animal attacking
            this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(this, EntityLiving.class, 10, false, false,
                    entityLiving -> (!tamed || (Config.doesTamedAttackWildAnimals() && tamed)) &&  (
                            entityLiving instanceof EntityChicken ||
                            entityLiving instanceof EntityRabbit ||
                            (entityLiving instanceof EntityTurtle && EntityTurtle.bw.test((EntityLiving) entityLiving)) ||
                            entityLiving instanceof EntityFishSchool)));
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
        this.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE).setValue(16.0D);
        this.getAttributeMap().b(GenericAttributes.ATTACK_KNOCKBACK);

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);

        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
    }

    public boolean isOtherFoxFamily(EntityLiving living) {
        if (living instanceof EntityTamableFox) {
            EntityTamableFox tamableFox = (EntityTamableFox) living;

            return (tamableFox.isTamed() && tamableFox.getOwner().getUniqueID() == this.getOwner().getUniqueID());
        } else {
            return false;
        }
    }

    public void setTamed(boolean tamed) {
        this.tamed = tamed;

        // Remove goals that are not needed when named, or defeats the purpose of taming
        this.goalSelector.a(goalRandomSitting);
        this.goalSelector.a(goalBerryPicking);
        this.goalSelector.a(goalFleeSun);
        this.goalSelector.a(goalNearestVillage);
    }

    public boolean isTamed() {
        return tamed;
    }

    public void setOwner(EntityLiving entityLiving) {
        this.owner = entityLiving;
        updateFoxVisual();
    }

    public EntityLiving getOwner() {
        return owner;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setChosenName(String name) {
        this.chosenName = name;
        updateFoxVisual();
    }

    public String getChosenName() {
        return chosenName;
    }

    public void setMouthItem(ItemStack item) {
        item.setCount(1);
        setSlot(EnumItemSlot.MAINHAND, item);

        save();
    }

    public void setMouthItem(org.bukkit.inventory.ItemStack item) {
        ItemStack itemNMS = CraftItemStack.asNMSCopy(item);
        setMouthItem(itemNMS);
    }

    public ItemStack getMouthItem() {
        return getEquipment(EnumItemSlot.MAINHAND);
    }

    public Item dropMouthItem() {
        Item droppedItem = getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation().add(0, 0, 0), CraftItemStack.asBukkitCopy(getMouthItem()));
        setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));

        return droppedItem;
    }

    @Override
    public void setSitting(boolean sit) {
        super.setSitting(sit);

        if (sleeping) {
            sleeping = false;
            super.setSleeping(false);
        }

        updateFoxVisual();
    }

    public void setHardSitting(boolean sit) {
        super.setSitting(sit);
        this.sitting = sit;

        if (sleeping) {
            sleeping = false;
            super.setSleeping(false);
        }

        updateFoxVisual();
    }

    public boolean toggleSitting() {
        this.sitting = !this.sitting;
        setHardSitting(sitting);

        return this.sitting;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public void updateFoxVisual() {
        new UpdateFoxRunnable().runTask(TamableFoxes.getPlugin());
    }

    private class UpdateFoxRunnable extends BukkitRunnable {
        UpdateFoxRunnable() {

        }

        public void run() {
            goalSit.setSitting(sitting);

            if (tamed) {
                getBukkitEntity().setCustomName((chosenName != null ? chosenName : "")
                        + (owner != null && Config.doesShowOwnerFoxName() ? ChatColor.RESET + " (" + owner.getName() + ")" : ""));
                getBukkitEntity().setCustomNameVisible(Config.doesShowNameTags());
            }
        }
    }


    // Used for all the nasty stuff below.
    private static boolean isLevelAtLeast(NBTTagCompound tag, int level) {
        return tag.hasKey("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
    }

    // To remove a call to initializePathFinderGoals()
    // This is all from every super class that has a method like this.
    // This was needed because you cant call a "super.super.method()"
    @Override
    public void a(NBTTagCompound nbttagcompound) {
        try {
            // EntityLiving
            this.setAbsorptionHearts(nbttagcompound.getFloat("AbsorptionAmount"));
            if (nbttagcompound.hasKeyOfType("Attributes", 9) && this.world != null && !this.world.isClientSide) {
                GenericAttributes.a(this.getAttributeMap(), nbttagcompound.getList("Attributes", 10));
            }

            if (nbttagcompound.hasKeyOfType("ActiveEffects", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("ActiveEffects", 10);

                for(int i = 0; i < nbttaglist.size(); ++i) {
                    NBTTagCompound nbttagcompound1 = nbttaglist.getCompound(i);
                    MobEffect mobeffect = MobEffect.b(nbttagcompound1);
                    if (mobeffect != null) {
                        this.effects.put(mobeffect.getMobEffect(), mobeffect);
                    }
                }
            }

            if (nbttagcompound.hasKey("Bukkit.MaxHealth")) {
                NBTBase nbtbase = nbttagcompound.get("Bukkit.MaxHealth");
                if (nbtbase.getTypeId() == 5) {
                    this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(((NBTTagFloat)nbtbase).asDouble());
                } else if (nbtbase.getTypeId() == 3) {
                    this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(((NBTTagInt)nbtbase).asDouble());
                }
            }

            if (nbttagcompound.hasKeyOfType("Health", 99)) {
                this.setHealth(nbttagcompound.getFloat("Health"));
            }

            this.hurtTicks = nbttagcompound.getShort("HurtTime");
            this.deathTicks = nbttagcompound.getShort("DeathTime");
            this.hurtTimestamp = nbttagcompound.getInt("HurtByTimestamp");
            if (nbttagcompound.hasKeyOfType("Team", 8)) {
                String s = nbttagcompound.getString("Team");
                ScoreboardTeam scoreboardteam = this.world.getScoreboard().getTeam(s);
                boolean flag = scoreboardteam != null && this.world.getScoreboard().addPlayerToTeam(this.getUniqueIDString(), scoreboardteam);
                if (!flag) {
                    LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", s);
                }
            }

            if (nbttagcompound.getBoolean("FallFlying")) {
                this.setFlag(7, true);
            }

            if (nbttagcompound.hasKeyOfType("SleepingX", 99) && nbttagcompound.hasKeyOfType("SleepingY", 99) && nbttagcompound.hasKeyOfType("SleepingZ", 99)) {
                BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("SleepingX"), nbttagcompound.getInt("SleepingY"), nbttagcompound.getInt("SleepingZ"));
                this.d(blockposition);
                this.datawatcher.set(POSE, EntityPose.SLEEPING);
                if (!this.justCreated) {
                    this.a(blockposition);
                }
            }

            if (nbttagcompound.hasKeyOfType("Brain", 10)) {
                this.bo = this.a(new Dynamic(DynamicOpsNBT.a, nbttagcompound.get("Brain")));
            }
            // EntityInsentient
            NonNullList<ItemStack> by = (NonNullList<ItemStack>) Utils.getPrivateFieldValue(EntityInsentient.class, "by", this);
            NonNullList<ItemStack> bx = (NonNullList<ItemStack>) Utils.getPrivateFieldValue(EntityInsentient.class, "bx", this);

            boolean data;
            if (nbttagcompound.hasKeyOfType("CanPickUpLoot", 1)) {
                data = nbttagcompound.getBoolean("CanPickUpLoot");
                if (isLevelAtLeast(nbttagcompound, 1) || data) {
                    this.setCanPickupLoot(data);
                }
            }

            data = nbttagcompound.getBoolean("PersistenceRequired");
            if (isLevelAtLeast(nbttagcompound, 1) || data) {
                this.persistent = data;
            }

            NBTTagList nbttaglist;
            int i;
            if (nbttagcompound.hasKeyOfType("ArmorItems", 9)) {
                nbttaglist = nbttagcompound.getList("ArmorItems", 10);

                for(i = 0; i < by.size(); ++i) {
                    by.set(i, ItemStack.a(nbttaglist.getCompound(i)));
                }
            }

            if (nbttagcompound.hasKeyOfType("HandItems", 9)) {
                nbttaglist = nbttagcompound.getList("HandItems", 10);

                for(i = 0; i < bx.size(); ++i) {
                    bx.set(i, ItemStack.a(nbttaglist.getCompound(i)));
                }
            }

            if (nbttagcompound.hasKeyOfType("ArmorDropChances", 9)) {
                nbttaglist = nbttagcompound.getList("ArmorDropChances", 5);

                for(i = 0; i < nbttaglist.size(); ++i) {
                    this.dropChanceArmor[i] = nbttaglist.i(i);
                }
            }

            if (nbttagcompound.hasKeyOfType("HandDropChances", 9)) {
                nbttaglist = nbttagcompound.getList("HandDropChances", 5);

                for(i = 0; i < nbttaglist.size(); ++i) {
                    this.dropChanceHand[i] = nbttaglist.i(i);
                }
            }

            if (nbttagcompound.hasKeyOfType("Leash", 10)) {
                //this.bG = nbttagcompound.getCompound("Leash");
                Utils.setPrivateFieldValue(EntityInsentient.class, "bG", this, nbttagcompound.getCompound("Leash"));
            }

            this.p(nbttagcompound.getBoolean("LeftHanded"));
            if (nbttagcompound.hasKeyOfType("DeathLootTable", 8)) {
                this.lootTableKey = new MinecraftKey(nbttagcompound.getString("DeathLootTable"));
                this.lootTableSeed = nbttagcompound.getLong("DeathLootTableSeed");
            }

            this.setNoAI(nbttagcompound.getBoolean("NoAI"));
            // EntityAgeable
            this.setAgeRaw(nbttagcompound.getInt("Age"));
            this.c = nbttagcompound.getInt("ForcedAge");
            this.ageLocked = nbttagcompound.getBoolean("AgeLocked");
            // EntityAnimal
            this.loveTicks = nbttagcompound.getInt("InLove");
            this.breedCause = nbttagcompound.b("LoveCause") ? nbttagcompound.a("LoveCause") : null;

            NBTTagList foxNBTTagList = nbttagcompound.getList("TrustedUUIDs", 10);

            Method method = this.getClass().getSuperclass().getDeclaredMethod("b", UUID.class);
            method.setAccessible(true);
            for (int index = 0; index < foxNBTTagList.size(); ++index) {
                //this.b(GameProfileSerializer.b(nbttaglist.getCompound(i)));
                method.invoke(this, GameProfileSerializer.b(foxNBTTagList.getCompound(index)));
            }
            method.setAccessible(false);

            this.setSleeping(nbttagcompound.getBoolean("Sleeping"));
            this.setFoxType(EntityFox.Type.a(nbttagcompound.getString("Type")));

            // Use super class due to the new set sitting causing errors
            super.setSitting(nbttagcompound.getBoolean("Sitting"));

            this.setCrouching(nbttagcompound.getBoolean("Crouching"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // To remove the last call to initializePathFinderGoals()
    // Cant just override because its a private method
    @Override
    @Nullable
    public GroupDataEntity prepare(GeneratorAccess generatoraccess, DifficultyDamageScaler difficultydamagescaler,
                                   EnumMobSpawn enummobspawn, GroupDataEntity groupdataentity, NBTTagCompound nbttagcompound) {
        BiomeBase biomebase = generatoraccess.getBiome(new BlockPosition(this));
        Type entityfox_type = Type.a(biomebase);
        boolean flag = false;
        if (groupdataentity instanceof i) {
            entityfox_type = ((i) groupdataentity).a;
            if (((i) groupdataentity).a() >= 2) {
                flag = true;
            } else {
                ((i) groupdataentity).b();
            }
        } else {
            groupdataentity = new i(entityfox_type);
            ((i) groupdataentity).b();
        }

        this.setFoxType(entityfox_type);
        if (flag) {
            this.setAgeRaw(-24000);
        }

        this.initPathfinder();
        this.a(difficultydamagescaler);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).addModifier(new AttributeModifier("Random spawn bonus",
                this.random.nextGaussian() * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
        if (this.random.nextFloat() < 0.05F) {
            this.p(true);
        } else {
            this.p(false);
        }

        return groupdataentity;
    }

    public void save() {
        NamespacedKey rootKey = new NamespacedKey(TamableFoxes.getPlugin(), "tamableFoxes");
        CraftPersistentDataContainer persistentDataContainer = getBukkitEntity().getPersistentDataContainer();
        PersistentDataContainer tamableFoxesData;
        if (persistentDataContainer.has(rootKey, PersistentDataType.TAG_CONTAINER)) {
            tamableFoxesData = persistentDataContainer.get(rootKey, PersistentDataType.TAG_CONTAINER);
        } else {
            tamableFoxesData = persistentDataContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey ownerKey = new NamespacedKey(TamableFoxes.getPlugin(), "owner");
        NamespacedKey chosenNameKey = new NamespacedKey(TamableFoxes.getPlugin(), "chosenName");
        NamespacedKey sittingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sitting");
        NamespacedKey sleepingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sleeping");
        tamableFoxesData.set(ownerKey, PersistentDataType.STRING, getOwner() == null ? "none" : getOwner().getUniqueID().toString());
        tamableFoxesData.set(chosenNameKey, PersistentDataType.STRING, getChosenName());
        tamableFoxesData.set(sittingKey, PersistentDataType.BYTE, (byte) (isSitting() ?  1 : 0));
        tamableFoxesData.set(sleepingKey, PersistentDataType.BYTE, (byte) (isSleeping() ?  1 : 0));

        persistentDataContainer.set(rootKey, PersistentDataType.TAG_CONTAINER, tamableFoxesData);
    }
}
