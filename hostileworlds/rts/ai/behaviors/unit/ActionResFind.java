package hostileworlds.rts.ai.behaviors.unit;

import hostileworlds.rts.ai.orders.OrdersGatherRes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class ActionResFind extends LeafAction {

	public OrdersGatherRes ordersRef;
	
	//inner function bools
	public boolean findClose = false;
	public boolean findFar = false;
	public boolean findSharedInfo = false;
	
	public ActionResFind(Behavior parParent, OrdersGatherRes gatherOrders, boolean parFindClose, boolean parFindFar, boolean parFindSharedInfo) {
		super(parParent);
		findClose = parFindClose;
		findFar = parFindFar;
		findSharedInfo = parFindSharedInfo;
		ordersRef = gatherOrders;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//this class is kind've badly used, only uesd if initial gather coords is null, the gather action behavior does better handling of null / empty gather coords, refactor to generic method?
		if (findClose) {
			
			
			
			ChunkCoordinates tryCoords = null;
			
			EntityLivingBase ent = ((EntityLivingBase)ordersRef.ent);
			
			//System.out.println(ent.entityId + " using ActionResFind");
			
			if (tryCoords == null/* && ent.worldObj.getWorldTime() % 40 == 0*/) {
				//alternate between these 2 tick based gradual scanning methods, the base scan one scans faster depending on how many workers are firing this method
				if (ent.worldObj.getWorldTime() % 2 == 0) {
					tryCoords = ordersRef.team.getResBaseScan();//getResClose();
				} else {
					tryCoords = ordersRef.getResClose();
				}
			}

			/*if (tryCoords == null) {
				noGatherCoordTicks++;
			} else {
				noGatherCoordTicks = 0;
			}*/
			
			if (tryCoords == null/* && noGatherCoordTicks > noGatherCoordTicksUseShared*/) {
				tryCoords = ordersRef.getResRecentlyDiscovered();
			}
			
			if (tryCoords != null) {
				ordersRef.coordLastGather[0] = tryCoords;
			}
		}
		return EnumBehaviorState.SUCCESS;
	}

}
