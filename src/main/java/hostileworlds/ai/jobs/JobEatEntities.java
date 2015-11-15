package hostileworlds.ai.jobs;

import hostileworlds.entity.EntityInvader;
import hostileworlds.entity.monster.ZombieHungry;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.jobSystem.JobBase;
import CoroUtil.componentAI.jobSystem.JobManager;
import CoroUtil.entity.EnumJobState;

public class JobEatEntities extends JobBase {
	
	public long huntRange = 16;
	
	public boolean xRay = false;
	
	public EntityLivingBase targetEat = null;
	
	public JobEatEntities(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		dontStrayFromHome = false;
		huntRange = 16;
		
		setJobState(EnumJobState.IDLE);
		
		if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F && */(ai.entityToAttack == null || ai.rand.nextInt(20) == 0)) {
			boolean found = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(entity1 instanceof EntityLivingBase/* && isEnemy(entity1)*/)
	            {
	            	if (xRay || ((EntityLivingBase) entity1).canEntityBeSeen(ent)) {
	            		if (sanityCheck(entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = ent.getDistanceToEntity(entity1);
	            			if (dist < closest) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
	            		}
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	targetEat = (EntityLivingBase) clEnt;
	        	ai.huntTarget(targetEat);
	        }
		} else {
			if (targetEat != null) {
				double distt = ent.getDistanceToEntity(targetEat);
				if (/*distt > 3D && */(ent.getNavigator().noPath() || distt < 16) && ent.onGround) {
					entInt.getAIAgent().huntTarget(targetEat);
					//PFQueue.getPath(ent, ai.entityToAttack, ai.maxPFRange);
				}
			}
			
		}
		
		if (targetEat != null) {
			if (ent.getDistanceToEntity(targetEat) < 3) {
				//System.out.println("eating entity");
				ent.attackEntityAsMob(targetEat);
				ent.swingItem();
				ent.worldObj.playSoundAtEntity(ent, "random.burp", 0.5F, ent.worldObj.rand.nextFloat() * 0.1F + 0.9F);
				
				//hold target still
				targetEat.motionX = 0;
				targetEat.motionZ = 0;
				
				if (targetEat.getHealth() <= 0) {
					if (ent instanceof ZombieHungry) {
						((ZombieHungry) ent).addSize(0.05F);
					}
					targetEat = null;
				}
			}
			
			if (targetEat != null) {
				if (targetEat.isDead || targetEat.getHealth() <= 0) {
					targetEat = null;
				} else {
					if (!ent.canEntityBeSeen(targetEat)) {
						targetEat = null;
					}
				}
			}
			
			if (targetEat != null && ent.isCollidedHorizontally && ent.onGround) {
				OldUtil.jump(ent);
				ent.motionY *= 1.5F;
			}
		}
	}
	
	@Override
	public boolean avoid(boolean actOnTrue) {
		return false;
	}
	
	@Override
	public boolean sanityCheck(Entity target) {
		return target instanceof EntityZombie && !(target instanceof EntityInvader);
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return targetEat == null/* || ai.entityToAttack != null*/;
	}
	
}
