package net.seanomik.tamablefoxes;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.CustomPathfinding.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Fox;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static net.seanomik.tamablefoxes.TamableFoxes.plugin;
import static net.seanomik.tamablefoxes.TamableFoxes.fileManager;

public class TamableFox extends EntityFox {
    private boolean isTamed;
    private EntityLiving owner;
    private UUID ownerUUID;
    private boolean sit = false;
    private static Fox thisFox;
    private BukkitTask sittingRunnable;

    protected FoxPathfinderGoalSit goalSit;

    @Override
    protected void initPathfinder() {
        this.goalSit = new FoxPathfinderGoalSit(this);

        this.goalSelector.a(1, new FoxPathfinderGoalFloat(this));

        this.goalSelector.a(2, this.goalSit);

        this.goalSelector.a(3, new FoxPathfinderGoalMeleeAttack(this, 1.2000000476837158D, true)); // l | Lunging
        this.goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D, (entityliving) -> {
            return !((EntityWolf)entityliving).isTamed();
        }));
        this.goalSelector.a(4, new FoxPathfinderGoalFollowOwner(this, 1.35D, 10.0F, 2.0F));

        this.goalSelector.a(4, new FoxPathfinderGoalLungeUNKNOWN_USE(this)); // u | Lunging
        this.goalSelector.a(5, new FoxPathfinderGoalLunge(this)); // o | Lunging

        this.goalSelector.a(5, new FoxPathfinderGoalFleeSun(this, 1.25D));
        this.goalSelector.a(5, new FoxPathfinderGoalBreed(this, 1.0D));

        this.goalSelector.a(7, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(7, new FoxPathfinderGoalBeg(this, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(9, new FoxPathfinderGoalPickBushes(this, 1.2000000476837158D, 12, 2));
        this.goalSelector.a(10, new FoxPathfinderGoalRandomStrollLand(this, 1D));

        this.targetSelector.a(1, new FoxPathfinderGoalOwnerHurtByTarget(this));
        this.targetSelector.a(2, new FoxPathfinderGoalOwnerHurtTarget(this));

        PathfinderGoal targetsGoal = new PathfinderGoalNearestAttackableTarget(this, EntityLiving.class, 10, false, false, (entityliving) -> {
            return entityliving instanceof EntityChicken || entityliving instanceof EntityRabbit;
        });

        this.targetSelector.a(4, targetsGoal);
        this.targetSelector.a(5, (new FoxPathfinderGoalHurtByTarget(this, new Class[0])).a(new Class[0]));
    }

    public TamableFox(EntityTypes entitytypes, World world) {
        super(EntityTypes.FOX, world);

        thisFox = (Fox) this.getBukkitEntity();
        TamableFoxes.foxUUIDs.put(this.getBukkitEntity().getUniqueId(), null);
    }

    //@Override
    //protected void a(DifficultyDamageScaler difficultydamagescaler) { } // Doesn't spawn with any items in its mouth

    @Override
    protected void initAttributes() {
        this.getAttributeMap().b(GenericAttributes.MAX_HEALTH);
        this.getAttributeMap().b(GenericAttributes.KNOCKBACK_RESISTANCE);
        this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED);
        this.getAttributeMap().b(GenericAttributes.ARMOR);
        this.getAttributeMap().b(GenericAttributes.ARMOR_TOUGHNESS);

        this.getAttributeMap().b(GenericAttributes.FOLLOW_RANGE).setValue(2.0D);
        this.getAttributeMap().b(GenericAttributes.ATTACK_KNOCKBACK);

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D);
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(24.0D);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(32.0D);
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE).setValue(3.0D);
    }

    @Override
    public EntityFox createChild(EntityAgeable entityageable) {
        WorldServer world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        Location location = entityageable.getBukkitEntity().getLocation();
        net.minecraft.server.v1_14_R1.Entity b = TamableFoxes.customType.b(world,
                null,
                null,
                null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()),
                null, false, false);

        EntityFox entityfox = (EntityFox) b;
        entityfox.setFoxType(this.random.nextBoolean() ? this.getFoxType() : ((EntityFox)entityageable).getFoxType());

        return entityfox;
    }

    // Pick up items
    @Override
    protected void a(EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItemStack();
        if (!isTamed() && !CraftEventFactory.callEntityPickupItemEvent(this, entityitem, itemstack.getCount() - 1, !this.g(itemstack)).isCancelled()) {
            try {
                int i = itemstack.getCount();
                if (i > 1) {
                    Method method = this.getClass().getSuperclass().getDeclaredMethod("l", ItemStack.class);
                    method.setAccessible(true);
                    method.invoke(this, itemstack.cloneAndSubtract(i - 1));
                    method.setAccessible(false);
                }
                Method method = this.getClass().getSuperclass().getDeclaredMethod("k", ItemStack.class);
                method.setAccessible(true);
                method.invoke(this, this.getEquipment(EnumItemSlot.MAINHAND));
                method.setAccessible(false);

                this.setSlot(EnumItemSlot.MAINHAND, itemstack.cloneAndSubtract(1));
                this.dropChanceHand[EnumItemSlot.MAINHAND.b()] = 2.0F;
                this.receive(entityitem, itemstack.getCount());
                entityitem.die();

                Field field = this.getClass().getSuperclass().getDeclaredField("bO");
                field.setAccessible(true);
                field.set(this, 0);
                field.setAccessible(false);
            } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isTamed() {
        return isTamed;
    }

    public void setOwner(EntityLiving owner) {
        this.owner = owner;
        fileManager.getConfig("foxes.yml").set("Foxes." + thisFox.getUniqueId() + ".owner", owner.getUniqueIDString());
        fileManager.saveConfig("foxes.yml");
        sittingRunnable = new UpdateFoxRunnable(plugin).runTask(plugin);
    }

    public EntityLiving getOwner() {
        return owner;
    }

    public void setTamed(Boolean tamed) {
        this.isTamed = tamed;
    }

    public boolean toggleSitting() {
        sit = !sit;
        sittingRunnable = new UpdateFoxRunnable(plugin).runTask(plugin);
        return sit;
    }

    public void updateFox() {
        sittingRunnable = new UpdateFoxRunnable(plugin).runTask(plugin);
    }

    @Nullable
    @Override
    public GroupDataEntity prepare(GeneratorAccess generatoraccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity, @Nullable NBTTagCompound nbttagcompound) {
        BiomeBase biomebase = generatoraccess.getBiome(new BlockPosition(this));
        EntityFox.Type entityfox_type = EntityFox.Type.a(biomebase);
        boolean flag = false;
        if (groupdataentity instanceof EntityFox.i) {
            entityfox_type = ((EntityFox.i)groupdataentity).a;
            if (((EntityFox.i)groupdataentity).b >= 2) {
                flag = true;
            } else {
                ++((EntityFox.i)groupdataentity).b;
            }
        } else {
            groupdataentity = new EntityFox.i(entityfox_type);
            ++((EntityFox.i)groupdataentity).b;
        }

        this.setFoxType(entityfox_type);
        if (flag) {
            this.setAgeRaw(-24000);
        }

        this.initPathfinder();
        this.a(difficultydamagescaler);

        // From EntityInsentient
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).addModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, AttributeModifier.Operation.MULTIPLY_BASE));
        if (this.random.nextFloat() < 0.05F) {
            this.p(true);
        } else {
            this.p(false);
        }

        return groupdataentity;
    }

    private class UpdateFoxRunnable extends BukkitRunnable {
        private final JavaPlugin plugin;

        public UpdateFoxRunnable(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            TamableFox.this.goalSit.setSitting(TamableFox.this.sit);
            TamableFox.thisFox.setVelocity(new Vector(0,0,0));

            TamableFox.this.setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);

            // Set custom name
            if (TamableFox.this.owner != null && fileManager.getConfig("config.yml").get().getBoolean("show-owner-in-fox-name")) {
                getBukkitEntity().setCustomName("Tamed by: " + TamableFox.this.owner.getName());
                getBukkitEntity().setCustomNameVisible(true);
            }
        }
    }

    public boolean isJumping() {
        return jumping;
    }
}
