package hostileworlds.entity.monster;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityInvader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroAI.componentAI.IAdvPF;
import CoroAI.componentAI.ICoroAI;

public class ZombieClimber extends EntityInvader implements IAdvPF {

	public int climbingTicks;
	public int climbingTicksMax = 600;
	
	public ZombieClimber(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobGroupHorde(agent.jobMan));
		agent.jobMan.addJob(new JobHunt(agent.jobMan));
		
		//this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetLeather));
		this.setCurrentItemOrArmor(0, new ItemStack(Block.ladder));
		this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetIron));
	}

	@Override
	public boolean canClimbWalls() {
		// TODO Auto-generated method stub
		return true;
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
		
		fallDistance = 0F;
		
		
		
		super.onLivingUpdate();
		if (!worldObj.isRemote) {
			
			if (climbingTicks < 0) climbingTicks++;
			
			if (/*this.getAIAgent().entityToAttack != null && */!this.getNavigator().noPath() && isCollidedHorizontally/*isNearWall(this.boundingBox)*/) {
				
				climbingTicks++;
				
				if (climbingTicks > climbingTicksMax) {
					climbingTicks = -80;
					return;
				}
				
				this.motionY = 0.15F;
				
				//rotate by 45 for max chance of proper quantify
                float angle = rotationYaw + 45;
                while (angle < 0) angle += 360;
				while (angle > 360) angle -= 360;
				int meta = (int) angle / 90;
				//translate to ladder angle
				if (meta == 0) { meta = 2; } else if (meta == 2) { meta = 3; } else if (meta == 3) { meta = 4; } else if (meta == 1) { meta = 5; } 
				float dist2 = 1;
				Vec3 vPos = getPosition(1).addVector(0D, -0.5D, 0D);
		        Vec3 vLook = getLook(1);
		        Vec3 vec = vPos.addVector(vLook.xCoord * dist2, vLook.yCoord * dist2, vLook.zCoord * dist2);
		        MovingObjectPosition mop = worldObj.clip(vPos, vec);
            	
		        if (mop != null) {
		        	int fixX = 0;
		        	int fixZ = 0;
		        	if (mop.sideHit == 2) { fixZ = -1; } else if (mop.sideHit == 3) { fixZ = 1; } else if (mop.sideHit == 4) { fixX = -1; } else if (mop.sideHit == 5) { fixX = 1; }
		        	int id = worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
		        	
		        	if (id != 0 && Block.blocksList[id].blockMaterial.isSolid() && worldObj.getBlockId(mop.blockX+fixX, mop.blockY, mop.blockZ+fixZ) == 0) {
		        		int id2 = worldObj.getBlockId(mop.blockX+fixX, mop.blockY-1, mop.blockZ+fixZ);
		        		worldObj.setBlock(mop.blockX+fixX, mop.blockY, mop.blockZ+fixZ, (id2 == HostileWorlds.blockRaidingLadder.blockID || id2 == HostileWorlds.blockRaidingLadderBase.blockID) ? /*Block.ladder.blockID : Block.ladder.blockID*/HostileWorlds.blockRaidingLadder.blockID : HostileWorlds.blockRaidingLadderBase.blockID, meta, 3);
		        		HostileWorlds.blockRaidingLadder.onNeighborBlockChange(worldObj, mop.blockX+fixX, mop.blockY, mop.blockZ+fixZ, id);
		        	}
		        }
			} else {
				if (climbingTicks > 0) climbingTicks = 0;
			}
			
		}
	}

	@Override
	public int overrideBlockPathOffset(ICoroAI ent, int id, int meta, int x,
			int y, int z) {
		// TODO Auto-generated method stub
		return -66;
	}

}
