package hostileworlds.ai.jobs;

import hostileworlds.entity.MovingBlock;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import particleman.entities.EntityParticleControllable;
import CoroAI.componentAI.jobSystem.JobBase;
import CoroAI.componentAI.jobSystem.JobManager;
import CoroAI.entity.EnumJobState;

public class JobBossWielder extends JobBase {
	
	public long huntRange = 256;	
	public boolean xRay = true;
	
	public int blockCount;
	public List<MovingBlock> blocks = new ArrayList<MovingBlock>();
	public List<EntityParticleControllable> particles = new ArrayList<EntityParticleControllable>();
	public int ticksBetweenGathers = 100;
	public int ticksBeforeNextGather = ticksBetweenGathers;
	public int ticksBeforeThrow = 60;
	public long overrideRange = 4;
	
	public boolean gathering = false;
	public boolean throwing = false;
	
	public double targPrevPosX;
	public double targPrevPosY;
	public double targPrevPosZ;
	
	public JobBossWielder(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		//ticksBetweenGathers = 100;
		
		//States
		//Idle: targeting
		//W1: 
		//W2:
		//W3: 
		
		manageBlocks();
		
		//remove dead player
		if (ai.entityToAttack != null && (ai.entityToAttack.isDead || ai.entityToAttack.getDistanceToEntity(ent) > huntRange)) {
			ai.entityToAttack = null;
		}
		
		if (ai.entityToAttack == null) {
			setJobState(EnumJobState.IDLE);
		}
		
		if (this.state == EnumJobState.IDLE) {
			if (ai.entityToAttack == null || ai.rand.nextInt(20) == 0) {
				Entity clEnt = null;
				float closest = 9999F;
		    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(isEnemy(entity1))
		            {
		            	if (xRay || ((EntityLivingBase) entity1).canEntityBeSeen(ent)) {
		            		//if (sanityCheck(entity1)) {
		            			float dist = ent.getDistanceToEntity(entity1);
		            			if (dist < closest) {
		            				closest = dist;
		            				clEnt = entity1;
		            			}
		            		//}
		            	}
		            }
		        }
		        if (clEnt != null) {
		        	ai.entityToAttack = clEnt;
		        }
			} else if (ai.entityToAttack != null) {
				setJobState(EnumJobState.W1);
			}
		} else if (this.state == EnumJobState.W1) {
			//gather and toss!
			double dist = ai.entityToAttack.getDistanceToEntity(ent);
			if (dist > overrideRange && ticksBeforeNextGather > 0) ticksBeforeNextGather--;
			
			if (ticksBeforeNextGather == 0) {
				blockCount = 10;
				
				//spawn 1 a tick, or do animation state stuff
				if (!throwing) {
					if (blocks.size() < blockCount) {
						gathering = true;
						spawnBlock();
					} else {
						gathering = false;
					}
				}
				
				//System.out.println(ent.getDistanceToEntity(ai.entityToAttack));

				if (ticksBeforeThrow > 0) ticksBeforeThrow--;
				
				if (ticksBeforeThrow == 0) {
					throwing = true;
					if (ent.worldObj.getWorldTime() % 5 == 0) {
						throwBlock();
					}
				}
				
				if (throwing && blocks.size() == 0) {
					throwing = false;
					ticksBetweenGathers = (int)dist;
					ticksBeforeNextGather = ticksBetweenGathers;
					ticksBeforeThrow = (int)dist * 2;
				}
			}
		}
		
		if (ai.entityToAttack != null) {
			targPrevPosX = ai.entityToAttack.posX;
			targPrevPosY = ai.entityToAttack.posY;
			targPrevPosZ = ai.entityToAttack.posZ;
		}
	}
	
	public void manageBlocks() {
		
		for (int i = 0; i < blocks.size(); i++) {
			MovingBlock mb = blocks.get(i);
			//Math.cos(ent.worldObj.getWorldTime() * 0.025F)
			float speed = 15F;
			float dist = (float) (3F + Math.sin((ent.worldObj.getWorldTime() % 360) * 0.1F) * 2F);
			float diff = 36F / speed;//3.6F;
			mb.posX = ent.posX + (Math.sin(((ent.worldObj.getWorldTime() % 360) + i * diff) * speed * 0.01745329F) * dist);
			mb.posY = ent.posY + 7;
			mb.posZ = ent.posZ + (Math.cos(((ent.worldObj.getWorldTime() % 360) + i * diff) * speed * 0.01745329F) * dist);
			
			mb.motionX = mb.motionY = mb.motionZ = 0F;
		}
		
		for (int i = 0; i < particles.size(); i++) {
			EntityParticleControllable mb = particles.get(i);
			
			if (mb.isDead) {
				particles.remove(mb);
			} else {
				mb.influenceParticle(0, 0, 0);
			}
			
		}
		
		if (particles.size() < 30 && ent.worldObj.rand.nextInt(10) == 0) {
			EntityParticleControllable particle = new EntityParticleControllable(ent.worldObj, "", ent.worldObj.rand.nextInt(3));
			particle.setPosition(ent.posX, ent.posY + 2, ent.posZ);
			particle.ownerEntityID = ent.entityId;
			particle.index = particles.size();
			particles.add(particle);
			ent.worldObj.spawnEntityInWorld(particle);
		}
		
        
	}
	
	public void throwBlock() {
		MovingBlock mb = blocks.get(0);
		blocks.remove(0);
		
		mb.triggerOwnerDied();
		mb.target = (EntityLivingBase) ai.entityToAttack;
		mb.targetTillDist = 2;
		
		
		float dist = ent.getDistanceToEntity(ai.entityToAttack);
		
		//moveTowards(mb, ai.entityToAttack, dist * (float)(0.04F + (Math.random() * 0.005F)), (int) (dist * 1.6F));
		moveTowards(mb, ai.entityToAttack, dist * (float)(0.025F + (Math.random() * 0.005F)), (int) (dist * 1.1F));
		mb.motionY += 0.2F + (0.03F * ai.entityToAttack.getDistanceToEntity(ent));//1.65F;
	}
	
	public void spawnBlock() {
		MovingBlock mb = new MovingBlock(ent.worldObj, Block.cobblestoneMossy.blockID, 0);
		mb.setPosition(ent.posX, ent.posY + 3, ent.posZ);
		//mb.gravity = 0F;
		mb.createParticles = true;
		//mb.blockifyDelay = -1;
		mb.blockNum = blocks.size();
		//mb.blockRow = nodes.size();
		//mb.motionY = 0.1F;
		float speed = 0.5F;
		//mb.motionX = (rand.nextFloat()*2-1) * speed;
		//mb.motionZ = (rand.nextFloat()*2-1) * speed;
		ent.worldObj.spawnEntityInWorld(mb);
		blocks.add(mb);
	}
	
	public void moveTowards(Entity ent, Entity targ, float speed, int leadTicks) {
		double vecX = (targ.posX + ((targ.posX - targPrevPosX) * leadTicks)) - ent.posX;
		double vecY = (targ.posY/* + ((targ.posY - targPrevPosY) * leadTicks)*/) - ent.posY;
		double vecZ = (targ.posZ + ((targ.posZ - targPrevPosZ) * leadTicks)) - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return true;
	}
}
