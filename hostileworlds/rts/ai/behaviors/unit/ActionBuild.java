package hostileworlds.rts.ai.behaviors.unit;

import hostileworlds.HostileWorlds;
import hostileworlds.rts.ai.orders.OrdersBuildBuilding;
import hostileworlds.rts.block.TileEntityRTSBuilding;
import hostileworlds.rts.entity.EntityRtsBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class ActionBuild extends LeafAction {

	public OrdersBuildBuilding ordersRef;
	
	public ActionBuild(Behavior parParent, OrdersBuildBuilding parOrders) {
		super(parParent);
		ordersRef = parOrders;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		dbg("trying to build: " + ordersRef.buildingName);
		World world = ((EntityLivingBase)ordersRef.ent).worldObj;
		world.setBlock(ordersRef.coordsBuild.posX, ordersRef.coordsBuild.posY, ordersRef.coordsBuild.posZ, HostileWorlds.blockRtsBuilding.blockID);
		TileEntity tEnt = world.getBlockTileEntity(ordersRef.coordsBuild.posX, ordersRef.coordsBuild.posY, ordersRef.coordsBuild.posZ);
		if (tEnt instanceof TileEntityRTSBuilding) {
			dbg("starting to build: " + ordersRef.buildingName);
			((TileEntityRTSBuilding) tEnt).setBuildingAndMarkInitReady(ordersRef.buildingName, ((EntityRtsBase)ordersRef.ent).teamID);
			return EnumBehaviorState.SUCCESS;
		}
		//place block, get tile ent, set the building type, set not waiting on external init
		//init build job, return success? or wait on build, meh------- nm, dont forget first tile building init starts build job, so we're done here
		
		return EnumBehaviorState.FAILURE;
	}

}
