package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import net.seanomik.tamablefoxes.versions.version_1_15.pathfinding.*;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class EntityTamableFox extends EntityFox {

    List<PathfinderGoal> untamedGoals = new ArrayList<>();
    private boolean tamed;
    private boolean sitting;
    private EntityLiving owner;
    private UUID ownerUUID;
    private FoxPathfinderGoalSit goalSit;
    private String customName = "";

    public EntityTamableFox(EntityTypes<? extends EntityFox> entitytypes, World world) {
        super(entitytypes, world);

        clearPathFinderGoals();
        initPathfinderGoals();
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

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName, List<Object> args, List<Class<?>> argTypes) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, args, argTypes);
    }

    private PathfinderGoal getFoxInnerPathfinderGoal(String innerName) {
        return (PathfinderGoal) Utils.instantiatePrivateInnerClass(EntityFox.class, innerName, this, Arrays.asList(), Arrays.asList());
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

        // Default value is 10, might want to make this configurable in the future
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);

        // Default value is 2, might want to make this configurable in the future
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
    }

    private void initPathfinderGoals() {
        try {
            this.goalSelector.a(0, getFoxInnerPathfinderGoal("g")); // FloatGoal

            this.goalSit = new FoxPathfinderGoalSit(this);
            this.goalSelector.a(1, goalSit);

            this.goalSelector.a(1, getFoxInnerPathfinderGoal("b")); // FaceplantGoal
            this.goalSelector.a(2, new FoxPathfinderGoalPanic(this, 2.2D)); // PanicGoal
            this.goalSelector.a(3, getFoxInnerPathfinderGoal("e", Arrays.asList(1.0D), Arrays.asList(double.class))); // BreedGoal

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
            this.goalSelector.a(6, getFoxInnerPathfinderGoal("u")); // StalkPrey
            this.goalSelector.a(7, new EntityFox.o()); // Pounce

            PathfinderGoal seekShelter = getFoxInnerPathfinderGoal("s", Arrays.asList(1.25D), Arrays.asList(double.class));
            this.goalSelector.a(7, seekShelter); // SeekShelter
            untamedGoals.add(seekShelter);

            this.goalSelector.a(8, getFoxInnerPathfinderGoal("t")); // Sleep
            this.goalSelector.a(9, getFoxInnerPathfinderGoal("h", Arrays.asList(this, 1.25D), Arrays.asList(EntityFox.class, double.class))); // FollowParent

            PathfinderGoal strollThroughVillage = getFoxInnerPathfinderGoal("q", Arrays.asList(32, 200), Arrays.asList(int.class, int.class));
            this.goalSelector.a(9, strollThroughVillage); // StrollThroughVillage
            untamedGoals.add(strollThroughVillage);

            // EatBerries (Pick berry bushes)
            PathfinderGoal eatBerries = new EntityFox.f(1.2000000476837158D, 12, 2);
            this.goalSelector.a(10, eatBerries);
            untamedGoals.add(eatBerries); // Maybe this should be configurable too?

            this.goalSelector.a(10, new PathfinderGoalLeapAtTarget(this, 0.4F));
            this.goalSelector.a(11, new PathfinderGoalRandomStrollLand(this, 1.15D));

            this.goalSelector.a(11, getFoxInnerPathfinderGoal("p")); // SearchForItems
            this.goalSelector.a(12, getFoxInnerPathfinderGoal("j", Arrays.asList(this, EntityHuman.class, 24.0f), Arrays.asList(EntityInsentient.class, Class.class, float.class))); // LookAtPlayer

            // PerchAndSearch (Random sitting?)
            PathfinderGoal perchAndSearch = getFoxInnerPathfinderGoal("r");
            this.goalSelector.a(13, perchAndSearch);
            untamedGoals.add(perchAndSearch);

            this.targetSelector.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
            this.targetSelector.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));
            this.targetSelector.a(3, (new FoxPathfinderGoalHurtByTarget(this, new Class[0])).a(new Class[0]));

            // Wild animal attacking
            this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(this, EntityLiving.class, 10, false, false,
                    entityLiving -> (!tamed || (Config.doesTamedAttackWildAnimals() && tamed)) && (
                            entityLiving instanceof EntityChicken ||
                                    entityLiving instanceof EntityRabbit ||
                                    (entityLiving instanceof EntityTurtle && EntityTurtle.bw.test((EntityLiving) entityLiving)) ||
                                    entityLiving instanceof EntityFishSchool)));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public boolean isTamed() {
        return tamed;
    }

    public void setTamed(boolean tamed) {
        this.tamed = tamed;

        // Remove goals that are not needed when named, or defeats the purpose of taming
        try {
            untamedGoals.forEach(goal -> goalSelector.a(goal));
        } catch (Exception e) {}
    }

    public EntityLiving getOwner() {
        if (Objects.isNull(owner)) {
            if (ownerUUID == null) return null;
            OfflinePlayer opOwner = TamableFoxes.getPlugin().getServer().getOfflinePlayer(UUID.fromString(ownerUUID.toString()));
            if (opOwner.isOnline()) this.owner = (EntityLiving) ((CraftEntity) opOwner).getHandle();
        }
        return owner;
    }

    public void setOwner(EntityLiving entityLiving) {
        this.owner = entityLiving;
        this.ownerUUID = entityLiving.getUniqueID();
        updateFoxVisual();
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public boolean isOtherFoxFamily(EntityLiving living) {
        if (living instanceof EntityTamableFox) {
            EntityTamableFox tamableFox = (EntityTamableFox) living;

            return (tamableFox.isTamed() && tamableFox.getOwner() != null && tamableFox.getOwner().getUniqueID() == this.getOwner().getUniqueID());
        } else {
            return false;
        }
    }

    // This is needed for the updateFoxVisual runnable to set the foxes name.
    void setCustomName(String customName) {
        this.customName = customName;
        updateFoxVisual();
    }

    public void updateFoxVisual() {
        new BukkitRunnable() {
            @Override
            public void run() {
                goalSit.setSitting(sitting);

                if (tamed && owner != null && !customName.isEmpty()) {
                    if (Config.doesShowOwnerFoxName()) {
                        getBukkitEntity().setCustomName(LanguageConfig.getFoxNameFormat().replaceAll("%OWNER%", owner.getName()).replaceAll("%FOX_NAME%", customName));
                    } else {
                        getBukkitEntity().setCustomName(LanguageConfig.getFoxNameFormat().replaceAll("%FOX_NAME%", customName));
                    }
                }
            }
        }.runTask(TamableFoxes.getPlugin());
    }

    public void setHardSitting(boolean hardSitting) {
        super.setSitting(hardSitting);
        this.sitting = hardSitting;

        if (super.isSleeping()) super.setSleeping(false);

        updateFoxVisual();
    }


    public boolean toggleSitting() {
        this.sitting = !this.sitting;
        setHardSitting(sitting);

        return this.sitting;
    }

    public ItemStack getMouthItem() {
        return getEquipment(EnumItemSlot.MAINHAND);
    }

    public void setMouthItem(ItemStack item) {
        item.setCount(1);
        setSlot(EnumItemSlot.MAINHAND, item);
        saveNbt();
    }

    public void setMouthItem(org.bukkit.inventory.ItemStack item) {
        ItemStack itemNMS = CraftItemStack.asNMSCopy(item);
        setMouthItem(itemNMS);
    }

    public org.bukkit.entity.Item dropMouthItem() {
        Item droppedItem = getBukkitEntity().getWorld().dropItem(getBukkitEntity().getLocation(), CraftItemStack.asBukkitCopy(getMouthItem()));
        setSlot(EnumItemSlot.MAINHAND, new net.minecraft.server.v1_15_R1.ItemStack(Items.AIR));

        return droppedItem;
    }

    public void saveNbt() {
        NamespacedKey rootKey = new NamespacedKey(TamableFoxes.getPlugin(), "tamableFoxes");
        CraftPersistentDataContainer persistentDataContainer = getBukkitEntity().getPersistentDataContainer();
        PersistentDataContainer tamableFoxesData;
        if (persistentDataContainer.has(rootKey, PersistentDataType.TAG_CONTAINER)) {
            tamableFoxesData = persistentDataContainer.get(rootKey, PersistentDataType.TAG_CONTAINER);
        } else {
            tamableFoxesData = persistentDataContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey ownerKey = new NamespacedKey(TamableFoxes.getPlugin(), "owner");
        NamespacedKey sittingKey = new NamespacedKey(TamableFoxes.getPlugin(), "sitting");
        tamableFoxesData.set(ownerKey, PersistentDataType.STRING, getOwner() == null ? "none" : getOwner().getUniqueID().toString());
        tamableFoxesData.set(sittingKey, PersistentDataType.BYTE, (byte) (isSitting() ? 1 : 0));

        persistentDataContainer.set(rootKey, PersistentDataType.TAG_CONTAINER, tamableFoxesData);
    }
}
