package hostileworlds.ai.tasks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import CoroPets.ai.ITaskInitializer;

public class TaskDigTowardsTarget extends EntityAIBase implements ITaskInitializer
{
    private EntityCreature entity = null;
    private ChunkCoordinates posCurMining = null;
    private EntityLivingBase targetLastTracked = null;
    private int digTimeCur = 0;
    private int digTimeMax = 15*20;
    private double curBlockDamage = 0D;

    public TaskDigTowardsTarget()
    {
        //this.setMutexBits(3);
    }
    
    @Override
    public void setEntity(EntityCreature creature) {
    	this.entity = creature;
    }
    
    //method for future order handling
    public boolean canMove() {
    	return true;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	//System.out.println("should?");
    	
    	if (!entity.onGround && !entity.isInWater()) return false;
    	//return true if not pathing, has target
    	if (entity.getAttackTarget() != null || targetLastTracked != null) {
    		if (entity.getAttackTarget() == null) {
    			System.out.println("forcing reset of target2");
    			entity.setAttackTarget(targetLastTracked);
    		} else {
    			targetLastTracked = entity.getAttackTarget();
    		}
    		//if (!entity.getNavigator().noPath()) System.out.println("path size: " + entity.getNavigator().getPath().getCurrentPathLength());
    		if (entity.getNavigator().noPath() || entity.getNavigator().getPath().getCurrentPathLength() == 1) {
    		//if (entity.motionX < 0.1D && entity.motionZ < 0.1D) {
    			if (updateBlockToMine()) {
    				System.out.println("should!");
    				return true;
    			}
    		}
    	}
    	
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
    	//System.out.println("continue!");
    	if (posCurMining == null) return false;
    	if (entity.worldObj.getBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ) != Blocks.air) {
    		return true;
    	} else {
    		posCurMining = null;
    		System.out.println("ending execute");
    		return false;
    	}
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	System.out.println("start!");
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
    	System.out.println("reset!");
    	digTimeCur = 0;
    	curBlockDamage = 0;
    	posCurMining = null;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	//System.out.println("running!");
    	
    	if (entity.getAttackTarget() != null) {
    		targetLastTracked = entity.getAttackTarget();
    	} else {
    		if (targetLastTracked != null) {
    			System.out.println("forcing reset of target");
    			entity.setAttackTarget(targetLastTracked);
    		}
    	}
    	
    	tickMineBlock();
    }
    
    public boolean updateBlockToMine() {
    	
    	posCurMining = null;
    	
    	double vecX = entity.getAttackTarget().posX - entity.posX;
    	//feet
    	double vecY = entity.getAttackTarget().posY - entity.boundingBox.minY;
    	double vecZ = entity.getAttackTarget().posZ - entity.posZ;
    	
    	double dist = (double)MathHelper.sqrt_double(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
    	
    	double scanX = entity.posX + (vecX / dist);
    	double scanZ = entity.posZ + (vecZ / dist);
    	
    	Random rand = new Random(entity.worldObj.getTotalWorldTime());
    	
    	if (rand.nextBoolean()/*Math.abs(vecX) < Math.abs(vecZ)*/) {
    		//scanX = entity.posX;
        	scanZ = entity.posZ + 0;
    	} else {
    		scanX = entity.posX + 0;
        	//scanZ = entity.posZ;
    	}
    	
    	ChunkCoordinates coords = new ChunkCoordinates(MathHelper.floor_double(scanX), MathHelper.floor_double(entity.boundingBox.minY + 1), MathHelper.floor_double(scanZ));
    	
    	Block block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
    	
    	//System.out.println("ahead to target: " + block);
    	
    	if (canMineBlock(block)) {
    		posCurMining = coords;
    		//entity.worldObj.setBlock(coords.posX, coords.posY, coords.posZ, Blocks.air);
    		return true;
    	} else {
    		if (vecY > 0) {
    			coords.posY++;
        		block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
        		if (canMineBlock(block)) {
            		posCurMining = coords;
            		return true;
        		}
    		}
    		
    		//if dont or cant dig up, continue strait
    		coords.posY--;
    		block = entity.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
    		if (canMineBlock(block)) {
        		posCurMining = coords;
        		return true;
    		}
    		
    		return false;
    	}
    }
    
    public boolean canMineBlock(Block block) {
    	if (block == Blocks.air) {
    		return false;
    	}
    	if (block == Blocks.obsidian) {
    		return false;
    	}
    	if (block.getMaterial().isLiquid()) {
    		return false;
    	}
    	return true;
    }
    
    public void tickMineBlock() {
    	if (posCurMining == null) return;
    	
    	entity.getNavigator().clearPathEntity();
    	
    	Block block = entity.worldObj.getBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	double blockStrength = block.getBlockHardness(entity.worldObj, posCurMining.posX, posCurMining.posY, posCurMining.posZ);
    	
    	if (blockStrength == -1) {
    		posCurMining = null;
    		return;
    	}
    	
    	curBlockDamage += 0.01D / blockStrength;
    	
    	if (curBlockDamage > 1D) {
    		entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, -1);
    		entity.worldObj.setBlock(posCurMining.posX, posCurMining.posY, posCurMining.posZ, Blocks.air);
    	} else {
    		entity.worldObj.destroyBlockInWorldPartially(entity.getEntityId(), posCurMining.posX, posCurMining.posY, posCurMining.posZ, (int)(curBlockDamage * 10D));
    	}
    	if (entity.worldObj.getTotalWorldTime() % (30+entity.worldObj.rand.nextInt(20)) == 0) {
    		entity.swingItem();
    	}
    }
}
