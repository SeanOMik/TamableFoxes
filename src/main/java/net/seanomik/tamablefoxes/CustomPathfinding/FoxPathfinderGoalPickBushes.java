package net.seanomik.tamablefoxes.CustomPathfinding;

import net.minecraft.server.v1_14_R1.*;
import net.seanomik.tamablefoxes.TamableFox;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;

import java.util.Random;

public class FoxPathfinderGoalPickBushes  extends PathfinderGoalGotoTarget {
    protected int g;
    protected TamableFox tamableFox;

    public FoxPathfinderGoalPickBushes(TamableFox tamableFox, double d0, int i, int j) {
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
        if (!tamableFox.isTamed()) {
            IBlockData iblockdata = iworldreader.getType(blockposition);
            return iblockdata.getBlock() == Blocks.SWEET_BERRY_BUSH && (Integer) iblockdata.get(BlockSweetBerryBush.a) >= 2;
        }
        return false;
    }

    public void e() {
        if (this.k()) {
            if (this.g >= 40) {
                this.m();
            } else {
                ++this.g;
            }
        //} else if (!this.k() && tamableFox.getRandom().nextFloat() < 0.05F && !tamableFox.isTamed()) {
        } else if (!this.k() && tamableFox.getRandom().nextFloat() < 0.05F && !tamableFox.isTamed()) {
            tamableFox.a(SoundEffects.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
        }

        super.e();
    }

    protected void m() {
        if (tamableFox.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && !tamableFox.isTamed()) {
            IBlockData iblockdata = tamableFox.world.getType(this.e);
            if (iblockdata.getBlock() == Blocks.SWEET_BERRY_BUSH) {
                int i = (Integer)iblockdata.get(BlockSweetBerryBush.a);
                iblockdata.set(BlockSweetBerryBush.a, 1);
                if (CraftEventFactory.callEntityChangeBlockEvent(tamableFox, this.e, (IBlockData)iblockdata.set(BlockSweetBerryBush.a, 1)).isCancelled()) {
                    return;
                }

                int j = 1 + tamableFox.world.random.nextInt(2) + (i == 3 ? 1 : 0);
                ItemStack itemstack = tamableFox.getEquipment(EnumItemSlot.MAINHAND);
                if (itemstack.isEmpty()) {
                    tamableFox.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                    --j;
                }

                if (j > 0) {
                    Block.a(tamableFox.world, this.e, new ItemStack(Items.SWEET_BERRIES, j));
                }

                tamableFox.a(SoundEffects.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, 1.0F, 1.0F);
                tamableFox.world.setTypeAndData(this.e, (IBlockData)iblockdata.set(BlockSweetBerryBush.a, 1), 2);
            }
        }

    }

    public boolean a() {
        return !tamableFox.isSleeping() && super.a();
    }

    public void c() {
        this.g = 0;
        tamableFox.setSitting(false);
        super.c();
    }
}