package hostileworlds.rts.ai.behaviors;

import hostileworlds.rts.TeamObject;
import hostileworlds.rts.ai.orders.OrdersBuildBuilding;
import hostileworlds.rts.entity.EntityRtsBase;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.OrdersHandler;
import CoroAI.bt.leaf.LeafAction;

public class ActionBuildBuildingTemp extends LeafAction {

	public TeamObject team;
	public String buildingName;
	public OrdersHandler ordersH;
	
	public int ticksCurOrders = 0;
	public int ticksCooldownGiveOrders = 0;
	
	public ActionBuildBuildingTemp(Behavior parParent, TeamObject parTeam) {
		super(parParent);
		team = parTeam;
		//buildingName = parName;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		int ticksCurOrdersMax = 20 * 60;
		int ticksCooldownGiveOrdersMax = 60;
		
		if (ticksCooldownGiveOrders > 0) ticksCooldownGiveOrders--;
		
		int buildCost = 100;
		
		
		//track given order for success status, then.. stuff
		if (ordersH == null || ordersH.activeOrders == null || !(ordersH.activeOrders instanceof OrdersBuildBuilding) || ordersH.activeOrders.ent == null || ((EntityLivingBase)ordersH.activeOrders.ent).isDead) {
			//logic to check for house need
			
			//so very temp - a little less temp now
			if (team.resources.resWood[0] > 200 + buildCost) {
				if (ticksCooldownGiveOrders == 0 && team.listWorkers.size() > 0) {
					Random rand = new Random();
					
					ChunkCoordinates spawnCoords = team.getValidBuildingSpot(buildingName);
					
					if (spawnCoords != null) {
						
						EntityRtsBase ent = team.listWorkers.get(rand.nextInt(team.listWorkers.size()));
						dbg("giving unit " + ent.entityId + " house build order");
						ordersH = ent.bh.ordersHandler;
						buildingName = "house";
						OrdersBuildBuilding orders = new OrdersBuildBuilding(buildingName, spawnCoords);
						orders.ent = ent;
						orders.initBehaviors();
						
						ordersH.setOrders(orders);
						ordersH.ordersAcceptable = "build"; //???
						
						ticksCooldownGiveOrders = ticksCooldownGiveOrdersMax;
						
						return EnumBehaviorState.RUNNING;
					} else {
						team.townSizeRadius += 10;
						System.out.println("cant build, increasing town size to: " + team.townSizeRadius);
					}
				}
			}
			
		} else {
			if (ticksCurOrders <= ticksCurOrdersMax) {
				if (ordersH.activeOrders.activeOrdersStatusLast == EnumBehaviorState.RUNNING) {
					ticksCurOrders++;
					return EnumBehaviorState.RUNNING;
				} else {
					ticksCurOrders = 0;
					if (ordersH.activeOrders.activeOrdersStatusLast == EnumBehaviorState.SUCCESS) {
						team.resources.resWood[0] = team.resources.resWood[0] - buildCost;
						//orders callback says build worked out
					} else if (ordersH.activeOrders.activeOrdersStatusLast == EnumBehaviorState.FAILURE) {
						//something went wrong
					}
					
					EnumBehaviorState returnStatus = ordersH.activeOrders.activeOrdersStatusLast;
					
					dbg("unit " + ((EntityLivingBase)ordersH.ent).entityId + " nulling build orders, wood now: " + team.resources.resWood[0] + ", return was: " + ordersH.activeOrders.activeOrdersStatusLast);
					resetOrders();
					return returnStatus;
					//at this point itll recheck active buildings for tech tree and population max info etc
					//
				}
			} else {
				dbg("build order timed out, nulling and letting a different worker try");
				resetOrders();
			}
		}
		
		
		return EnumBehaviorState.SUCCESS;
	}
	
	public void resetOrders() {
		ticksCurOrders = 0;
		ordersH.setOrders(null);
		ordersH = null;
	}

}
