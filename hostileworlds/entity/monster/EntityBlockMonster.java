package hostileworlds.entity.monster;

import hostileworlds.entity.EntityWorm;
import hostileworlds.entity.MovingBlock;
import hostileworlds.entity.particle.EntityMeteorTrailFX;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityBlockMonster extends EntityWorm {

	public boolean spawnBlock = true;
	
	public List<MovingBlock> blocks = new ArrayList<MovingBlock>();
	
	public float smoothYaw;
	public float smoothPitch;
	
	public int nodePieces = 10;
	public int nodePieceBlockCount = 18; //make this dynamic depending on other values, determine amount needed for coverage
	public int maxHealth = 20; //static setting so it doesnt change over time
	public float nodeToNodeDist = 1F;
	public float blockRotateSpeed = 0.11F;
	public float baseRadius = 2F;
	public float bodyShiftRate = 0.2F;
	public float bodyShiftSize = 0.2F;
	public float moveSpeedMax = 1.5F;
	public float moveSpeed = 0.04F;
	public float lastHealth = maxHealth;
	public boolean spawning = true;
	
	public double lastMotionX = 0;
	public double lastMotionY = 0;
	public double lastMotionZ = 0;
	
	public EntityBlockMonster(World par1World) {
		super(par1World);
		
		//maxHealth = nodePieces * nodePieceBlockCount;
		
		setSize(1.5F, 1F);
		
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40);
	}
	
	@Override
	public void extinguish()
    {
		super.extinguish();
		
    }
	
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
		if (par1DamageSource == DamageSource.cactus) {
			System.out.println("boom");
			setHealth(getHealth() -1);
		}
		return super.attackEntityFrom(par1DamageSource, par2);
	}
	
	public void spawnBlock() {
		MovingBlock mb = new MovingBlock(worldObj, Block.cobblestoneMossy.blockID, 0);
		mb.setPosition(posX, posY + 3, posZ);
		mb.gravity = 0F;
		mb.blockifyDelay = -1;
		//mb.blockNum = node.blocks.size();
		//mb.blockRow = nodes.size();
		//mb.motionY = 0.1F;
		float speed = 0.5F;
		//mb.motionX = (rand.nextFloat()*2-1) * speed;
		//mb.motionZ = (rand.nextFloat()*2-1) * speed;
		worldObj.spawnEntityInWorld(mb);
		blocks.add(mb);
		//node.blocks.add(mb);
	}
	
	@Override
	public void setDead()
    {
        this.isDead = true;
    }
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		//setDead();
		//if (true) return;
		
		//temp
		//nodePieces = 10;
		//spawning = true;
		
		//System.out.println("health: " + getHealth());
		
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
				if (blocks.size() < nodePieces) {
					spawnBlock();
					//blocks.add(new Node(worldObj, nodePieceBlockCount));
				} else {
					spawning = false;
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
		
		if (!worldObj.isRemote) {
			float startY = (float) (posY + 1F);
			
			for (int i = 0; i < blocks.size(); i++) {
				MovingBlock mb = blocks.get(i);
				if (mb != null) {
					
					
					
					double dist = 5D;
					double speedRot = 10D;
					double speedRot2 = 2D;
					double distNodes = 36;
					
					smoothYaw = 45F;
					double tryYaw = worldObj.getWorldTime()*speedRot;
					double tryYaw2 = worldObj.getWorldTime()*speedRot;
					speedRot = 10D;
					double tryPitch = (i*distNodes)+worldObj.getWorldTime()*speedRot;
					
					EntityPlayer entP = worldObj.getClosestPlayerToEntity(this, -1);
					
					if (entP != null) {
						tryYaw = entP.rotationYaw;
						tryYaw2 = entP.rotationYaw;
						tryPitch = entP.rotationPitch + 180 + 45;
						this.setPosition(entP.posX, entP.posY-2, entP.posZ);
					}
					
					tryYaw+=(i*distNodes);
					tryYaw2+=0;//(i*distNodes);
					tryPitch+=(i*distNodes);
					
					//relative adjustments for player aiming test
					tryYaw += 45;
					tryPitch -= 45;
					
					double newX = this.posX + Math.cos(((tryYaw)+45) * 0.01745329F) * dist;
					double newY = startY + Math.sin(tryPitch * 0.01745329F) * dist;
					double newZ = this.posZ + Math.sin(((tryYaw2)+45) * 0.01745329F) * dist;
					
					//try 2
					if (entP != null) {
						double distVec = 3D;
						Vec3 vecPos = Vec3.createVectorHelper(entP.posX, entP.posY-2, entP.posZ);
						Vec3 vecRel = Vec3.createVectorHelper(distVec, distVec, distVec);
						Vec3 vecRel2 = Vec3.createVectorHelper(distVec, distVec, distVec);
						
						float trySpeed = 5F;
						float tryValX = 0;//;
						float tryValY = 0;//-entP.rotationYaw + (i*36);//+worldObj.getWorldTime()*trySpeed;
						float tryValZ = worldObj.getWorldTime()*trySpeed;
						
						//System.out.println("coords pre: " + vec.xCoord + " - " + vec.yCoord + " - " + vec.zCoord);
						vecRel.rotateAroundX(tryValX * 0.01745329F);
						vecRel.rotateAroundY(tryValY * 0.01745329F);
						vecRel.rotateAroundZ(tryValZ * 0.01745329F);
						//System.out.println("coords post: " + vec.xCoord + " - " + vec.yCoord + " - " + vec.zCoord);
						
						newX = vecPos.xCoord + vecRel.xCoord;
						newY = vecPos.yCoord + vecRel.yCoord;
						newZ = vecPos.zCoord + vecRel.zCoord;
					}
					
					mb.setPosition(newX, newY, newZ);
					
					mb.motionX = 0;
					mb.motionY = 0;
					mb.motionZ = 0;
				}
			}
			
		}
		
		//rotationYaw = 45F;// + worldObj.getWorldTime();//(float) (90F + Math.cos(worldObj.getWorldTime() * 0.05F) * 22F);
		
		
		//rotationPitch = 45F;
		
		
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
