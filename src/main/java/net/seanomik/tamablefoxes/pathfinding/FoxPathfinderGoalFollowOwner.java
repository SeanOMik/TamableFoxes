package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.EnumSet;

public class FoxPathfinderGoalFollowOwner extends PathfinderGoal {
    private final EntityTamableFox a;
    private EntityLiving b;
    private final IWorldReader c;
    private final double d;
    private final NavigationAbstract e;
    private int f;
    private final float g;
    private final float h;
    private float i;
    private final boolean j;

    public FoxPathfinderGoalFollowOwner(EntityTamableFox entityTamableFox, double d0, float f, float f1, boolean flag) {
        this.a = entityTamableFox;
        this.c = entityTamableFox.world;
        this.d = d0;
        this.e = entityTamableFox.getNavigation();
        this.h = f;
        this.g = f1;
        this.j = flag;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
        if (!(entityTamableFox.getNavigation() instanceof Navigation) && !(entityTamableFox.getNavigation() instanceof NavigationFlying)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean a() {
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving == null) {
            return false;
        } else if (entityliving.isSpectator()) {
            return false;
        } else if (this.a.isSitting()) {
            return false;
        } else if (this.a.h(entityliving) < (double)(this.h * this.h)) {
            return false;
        } else {
            this.b = entityliving;
            return true;
        }
    }

    public boolean b() {
        return this.e.m() ? false : (this.a.isSitting() ? false : this.a.h(this.b) > (double)(this.g * this.g));
    }

    public void c() {
        this.f = 0;
        this.i = this.a.a(PathType.WATER);
        this.a.a(PathType.WATER, 0.0F);
    }

    public void d() {
        this.b = null;
        this.e.o();
        this.a.a(PathType.WATER, this.i);
    }

    public void e() {
        this.a.getControllerLook().a(this.b, 10.0F, (float)this.a.dU());
        if (--this.f <= 0) {
            this.f = 10;
            if (!this.a.isLeashed() && !this.a.isPassenger()) {
                if (this.a.h(this.b) >= 144.0D) {
                    this.g();
                } else {
                    this.e.a(this.b, this.d);
                }
            }
        }

    }

    private void g() {
        BlockPosition blockposition = new BlockPosition(this.b);

        for(int i = 0; i < 10; ++i) {
            int j = this.a(-3, 3);
            int k = this.a(-1, 1);
            int l = this.a(-3, 3);
            boolean flag = this.a(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean a(int i, int j, int k) {
        if (Math.abs((double)i - this.b.locX()) < 2.0D && Math.abs((double)k - this.b.locZ()) < 2.0D) {
            return false;
        } else if (!this.a(new BlockPosition(i, j, k))) {
            return false;
        } else {
            CraftEntity entity = this.a.getBukkitEntity();
            Location to = new Location(entity.getWorld(), (double)((float)i + 0.5F), (double)j, (double)((float)k + 0.5F), this.a.yaw, this.a.pitch);
            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
            this.a.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            } else {
                to = event.getTo();
                this.a.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                this.e.o();
                return true;
            }
        }
    }

    private boolean a(BlockPosition blockposition) {
        PathType pathtype = PathfinderNormal.b(this.c, blockposition.getX(), blockposition.getY(), blockposition.getZ());
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            IBlockData iblockdata = this.c.getType(blockposition.down());
            if (!this.j && iblockdata.getBlock() instanceof BlockLeaves) {
                return false;
            } else {
                BlockPosition blockposition1 = blockposition.b(new BlockPosition(this.a));
                return this.c.getCubes(this.a, this.a.getBoundingBox().a(blockposition1));
            }
        }
    }

    private int a(int i, int j) {
        return this.a.getRandom().nextInt(j - i + 1) + i;
    }

}
