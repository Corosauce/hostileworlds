package hostileworlds.rts.ai;

import hostileworlds.rts.building.BuildingBase;
import CoroAI.bt.OrdersHandler;
import CoroAI.componentAI.ICoroAI;

public class OrdersHandlerRtsBuilding extends OrdersHandler {

	public BuildingBase building;

	public OrdersHandlerRtsBuilding(ICoroAI parEnt) {
		super(parEnt);
	}

}
