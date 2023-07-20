package net.seanomik.tamablefoxes.versions.version_1_20_R1.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.seanomik.tamablefoxes.versions.version_1_20_R1.EntityTamableFox;

import java.util.Iterator;
import java.util.List;

// From class EntityCat#b
public class FoxPathfinderGoalSleepWithOwner extends Goal {
    private final EntityTamableFox fox;
    private Player ownerPlayer;
    private BlockPos goalPos;
    private int onBedTicks;

    public FoxPathfinderGoalSleepWithOwner(EntityTamableFox entityTamableFox) {
        this.fox = entityTamableFox;
    }

    public boolean canUse() {
        if (!this.fox.isTamed()) {
            return false;
        } else if (this.fox.isOrderedToSleep()) {
            return false;
        } else {
            LivingEntity entityliving = this.fox.getOwner();
            if (entityliving instanceof Player) {
                this.ownerPlayer = (Player)entityliving;
                if (!entityliving.isSleeping()) {
                    return false;
                }

                if (this.fox.distanceToSqr(this.ownerPlayer) > 100.0D) {
                    return false;
                }

                BlockPos blockposition = this.ownerPlayer.blockPosition();
                BlockState iblockdata = this.fox.level().getBlockState(blockposition);
                if (iblockdata.is(BlockTags.BEDS)) {
                    this.goalPos = (BlockPos)iblockdata.getOptionalValue(BedBlock.FACING).map((enumdirection) -> {
                        return blockposition.relative(enumdirection.getOpposite());
                    }).orElseGet(() -> {
                        return new BlockPos(blockposition);
                    });
                    return !this.spaceIsOccupied();
                }
            }

            return false;
        }
    }

    private boolean spaceIsOccupied() {
        List<EntityTamableFox> list = this.fox.level().getEntitiesOfClass(EntityTamableFox.class, (new AABB(this.goalPos)).inflate(2.0D));
        Iterator iterator = list.iterator();

        EntityTamableFox entityTamableFox;
        do {
            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entityTamableFox = (EntityTamableFox)iterator.next();
            } while(entityTamableFox == this.fox);
        } while(!entityTamableFox.isSleeping());// && !entityTamableFox.isRelaxStateOne());

        return true;
    }

    public boolean canContinueToUse() {
        return this.fox.isTamed() && !this.fox.isOrderedToSleep() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
    }

    public void start() {
        if (this.goalPos != null) {
            this.fox.setSitting(false);
            this.fox.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.100000023841858D);
        }

    }

    public void stop() {
        this.fox.setSleeping(false);
        this.onBedTicks = 0;
        this.fox.getNavigation().stop();
    }

    public void tick() {
        if (this.ownerPlayer != null && this.goalPos != null) {
            this.fox.setSitting(false);
            this.fox.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.100000023841858D);
            if (this.fox.distanceToSqr(this.ownerPlayer) < 2.5D) {
                ++this.onBedTicks;
                if (this.onBedTicks > 16) {
                    this.fox.setSleeping(true);
                } else {
                    this.fox.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                }
            } else {
                this.fox.setSleeping(false);
            }
        }
    }
}
