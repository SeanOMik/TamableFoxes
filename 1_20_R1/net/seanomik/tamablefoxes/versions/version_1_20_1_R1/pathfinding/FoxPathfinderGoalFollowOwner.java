package net.seanomik.tamablefoxes.versions.version_1_20_1_R1.pathfinding;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.seanomik.tamablefoxes.versions.version_1_20_1_R1.EntityTamableFox;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.EnumSet;

@SuppressWarnings("unused")
public class FoxPathfinderGoalFollowOwner extends PathfinderGoal {
    public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
	private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final EntityTamableFox tamableFox;
    private EntityLiving owner;
    private final IWorldReader level;
    private final double speedModifier;
    private final NavigationAbstract navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FoxPathfinderGoalFollowOwner(EntityTamableFox entityfox, double d0, float f, float f1, boolean flag) {
        this.tamableFox = entityfox;
        this.level = entityfox.dI();
        this.speedModifier = d0;
        this.navigation = entityfox.J();
        this.startDistance = f;
        this.stopDistance = f1;
        this.canFly = flag;
        this.a(EnumSet.of(PathfinderGoal.Type.a, Type.b));
        if (!(entityfox.J() instanceof Navigation) && !(entityfox.J() instanceof NavigationFlying)) {
            throw new IllegalArgumentException("Unsupported mob type for FoxPathfinderGoalFollowOwner");
        }
    }

    @Override
    public boolean a() { //canUse
        EntityLiving entityliving = this.tamableFox.getOwner();
        if (entityliving == null) {
            return false;
        } else if (entityliving.G_()) {
            return false;
        } else if (this.tamableFox.isOrderedToSit() || this.tamableFox.isOrderedToSleep()) {
            return false;
        } else if (this.tamableFox.f(entityliving) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = entityliving;
            return true;
        }
    }
    
    @Override
    public boolean b() { //canContinueToUse
        return !this.navigation.l() && (!this.tamableFox.isOrderedToSit() && !this.tamableFox.isOrderedToSleep() && this.tamableFox.f(this.owner) > (double) (this.stopDistance * this.stopDistance));
    }

    @Override
    public void c() { //start
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamableFox.a(PathType.j);
        this.tamableFox.a(PathType.j, 0.0F);
    }

    @Override
    public void d() { //stop
        this.owner = null;
        this.navigation.n();
        this.tamableFox.a(PathType.j, this.oldWaterCost);
    }

    @Override
    public void e() { //tick
        this.tamableFox.E().a(this.owner, 10.0F, (float)this.tamableFox.X());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.tamableFox.fO() && !this.tamableFox.bM()) {
                if (this.tamableFox.f(this.owner) >= 144.0D) {
                	TamableFoxes.getPlugin().getLogger().severe("teleporting to owbner");
                    this.teleportToOwner();
                } else {
                    this.navigation.a(this.owner, this.speedModifier);
                }
            }
        }

    }

    private void teleportToOwner() {
        BlockPosition blockposition = this.owner.di();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockposition.u() + j, blockposition.v() + k, blockposition.w() + l);
            if (flag) {
                return;
            }
        }

    }

    @SuppressWarnings("resource")
	private boolean maybeTeleportTo(int i, int j, int k) {
        if (Math.abs((double)i - this.owner.dn()) < 2.0D && Math.abs((double)k - this.owner.dt()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPosition(i, j, k))) {
            return false;
        } else {
            CraftEntity entity = this.tamableFox.getBukkitEntity();
            Location to = new Location(entity.getWorld(), (double)i + 0.5D, (double)j, (double)k + 0.5D, this.tamableFox.dy(), this.tamableFox.dA());
            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
            this.tamableFox.dI().getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            } else {
                to = event.getTo();
                this.tamableFox.b(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                this.navigation.n();
                return true;
            }
        }
    }

    private boolean canTeleportTo(BlockPosition blockposition) {
        PathType pathtype = PathfinderNormal.a(this.level, blockposition.j());
        if (pathtype != PathType.c) {
            return false;
        } else {
            IBlockData iblockdata = this.level.a_(blockposition.d());
            if (!this.canFly && iblockdata.b() instanceof BlockLeaves) {
                return false;
            } else {
                BlockPosition blockposition1 = blockposition.b(this.tamableFox.di());
                return this.level.a(this.tamableFox, this.tamableFox.cE().a(blockposition1));
            }
        }
    }

    private int randomIntInclusive(int i, int j) {
        return this.tamableFox.ec().a(j - i + 1) + i;
    }
}
