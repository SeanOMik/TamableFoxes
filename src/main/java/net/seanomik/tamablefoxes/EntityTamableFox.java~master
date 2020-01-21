package net.seanomilk.tamablefoxes;

import net.minecraft.server.v1_15_R1.*;
import net.seanomilk.tamablefoxes.pathfinding.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.Fox;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EntityTamableFox extends EntityFox {

    private static Fox thisFox;
    private final TamableFoxes plugin;

    private boolean isTamed;
    private EntityLiving owner;
    private String chosenName;
    private boolean sit = false;
    private BukkitTask sittingRunnable;

    private FoxPathfindGoalSit goalSit;
    private PathfinderGoalNearestAttackableTarget goalAttack;

    public EntityTamableFox(TamableFoxes plugin, EntityTypes entitytypes, World world) {
        super(EntityTypes.FOX, world);
        this.plugin = plugin;
        thisFox = (Fox) this.getBukkitEntity();
        plugin.getFoxUUIDs().put(this.getBukkitEntity().getUniqueId(), null);
        this.setPersistent();
    }

    @Override
    protected void initPathfinder() {
        this.goalSit = new FoxPathfindGoalSit(this);
        this.goalSelector.a(1, new FoxPathfindGoalFloat(this));
        this.goalSelector.a(2, this.goalSit);
        this.goalSelector.a(3, new FoxPathfindGoalMeleeAttack(this, 1.2000000476837158D, true));
        this.goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityWolf.class, 8.0F, 1.6D, 1.4D,
                entityliving -> !((EntityWolf) entityliving).isTamed()));
        this.goalSelector.a(4, new FoxPathfindGoalFollowOwner(this, 1.35D, 10.0F, 2.0F));
        this.goalSelector.a(4, new FoxPathfindGoalLungeUNKNOWN_USE(this));
        this.goalSelector.a(5, new FoxPathfindGoalLunge(this));
        this.goalSelector.a(5, new FoxPathfindGoalFleeSun(this, 1.25D));
        this.goalSelector.a(5, new FoxPathfindGoalBreed(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(7, new FoxPathfindGoalBeg(this, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(9, new FoxPathfindGoalPickBushes(this, 1.2000000476837158D, 12, 2));
        this.goalSelector.a(9, new FoxPathfindGoalPickBushes(this, 1.2000000476837158D, 12, 2));
        this.goalSelector.a(10, new FoxPathfindGoalRandomStrollLand(this, 1.0D));
        this.targetSelector.a(1, new FoxPathfindGoalHurtByTarget(this));
        this.targetSelector.a(2, new FoxPathfindGoalOwnerHurtByTarget(this));

        this.goalAttack = new PathfinderGoalNearestAttackableTarget(this, EntityLiving.class, 10, false, false,
                entityLiving -> !isTamed && (entityLiving instanceof EntityChicken || entityLiving instanceof EntityRabbit));

        if (!isTamed || (plugin.isTamedAttackRabbitChicken() && isTamed)) {
            this.targetSelector.a(4, goalAttack);
        }

        this.targetSelector.a(5, new FoxPathfindGoalHurtByTarget(this).a(new Class[0]));
    }

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

    public EntityFox createChild(EntityAgeable entityageable) {
        WorldServer world = ((CraftWorld) entityageable.getBukkitEntity().getWorld()).getHandle();
        Location location = entityageable.getBukkitEntity().getLocation();
        Entity b = plugin.getCustomType().b(world, null, null, null,
                new BlockPosition(location.getX(), location.getY(), location.getZ()), null, false, false);
        EntityFox entityfox = (EntityFox) b;
        entityfox.setFoxType(this.random.nextBoolean() ? this.getFoxType() : ((EntityFox) entityageable).getFoxType());
        return entityfox;
    }

    protected void a(EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItemStack();
        if (!this.isTamed() && !CraftEventFactory.callEntityPickupItemEvent(this, entityitem,
                itemstack.getCount() - 1, !this.g(itemstack)).isCancelled()) {
            try {
                int i = itemstack.getCount();
                Method method;
                if (i > 1) {
                    method = this.getClass().getSuperclass().getDeclaredMethod("l", ItemStack.class);
                    method.setAccessible(true);
                    method.invoke(this, itemstack.cloneAndSubtract(i - 1));
                    method.setAccessible(false);
                }

                method = this.getClass().getSuperclass().getDeclaredMethod("k", ItemStack.class);
                method.setAccessible(true);
                method.invoke(this, this.getEquipment(EnumItemSlot.MAINHAND));
                method.setAccessible(false);
                this.setSlot(EnumItemSlot.MAINHAND, itemstack.cloneAndSubtract(1));
                this.dropChanceHand[EnumItemSlot.MAINHAND.b()] = 2.0F;
                this.receive(entityitem, itemstack.getCount());
                entityitem.die();
                Field field = this.getClass().getSuperclass().getDeclaredField("bL");
                field.setAccessible(true);
                field.set(this, 0);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

    }

    public TamableFoxes getPlugin() {
        return plugin;
    }

    public boolean isTamed() {
        return this.isTamed;
    }

    public void setTamed(boolean tamed) {
        this.isTamed = tamed;
        // Remove attack goal if tamed
        if (isTamed && plugin.isTamedAttackRabbitChicken()) {
            this.targetSelector.a(goalAttack);
        }  else {
            this.targetSelector.a(4, goalAttack);
        }
    }

    public String getChosenName() {
        return chosenName;
    }

    public void setChosenName(String chosenName) {
        this.chosenName = chosenName;
        plugin.getConfigFoxes().set("Foxes." + getUniqueID() + ".name", chosenName).save();
        updateFoxVisual();
    }

    public EntityLiving getOwner() {
        return this.owner;
    }

    public void setOwner(EntityLiving owner) {
        this.owner = owner;
        plugin.getConfigFoxes().set("Foxes." + getUniqueID() + ".owner", owner.getUniqueIDString()).save();
        updateFoxVisual();
    }

    public boolean toggleSitting() {
        this.sit = !this.sit;
        updateFoxVisual();
        return this.sit;
    }

    public void updateFox() {
        updateFoxVisual();
    }

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

    public boolean isJumping() {
        return this.jumping;
    }

    private void updateFoxVisual() {
        this.sittingRunnable = new UpdateFoxRunnable(plugin).runTask(plugin);
    }

    private class UpdateFoxRunnable extends BukkitRunnable {

        private final TamableFoxes plugin;

        UpdateFoxRunnable(TamableFoxes plugin) {
            this.plugin = plugin;
        }

        public void run() {
            goalSit.setSitting(sit);
            thisFox.setVelocity(new Vector(0, 0, 0));
            setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);

            getBukkitEntity().setCustomName((chosenName != null ? chosenName : "")
                    + (owner != null && plugin.isShowOwnerFoxName() ? ChatColor.RESET + " (" + owner.getName() + ")" : ""));
            getBukkitEntity().setCustomNameVisible(plugin.isShowNameTags());
        }
    }

}
