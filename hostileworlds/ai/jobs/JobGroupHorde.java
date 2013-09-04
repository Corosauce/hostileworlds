package hostileworlds.ai.jobs;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.config.ModConfigFields;
import hostileworlds.dimension.HWTeleporter;
import hostileworlds.entity.EntityInvader;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieMiner;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.jobSystem.JobBase;
import CoroAI.componentAI.jobSystem.JobManager;
import CoroAI.entity.EnumJobState;
import CoroAI.formation.Formation;

public class JobGroupHorde extends JobBase {
	
	//Adaptive difficulty ideas for players:
	
	//- Detect who kill invaders, those who kill more are given more challenge?
	//- Detect damage of their primary weapon
	//- Detect total armor defense
	
	public long huntRange = 24;
	
	public ChunkCoordinates attackCoord;
	
	public ArrayList<EntityLivingBase> groupMembers;
	public EntityLivingBase leader = null;
	
	public int maxSize = 20;
	public int anger;
	
	public int lifeTime = 0;
	public int lifeTimeMax = 96000;
	
	public JobGroupHorde(JobManager jm) {
		super(jm);
		groupMembers = new ArrayList();
	}
	
	@Override
	public void tick() {
		super.tick();
		
		lifeTime++;
		
		if (!(ent instanceof ZombieMiner)) {
			if (lifeTime > lifeTimeMax) {
				ent.setDead();
				return;
			}
		}
		
		boolean attackCurse = false;
		
		try {
			if (ent.worldObj.getWorldTime() % 80 == 0) {
				//if (ent instanceof ZombieMiner) System.out.println(ent.entityId + ": invader cur dist: " + ent.getDistance(attackCoord.posX, attackCoord.posY, attackCoord.posZ));
				//System.out.println(ent.entityId + ": dim: " + ent.dimension + ", invader cur pos: " + ent.posX + ", " + ent.posY + ", " + ent.posZ);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (attackCoord == null) {
			if (ServerTickHandler.wd != null && ServerTickHandler.wd.coordCurses.get(ent.worldObj.provider.dimensionId) != null && ServerTickHandler.wd.coordCurses.get(ent.worldObj.provider.dimensionId).size() > 0) {
				//System.out.println("giving miner curse coords");
				attackCoord = ServerTickHandler.wd.coordCurses.get(ent.worldObj.provider.dimensionId).get(0);
			}
		} else {
			
		}
		
		checkCoord();
		
		//weird temp kill
		if (ent.worldObj.getBlockId((int)ent.posX, (int)ent.posY-1, (int)ent.posZ) != HostileWorlds.blockBloodyCobblestone.blockID) {
			//System.out.println("weawewaeawe");
			//ent.setDead();
			//return;
		}
		
		//water nav help
		if (ent.isInWater()) {
			ent.motionY += 0.05F;
			if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.1F) {
				ent.moveFlying(/*(ent.worldObj.rand.nextFloat() * 0.2F) - 0.1F*/0, 0.1F, 0.1F);
			}
		}
		
		//jump help - something broke default nav jumping, why?
		if (ent.onGround && ent.isCollidedHorizontally/* && ent.boundingBox.minY < bestDigCoord.posY*/) {
			ent.jump();
		}
		
		
		if (ent instanceof ZombieMiner) {
			//have JobPathDigger handle it
			attackCurse = true;
		} else {
			//Walk to leader
			huntRange = 3;
			float teleportRange = 30;
			
			int findLeaderRange = 60;
			int minPlayerDist = 60;
			
			if (leader == null && ent.worldObj.getWorldTime() % 200 == 0) {
				List<Entity> ents = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(findLeaderRange, findLeaderRange, findLeaderRange));
				for (int i = 0; i < ents.size(); i++) {
					if (ents.get(i) instanceof ZombieMiner && !ents.get(i).isDead) {
						leader = (EntityLivingBase) ents.get(i);
						break;
					}
				}
			}
			
			if (leader != null && !leader.isDead) {
				
				if (isInFormation() && !(ai.activeFormation.leaderTarget instanceof ZombieMiner)) {
					if (leader instanceof ICoroAI) {
						
						
						Formation fm = null;
						
						//diff check to make sure the cooldown override doesnt mess with it - fixed!
						
						if (ai.activeFormation.leaderEnt instanceof ZombieMiner) {
							
						} else {
							ai.activeFormation.leaderEnt = (EntityLiving) leader;
						}
						
						/*if (((ICoroAI) leader).getAIAgent().isInFormation()) {
							fm = ((ICoroAI) leader).getAIAgent().activeFormation;
							if (ai.activeFormation != fm) {
								ai.activeFormation.leave(entInt);
								fm.join(entInt);
								System.out.println("joining leader formation");
							}
						} else {
							System.out.println("making leader join a formation");
							ai.activeFormation.join((ICoroAI) leader);
						}
						if (fm != null && !(fm.leaderEnt instanceof ZombieMiner)) {
							System.out.println("updating formation to have miner as leader");
							fm.leaderEnt = leader;
						}*/
					}
				}
				
				float dist = ent.getDistanceToEntity(leader);
				if (entInt.getAIAgent().entityToAttack == null) {
					EntityPlayer entP = ent.worldObj.getClosestPlayerToEntity(ent, minPlayerDist);
					if (dist > teleportRange && ((entP == null || !ent.canEntityBeSeen(entP)) && (entP == null || !leader.canEntityBeSeen(entP)))) {
						//teleport if far
						ent.setPosition(leader.posX, leader.posY, leader.posZ);
						ent.getNavigator().setPath(null, 0);
						entInt.getAIAgent().jobMan.getPrimaryJob().ticksBeforeFormationRetry = 0;
						entInt.getAIAgent().jobMan.getPrimaryJob().ticksBeforeCloseCombatRetry = 0;
						
						//cheaty teleport fixing for followers when not near players
						
					} else if (dist > huntRange) {
						
						//entInt.getAIAgent().entityToAttack = null;
							if (this.ent.getNavigator().noPath() && ent.onGround && ent.worldObj.getWorldTime() % 10 == 0) {
								entInt.getAIAgent().walkTo(ent, (int)leader.posX, (int)leader.posY, (int)leader.posZ, 64, 600, 1);
								//System.out.println("derp222222222");
							}
						
					}
				}
			} else {
				leader = null;
				attackCurse = true;
			}
		}
		
		//Walk to curse
		if (attackCurse) {
			huntRange = 3;
			int pfRange = 128;
			
			if (ent instanceof ZombieMiner) {
				huntRange = 3;
				pfRange = 512;
			} else {
				
			}
			
			if (attackCoord != null) {
				if (ent.getDistance(attackCoord.posX, attackCoord.posY, attackCoord.posZ) > huntRange) {
					
					if (this.ent.getNavigator().getPath() != null) {
						//System.out.println(this.ent.getNavigator().getPath().getCurrentPathLength());
					} else {
						//System.out.println("bam?!");
					}
					if (entInt.getAIAgent().entityToAttack == null && this.ent.getNavigator().noPath() && ent.onGround/*entInt.getAIAgent().notPathing()*/) {
						//entInt.getAIAgent().entityToAttack = null;
						entInt.getAIAgent().walkTo(ent, attackCoord.posX, attackCoord.posY, attackCoord.posZ, pfRange, 600, 1);
						//System.out.println("derp");
					}
				}
			}
		}
	}
	
	public void checkCoord() {
		if (attackCoord == null || (ent.worldObj.getWorldTime() % 500 == 0 && ent.getDistance(attackCoord.posX, attackCoord.posY, attackCoord.posZ) < 64)) {
			//if (ent.worldObj.provider.getDimensionName().equalsIgnoreCase("catacombs")) {
				
			 	//&& ent.worldObj.getClosestPlayerToEntity(ent, 64)
				EntityPlayer entP = ent.worldObj.getClosestPlayerToEntity(ent, 256);
				if (entP == null) {
					entP = ent.worldObj.getPlayerEntityByName(((EntityInvader)ent).primaryTarget);
				}
				
				//test
				//entP = null;
				
				if (entP != null && !ModConfigFields.noInvadeWhitelist.contains(entP.username)) {
					//System.out.println("setting player coords");
					((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ);
				} else {
					if (ent.worldObj.provider.getDimensionName().equalsIgnoreCase("catacombs")) {
						((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = HWTeleporter.portalCoord;
					} else {
						//THEY WIN!
					}
				}
			//}
		}
	}
	
	@Override
	public boolean shouldExecute() {
		return true/*entInt.getAIAgent().entityToAttack == null*/;
	}
	
	@Override
	public boolean shouldContinue() {
		return true;
	}
	
	@Override
	public boolean avoid(boolean actOnTrue) {
		return false;
	}
	
	@Override
	public boolean shouldTickFormation() {
		if (ent instanceof Zombie && ((Zombie) ent).stackMode) {
			return false;
		} else {
			return super.shouldTickFormation();
		}
	}
	
	@Override
	public boolean shouldTickCloseCombat() {
		return super.shouldTickCloseCombat();
	}
	
	@Override
	public void onTickFormation() {
		// TODO Auto-generated method stub
		super.onTickFormation();
		if (isInFormation() && leader != null) {
			if (((ICoroAI)leader).getAIAgent() != null && ((ICoroAI)leader).getAIAgent().jobMan.getPrimaryJob().state == EnumJobState.W3) {
				//System.out.println("no!");
				ent.entityCollisionReduction = 1F;				
			} else {
				//System.out.println("yes!");
				ent.entityCollisionReduction = ai.collideResistFormation;
				//ent.entityCollisionReduction = 1F;
			}
		}
	}
}
