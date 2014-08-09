package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.client.sound.MovingSoundStreamingSource;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.particle.EntityAuraCurseFX;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import CoroUtil.componentAI.ICoroAI;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityInvasionSource extends TileEntity
{
    
    //Curse stuff
    public long lastCoordCheckTime;
    public boolean invasionActive = false;
    public long invasionLastTime = 0;
    public float difficultyFactor = 0;
    public ArrayList<ICoroAI> invasionEntities = new ArrayList<ICoroAI>();
	public ArrayList<String> cursedPlayers = new ArrayList<String>();
    
    public ArrayList<EntityAuraCurseFX> particles = new ArrayList<EntityAuraCurseFX>();
    
    public boolean hasSpawnedBoss1 = false;
    public boolean hasSpawnedBoss2 = false;
    
    public long lastPlayTime = 0L;
    @SideOnly(Side.CLIENT)
    public MovingSoundStreamingSource sound;
    
    public static int portalHeight = 5;
    public static int portalBorderRadius = 2;

    public TileEntityInvasionSource() {
    	
    }
    
    public void updateEntity()
    {
    	//int sdfsdf = this.xCoord;
    	if (worldObj.isRemote) {
    		herp();
    		tickSound();
    		
    	} else {
    		//checkCoordAdd();
    		if (invasionActive && worldObj.getWorldTime() % 100 == 0) {
    			//watchActiveInvaders();
    			//updatePlayerStates();
    		}
    		
    		if (worldObj.getWorldTime() % 60 == 0) {
    			if (!hasSpawnedBoss1) {
		    		EntityPlayer player = getWorldObj().getClosestPlayer(xCoord, yCoord, zCoord, 64);
		    		
		    		if (player != null) {
		    			ZombieBlockWielder boss = new ZombieBlockWielder(worldObj);
		    			boss.setPosition(xCoord + 0.5, yCoord + 1.5, zCoord + 0.5);
		    			
		    			boss.agent.homeX = xCoord;
		    			boss.agent.homeY = yCoord+1;
		    			boss.agent.homeZ = zCoord;
		    			
		    			worldObj.spawnEntityInWorld(boss);
		    			hasSpawnedBoss1 = true;
		    		}
    			}
    			
    			//done within JobBossCloseCombat instead
    			/*if (!hasSpawnedBoss2) {
		    		EntityPlayer player = getWorldObj().getClosestPlayer(xCoord, yCoord, zCoord, 64);
		    		
		    		if (player != null) {
		    			EntityWormFire worm = new EntityWormFire(worldObj);
		    			worm.setPosition(xCoord + 0.5, yCoord + 20.5, zCoord + 0.5);
						
		    			worm.agent.homeX = xCoord;
						worm.agent.homeY = yCoord+20;
						worm.agent.homeZ = zCoord;
		    			
						worldObj.spawnEntityInWorld(worm);
						
						
						
						hasSpawnedBoss2 = true;
		    		}
    			}*/
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickSound() {
    	//if (this.lastPlayTime < System.currentTimeMillis())
        //{
    		float range = 120;
    		EntityPlayer player = worldObj.getClosestPlayer(xCoord, yCoord, zCoord, range);

            if (player != null)
            {
            	
                this.lastPlayTime = System.currentTimeMillis() + 43*1000L + (2000L);
                playNonMovingSound(Vec3.createVectorHelper(xCoord, yCoord, zCoord), HostileWorlds.modID + ":bossfight", 1.0F, 1.0F, range);
            
            }
        //}
    }
    
    @SideOnly(Side.CLIENT)
    public void playNonMovingSound(Vec3 parPos, String var1, float var5, float var6, float parCutOffRange)
    {
    	if (sound == null) {
    		System.out.println("PLAY!!!!!!!!!!!!!!!!!!!!!");
	    	ResourceLocation res = new ResourceLocation(var1);
	    	sound = new MovingSoundStreamingSource(parPos, res, var5, var6, parCutOffRange);
	    	FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    	}
    	
    	/*if (sound.isDonePlaying()) {
    		System.out.println("PLAY!!!!!!!!!!!!!!!!!!!!!");
    		FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    	}*/
    }
    
    @SideOnly(Side.CLIENT)
    public void stopSound() {
    	if (sound != null) {
    		sound.stopPlaying();
    	}
    }
    
    @Override
    public void invalidate() {
    	super.invalidate();
    	if (worldObj.isRemote) {
    		stopSound();
    	}
    }
    
    public void registerWithInvasion(ICoroAI ent) {
    	System.out.println("ent registered with invasion: " + ent);
    	invasionEntities.add(ent);
    }
    
    
    
    public void watchActiveInvaders() {
    	for (int i = 0; i < invasionEntities.size(); i++) {
    		ICoroAI ent = invasionEntities.get(i);
    		
    		if (ent.getAIAgent().ent.isDead) {
    			invasionEntities.remove(i);
    		}
    	}
    	
    	if (invasionEntities.size() == 0) {
    		invasionEnd();
    	}
    }
    
    public void invasionStart() {
    	System.out.println("Invasion started for: " + xCoord + ", " + yCoord + ", " + zCoord);
    	invasionActive = true;
    	//invasionEntities.clear();
    }
    
    public void invasionEnd() {
    	System.out.println("Invasion ended for: " + xCoord + ", " + yCoord + ", " + zCoord);
    	invasionActive = false;
    	invasionLastTime = System.currentTimeMillis();
    	cursedPlayers.clear();
    }
    
    public void checkCoordAdd() {
    	if (lastCoordCheckTime < System.currentTimeMillis()) {
			if (ServerTickHandler.wd != null) {
				lastCoordCheckTime = System.currentTimeMillis() + 30000;
				boolean found = false;
				for (int i = 0; i < ServerTickHandler.wd.coordCurses.get(worldObj.provider.dimensionId).size(); i++) {
					ChunkCoordinates cc = ServerTickHandler.wd.coordCurses.get(worldObj.provider.dimensionId).get(i);
					if (cc.posX == xCoord && cc.posY == yCoord && cc.posZ == zCoord) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					System.out.println("FIX CURSE COORD REGISTRATION - checkCoordAdd()");
					//ServerTickHandler.wd.coordCurses.get(worldObj.provider.dimensionId).add(new ChunkCoordinates(xCoord, yCoord, zCoord));
				}
			}
		}
    }
    
    @SideOnly(Side.CLIENT)
    public void herp() {
    	
    	int y = 80;
    	
    	if (/*worldObj.canBlockSeeTheSky(xCoord, yCoord+1, zCoord) && */Minecraft.getMinecraft().thePlayer.getDistance(xCoord, yCoord, zCoord) < 256) {
    		
    		float look = worldObj.getWorldTime() * (10);
    		float speed = 0.5F;
    		Random rand = new Random();
    		
    		double motionX = ((double)(-Math.sin((look) / 180.0F * 3.1415927F) * Math.cos(0 / 180.0F * 3.1415927F)) * (speed + (0.1 * rand.nextDouble())));
    		double motionZ = ((double)(Math.cos((look) / 180.0F * 3.1415927F) * Math.cos(0 / 180.0F * 3.1415927F)) * (speed + (0.1 * rand.nextDouble())));
    		
	    	EntityAuraCurseFX particle = new EntityAuraCurseFX(worldObj, this.xCoord + 0.5F, y, this.zCoord + 0.5F, motionX, 0.25F, motionZ, 20);
	    	particles.add(particle);
	    	Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	    	
	    	particle = new EntityAuraCurseFX(worldObj, this.xCoord + 0.5F, y, this.zCoord + 0.5F, -motionX, 0.25F, -motionZ, 30);
	    	particles.add(particle);
	    	Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    	}
    	
    	if (Minecraft.getMinecraft().thePlayer.getDistance(xCoord, yCoord, zCoord) < 64) {
    		for (int i = 0; i < 3; i++) {
	    		Random rand = new Random();
	    		double range = 1D;
	    		//EntityAuraCurseFX particle = new EntityAuraCurseFX(worldObj, this.xCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, yCoord + rand.nextDouble()*range, this.zCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, 0, 0.25F, 0, 30);
	    		EntityReddustFX particle = new EntityReddustFX(worldObj, this.xCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, yCoord + rand.nextDouble()*range, this.zCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, 2F, rand.nextFloat(), 0F, 0F);
		    	//particles.add(particle);
		    	Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    		}
    	}
    	
    	for (int var9 = 0; var9 < this.particles.size(); ++var9)
        {
    		EntityAuraCurseFX particle = (EntityAuraCurseFX)this.particles.get(var9);
            
    		if (particle != null) {
    			if (particle.isDead) {
    				particles.remove(particle);
    			} else {
    				float vecX = (xCoord + 0.5F) - (float)particle.posX;
    				float vecZ = (zCoord + 0.5F) - (float)particle.posZ;
    	            float dist = (float)MathHelper.sqrt_double(vecX * vecX + vecZ * vecZ);
    	            
    	            float speed = 0.08F;
    	            //if (speed < 0) speed = 0;
    	            //if (speed > 1) speed = 1;
    	            
    	            float angle = (float)((Math.atan2(vecZ, vecX) * 180D) / 3.1415927410125732D);
    	            float angleAdj = (float)Math.sin((particle.getParticleAge() % 100F) / 20F) * 60;
    	            
    	            int max = 30;    	            
    	            if (angleAdj < -max) angleAdj = -max;
    	            if (angleAdj > max) angleAdj = max;
    	            
    	            float angleAdj2 = (float)Math.sin((worldObj.getWorldTime() % 50F) / 20F) * 30;
    	            float angleAdj3 = (float)Math.sin((worldObj.getWorldTime() % 200F) / 20F) * 30;
    	            angleAdj2 += angleAdj + angleAdj3;
    	            
    	            float staticAngle = 10;
    	            
    	            if (particle.direction == 0) {
    	            	angle += staticAngle + angleAdj2;
    	            } else {
    	            	angle -= staticAngle + angleAdj2;
    	            }
    	            
    	            double rad_angle = angle * 0.01745329F;
    	            
    	            float newX = (float)Math.cos(rad_angle);
    	            float newZ = (float)Math.sin(rad_angle);
    	            
    	            particle.motionX += newX * speed;
    	            particle.motionZ += newZ * speed;
    				
    			}
    		}
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	return AxisAlignedBB.getBoundingBox(xCoord - portalBorderRadius, yCoord, zCoord - portalBorderRadius, xCoord + 1 + portalBorderRadius, yCoord + portalHeight + 2, zCoord + 1 + portalBorderRadius);
    }

    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        var1.setBoolean("hasSpawnedBoss1", hasSpawnedBoss1);
        var1.setBoolean("hasSpawnedBoss2", hasSpawnedBoss2);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        hasSpawnedBoss1 = var1.getBoolean("hasSpawnedBoss1");
        hasSpawnedBoss2 = var1.getBoolean("hasSpawnedBoss2");

    }
}
