package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityWorm;
import hostileworlds.entity.MovingBlock;
import hostileworlds.entity.particle.EntityMeteorTrailFX;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityWormFire extends EntityWorm {

	public boolean spawnBlock = true;
	
	public List<WormNode> nodes = new ArrayList<WormNode>();
	
	public float smoothYaw;
	public float smoothPitch;
	
	public int nodePieces = 15;
	public int nodePieceBlockCount = 18; //make this dynamic depending on other values, determine amount needed for coverage
	public int maxHealth = 20; //static setting so it doesnt change over time
	public float nodeToNodeDist = 1F;
	public float blockRotateSpeed = 0.11F;
	public float baseRadius = 2F;
	public float bodyShiftRate = 0.2F;
	public float bodyShiftSize = 0.2F;
	public float moveSpeedMax = 1.5F;
	public float moveSpeed = 0.04F;
	public int lastHealth = maxHealth;
	public boolean spawning = true;
	
	public double lastMotionX = 0;
	public double lastMotionY = 0;
	public double lastMotionZ = 0;
	
	public class WormNode extends Entity {
		public int blockCount;
		public List<MovingBlock> blocks = new ArrayList<MovingBlock>();
		
		/*public float posX;
		public float posY;
		public float posZ;
		public float motionX;
		public float motionY;
		public float motionZ;*/
		
		public WormNode(World world, int parBlockCount) {
			super(world);
			blockCount = parBlockCount;
			
			for (int i = 0; i < blockCount; i++) {
				spawnBlock(this);
			}
		}

		@Override
		protected void entityInit() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound var1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound var1) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public EntityWormFire(World par1World) {
		super(par1World);
		
		maxHealth = nodePieces * nodePieceBlockCount;
		health = getMaxHealth();
		
		texture = "/coro/hw/test2.png";
		
		setSize(1.5F, 1F);
		
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
	}
	
	@Override
	public void extinguish()
    {
		super.extinguish();
		
    }
	
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) {
		if (par1DamageSource == DamageSource.cactus) {
			System.out.println("boom");
			health -= 1;
		}
		return super.attackEntityFrom(par1DamageSource, par2);
	}
	
	@Override
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public void spawnBlock(WormNode node) {
		MovingBlock mb = new MovingBlock(worldObj, Block.cobblestoneMossy.blockID, 0);
		mb.setPosition(posX, posY + 3, posZ);
		mb.gravity = 0F;
		mb.blockifyDelay = -1;
		mb.blockNum = node.blocks.size();
		mb.blockRow = nodes.size();
		//mb.motionY = 0.1F;
		float speed = 0.5F;
		//mb.motionX = (rand.nextFloat()*2-1) * speed;
		//mb.motionZ = (rand.nextFloat()*2-1) * speed;
		worldObj.spawnEntityInWorld(mb);
		node.blocks.add(mb);
	}
	
	@Override
	public void setDead()
    {
        this.isDead = true;
        
        //Cleanup / Death triggers
        for (int j = 0; j < nodes.size(); j++) {
        	List<MovingBlock> blocks = nodes.get(j).blocks;
			
			for (int i = 0; i < blocks.size(); i++) {
				MovingBlock mb = blocks.get(i);
				mb.triggerOwnerDied();
			}
        }
    }
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		//setDead();
		
		isImmuneToFire = true;
		
		//motionX = 0.00F;
		//motionY = 0.00F;
		//motionZ = 0.00F;
		//this.setPosition(posX, posY, posZ);
		
		//Usefull worm configs
		
		
		//Server code
		if (!worldObj.isRemote) {
			
			//motionX = lastMotionX;
			//motionY = lastMotionY;
			//motionZ = lastMotionZ;
			
			if (isInWater() && worldObj.getWorldTime() % 20 == 0) this.attackEntityFrom(DamageSource.drown, 2);
			
			if (spawning) {
				if (nodes.size() < nodePieces) {
					nodes.add(new WormNode(worldObj, nodePieceBlockCount));
				} else {
					spawning = false;
				}
			}
			
			if (health != lastHealth) {
				int diff = lastHealth - health;
				lastHealth = health;
				for (int j = nodes.size()-1; j >= 0; j--) {
					
					List<MovingBlock> blocks = nodes.get(j).blocks;
					
					for (int i = 0; i < blocks.size(); i++) {
						MovingBlock mb = blocks.get(i);
						if (diff > 0) {
							mb.triggerOwnerDied();
							blocks.remove(i);
							diff--;
						} else {
							break;
						}
						
					}
				}
			}
		}
		
		//Client & Server movement
		double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		
		//lock in
		//posX = -2F;
		//posY = 71F;
		//posZ = 367F;
		
		/*motionX = 0.00F;
		motionY = 0.00F;
		motionZ = 0.00F;*/
		
		//this.setPosition(posX+motionX, posY+motionY, posZ+motionZ);
		
		float startY = (float) (posY + 1F);
		
		//rotationYaw = 45F;// + worldObj.getWorldTime();//(float) (90F + Math.cos(worldObj.getWorldTime() * 0.05F) * 22F);
		
		
		//rotationPitch = 45F;
		
		this.entityCollisionReduction = 1F;
		
		if (!worldObj.isRemote) {

			int range = 3;
	    	for (int xx = (int)posX-range; xx < posX+range; xx++) {
	    		for (int yy = (int)posY-range; yy < posY+range; yy++) {
	    			for (int zz = (int)posZ-range; zz < posZ+range; zz++) {
	    				int id = worldObj.getBlockId(xx, yy, zz);
	    				if (id != 0 && id != Block.fire.blockID && worldObj.getBlockTileEntity(xx, yy, zz) == null && Block.blocksList[id].blockMaterial != Material.water) {
	    					if (!worldObj.isRemote) {
	    						if (this.getDistance(xx, yy, zz) <= range) {
		    						//System.out.println("explode block!");
	    							//cant eat catacombs and ground
	    							if (Block.blocksList[id].getBlockHardness(worldObj, xx, yy, zz) != -1 && (yy > 7 || posY < 7)) {
	    								worldObj.setBlock(xx, yy, zz, 0);
	    							}
			    					/*if (worldObj.getClosestPlayerToEntity(this, 256) != null && worldObj.rand.nextInt(5) == 0) {
				    					MovingBlock mb = new MovingBlock(worldObj, id, worldObj.getBlockMetadata(xx, yy, zz));
				    					mb.setPosition(xx+0.5F, yy+0.5F, zz+0.5F);
				    					mb.motionY = 1F;
				    					float speed3 = 0.5F;
				    					mb.motionX = (rand.nextFloat()*2-1) * speed;
				    					mb.motionZ = (rand.nextFloat()*2-1) * speed;
				    					worldObj.spawnEntityInWorld(mb);
			    					}*/
	    						} else {
	    							if (worldObj.getBlockId(xx, yy+1, zz) == 0 && worldObj.rand.nextInt(10) == 0) {
	    								worldObj.setBlock(xx, yy+1, zz, Block.fire.blockID);
	    							}
	    						}
	    					}
	    				}
	    			}
	    		}
	    	}
			
			//System.out.println(speed);
			
			//temp ai testing
			EntityPlayer player = worldObj.getClosestPlayerToEntity(this, -1);
			if (player != null) {
				//keep in this file
				double vecX = player.posX - posX;
				double vecZ = player.posZ - posZ;
				float aimYaw = (float) ((Math.atan2(vecZ, vecX) * 180D) / Math.PI);
				aimYaw = MathHelper.wrapAngleTo180_float(smoothYaw - aimYaw);
				smoothYaw += aimYaw < 0 ? -0.7F : 0.7F;
				
				//rotationPitch = (float)(Math.atan2(player.posY - posY, Math.sqrt(vecX * vecX + vecZ * vecZ)) * 180.0D / Math.PI) - 90;
				smoothPitch = (float)(Math.atan2(player.posY - posY, Math.sqrt(vecX * vecX + vecZ * vecZ)) * 180.0D / Math.PI);
				
				this.setRotation(smoothYaw + 90, rotationPitch);
				
				//System.out.println(180 - Math.abs(aimYaw));
				if (180 - Math.abs(aimYaw) < 30F) {
					if (speed < moveSpeedMax) {
						moveTowards(this, player, moveSpeed);
						//motionY *= 1.01F;
					} else {
						motionX *= 0.99F;
						motionY *= 0.99F;
						motionZ *= 0.99F;
					}
				} else {
					if (this.getDistanceToEntity(player) < 90F) {
					//if (Math.sqrt(player.posZ - posZ * player.posX - posX) < 10F) {
						//System.out.println("wat");
						//this.posX += 20;
						//moveTowards(this, player, moveSpeed * 0.1F);
						//this.posX -= 20;
						motionX *= 1.1F;
						motionY *= 1.1F;
						motionZ *= 1.1F;
						
						motionY = 0.05F;
					} else {
						motionX *= 1.05F;
						motionY *= 1.05F;
						motionZ *= 1.05F;
						motionY = 0.05F;
					}
					
				}
				float distt = this.getDistanceToEntity(player);
				if (distt < 8F*2F) {
					baseRadius = 2F + ((8F*2F - distt) / 6F);
				}
			}
		}
		
		/*motionX *= 0F;
		motionY *= 0F;
		motionZ *= 0F;*/
		
		//test dat trig
		//if (true) return;
		for (int j = 0; j < nodes.size(); j++) {
			
			nodes.get(j).entityCollisionReduction = 1F;
			float dist = nodeToNodeDist;
			
			float shiftRate = 0F;//(float)Math.cos(worldObj.getWorldTime() * 0.025F) * 0.9F;
			//shiftRate = j * 0.05F;
			//float wiggleRate = worldObj.getWorldTime() * 0.1F;//(float)Math.sin(worldObj.getWorldTime() * 0.5F) % 600;
			
			//nodes.get(j).posX = posX + Math.cos(((rotationYaw - j * shiftRate)+90) * 0.01745329F) * dist * j * 0.8F;
			
			if (j == 0) {
				nodes.get(j).posX = posX + Math.cos(((smoothYaw + j * shiftRate)+90) * 0.01745329F) * dist * j;
				nodes.get(j).posY = startY + Math.sin(rotationPitch - (j * 0.3F) + worldObj.getWorldTime() * 0.2F) * dist * 0.5F/* * dist * j*/; //???
				nodes.get(j).posZ = posZ + Math.sin(((smoothYaw + j * shiftRate)+90) * 0.01745329F) * dist * j;
				
				nodes.get(j).rotationYaw = smoothYaw + 90;
			} else {
				double targX = nodes.get(j-1).posX;
				double targY = nodes.get(j-1).posY;
				double targZ = nodes.get(j-1).posZ;
				
				double vecX = nodes.get(j-1).posX - nodes.get(j).posX;
				double vecY = nodes.get(j-1).posY - nodes.get(j).posY;
				double vecZ = nodes.get(j-1).posZ - nodes.get(j).posZ;
				
				double nodeDist = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
				
				//First time warp to pos, and for corrections
				if (nodeDist > 40F) {
					nodes.get(j).posX = posX + Math.cos(((smoothYaw + j * shiftRate)+90) * 0.01745329F) * dist * j;
					nodes.get(j).posY = startY + Math.sin(rotationPitch - (j * 0.3F) + worldObj.getWorldTime() * 0.2F) * dist * 0.5F/* * dist * j*/; //???
					nodes.get(j).posZ = posZ + Math.sin(((smoothYaw + j * shiftRate)+90) * 0.01745329F) * dist * j;
					nodes.get(j).motionX = 0F;
					nodes.get(j).motionY = 0F;
					nodes.get(j).motionZ = 0F;
				} else if (nodeDist > nodeToNodeDist * 1F) {
					double speed2 = Math.sqrt(nodes.get(j).motionX * nodes.get(j).motionX + nodes.get(j).motionY * nodes.get(j).motionY + nodes.get(j).motionZ * nodes.get(j).motionZ);
					if (speed2 < moveSpeedMax * 1.2F) {
						nodes.get(j).motionX *= 0.85F;
						nodes.get(j).motionY *= 0.85F;
						nodes.get(j).motionZ *= 0.85F;
						moveTowards(nodes.get(j), nodes.get(j-1), moveSpeed * 1.2F);
					} else {
						nodes.get(j).motionX *= 0.95F;
						nodes.get(j).motionY *= 0.95F;
						nodes.get(j).motionZ *= 0.95F;
					}
					moveTowards(nodes.get(j), nodes.get(j-1), 0.05F);
				} else {
					nodes.get(j).motionX = 0F;
					nodes.get(j).motionY = 0F;
					nodes.get(j).motionZ = 0F;
				}
				
				nodes.get(j).setPosition(nodes.get(j).posX+nodes.get(j).motionX, nodes.get(j).posY+nodes.get(j).motionY, nodes.get(j).posZ+nodes.get(j).motionZ);
				float aimYaw = (float) ((Math.atan2(vecZ, vecX) * 180D) / Math.PI) + 90;
				nodes.get(j).rotationYaw = aimYaw;
			}
			
			
			
			//nodes.get(j).posX = nodes.get(j).posX + (Math.cos((rotationYaw + j) * 0.01745329F));
			//nodes.get(j).posZ = nodes.get(j).posZ + (Math.sin((rotationYaw + j) * 0.01745329F));
			
			List<MovingBlock> blocks = nodes.get(j).blocks;
			
			for (int i = 0; i < blocks.size(); i++) {
				MovingBlock mb = blocks.get(i);
				
				mb.entityCollisionReduction = 1F;
				
				if (mb.isDead) {
					blocks.remove(mb);
				} else {
				
					double blockSpeed = Math.sqrt(mb.motionX * mb.motionX + mb.motionY * mb.motionY + mb.motionZ * mb.motionZ);
					
					//revert
					if (mb.getDistanceToEntity(this) > 60F) {
						/*mb.posX = -3F;
						mb.posY = 72F;
						mb.posZ = 368F;*/
					} else if (mb.getDistanceToEntity(this) < 2F && blockSpeed < 0.1F) {
						//mb.motionX = 0.4F;
						//mb.motionZ = 0.2F;
					}
					
			        float angleXZ = (float)((Math.atan2(nodes.get(j).posZ - mb.posZ, nodes.get(j).posX - mb.posX) * 180D) / Math.PI) - 90F;
			        
			        angleXZ = -(nodes.get(j).rotationYaw + (j * shiftRate * 2.5F)) * 0.01745329F;
			        
			        float radius = (float) (baseRadius + (Math.cos((worldObj.getWorldTime() + j) * bodyShiftRate) * bodyShiftSize));
			        float rotateSpeed = blockRotateSpeed;
			        
			        float range1 = (float) (Math.sin(((worldObj.getWorldTime() - (i*3.5F)) * rotateSpeed)) * radius);
			        float range2 = (float) (Math.cos(((worldObj.getWorldTime() - (i*3.5F)) * rotateSpeed)) * radius);
			        
			        //float range3 = (float) (Math.sin(((worldObj.getWorldTime() - (i*3.5F)) * rotateSpeed)) * radius);
			        
			        //range1 = (float) (Math.sin((((i*3.5F)) * rotateSpeed)) * 1);
			        
			        //smoothPitch = (worldObj.getWorldTime() % 360) - 180;
			        
			        //half way
			        //smoothPitch = -45F;
			        
			        //range1 = 0F;
			        
			        double newX = (float)Math.cos(angleXZ);
			        double newZ = (float)Math.sin(angleXZ);
			        double newY = 1F;//(float)Math.cos(smoothPitch * 0.01745329F);
			        
			        //System.out.println(smoothPitch/*Math.sin(smoothPitch * 0.01745329F)*/);
			        
					mb.posX = nodes.get(j).posX - (newX * (j % 2 == 0 ? range1 : -range1))/* - (newX * Math.sin(smoothPitch * 0.01745329F) * (j % 2 == 0 ? range1 : -range1))*/;
					mb.posY = nodes.get(j).posY + (newY * range2);
					mb.posZ = nodes.get(j).posZ + (newZ * (j % 2 == 0 ? range1 : -range1));
					
					mb.motionX = 0F;
					mb.motionY = 0F;
					mb.motionZ = 0F;
					
				}
			}
		}
		
		
		
		if (worldObj.isRemote) {
        	//spawnParticles();
        } else {
        	lastMotionX = motionX * 0.95F;
        	lastMotionY = motionY * 0.95F;
        	lastMotionZ = motionZ * 0.95F;
        }
	}
	
	public void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
	
	@SideOnly(Side.CLIENT)
    public void spawnParticles() {
    	for (int i = 0; i < 1; i++) {
    		
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

}
