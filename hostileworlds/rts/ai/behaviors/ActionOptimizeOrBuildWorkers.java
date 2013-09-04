package hostileworlds.rts.ai.behaviors;

import hostileworlds.rts.TeamObject;
import hostileworlds.rts.ai.orders.OrdersGatherRes;
import hostileworlds.rts.entity.EntityRtsBase;

import java.util.List;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.OrdersData;
import CoroAI.bt.leaf.LeafAction;

public class ActionOptimizeOrBuildWorkers extends LeafAction {

	int id = 0;
	int countCur = 0;
	int countMax = 80;
	int countCurCheckForIdleWorkers = 0;
	int countMaxCheckForIdleWorkers = 150; //make longer when not testing
	
	//tracking
	int lastCountGatherers; //assumed active, technically stuck gatherers etc could be in this count
	
	public TeamObject team;
	
	public ActionOptimizeOrBuildWorkers(Behavior parParent, TeamObject parTeam) {
		super(parParent);
		team = parTeam;
	}
	
	@Override
	public EnumBehaviorState tick() {
		//dbg("Leaf BuildWorker Tick: " + countCur + "/" + countMax);
		
		if (countCurCheckForIdleWorkers++ > countMaxCheckForIdleWorkers) {
			List<EntityRtsBase> workers = team.getWorkersTakingOrders("gather", false);
			
			if (workers.size() > 0) {
				for (int i = 0; i < workers.size(); i++) {
					EntityRtsBase entRts = workers.get(i);
					
					dbg("giving unit " + entRts.entityId + " gather order - " + entRts.entityId);
					
					//test coord again
					ChunkCoordinates coordsGather = new ChunkCoordinates(team.spawn.posX + 10, team.spawn.posY, team.spawn.posZ);
					coordsGather = new ChunkCoordinates(MathHelper.floor_double(entRts.posX), MathHelper.floor_double(entRts.posY), MathHelper.floor_double(entRts.posZ));
					OrdersData orders = new OrdersGatherRes(new ChunkCoordinates[] { coordsGather }, new ChunkCoordinates[] { new ChunkCoordinates(team.spawn) }, team);
					orders.ent = entRts;
					orders.initBehaviors();
					
					entRts.bh.ordersHandler.setOrders(orders);
				}
			}
			countCurCheckForIdleWorkers = 0;
		} else {
			
		}
		
		if (countCur++ > countMax) {
			
			if (false || ((/*team.res_Rate[0] != 0 && */team.resources.resWoodRate[0] < 100) && team.listWorkers.size() <= 20)) {
				World world = DimensionManager.getWorld(team.dimID);
				
				if (world != null) {
					//test coord
					ChunkCoordinates coordsGather = null;//new ChunkCoordinates(team.spawn.posX, team.spawn.posY, team.spawn.posZ);
					EntityRtsBase ent = team.spawnUnit(world, "worker", team.spawn, null/*new OrdersGatherRes(new ChunkCoordinates[] { coordsGather }, new ChunkCoordinates[] { new ChunkCoordinates(team.spawn) }, team)*/);
					OrdersData orders = new OrdersGatherRes(new ChunkCoordinates[] { new ChunkCoordinates(team.spawn) }, new ChunkCoordinates[] { new ChunkCoordinates(team.spawn) }, team);
					orders.ent = ent;
					orders.initBehaviors();
					ent.bh.ordersHandler.setOrders(orders);
				}
			}
			
			reset();
			return EnumBehaviorState.SUCCESS;
		} else {
			return EnumBehaviorState.RUNNING;
		}
	}
	
	@Override
	public void reset() {
		//dbg("Leaf BuildWorker Reset");
		countCur = 0;
		super.reset();
	}
	
}
