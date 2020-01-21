package net.seanomik.tamablefoxes.pathfinding;

import net.seanomik.tamablefoxes.EntityTamableFox;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.EnumSet;

public class FoxPathfindGoalFollowOwner extends PathfinderGoal {

    protected final EntityTamableFox a;
    protected final IWorldReader b;
    private final double d;
    private final NavigationAbstract e;
    private final float g;
    private final float h;
    private EntityLiving c;
    private int f;
    private float i;

    public FoxPathfindGoalFollowOwner(EntityTamableFox tamableFox, double d0, float f, float f1) {
        this.a = tamableFox;
        this.b = tamableFox.world;
        this.d = d0;
        this.e = tamableFox.getNavigation();
        this.h = f;
        this.g = f1;
        this.a(EnumSet.of(Type.MOVE, Type.LOOK));
        if (!(tamableFox.getNavigation() instanceof Navigation) && !(tamableFox.getNavigation() instanceof NavigationFlying)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean a() {
        EntityLiving entityliving = this.a.getOwner();
        if (entityliving == null) {
            return false;
        } else if (entityliving instanceof EntityHuman && entityliving.isSpectator()) {
            return false;
        } else if (this.a.isSitting()) {
            return false;
        } else if (this.a.h(entityliving) < (double) (this.h * this.h)) {
            return false;
        } else {
            this.c = entityliving;
            return true;
        }
    }

    public boolean b() {
        return !this.e.n() && this.a.h(this.c) > (double) (this.g * this.g) && !this.a.isSitting();
    }

    public void c() {
        this.f = 0;
        this.i = this.a.a(PathType.WATER);
        this.a.a(PathType.WATER, 0.0F);
    }

    public void d() {
        this.c = null;
        this.e.o();
        this.a.a(PathType.WATER, this.i);
    }

    public void e() {
        this.a.getControllerLook().a(this.c, 10.0F, (float) this.a.dU());
        if (!this.a.isSitting() && --this.f <= 0) {
            this.f = 10;
            if (!this.e.a(this.c, this.d) && !this.a.isLeashed() && !this.a.isPassenger() && this.a.h(this.c) >= 144.0D) {
                int i = MathHelper.floor(this.c.locX()) - 2;
                int j = MathHelper.floor(this.c.locZ()) - 2;
                int k = MathHelper.floor(this.c.getBoundingBox().minY);

                for (int l = 0; l <= 4; ++l) {
                    for (int i1 = 0; i1 <= 4; ++i1) {
                        if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.a(new BlockPosition(i + l, k - 1, j + i1))) {
                            CraftEntity entity = this.a.getBukkitEntity();
                            Location to = new Location(entity.getWorld(), (double) ((float) (i + l) + 0.5F), k, ((float) (j + i1) + 0.5F), this.a.yaw, this.a.pitch);
                            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
                            this.a.world.getServer().getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }

                            to = event.getTo();
                            this.a.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                            this.e.o();
                            return;
                        }
                    }
                }
            }
        }

    }

    protected boolean a(BlockPosition blockposition) {
        IBlockData iblockdata = this.b.getType(blockposition);
        return iblockdata.a(this.b, blockposition, this.a.getEntityType()) && this.b.isEmpty(blockposition.up()) && this.b.isEmpty(blockposition.up(2));
    }

}
