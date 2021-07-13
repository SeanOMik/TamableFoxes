package net.seanomik.tamablefoxes.versions.version_1_16_R1.pathfinding;

import net.minecraft.server.v1_16_R1.*;
import net.seanomik.tamablefoxes.versions.version_1_16_R1.EntityTamableFox;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FoxPathfinderGoalSleepWithOwner extends PathfinderGoal {
    private final EntityTamableFox a;
    private EntityHuman b;
    private BlockPosition c;
    private int d;

    public FoxPathfinderGoalSleepWithOwner(EntityTamableFox tamableFox) {
        this.a = tamableFox;
    }

    public boolean a() {
        if (!this.a.isTamed()) {
            return false;
        } else if (this.a.isSitting()) {
            return false;
        } else {
            EntityLiving entityliving = this.a.getOwner();
            if (entityliving instanceof EntityHuman) {
                this.b = (EntityHuman)entityliving;
                if (!entityliving.isSleeping()) {
                    return false;
                }

                if (this.a.h(this.b) > 100.0D) {
                    return false;
                }

                BlockPosition blockposition = this.b.getChunkCoordinates();
                IBlockData iblockdata = this.a.world.getType(blockposition);
                if (iblockdata.getBlock().a(TagsBlock.BEDS)) {
                    this.c = (BlockPosition)iblockdata.d(BlockBed.FACING).map((enumdirection) -> {
                        return blockposition.shift(enumdirection.opposite());
                    }).orElseGet(() -> {
                        return new BlockPosition(blockposition);
                    });
                    return !this.g();
                }
            }

            return false;
        }
    }

    private boolean g() {
        List<EntityTamableFox> list = this.a.world.a(EntityTamableFox.class, (new AxisAlignedBB(this.c)).g(2.0D));
        Iterator iterator = list.iterator();

        EntityTamableFox entityTamableFox;
        do {
            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entityTamableFox = (EntityTamableFox) iterator.next();
            } while(entityTamableFox == this.a);
        } while(!entityTamableFox.isSleeping()); // !entityTamableFox.eY()

        return true;
    }

    public boolean b() {
        //!this.a.isWillSit()
        return this.a.isTamed() && !this.a.isSitting() && this.b != null && this.b.isSleeping() && this.c != null && !this.g();
    }

    public void c() {
        if (this.c != null) {
            this.a.setSitting(false);
            this.a.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), 1.100000023841858D);
        }

    }

    public void d() {
        this.a.setSleeping(false);
        this.d = 0;
        this.a.getNavigation().o();
    }

    public void e() {
        if (this.b != null && this.c != null) {
            this.a.setSitting(false);
            this.a.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), 1.100000023841858D);
            if (this.a.h(this.b) < 2.5D) {
                ++this.d;
                if (this.d > 16) {
                    this.a.setSleeping(true);
                    //this.a.y(false); // setRelaxStateOne
                } else {
                    this.a.a(this.b, 45.0F, 45.0F);
                    //this.a.y(true); // setRelaxStateOne
                }
            } else {
                this.a.setSleeping(false);
                //this.a.x(false);
            }
        }

    }
}
