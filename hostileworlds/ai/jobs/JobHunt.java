package hostileworlds.ai.jobs;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import CoroAI.componentAI.jobSystem.JobBase;
import CoroAI.componentAI.jobSystem.JobManager;
import CoroAI.entity.EnumJobState;

public class JobHunt extends JobBase {
	
	public long huntRange = 24;
	
	public boolean xRay = false;
	
	public JobHunt(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		super.tick();
		jobHunter();
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return ai.entityToAttack == null;
	}
	
	protected void jobHunter() {
	
		dontStrayFromHome = false;
		
		//this whole function is crap, redo it bitch
		
		//a use for the states
		
		//responding to alert, so you know to cancel it if alert entity / active target is dead
		
		/*if (tryingToFlee && (onGround || isInWater())) {
			tryingToFlee = false;
			fleeFrom(lastFleeEnt);
		}*/
		
		ai.maxPFRange = 96;
		huntRange = 64;
		ai.maxDistanceFromHome = 48F;
		
		
		//if (true) return;
		
		//health = 8;
		/*if (health < getMaxHealth() * 0.75F) {
			avoid();
			if (rand.nextInt(5) == 0) entityToAttack = null;
		} else {*/
			setJobState(EnumJobState.IDLE);
			
			if (/*ent.getHealth() > ent.getMaxHealth() * 0.90F && */(ai.entityToAttack == null || ai.rand.nextInt(20) == 0)) {
				boolean found = false;
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
			            		
			            		//found = true;
			            		//break;
		            		}
		            		//this.hasAttacked = true;
		            		//getPathOrWalkableBlock(entity1, 16F);
		            	}
		            }
		        }
		        if (clEnt != null) {
		        	//System.out.println("TEMP OFF FOR REFACTOR");
		        	ai.huntTarget(clEnt);
		        }
		        /*if (!found) {
		        	setState(EnumKoaActivity.IDLE);
		        }*/
			} else {
				
				if (ai.entityToAttack != null) {
					double distt = ent.getDistanceToEntity(ai.entityToAttack);
					if (/*distt > 3D && */(ent.getNavigator().noPath() || distt < 16) && ent.onGround) {
						entInt.getAIAgent().huntTarget(ai.entityToAttack);
						//PFQueue.getPath(ent, ai.entityToAttack, ai.maxPFRange);
					}
				}
				
			}
			
			//derp
			/*if (ent.entityToAttack == null && ent.rand.nextInt(6000) == 0) {
				ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
			}*/
			
		//}
		ent.prevHealth = ent.getHealth();
	}
	
	
	
	public void hunterHitHook(DamageSource ds, int damage) {
		
		/*if (health < getMaxHealth() / 4 * 3) {
			if (ds.getEntity() != null) {
				lastFleeEnt = ds.getEntity();
				tryingToFlee = true;
				//fleeFrom(ds.getEntity());
			}
		}
		prevKoaHealth = health;*/
	}
	
	public boolean avoid(boolean actOnTrue) {
		return false;
	}
	
	public boolean sanityCheckHelp(Entity caller, Entity target) {
		if (ent.getHealth() < 10) {
			return false;
		}
		
		if (dontStrayFromHome) {
			if (target.getDistance(ai.homeX, ai.homeY, ai.homeZ) > ai.maxDistanceFromHome * 1.5) {
				return false;
			}
		}
		if (ai.rand.nextInt(2) == 0) {
			return true;
		}
		return false;
	}
	
	public boolean sanityCheck(Entity target) {
		if (ent.getHealth() < 6) {
			return false;
		}
		
		if (dontStrayFromHome) {
			if (target.getDistance(ai.homeX, ai.homeY, ai.homeZ) > ai.maxDistanceFromHome) {
				return false;
			}
		}
		return true;
	}
	
	
	
}
