package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityInvader;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import CoroAI.componentAI.IAdvPF;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.jobSystem.JobFormation;

public class Zombie extends EntityInvader implements IAdvPF {
	
	public int tryHoist = 0;
	public boolean stackMode = false;
	public int noMoveTicks = 0;

	public Zombie(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobGroupHorde(agent.jobMan));
		agent.jobMan.addJob(new JobHunt(agent.jobMan));
		agent.jobMan.addJob(new JobFormation(agent.jobMan));
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Item.swordIron));
		this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetIron));
		
		//agent.setMoveSpeed(0.30F);
		agent.maxReach_Melee = 1.3F;
		
		
		
		entityCollisionReduction = 1F;
	}
	
	@Override
	public boolean canClimbWalls() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canClimbLadders() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getDropSize() {
		// TODO Auto-generated method stub
		return 999;
	}
	
	@Override
	public void onLivingUpdate() {
		if (onGround) {
			entityCollisionReduction = 0.4F;
		} else {
			entityCollisionReduction = 1F;
		}
		
		//fallDistance = 0;
		
		super.onLivingUpdate();
		
		//so temp
		/*if (!worldObj.isRemote) {
			EntityPlayer entP = worldObj.getClosestPlayerToEntity(this, -1);
			if (entP != null) {
				agent.jobMan.getPrimaryJob().tamable.owner = entP.username;
			}
		}*/
		
		//boolean moveToPlayer = true;
		if (!worldObj.isRemote) {
			List<Entity> var2 = this.worldObj.getEntitiesWithinAABB(EntityFallingSand.class, this.boundingBox.expand(1.0D, 3D, 1.0D));
			
			Random rand = new Random();
			Entity ent = null;
			
			if (var2.size() > 0) {
				for (int i = 0; i < var2.size(); i++) {
					var2.get(i).setDead();
				}
			}
		}
		
		if (!stackMode) {
			double speed = (double)MathHelper.sqrt_double(motionX * motionX/* + vecY * vecY*/ + motionZ * motionZ);
			//System.out.println(speed);
			if (speed < 0.05D) {
				noMoveTicks++;
			} else {
				noMoveTicks = 0;
			}
		} else {
			//noMoveTicks = 0;
		}
		
		//System.out.println(speed);
		
		double factor = -1;
		
		if (!worldObj.isRemote) {
			if (this.getAIAgent().entityToAttack != null) {
				double xDiff = this.getAIAgent().entityToAttack.posX - posX;
				double zDiff = this.getAIAgent().entityToAttack.posZ - posZ;
				double distHoriz = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
				if (distHoriz < 0) distHoriz = 1;
				
				double distVert = this.getAIAgent().entityToAttack.posY - posY;
				
				factor = distVert / distHoriz;
			}
		}
		
		
		
		if (!stackMode) {
			 if (this.getAIAgent().entityToAttack != null && this.getNavigator().noPath() && !isInWater() && (noMoveTicks > 60 || (factor != -1 && factor > 5))) {
				 stackMode = true;
				 noMoveTicks = 0;
			 }
		} else {
			if (this.getAIAgent().entityToAttack == null) {
				stackMode = false;
			}
		}
		
		/*double rangeBox = 0.8D;
		double rangeShiftToStack = 0.5D;
		double rangeShiftToTarget = 0.2D;*/
		
		double rangeBox = 1.5D;
		double lungeSpeed = 0.4D;
		double rangeShiftBase = 0.4D;
		double rangeShiftAdjToStack = 0.3D;
		double rangeShiftAdjToTarget = 0.1D;
		double rangeShiftAdjFromTarget = 0.3D;
		double rangeNeededToShiftToTarget = 1.4D;
		
		/* || isNearWall(boundingBox) || isOnLadder()*/
		if (!worldObj.isRemote && stackMode) {
			List<EntityLivingBase> var2 = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.boundingBox.expand(rangeBox, 2D, rangeBox));
			
			Random rand = new Random();
			Entity ent = null;
			
			if (var2.size() > 1) {
				for (int i = 0; i < var2.size(); i++) {
					if (var2.get(i) != this && var2.get(i) instanceof Zombie && this.entityId > var2.get(i).entityId && this.boundingBox.minY+1.5D > var2.get(i).boundingBox.minY && this.boundingBox.minY < var2.get(i).boundingBox.maxY/*this.boundingBox.minY-0.1D < var2.get(i).boundingBox.maxY && this.boundingBox.minY < var2.get(i).boundingBox.maxY*/) {
						ent = (Entity)var2.get(i);
						break;
					}
				}
			}
			//this.boundingBox.minY+0.001D > var2.get(i).boundingBox.minY && this.boundingBox.minY < var2.get(i).boundingBox.maxY
			if (tryHoist > 0) {
				if (this.getAIAgent().entityToAttack.getDistanceToEntity(this) < 3F || (tryHoist == 1 && this.getAIAgent().entityToAttack.boundingBox.minY < this.boundingBox.minY - 1)) {
					double vecX = this.getAIAgent().entityToAttack.posX - posX;
					double vecY = this.getAIAgent().entityToAttack.posY - posY;
					double vecZ = this.getAIAgent().entityToAttack.posZ - posZ;
			    	
					double var9 = (double)MathHelper.sqrt_double(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
			    
					double shiftSpeed = lungeSpeed;
			        this.motionX = vecX / var9 * shiftSpeed;
			        this.motionY = 0.5;
			        this.motionZ = vecZ / var9 * shiftSpeed;
			        //System.out.println("hoissst!");
			        tryHoist = 0;
			        stackMode = false;
			        
				}
				tryHoist--;
			}
			
			//if (onGround) tryHoist = 0;
			
			if (ent != null) {
				if (this.getAIAgent().entityToAttack.boundingBox.minY > this.boundingBox.minY + 2/* && this.getNavigator().noPath()*/) {
					tryHoist = 40;
				} else if (this.getAIAgent().entityToAttack.boundingBox.minY < this.boundingBox.minY - 1 || !isNearWall(boundingBox)) {
					
					//tryHoist = 0;
				}
				
				
				
				if (tryHoist > 0) {
					
					//break through leafs
					if (!worldObj.isRemote) {
						double tryX = posX-0.8D+rand.nextFloat();
						double tryY = posY+0.5D+(rand.nextFloat() * 2D);
						double tryZ = posZ-0.8D+rand.nextFloat();
						int id = worldObj.getBlockId((int)(tryX), (int)(tryY), (int)(tryZ));
						if (id != 0 && (Block.blocksList[id].blockMaterial == Material.leaves || Block.blocksList[id].blockMaterial == Material.plants)) {
							//System.out.println("remove leafs!");
							worldObj.setBlock((int)(tryX), (int)(tryY), (int)(tryZ), 0);
						}
					}
					
					double shiftSpeed = rangeShiftBase;
					/*this.motionX += Math.sin(ent.posX - posX) * shiftSpeed;
					this.motionZ -= Math.sin(ent.posZ - posZ) * shiftSpeed;*/
					double var9;
					double vecX = ent.posX - posX;
			    	double vecY = ent.posY - posY;
			    	double vecZ = ent.posZ - posZ;
					
			    	double dist = (double)MathHelper.sqrt_double(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
			    	if (dist > 0.1D) {
				        this.motionX = vecX / dist * shiftSpeed * rangeShiftAdjToStack;
				        //this.motionY = vecY / var9 * shiftSpeed;
				        this.motionZ = vecZ / dist * shiftSpeed * rangeShiftAdjToStack;
			    	}
			    	
					//if (this.getAIAgent().entityToAttack != null) {
						vecX = this.getAIAgent().entityToAttack.posX - posX;
				    	vecY = this.getAIAgent().entityToAttack.posY - posY;
				    	vecZ = this.getAIAgent().entityToAttack.posZ - posZ;
					//}
				    
					
			        var9 = (double)MathHelper.sqrt_double(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
			        if (dist+shiftSpeed < rangeNeededToShiftToTarget) {
			        	if (worldObj.canBlockSeeTheSky((int)(posX+0.0D+(vecX/var9*1.5F)), (int)(posY), (int)(posZ+0.0D+(vecZ/var9*1.5F))) || (worldObj.getHeightValue((int)(posX+0.5D), (int)(posZ+0.5D)) > this.getAIAgent().entityToAttack.posY + 1)) {
					        this.motionX = vecX / var9 * shiftSpeed * rangeShiftAdjToTarget;
					        //this.motionY = vecY / var9 * shiftSpeed;
					        this.motionZ = vecZ / var9 * shiftSpeed * rangeShiftAdjToTarget;
			        	} else {
			        		this.motionX = -vecX / var9 * shiftSpeed * /*rand.nextFloat() * */rangeShiftAdjFromTarget;
					        //this.motionY = vecY / var9 * shiftSpeed;
					        this.motionZ = -vecZ / var9 * shiftSpeed * /*rand.nextFloat() * */rangeShiftAdjFromTarget;
			        	}
			        }
			        
			        
					
			        float wat = 0.15F;// + rand.nextFloat() * 0.05F;
			        
			        //if (wat > 0.2F) wat = 0.2F;
			        
			        if (this.motionY < -0.0) this.motionY = -0.0F;
			        
			        this.motionY += wat;//+= Math.min(0.3F, Math.max(0F, var2.size()-1) * (/*rand.nextFloat() * */0.025F));
					
					if (motionY > 0.15F) motionY = 0.15F;
					if (ent.onGround) {
						//ent.motionX = -vecX / var9 * shiftSpeed;
				        //ent.motionZ = -vecZ / var9 * shiftSpeed;
						//ent.moveEntity(ent.motionX * 15D, 0D, ent.motionZ * 15D);
					} else {
						
						
					}
				}
			} else {
				//stackMode = false;
			}
			
		} else {
			
		}
		
		if (!worldObj.isRemote) {
			if (isOnLadder()) {
				//this.motionY = -0.2F;
			}
		}
		
	}

	@Override
	public int overrideBlockPathOffset(ICoroAI ent, int id, int meta, int x,
			int y, int z) {
		return -66;
	}
}
