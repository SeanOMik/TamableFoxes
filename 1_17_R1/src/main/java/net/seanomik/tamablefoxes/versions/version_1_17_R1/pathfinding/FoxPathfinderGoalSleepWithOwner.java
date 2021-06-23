package net.seanomik.tamablefoxes.versions.version_1_17_R1.pathfinding;

import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.EntityTamableFox;
import org.bukkit.event.entity.EntityDropItemEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

// From class EntityCat#b
public class FoxPathfinderGoalSleepWithOwner extends PathfinderGoal {
    private final EntityTamableFox a;
    private EntityHuman b;
    private BlockPosition c;
    private int d;

    public FoxPathfinderGoalSleepWithOwner(EntityTamableFox entitycat) {
        this.a = entitycat;
    }

    public boolean a() {
        if (!this.a.isTamed()) {
            return false;
        } else if (this.a.isSitting()) {
            return false;
        } else {
            EntityLiving entityliving = this.a.getOwner();
            if (entityliving instanceof EntityHuman) {
                this.b = (EntityHuman) entityliving;
                if (!entityliving.isSleeping()) {
                    return false;
                }

                if (this.a.f(this.b) > 100.0D) {
                    return false;
                }

                BlockPosition blockposition = this.b.getChunkCoordinates();
                IBlockData iblockdata = this.a.getWorld().getType(blockposition);
                if (iblockdata.a(TagsBlock.L)) {
                    this.c = (BlockPosition) iblockdata.d(BlockBed.aE).map((enumdirection) -> {
                        return blockposition.shift(enumdirection.opposite());
                    }).orElseGet(() -> {
                        return new BlockPosition(blockposition);
                    });
                    return !this.g();
                }
            }

        }
        return false;
    }

    private boolean g() {
        List<EntityTamableFox> list = this.a.getWorld().a(EntityTamableFox.class, (new AxisAlignedBB(this.c)).g(2.0D));
        Iterator iterator = list.iterator();

        EntityTamableFox entitycat;
        do {
            do {
                if (!iterator.hasNext()) {
                    return false;
                }

                entitycat = (EntityTamableFox)iterator.next();
            } while(entitycat == this.a);
        } while(!entitycat.isSleeping());

        return true;
    }

    public boolean b() {
        return this.a.isTamed() && this.b != null && this.b.isSleeping() && this.c != null && !this.g();
    }

    public void c() {
        if (this.c != null) {
            this.a.setSitting(false);
            this.a.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), 1.100000023841858D);
        }

    }

    public void d() {
        this.a.setSleeping(false);
        float f = this.a.getWorld().f(1.0F);
        if (this.b.fm() >= 100 && (double)f > 0.77D && (double)f < 0.8D && (double)this.a.getWorld().getRandom().nextFloat() < 0.7D) {
            this.h();
        }

        this.d = 0;
        //this.a.y(false);
        this.a.getNavigation().o();
    }

    private void h() {
        Random random = this.a.getRandom();
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        blockposition_mutableblockposition.g(this.a.getChunkCoordinates());
        this.a.a((double)(blockposition_mutableblockposition.getX() + random.nextInt(11) - 5), (double)(blockposition_mutableblockposition.getY() + random.nextInt(5) - 2), (double)(blockposition_mutableblockposition.getZ() + random.nextInt(11) - 5), false);
        blockposition_mutableblockposition.g(this.a.getChunkCoordinates());
        LootTable loottable = this.a.t.getMinecraftServer().getLootTableRegistry().getLootTable(LootTables.ak);
        net.minecraft.world.level.storage.loot.LootTableInfo.Builder loottableinfo_builder = (new net.minecraft.world.level.storage.loot.LootTableInfo.Builder((WorldServer)this.a.t)).set(LootContextParameters.f, this.a.getPositionVector()).set(LootContextParameters.a, this.a).a(random);
        List<ItemStack> list = loottable.populateLoot(loottableinfo_builder.build(LootContextParameterSets.g));
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            ItemStack itemstack = (ItemStack)iterator.next();
            EntityItem entityitem = new EntityItem(this.a.t, (double)blockposition_mutableblockposition.getX() - (double) MathHelper.sin(this.a.aX * 0.017453292F), (double)blockposition_mutableblockposition.getY(), (double)blockposition_mutableblockposition.getZ() + (double)MathHelper.cos(this.a.aX * 0.017453292F), itemstack);
            EntityDropItemEvent event = new EntityDropItemEvent(this.a.getBukkitEntity(), (org.bukkit.entity.Item)entityitem.getBukkitEntity());
            entityitem.t.getCraftServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                this.a.t.addEntity(entityitem);
            }
        }
    }

    public void e() {
        if (this.b != null && this.c != null) {
            this.a.setSitting(false);
            this.a.getNavigation().a((double)this.c.getX(), (double)this.c.getY(), (double)this.c.getZ(), 1.100000023841858D);
            if (this.a.f(this.b) < 2.5D) {
                ++this.d;
                if (this.d > 16) {
                    this.a.setSleeping(true);
                    //this.a.y(false);
                } else {
                    this.a.a(this.b, 45.0F, 45.0F);
                    //this.a.y(true);
                }
            } else {
                this.a.setSleeping(false);
            }
        }

    }
}
