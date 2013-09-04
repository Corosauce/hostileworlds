package hostileworlds.rts.ai.behaviors.unit;

import hostileworlds.rts.ai.orders.OrdersGatherRes;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class ActionResDropOff extends LeafAction {

	public OrdersGatherRes ordersRef;
	
	public ActionResDropOff(Behavior parParent, OrdersGatherRes gatherOrders) {
		super(parParent);
		ordersRef = gatherOrders;
		//need dropoff coords and world reference, same for gather?
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//how does this method link its resources gathered back to the team data?--- no, wrong thought process
		//unit gives gather data to the coords its dropping off at, an attempt to access a tile entity with economy interface
		//from there the building would decide what to do with the stuff, pass to main town eco or it does some special per building sto
		
		ordersRef.hasCargo[0] = false;
		ordersRef.team.resources.resWood[0] += ordersRef.cargoCount[0];
		ordersRef.cargoCount[0] = 0;
		//dbg("DROPOFF!! - wood: " + ordersRef.team.resources.resWood[0]);
		return EnumBehaviorState.SUCCESS;
	}

}
