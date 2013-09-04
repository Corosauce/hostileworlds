package hostileworlds.rts.ai.orders;

import hostileworlds.rts.TeamObject;
import hostileworlds.rts.ai.behaviors.unit.ActionResDropOff;
import hostileworlds.rts.ai.behaviors.unit.ActionResFind;
import hostileworlds.rts.ai.behaviors.unit.ActionResGather;
import hostileworlds.rts.entity.EntityRtsBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.bt.Behavior;
import CoroAI.bt.OrdersData;
import CoroAI.bt.actions.Delay;
import CoroAI.bt.actions.MoveToCoords;
import CoroAI.bt.selector.Selector;
import CoroAI.bt.selector.SelectorBoolean;

public class OrdersGatherRes extends OrdersData {

	//now used for marking full cargo load
	public boolean[] hasCargo = new boolean[1]; //does this really need to be reference style? - yes....
	public int[] cargoCount = new int[1];
	public int cargoCountMax = 5;
	public ChunkCoordinates[] coordLastGather = new ChunkCoordinates[1];
	public ChunkCoordinates[] coordLastReturn = new ChunkCoordinates[1];
	public TeamObject team;
	
	public int scanCurDist = 1;
	public int scanCurAngle = 0;
	
	public OrdersGatherRes(ChunkCoordinates[] parCoordLastGather, ChunkCoordinates[] parCoordLastReturn, TeamObject parTeam) {
		super();
		coordLastGather = parCoordLastGather;
		coordLastReturn = parCoordLastReturn;
		activeOrdersName = "gather";
		team = parTeam;
		
	}
	
	@Override
	public void initBehaviors() {
		this.activeOrdersAI = new SelectorBoolean(null, hasCargo);
		this.activeOrdersAI.dbgName = "hasCargo";
		Selector branch0_0 = new MoveToCoords(this.activeOrdersAI, ent, coordLastGather, 5, true, true);
		Selector branch0_1 = new MoveToCoords(this.activeOrdersAI, ent, coordLastReturn, 5, false, false); //NO DO NOT HAVE RETURN COORDS BECOME NULL FROM NO MOVE TICKS HELPER
		this.activeOrdersAI.add(branch0_0);
		this.activeOrdersAI.add(branch0_1);
		Behavior branch1_0 = new ActionResFind(branch0_0, this, true, false, false); //trying OrdersGatherRes reference usage here, starter 'blackboard' ? or should such info be read only ?
		Behavior branch1_1 = new ActionResGather(branch0_0, this);
		Behavior branch1_2 = new Delay(branch0_1, 1, 0);
		Behavior branch1_3 = new ActionResDropOff(branch0_1, this);
		branch0_0.add(branch1_0);
		branch0_0.add(branch1_1);
		branch0_1.add(branch1_2);
		branch0_1.add(branch1_3);
	}
	
	public ChunkCoordinates getResBeside(ChunkCoordinates prevMinedCoords, int parCloseRange) {
		//System.out.println("searching for resource beside with range of " + parCloseRange);
		int range = parCloseRange;
		
		EntityLivingBase ent2 = ((EntityLivingBase)ent);
		
		for (int xx = (int)Math.floor(prevMinedCoords.posX - range/2); xx < prevMinedCoords.posX + range/2; xx++) {
			for (int yy = prevMinedCoords.posY + 1; yy > (int)Math.max(1, Math.floor(prevMinedCoords.posY - 1)); yy--) {
				for (int zz = (int)Math.floor(prevMinedCoords.posZ - range/2); zz < prevMinedCoords.posZ + range/2; zz++) {
					int id = ent2.worldObj.getBlockId(xx, yy, zz);
					
					//temp wood check
					if (team.canUseIDForResource(id)) {
						return new ChunkCoordinates(xx, yy, zz);
					}
				}
			}
		}
		return null;
	}
	
	//you could make this use ticks to actively do a radius scan, for longer scans that dont kill cpu
	public ChunkCoordinates getResClose() {
		//System.out.println("searching for resource close");
		int range = 10;
		int rangeMax = 120;
		
		EntityLivingBase ent2 = ((EntityLivingBase)ent);
		
		int x = (int)Math.floor(ent2.posX);
		int z = (int)Math.floor(ent2.posZ);
		int r = range;
		
		int stepsToDo = 5;
		int scanCurDistMax = 15;
		for (int i = 0; i < stepsToDo; i++) {
			//scan facing forward + scan offset
			int scanX = x + (int)(Math.cos((ent2.rotationYaw + scanCurAngle) * 0.017453D) * scanCurDist);
			int scanZ = z + (int)(Math.sin((ent2.rotationYaw + scanCurAngle) * 0.017453D) * scanCurDist);
			
			for (int yy = (int) Math.max(1, Math.floor(ent2.posY - 2)); yy < (int)ent2.posY + 6; yy++) {
				scanCurAngle += 90/scanCurDist;
				if (scanCurAngle >= 360) {
					scanCurAngle = 0;
					scanCurDist++;
					if (scanCurDist > scanCurDistMax) {
						scanCurDist = 1;
					}
				}
				
				int id = ent2.worldObj.getBlockId(scanX, yy, scanZ);

				if (team.canUseIDForResource(id)/* && ent2.getDistance(this.coordLastReturn[0].posX, this.coordLastReturn[0].posY, this.coordLastReturn[0].posZ) < rangeMax*//* && rand.nextInt(3) == 0*/) {
					//System.out.println("it works #2!");
					scanCurAngle = 0;
					scanCurDist = 1;
					return new ChunkCoordinates(scanX, yy, scanZ);
				}
				
			}
		}
		
		return null;
	}

	public ChunkCoordinates getResRecentlyDiscovered() {
		Random rand = new Random();
		if (team.listRecentResLocations.size() > 0) return team.listRecentResLocations.get(rand.nextInt(team.listRecentResLocations.size()));
		return null;
	}
	
	//also slightly cpu heavy if the list is big enough, more caching needed
	public ChunkCoordinates getResSharedInfo() {
		//System.out.println("searching for resource iterate gatherers");
		List<EntityRtsBase> listEntitiesTemp = new ArrayList<EntityRtsBase>();
		listEntitiesTemp = team.getWorkersTakingOrders("gather", true);
		if (listEntitiesTemp.size() > 0) {
			for (int i = 0; i < listEntitiesTemp.size(); i++) {
				EntityRtsBase entRts = listEntitiesTemp.get(i);
				
				if (entRts.getAIAgent() != null) {
					if (entRts.bh.ordersHandler.activeOrders instanceof OrdersGatherRes) {
						ChunkCoordinates tryCoords = ((OrdersGatherRes)entRts.bh.ordersHandler.activeOrders).coordLastGather[0];
						
						int id = entRts.worldObj.getBlockId(tryCoords.posX, tryCoords.posY, tryCoords.posZ);
						
						//temp wood check
						if (team.canUseIDForResource(id)) {
							System.out.println("found gatherer with valid resource coord!");
							return new ChunkCoordinates(tryCoords.posX, tryCoords.posY, tryCoords.posZ);
						}
					}
				}
			}
		}
		return null;
	}
	
	public ChunkCoordinates getResFar() {
		return null;
	}
}
