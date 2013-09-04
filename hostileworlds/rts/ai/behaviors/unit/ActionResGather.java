package hostileworlds.rts.ai.behaviors.unit;

import hostileworlds.rts.ai.orders.OrdersGatherRes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class ActionResGather extends LeafAction {

	public OrdersGatherRes ordersRef;
	
	//runtime stuff
	int noGatherCoordTicks = 0;
	
	public ActionResGather(Behavior parParent, OrdersGatherRes gatherOrders) {
		super(parParent);
		ordersRef = gatherOrders;
	}
	
	@Override
	public EnumBehaviorState tick() {
	
		int noGatherCoordTicksUseShared = 60;
		
		EntityLivingBase ent = ((EntityLivingBase)ordersRef.ent);
		
		int id = ent.worldObj.getBlockId(ordersRef.coordLastGather[0].posX, ordersRef.coordLastGather[0].posY, ordersRef.coordLastGather[0].posZ);
		
		//temp wood check
		if (!ordersRef.team.canUseIDForResource(id)) {
			ChunkCoordinates tryCoords = ordersRef.getResBeside(ordersRef.coordLastGather[0], 2);
			if (tryCoords == null/* && ent.worldObj.getWorldTime() % 40 == 0*/) {
				//alternate between these 2 tick based gradual scanning methods, the base scan one scans faster depending on how many workers are firing this method
				if (ent.worldObj.getWorldTime() % 2 == 0) {
					tryCoords = ordersRef.team.getResBaseScan();//getResClose();
				} else {
					tryCoords = ordersRef.getResClose();
				}
			}

			if (tryCoords == null) {
				noGatherCoordTicks++;
			} else {
				noGatherCoordTicks = 0;
			}
			
			if (tryCoords == null && noGatherCoordTicks > noGatherCoordTicksUseShared) {
				tryCoords = ordersRef.getResRecentlyDiscovered();
			}
			
			if (tryCoords != null) {
				ordersRef.coordLastGather[0] = tryCoords;
			}
		} else {
			//dbg("GATHER!!");
			ordersRef.cargoCount[0] += 1;
			ordersRef.team.listRecentResLocations.add(ordersRef.coordLastGather[0]);
			ent.worldObj.setBlock(ordersRef.coordLastGather[0].posX, ordersRef.coordLastGather[0].posY, ordersRef.coordLastGather[0].posZ, 0);
			if (ordersRef.cargoCount[0] >= ordersRef.cargoCountMax) {
				ordersRef.hasCargo[0] = true;
			}
		}
		return EnumBehaviorState.SUCCESS;
		
	}

}
