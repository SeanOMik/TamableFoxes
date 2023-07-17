package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;

import java.util.Iterator;
import java.util.List;

// From class EntityCat#b
public class FoxPathfinderGoalSleepWithOwner extends PathfinderGoal {
    private final EntityTamableFox fox;
    private EntityHuman ownerPlayer;
    private BlockPosition goalPos;
    private int onBedTicks;

    public FoxPathfinderGoalSleepWithOwner(EntityTamableFox entityTamableFox) {
        this.fox = entityTamableFox;
    }

    @SuppressWarnings("resource")
	@Override
    public boolean a() { //canUse
        if (!this.fox.isTamed()) {
            return false;
        } else if (this.fox.isOrderedToSleep()) {
            return false;
        } else {
            EntityLiving entityliving = this.fox.getOwner();
            if (entityliving instanceof EntityHuman) {
                this.ownerPlayer = (EntityHuman)entityliving;
                if (!entityliving.fy()) {
                    return false;
                }

                if (this.fox.f(this.ownerPlayer) > 100.0D) {
                    return false;
                }

                BlockPosition blockposition = this.ownerPlayer.aD();
                IBlockData iblockdata = this.fox.dI().a_(blockposition);
                if (iblockdata.a(TagsBlock.R)) {
                    this.goalPos = (BlockPosition)iblockdata.d(BlockBed.aC).map((enumdirection) -> {
                        return blockposition.a(enumdirection.g());
                    }).orElseGet(() -> {
                        return new BlockPosition(blockposition);
                    });
                    return !this.spaceIsOccupied();
                }
            }

            return false;
        }
    }

    @SuppressWarnings("resource")
	private boolean spaceIsOccupied() {
        List<EntityTamableFox> list = this.fox.dI().a(EntityTamableFox.class, (new AxisAlignedBB(this.goalPos)).g(2.0D));
        Iterator iterator = list.iterator();

        EntityTamableFox entityTamableFox;
        do {
            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entityTamableFox = (EntityTamableFox)iterator.next();
            } while(entityTamableFox == this.fox);
        } while(!entityTamableFox.fy());// && !entityTamableFox.isRelaxStateOne());

        return true;
    }

    @Override
    public boolean b() {
        return this.fox.isTamed() && !this.fox.isOrderedToSleep() && this.ownerPlayer != null && this.ownerPlayer.fy() && this.goalPos != null && !this.spaceIsOccupied();
    }

    @Override
    public void c() {
        if (this.goalPos != null) {
            this.fox.w(false);
            this.fox.J().a((double)this.goalPos.u(), (double)this.goalPos.v(), (double)this.goalPos.w(), 1.100000023841858D);
        }

    }

    @Override
    public void d() {
        this.fox.C(false);
        this.onBedTicks = 0;
        this.fox.J().n();
    }

    @Override
    public void e() {
        if (this.ownerPlayer != null && this.goalPos != null) {
            this.fox.w(false);
            this.fox.J().a((double)this.goalPos.u(), (double)this.goalPos.v(), (double)this.goalPos.w(), 1.100000023841858D);
            if (this.fox.f(this.ownerPlayer) < 2.5D) {
                ++this.onBedTicks;
                if (this.onBedTicks > 16) {
                    this.fox.C(true);
                } else {
                    this.fox.a(this.ownerPlayer, 45.0F, 45.0F);
                }
            } else {
                this.fox.C(false);
            }
        }
    }
}
