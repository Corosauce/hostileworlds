package hostileworlds.entity;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.block.TileEntitySourceInvasion;
import hostileworlds.entity.particle.EntityMeteorTrailFX;
import hostileworlds.quest.InvasionSourceBreakEvent;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.quest.quests.ItemQuest;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityMeteorite extends Entity
{
    private int xTileSnowball = -1;
    private int yTileSnowball = -1;
    private int zTileSnowball = -1;
    private Block inTileSnowball;
    private boolean inGroundSnowball = false;
    public int shakeSnowball = 0;
    private EntityLivingBase shootingEntity;
    private int ticksInGroundSnowball;
    private int ticksInAirSnowball = 0;

    public int age = 0;
    public int ticksDying = 0;
    public boolean dying = false;
    
    public ChunkCoordinates crashDestination = null;
    public WorldEvent invasion = null;
    
    public EntityMeteorite(World var1)
    {
        super(var1);
        this.setSize(0.25F, 0.25F);
    }
    
    public EntityMeteorite(World var1, ChunkCoordinates parDest, WorldEvent parInvasion)
    {
        super(var1);
        this.setSize(0.25F, 0.25F);
        crashDestination = parDest;
        invasion = parInvasion;
    }
    
    protected void entityInit() {}

    public void updateTargetCoord() {
    	
    	if (crashDestination == null) { HostileWorlds.dbg("crashDestination is null for meteor: " + getEntityId()); return; }
    	
    	float speed = 1.5F + (this.age * 0.005F);
    	
    	double vecX = crashDestination.posX - posX;
    	double vecY = crashDestination.posY - posY;
    	double vecZ = crashDestination.posZ - posZ;
        
        double var9 = (double)MathHelper.sqrt_double(vecX * vecX + vecY * vecY + vecZ * vecZ);
        this.motionX = vecX / var9 * speed;
        this.motionY = vecY / var9 * speed;
        this.motionZ = vecZ / var9 * speed;
        
    }
    
    //motions x y z, speed 
    public void setHeading(double var1, double var3, double var5, float var7)
    {
        float var9 = MathHelper.sqrt_double(var1 * var1 + var3 * var3 + var5 * var5);
        var1 /= (double)var9;
        var3 /= (double)var9;
        var5 /= (double)var9;
        /*var1 += this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
        var3 += this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
        var5 += this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;*/
        var1 *= (double)var7;
        var3 *= (double)var7;
        var5 *= (double)var7;
        this.motionX = var1;
        this.motionY = var3;
        this.motionZ = var5;
        float var10 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(var1, var5) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(var3, (double)var10) * 180.0D / Math.PI);
        this.ticksInGroundSnowball = 0;
    }

    public void setVelocity(double var1, double var3, double var5)
    {
        this.motionX = var1;
        this.motionY = var3;
        this.motionZ = var5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt_double(var1 * var1 + var5 * var5);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(var1, var5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(var3, (double)var7) * 180.0D / Math.PI);
        }
    }

    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();
        
        age++;
        
        //setDead();
        
        if (worldObj.isRemote) {
        	spawnParticles();
        }

        if (this.shakeSnowball > 0)
        {
            --this.shakeSnowball;
        }
        
        if (this.inGroundSnowball)
        {
            Block var1 = this.worldObj.getBlock(this.xTileSnowball, this.yTileSnowball, this.zTileSnowball);

            if (var1 == this.inTileSnowball)
            {
                ++this.ticksInGroundSnowball;

                if (this.ticksInGroundSnowball == 1200)
                {
                    this.setDead(); //let client do this one (if it even does) for safety kill
                }

                return;
            }

            this.inGroundSnowball = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGroundSnowball = 0;
            this.ticksInAirSnowball = 0;
        }
        else
        {
            ++this.ticksInAirSnowball;
        }

        Vec3 var15 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
        Vec3 var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        MovingObjectPosition var3 = this.worldObj.rayTraceBlocks(var15, var2);
        var15 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
        var2 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        if (var3 != null)
        {
            var2 = Vec3.createVectorHelper(var3.hitVec.xCoord, var3.hitVec.yCoord, var3.hitVec.zCoord);

            if (worldObj.getBlock((int)var3.hitVec.xCoord, (int)var3.hitVec.yCoord, (int)var3.hitVec.zCoord) == Blocks.vine)
            {
                worldObj.setBlock((int)var3.hitVec.xCoord, (int)var3.hitVec.yCoord, (int)var3.hitVec.zCoord, Blocks.air);
            }
        }

        if (!this.worldObj.isRemote)
        {
            Entity var4 = null;
            List var5 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double var6 = 0.0D;

            for (int var8 = 0; var8 < var5.size(); ++var8)
            {
                Entity var9 = (Entity)var5.get(var8);

                if (var9.canBeCollidedWith() && (var9 != this.shootingEntity || this.ticksInAirSnowball >= 5))
                {
                    float var10 = 0.3F;
                    AxisAlignedBB var11 = var9.boundingBox.expand((double)var10, (double)var10, (double)var10);
                    MovingObjectPosition var12 = var11.calculateIntercept(var15, var2);

                    if (var12 != null)
                    {
                        double var13 = var15.distanceTo(var12.hitVec);

                        if (var13 < var6 || var6 == 0.0D)
                        {
                            var4 = var9;
                            var6 = var13;
                        }
                    }
                }
            }

            if (var4 != null)
            {
                var3 = new MovingObjectPosition(var4);
            }
        }

        if (var3 != null)
        {
            if (var3.entityHit != null && var3.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), 0))
            {
                ;
            }

            //if (!worldObj.isRemote) this.setDead();
        }

        
        float var18 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)var18) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        
        float drag = 0.99F;
        float gravity = 0.07F;

        if (this.isInWater())
        {
            for (int var7 = 0; var7 < 4; ++var7)
            {
                float var20 = 0.25F;
                this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)var20, this.posY - this.motionY * (double)var20, this.posZ - this.motionZ * (double)var20, this.motionX, this.motionY, this.motionZ);
            }

            drag = 0.8F;
        }

        
        if (ticksDying <= 3) {
        	if (!worldObj.isRemote) updateTargetCoord();
        	
        	this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
        	
	        this.motionX *= (double)drag;
	        this.motionY *= (double)drag;
	        this.motionZ *= (double)drag;
	        this.motionY -= (double)gravity;
        }
        
        this.setPosition(this.posX, this.posY, this.posZ);
        
        if (!isDead) {
        	if (!worldObj.isRemote) {
        		if (!dying) {
        			HostileWorlds.eventChannel.sendToDimension(WorldDirectorMultiDim.getMeteorPacket(this, 1), this.dimension);
        			//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(WorldDirector.getMeteorPacket(this, 1), this.dimension);
        		}
        		onTickExplode();
        	}
        } else {
        	
        }

        if (!worldObj.isRemote) {
        	if (!dying && Math.abs(crashDestination.posY - posY) < 3) {
        		System.out.println("METEOR DEATH BEGIN, killing on client side");
        		HostileWorlds.eventChannel.sendToDimension(WorldDirectorMultiDim.getMeteorPacket(this, 2), this.dimension);
        		//MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(WorldDirector.getMeteorPacket(this, 2), this.dimension);
        		dying = true;
        	}
        	if (dying) {
	        	ticksDying++;
	        	if (ticksDying > 60) {
	        		int y = (int)posY;
	        		
	        		boolean keepTry = true;
	        		
	        		while (keepTry) {
	        			Block id = worldObj.getBlock((int)posX, (int)y, (int)posZ);
	        			
	        			//if (id == 0) keepTry = false;
	        			
	        			if (!CoroUtilBlock.isAir(id) && id.getMaterial().isSolid()) {
	        				keepTry = false;
	        			}
	        			
	        			y--;
	        			
	        			if (y <= 0) keepTry = false;
	        		}
	        		
	        		y++;
	        		
	        		//change to gen temple out of the hole
	        		y += 8;
	        		
	        		int topY = genBuilding(worldObj, MathHelper.floor_double(posX), MathHelper.floor_double(y), MathHelper.floor_double(posZ), 22);

	        		updateInvasionInfo(MathHelper.floor_double(posX), MathHelper.floor_double(y)+1+topY, MathHelper.floor_double(posZ));
	        		
	        		//while ( != 0) {} //look ahead until its air, so ground top is new y
	        		//HWTeleporter tp = new HWTeleporter(worldObj);
	        		//tp.worldDest = worldObj;
	        		//tp.makePortal(posX, y, posZ, null, new ChunkCoordinates((int)posX, y-1, (int)posZ));
	        		
	        		
	        		this.setDead();
	        	}
        	}
        }
        
    }
    
    //returns the topY value it determined to be
    public static int genBuilding(World worldObj, int xx, int yy, int zz, int origSize) {
    	/*int xx = (int)posX;
		int yy = (int)y;
		int zz = (int)posZ;*/
		Block id = HostileWorlds.blockBloodyCobblestone;
		
		//int portalHeight = 5;
		int portalBorderRadius = TileEntitySourceInvasion.portalBorderRadius;
		
		int startX = 0;
		int startZ = 0;
		
		int x = 0;
		int y = 0;
		int z = 0;
		int size = origSize;
		
		for (y = 0; y < 50; y++) {
    		for (x = startX; x <= size; x++) {
    			for (z = startZ; z <= size; z++) {
    				worldObj.setBlock(xx + x - origSize/2, yy + y, zz + z - origSize/2, HostileWorlds.blockBloodyCobblestone);
    			}
    		}
    		startX += 1;
    		startZ += 1;
    		size -= 1;
    		
    		if (size - startX < 5) {
    			break;
    		}
    	}
		
		//portal border
		for (int i = -portalBorderRadius; i <= portalBorderRadius; i++) {
    		worldObj.setBlock(xx-portalBorderRadius, yy+y+TileEntitySourceInvasion.portalHeight, zz+i, id);
    		worldObj.setBlock(xx+portalBorderRadius, yy+y+TileEntitySourceInvasion.portalHeight, zz+i, id);
		}
		
		//portal border
		for (int i = -portalBorderRadius+1; i <= portalBorderRadius-1; i++) {
    		worldObj.setBlock(xx+i, yy+y+TileEntitySourceInvasion.portalHeight, zz-portalBorderRadius, id);
    		worldObj.setBlock(xx+i, yy+y+TileEntitySourceInvasion.portalHeight, zz+portalBorderRadius, id);
		}
		
		//pillars
		for (int i = 0; i < TileEntitySourceInvasion.portalHeight; i++) {
			worldObj.setBlock(xx-portalBorderRadius, yy+y+i, zz-portalBorderRadius, id);
			worldObj.setBlock(xx+portalBorderRadius, yy+y+i, zz-portalBorderRadius, id);
			worldObj.setBlock(xx-portalBorderRadius, yy+y+i, zz+portalBorderRadius, id);
			worldObj.setBlock(xx+portalBorderRadius, yy+y+i, zz+portalBorderRadius, id);
		}
		
		
		/*worldObj.setBlock(xx, yy, zz, id);
		worldObj.setBlock(xx, yy+1, zz, id);
		worldObj.setBlock(xx, yy+2, zz, id);
		worldObj.setBlock(xx, yy+3, zz, id);
		
		worldObj.setBlock(xx, yy+4, zz, id);
		
		worldObj.setBlock(xx+1, yy+4, zz, id);
		worldObj.setBlock(xx+2, yy+4, zz, id);
		worldObj.setBlock(xx+3, yy+4, zz, id);
		
		worldObj.setBlock(xx+3, yy+3, zz, id);
		
		worldObj.setBlock(xx+3, yy+2, zz, id);
		worldObj.setBlock(xx+3, yy+1, zz, id);
		worldObj.setBlock(xx+3, yy, zz, id);
		
		worldObj.setBlock(xx+2, yy, zz, id);
		worldObj.setBlock(xx+1, yy, zz, id);*/
		
		id = HostileWorlds.blockSourceInvasion;
		
		worldObj.setBlock(xx, yy+y+1, zz, id, 0, 2);
		/*worldObj.setBlock(xx+1, yy+2, zz, id, 0, 2);
		worldObj.setBlock(xx+1, yy+3, zz, id, 0, 2);
		worldObj.setBlock(xx+2, yy+3, zz, id, 0, 2);
		worldObj.setBlock(xx+2, yy+2, zz, id, 0, 2);
		worldObj.setBlock(xx+2, yy+1, zz, id, 0, 2);*/
		
		HostileWorlds.dbg("CREATED PORTAL AT " + (xx) + ", " + (yy) + ", " + zz);
		
		return y;
    }
    
    public void updateInvasionInfo(int x, int y, int z) {
    	try {
    		
    		TileEntitySourceInvasion tEnt = ((TileEntitySourceInvasion)worldObj.getTileEntity(x, y, z));
    		if (tEnt != null) {
    			invasion.coordSource = new ChunkCoordinates(tEnt.xCoord, tEnt.yCoord, tEnt.zCoord);
    			ServerTickHandler.wd.coordInvasionSources.get(dimension).add(invasion.coordSource);
    		}
    		
    		PlayerQuests plQuests = PlayerQuestManager.i().getPlayerQuests(invasion.mainPlayerName);
    		InvasionSourceBreakEvent aq = new InvasionSourceBreakEvent();//PlayerQuests.createQuestFromString(createQuestStr);
			
			if (aq != null) {
				aq.initCreateObject(plQuests);
				aq.initFirstTime(worldObj.provider.dimensionId);
				aq.initCustomData(invasion.coordSource, HostileWorlds.blockSourceInvasion);
				
				plQuests.questAdd(aq);
				plQuests.saveAndSyncPlayer();
			}
    		
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    public void onTickExplode() {
    	int radius = 6;
    	for (int xx = (int)posX-radius; xx <= posX+radius; xx++) {
    		for (int yy = (int)posY-radius; yy <= posY+radius; yy++) {
    			for (int zz = (int)posZ-radius; zz <= posZ+radius; zz++) {
    				Block id = worldObj.getBlock(xx, yy, zz);
    				if (!CoroUtilBlock.isAir(id) && id != Blocks.bedrock && worldObj.getTileEntity(xx, yy, zz) == null && id.getMaterial() != Material.water) {
    					if (!worldObj.isRemote) {
    						if (this.getDistance(xx+0.5, yy+0.5, zz+0.5) <= radius-2) {
	    						//System.out.println("explode block!");
		    					worldObj.setBlock(xx, yy, zz, Blocks.air);
		    					if (worldObj.getClosestPlayerToEntity(this, 256) != null && worldObj.rand.nextInt(5) == 0) {
			    					MovingBlock mb = new MovingBlock(worldObj, id, worldObj.getBlockMetadata(xx, yy, zz));
			    					mb.setPosition(xx+0.5F, yy+0.5F, zz+0.5F);
			    					mb.motionY = 1F;
			    					float speed = 0.5F;
			    					mb.motionX = (rand.nextFloat()*2-1) * speed;
			    					mb.motionZ = (rand.nextFloat()*2-1) * speed;
			    					worldObj.spawnEntityInWorld(mb);
		    					}
		    					if (worldObj.rand.nextInt(10) == 0) {
			    					if (worldObj.getBlock(xx, yy-1, zz).getMaterial().isSolid()) {
										worldObj.setBlock(xx, yy, zz, Blocks.fire);
									}
		    					}
    						} else {
    							/*if (worldObj.rand.nextInt(5) == 0) {
    								if (worldObj.getBlock(xx, yy-1, zz).getMaterial().isSolid()) {
    									worldObj.setBlock(xx, yy, zz, Blocks.fire);
    								}
    							}*/
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    public void setDead()
    {
    	if (!isDead && !worldObj.isRemote) {
    		//client kill moved to a better location, since meteor hangs around post crash
    	}
        super.setDead();
    }

    @SideOnly(Side.CLIENT)
    public void spawnExplodeParticles() {
    	
    }
    
    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
    	for (int i = 0; i < 10; i++) {
    		
    		float speed = 0.1F;
    		float randPos = 8.0F;
    		float ahead = 2.5F;
    		
    		EntityMeteorTrailFX particle = new EntityMeteorTrailFX(worldObj, 
    				posX + (motionX*ahead) + (rand.nextFloat()*2-1) * randPos, 
    				posY + (motionY*ahead) + (rand.nextFloat()*2-1) * randPos, 
    				posZ + (motionZ*ahead) + (rand.nextFloat()*2-1) * randPos, motionX, 0.25F, motionZ, 0, posX, posY, posZ);
    		
    		particle.motionX = (rand.nextFloat()*2-1) * speed;
    		particle.motionY = (rand.nextFloat()*2-1) * 0.1F;
    		particle.motionZ = (rand.nextFloat()*2-1) * speed;
    		
        	//particles.add(particle);

    		particle.spawnAsWeatherEffect();
        	//Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    	}
    }

    public void writeEntityToNBT(NBTTagCompound var1)
    {
        var1.setShort("xTile", (short)this.xTileSnowball);
        var1.setShort("yTile", (short)this.yTileSnowball);
        var1.setShort("zTile", (short)this.zTileSnowball);
        //var1.setByte("inTile", (byte)this.inTileSnowball);
        var1.setByte("shake", (byte)this.shakeSnowball);
        var1.setByte("inGround", (byte)(this.inGroundSnowball ? 1 : 0));
    }

    public void readEntityFromNBT(NBTTagCompound var1)
    {
        this.xTileSnowball = var1.getShort("xTile");
        this.yTileSnowball = var1.getShort("yTile");
        this.zTileSnowball = var1.getShort("zTile");
        //this.inTileSnowball = var1.getByte("inTile") & 255;
        this.shakeSnowball = var1.getByte("shake") & 255;
        this.inGroundSnowball = var1.getByte("inGround") == 1;
    }

    public void onCollideWithPlayer(EntityPlayer var1)
    {
        if (this.inGroundSnowball && this.shootingEntity == var1 && this.shakeSnowball <= 0 && var1.inventory.addItemStackToInventory(new ItemStack(Items.arrow, 1)))
        {
            this.worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            var1.onItemPickup(this, 1);
            this.setDead();
        }
    }

    public float getShadowSize()
    {
        return 0.0F;
    }
    
    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_,
    		double p_145770_5_) {
    	return true;
    }
}
