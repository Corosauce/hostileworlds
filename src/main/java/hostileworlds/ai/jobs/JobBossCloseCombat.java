package hostileworlds.ai.jobs;

import hostileworlds.entity.monster.EntityWormFire;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import CoroUtil.componentAI.jobSystem.JobBase;
import CoroUtil.componentAI.jobSystem.JobManager;
import CoroUtil.entity.EnumJobState;

//Targets, overrides jobs under it if target is close
public class JobBossCloseCombat extends JobBase {
	
	public long huntRange = 256;
	
	public boolean xRay = true;
	
	public boolean spawnedWorm = false;
	
	public JobBossCloseCombat(JobManager jm) {
		super(jm);
		dontStrayFromHome = true; //better set home coords on spawn now
	}
	
	@Override
	public boolean avoid(boolean actOnTrue) {
		return false;
	}
	
	@Override
	public void tick() {
		super.tick();
		//States
		//Idle: targeting
		//W1: 
		//W2: 
		//W3: 
		
		//remove dead player
		if (ai.entityToAttack != null && (ai.entityToAttack.isDead || ai.entityToAttack.getDistanceToEntity(ent) > huntRange)) {
			ai.entityToAttack = null;
		}
		
		if (ai.entityToAttack == null) {
			setJobState(EnumJobState.IDLE);
		}
		
		if (!spawnedWorm) {
			if (ent.getHealth() < ent.getMaxHealth() / 2 && ent.getHealth() != 0) {
				spawnedWorm = true;
				EntityWormFire worm = new EntityWormFire(ent.worldObj);
				worm.setPosition(ai.homeX, ai.homeY+10, ai.homeZ);
				
				//set worms home to wielders home but higher up
				worm.agent.homeX = ai.homeX;
				worm.agent.homeY = ai.homeY+10;
				worm.agent.homeZ = ai.homeZ;
				
				ent.worldObj.spawnEntityInWorld(worm);
			}
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
		        	ai.entityToAttack = clEnt;
		        }
			} else if (ai.entityToAttack != null) {
				setJobState(EnumJobState.W1);
			}
		} else if (this.state == EnumJobState.W1) {
			if (ai.entityToAttack != null) {
				//if (ent.getNavigator().noPath() && ent.getDistanceToEntity(ai.entityToAttack) > 1F) {
					entInt.getAIAgent().huntTarget(ai.entityToAttack, -1);
				//}
			}
		}
		
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return true;//(ai.entityToAttack == null || ai.entityToAttack.getDistanceToEntity(ent) > overrideRange);
	}
}
