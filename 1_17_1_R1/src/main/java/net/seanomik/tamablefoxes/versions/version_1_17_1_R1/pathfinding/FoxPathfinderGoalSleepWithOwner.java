package net.seanomik.tamablefoxes.versions.version_1_17_1_R1.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.seanomik.tamablefoxes.versions.version_1_17_1_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityDropItemEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
                BlockState iblockdata = this.fox.level.getBlockState(blockposition);
                if (iblockdata.is(BlockTags.BEDS)) {
                    this.goalPos = (BlockPos)iblockdata.getOptionalValue(BedBlock.FACING).map((enumdirection) -> {
                        return blockposition.shift(enumdirection.getOpposite());
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
        List<EntityTamableFox> list = this.fox.level.getEntitiesOfClass(EntityTamableFox.class, (new AABB(this.goalPos)).inflate(2.0D));
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
        float f = this.fox.level.getTimeOfDay(1.0F);
        if (this.ownerPlayer.getSleepTimer() >= 100 && (double)f > 0.77D && (double)f < 0.8D && (double)this.fox.level.getRandom().nextFloat() < 0.7D) {
            this.giveMorningGift();
        }

        this.onBedTicks = 0;
        this.fox.getNavigation().stop();
    }

    private void giveMorningGift() {
        Random random = this.fox.getRandom();
        BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
        blockposition_mutableblockposition.set(this.fox.blockPosition());
        this.fox.randomTeleport((double)(blockposition_mutableblockposition.getX() + random.nextInt(11) - 5), (double)(blockposition_mutableblockposition.getY() + random.nextInt(5) - 2), (double)(blockposition_mutableblockposition.getZ() + random.nextInt(11) - 5), false);
        blockposition_mutableblockposition.set(this.fox.blockPosition());
        LootTable loottable = this.fox.level.getServer().getLootTables().get(BuiltInLootTables.CAT_MORNING_GIFT);
        net.minecraft.world.level.storage.loot.LootContext.Builder loottableinfo_builder = (new net.minecraft.world.level.storage.loot.LootContext.Builder((ServerLevel)this.fox.level)).withParameter(LootContextParams.ORIGIN, this.fox.position()).withParameter(LootContextParams.THIS_ENTITY, this.fox).withRandom(random);
        List<ItemStack> list = loottable.getRandomItems(loottableinfo_builder.create(LootContextParamSets.GIFT));
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            ItemStack itemstack = (ItemStack)iterator.next();
            ItemEntity entityitem = new ItemEntity(this.fox.level, (double)blockposition_mutableblockposition.getX() - (double) Mth.sin(this.fox.yBodyRot * 0.017453292F), (double)blockposition_mutableblockposition.getY(), (double)blockposition_mutableblockposition.getZ() + (double)Mth.cos(this.fox.yBodyRot * 0.017453292F), itemstack);
            EntityDropItemEvent event = new EntityDropItemEvent(this.fox.getBukkitEntity(), (org.bukkit.entity.Item)entityitem.getBukkitEntity());
            entityitem.level.getCraftServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.fox.level.addFreshEntity(entityitem);
            }
        }

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
