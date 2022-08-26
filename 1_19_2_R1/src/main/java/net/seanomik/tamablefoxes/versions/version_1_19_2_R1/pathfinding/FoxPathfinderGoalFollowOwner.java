package net.seanomik.tamablefoxes.versions.version_1_19_2_R1.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.seanomik.tamablefoxes.versions.version_1_19_2_R1.EntityTamableFox;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.EnumSet;

public class FoxPathfinderGoalFollowOwner extends Goal {
    public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final EntityTamableFox tamableFox;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FoxPathfinderGoalFollowOwner(EntityTamableFox entityfox, double d0, float f, float f1, boolean flag) {
        this.tamableFox = entityfox;
        this.level = entityfox.level;
        this.speedModifier = d0;
        this.navigation = entityfox.getNavigation();
        this.startDistance = f;
        this.stopDistance = f1;
        this.canFly = flag;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        if (!(entityfox.getNavigation() instanceof GroundPathNavigation) && !(entityfox.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FoxPathfinderGoalFollowOwner");
        }
    }

    public boolean canUse() {
        LivingEntity entityliving = this.tamableFox.getOwner();
        if (entityliving == null) {
            return false;
        } else if (entityliving.isSpectator()) {
            return false;
        } else if (this.tamableFox.isOrderedToSit() || this.tamableFox.isOrderedToSleep()) {
            return false;
        } else if (this.tamableFox.distanceToSqr(entityliving) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = entityliving;
            return true;
        }
    }

    public boolean canContinueToUse() {
        return !this.navigation.isDone() && (!this.tamableFox.isOrderedToSit() && !this.tamableFox.isOrderedToSleep() && this.tamableFox.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance));
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamableFox.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamableFox.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamableFox.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    public void tick() {
        this.tamableFox.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamableFox.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.tamableFox.isLeashed() && !this.tamableFox.isPassenger()) {
                if (this.tamableFox.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }

    }

    private void teleportToOwner() {
        BlockPos blockposition = this.owner.blockPosition();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int i, int j, int k) {
        if (Math.abs((double)i - this.owner.getX()) < 2.0D && Math.abs((double)k - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(i, j, k))) {
            return false;
        } else {
            CraftEntity entity = this.tamableFox.getBukkitEntity();
            Location to = new Location(entity.getWorld(), (double)i + 0.5D, (double)j, (double)k + 0.5D, this.tamableFox.getYRot(), this.tamableFox.getXRot());
            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
            this.tamableFox.level.getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            } else {
                to = event.getTo();
                this.tamableFox.moveTo(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                this.navigation.stop();
                return true;
            }
        }
    }

    private boolean canTeleportTo(BlockPos blockposition) {
        BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockposition.mutable());
        if (pathtype != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState iblockdata = this.level.getBlockState(blockposition.below());
            if (!this.canFly && iblockdata.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockposition1 = blockposition.subtract(this.tamableFox.blockPosition());
                return this.level.noCollision(this.tamableFox, this.tamableFox.getBoundingBox().move(blockposition1));
            }
        }
    }

    private int randomIntInclusive(int i, int j) {
        return this.tamableFox.getRandom().nextInt(j - i + 1) + i;
    }
}
