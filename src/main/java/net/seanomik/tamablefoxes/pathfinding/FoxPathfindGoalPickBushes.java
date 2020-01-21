package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;

public class FoxPathfindGoalPickBushes extends PathfinderGoalGotoTarget {

    protected int g;
    protected EntityTamableFox tamableFox;

    public FoxPathfindGoalPickBushes(EntityTamableFox tamableFox, double d0, int i, int j) {
        super(tamableFox, d0, i, j);
        this.tamableFox = tamableFox;
    }

    public double h() {
        return 2.0D;
    }

    public boolean j() {
        return this.d % 100 == 0;
    }

    protected boolean a(IWorldReader iworldreader, BlockPosition blockposition) {
        if (this.tamableFox.isTamed()) {
            return false;
        } else {
            IBlockData iblockdata = iworldreader.getType(blockposition);
            return iblockdata.getBlock() == Blocks.SWEET_BERRY_BUSH && (Integer) iblockdata.get(BlockSweetBerryBush.a) >= 2;
        }
    }

    public void e() {
        if (this.k()) {
            if (this.g >= 40) {
                this.m();
            } else {
                ++this.g;
            }
        } else if (!this.k() && this.tamableFox.getRandom().nextFloat() < 0.05F && !this.tamableFox.isTamed()) {
            this.tamableFox.a(SoundEffects.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
        }

        super.e();
    }

    protected void m() {
        if (this.tamableFox.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && !this.tamableFox.isTamed()) {
            IBlockData iblockdata = this.tamableFox.world.getType(this.e);
            if (iblockdata.getBlock() == Blocks.SWEET_BERRY_BUSH) {
                int i = (Integer) iblockdata.get(BlockSweetBerryBush.a);
                iblockdata.set(BlockSweetBerryBush.a, 1);
                if (CraftEventFactory.callEntityChangeBlockEvent(this.tamableFox, this.e, (IBlockData) iblockdata.set(BlockSweetBerryBush.a, 1)).isCancelled()) {
                    return;
                }

                int j = 1 + this.tamableFox.world.random.nextInt(2) + (i == 3 ? 1 : 0);
                ItemStack itemstack = this.tamableFox.getEquipment(EnumItemSlot.MAINHAND);
                if (itemstack.isEmpty()) {
                    this.tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                    --j;
                }

                if (j > 0) {
                    Block.a(this.tamableFox.world, this.e, new ItemStack(Items.SWEET_BERRIES, j));
                }

                this.tamableFox.a(SoundEffects.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, 1.0F, 1.0F);
                this.tamableFox.world.setTypeAndData(this.e, (IBlockData) iblockdata.set(BlockSweetBerryBush.a, 1), 2);
            }
        }

    }

    public boolean a() {
        return !this.tamableFox.isSleeping() && super.a();
    }

    public void c() {
        this.g = 0;
        this.tamableFox.setSitting(false);
        super.c();
    }
}
