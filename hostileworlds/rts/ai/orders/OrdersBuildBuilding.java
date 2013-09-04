package hostileworlds.rts.ai.orders;

import hostileworlds.rts.ai.behaviors.unit.ActionBuild;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.bt.OrdersData;
import CoroAI.bt.actions.Delay;
import CoroAI.bt.actions.MoveToCoords;

public class OrdersBuildBuilding extends OrdersData {

	public String buildingName;
	public ChunkCoordinates coordsBuild; //shouldnt need to do reference magic...
	
	
	public OrdersBuildBuilding(String parName, ChunkCoordinates parCoords) {
		super();
		buildingName = parName;
		coordsBuild = parCoords;
		activeOrdersName = "";
	}
	
	@Override
	public void initBehaviors() {
		this.activeOrdersAI = new MoveToCoords(null, this.ent, new ChunkCoordinates[] { coordsBuild }, 7);
		this.activeOrdersAI.dbgName = "moveToBuildDest";
		activeOrdersAI.add(new Delay(activeOrdersAI, 1, 0)); //this should never fire because the coords are never null, so no worry of its success return value
		activeOrdersAI.add(new ActionBuild(activeOrdersAI, this));
	}

}
