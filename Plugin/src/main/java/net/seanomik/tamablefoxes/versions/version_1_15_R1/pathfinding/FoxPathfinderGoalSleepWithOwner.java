package net.seanomik.tamablefoxes.versions.version_1_15_R1.pathfinding;

import net.minecraft.server.v1_15_R1.*;
import net.seanomik.tamablefoxes.versions.version_1_15_R1.EntityTamableFox;

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
        } else if (this.a.isSitting()) { // this.a.isWillSit()
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

                BlockPosition blockposition = new BlockPosition(this.b);
                IBlockData iblockdata = this.a.world.getType(blockposition);
                if (iblockdata.getBlock().a(TagsBlock.BEDS)) {
                    EnumDirection enumdirection = (EnumDirection)iblockdata.get(BlockBed.FACING);
                    this.c = new BlockPosition(blockposition.getX() - enumdirection.getAdjacentX(), blockposition.getY(), blockposition.getZ() - enumdirection.getAdjacentZ());
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
        float f = this.a.world.f(1.0F);
        if (this.b.ef() >= 100 && (double)f > 0.77D && (double)f < 0.8D && (double)this.a.world.getRandom().nextFloat() < 0.7D) {
            this.h();
        }

        this.d = 0;
        //this.a.v(false); // setRelaxStateOne
        this.a.getNavigation().o();
    }

    private void h() {
        Random random = this.a.getRandom();
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        blockposition_mutableblockposition.g(this.a.getChunkCoordinates());
        this.a.a((double)(blockposition_mutableblockposition.getX() + random.nextInt(11) - 5), (double)(blockposition_mutableblockposition.getY() + random.nextInt(5) - 2), (double)(blockposition_mutableblockposition.getZ() + random.nextInt(11) - 5), false);
        blockposition_mutableblockposition.g(this.a.getChunkCoordinates());
        LootTable loottable = this.a.world.getMinecraftServer().getLootTableRegistry().getLootTable(LootTables.ak);
        LootTableInfo.Builder loottableinfo_builder = (new LootTableInfo.Builder((WorldServer)this.a.world)).set(LootContextParameters.POSITION, blockposition_mutableblockposition).set(LootContextParameters.THIS_ENTITY, this.a).a(random);
        List<ItemStack> list = loottable.populateLoot(loottableinfo_builder.build(LootContextParameterSets.GIFT));
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            ItemStack itemstack = (ItemStack)iterator.next();
            this.a.world.addEntity(new EntityItem(this.a.world, (double)blockposition_mutableblockposition.getX() - (double)MathHelper.sin(this.a.aH * 0.017453292F), (double)blockposition_mutableblockposition.getY(), (double)blockposition_mutableblockposition.getZ() + (double)MathHelper.cos(this.a.aH * 0.017453292F), itemstack));
        }

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
