package hostileworlds.rts;

import hostileworlds.rts.ai.BehaviorTreeBuild;
import hostileworlds.rts.ai.orders.OrdersGatherRes;
import hostileworlds.rts.building.BuildingBase;
import hostileworlds.rts.entity.EntityRtsBase;
import hostileworlds.rts.registry.UnitMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.bt.OrdersData;
import CoroAI.diplomacy.TeamTypes;
import CoroAI.util.CoroUtilFile;

/* Object that manages the team/town AI */
public class TeamObject {

	//somehow track active build orders dispatched
	//ie:
	//- needs to build house at x coords, finds closest idle/gathering worker
	//- creates orders object, adds it to list, build "house" at x coords
	//- gives orders to unit
	//- tech tree should know that order is in progress (look for house build order that has last return state of inprogress)
	//- if the build order is SUCCESS, then it removes fact that its awaiting build completion, and rechecks data, shows population max is higher due to +1 house, logic continues
	//- or if tech tree oriented building, the tech tree requirements recalc gets run (automatically on building registration to team) and then logic should see it can do next
	
	// --- Serialized data ---
	public int teamID = -1;
	public int dimID;
	public Resources resources;
	public ChunkCoordinates spawn;
	public int townSizeRadius;

	// --- Runtime data ---
	
	//management - for persistance, lets have entities and buildings remember their team number, and to automatically try to reregister themselves
	public List<EntityRtsBase> listEntities = new ArrayList<EntityRtsBase>();
	public List<BuildingBase> listBuildings = new ArrayList<BuildingBase>();
	public List<EntityRtsBase> listWorkers = new ArrayList<EntityRtsBase>();

	public List<ChunkCoordinates> listRecentResLocations = new ArrayList<ChunkCoordinates>();
	
	//Economy AI
	public BehaviorTreeBuild treeAIEconomy;
	//public List<OrdersData> listOrdersMonitored = new ArrayList<OrdersData>();
	
	//temp placed here for now
	//considerations:
	//resource count (wood)
	//resource rate of increase/decline
	//supply count
	// ....... see notes on paper
	
	//scanning stuff
	public int scanCurDist = 1;
	public int scanCurAngle = 0;
	public int scanCurY = 0;

	//flow
	public int res_RateSampleTime = 20*60;
	public int res_RateLastRes = 0;
	
	
	public TeamObject(int parTeam, int parDim, ChunkCoordinates parCoords) {
		teamID = parTeam;
		spawn = parCoords;
		dimID = parDim;
		
		resources = new Resources();
		townSizeRadius = 30;
		
		//reference updating?
		/*res_Wood[0] = resources.resWood;
		res_Stone[0] = resources.resStone;
		res_Coal[0] = resources.resCoal;
		res_Iron[0] = resources.resIron;*/
		
		
	}
	
	public void initAITree() {
		treeAIEconomy = new BehaviorTreeBuild(this);
		res_RateLastRes = resources.resWood[0];
	}
	
	public void cleanup() {
		listBuildings.clear();
		listEntities.clear();
		listWorkers.clear();
	}
	
	public void registerEntity(String unitType, EntityRtsBase ent) {
		listEntities.add(ent);
		if (unitType.equals("worker")) listWorkers.add(ent);
		ent.agent.dipl_info = TeamTypes.getType("hwrts_team" + teamID);
	}
	
	public void registerBuilding(BuildingBase bb) {
		listBuildings.add(bb);
	}
	
	public EntityRtsBase spawnUnit(World world, String unitName, ChunkCoordinates coords) {
		return spawnUnit(world, unitName, coords, null);
	}
	
	public EntityRtsBase spawnUnit(World world, String unitName, ChunkCoordinates coords, OrdersData orders) {
		
		EntityRtsBase unit = UnitMapping.newUnit(world, unitName);
		
		if (unit != null) {
			unit.setPosition(coords.posX, coords.posY+2, coords.posZ);
			world.spawnEntityInWorld(unit);
			unit.teamID = teamID;
			unit.unitType = unitName;
			registerEntity(unitName, unit);
			unit.getAIAgent().spawnedOrNBTReloadedInit();
			if (orders != null) {
				unit.bh.ordersHandler.activeOrders = orders;
				unit.bh.ordersHandler.activeOrders.ent = unit; //passing ent reference to orders, since they can't pass the entity reference in constructor outside of this method
			}
		} else {
			System.out.println("CRITICAL ERROR, unit " + unitName + " not found for spawning");
		}
		return unit;
	}
	
	//cpu heavy, use with care
	public List<EntityRtsBase> getWorkersTakingOrders(String ordersName, boolean onlyActiveOrders) {
		//System.out.println("using getEntitiesTakingOrders()");
		List<EntityRtsBase> listEntitiesTemp = new ArrayList<EntityRtsBase>();
		for (int i = 0; i < listWorkers.size(); i++) {
			EntityRtsBase entInt = listWorkers.get(i);
			
			if (entInt.getAIAgent() != null) {
				if (entInt instanceof EntityRtsBase) {
					EntityRtsBase entRts = (EntityRtsBase)entInt;
					if (entRts.bh.ordersHandler.ordersAcceptable.contains(ordersName)) {
						if (onlyActiveOrders && entRts.bh.ordersHandler.activeOrders != null) {
							listEntitiesTemp.add(entInt);
						} else if (!onlyActiveOrders && entRts.bh.ordersHandler.activeOrders == null) {
							listEntitiesTemp.add(entInt);
						}
					}
				}
			} else {
				//temp cleanup on demand
				listEntities.remove(entInt);
				listWorkers.remove(entInt);
			}
		}
		return listEntitiesTemp;
	}
	
	public void tickUpdate() {
		
		//FYI some sort of cleanup on the lists is required, until proper callbacks are in to remove as they die etc, perhaps once in a while iterate the lists and remove deads
		//iterate listEntities, remove from both lists if dead
		
		World world = DimensionManager.getWorld(dimID);
		//resources.resWood[0] = 200;
		if (world != null) {
			//get resource rate
			if (world.getTotalWorldTime() % res_RateSampleTime == 0) {
				//change to check all resources later? or make more relevant to a per resource type thing......
				/*res_Rate[0] = res_Wood[0]*/resources.resWoodRate[0] = resources.resWood[0] - res_RateLastRes;
				res_RateLastRes = resources.resWood[0];
				
				
				
				//temp simulation of gatherers
				//res_Rate[0] = listGatherers.size() * 30;
				
				//temp simulation of gathering results
				//res_Wood[0] += res_Rate[0];
			}
			
			if (world.getWorldTime() % (20*10) == 0) {
				System.out.println("team: " + this.teamID + ", resWood: " + resources.resWood[0] + ", rate: " + resources.resWoodRate[0] + " / 60s");
			}
			
			//quick and easy short term recent resource location management
			if (world.getWorldTime() % 80 == 0) {
				listRecentResLocations.clear();
			}
			
			if (world.getWorldTime() % 600 == 0) {
				for (int i = 0; i < listEntities.size(); i++) {
					EntityRtsBase ent = listEntities.get(i);
					if (ent.isDead) {
						listEntities.remove(i);
						listWorkers.remove(i);
						i--;
					} else {
						//this was to fix return coords becoming null, but it was no move ticks nuller outer running on return path behavior
						//so this is unneeded
						if (ent.bh.ordersHandler.activeOrders instanceof OrdersGatherRes && ((OrdersGatherRes)ent.bh.ordersHandler.activeOrders).coordLastReturn[0] == null) {
							System.out.println("aweaweaweawe");
							//((OrdersGatherRes)ent.bh.ordersHandler.activeOrders).coordLastReturn[0] = spawn;
						}
					}
				}
			}
		}
		
		//temp force them to keep building more workers
		//res_Rate[0] = 0;
		//res_Wood[0] = 0;
		
		/*if (DimensionManager.getWorld(dimID).getWorldTime() % 10 == 0) */treeAIEconomy.tick();
	}
	
	public void writeToNBT(NBTTagCompound var1)
    {
		var1.setInteger("teamID", teamID);
		var1.setInteger("dimID", dimID);
		CoroUtilFile.writeCoords("spawn", spawn, var1);
		var1.setInteger("resWood", resources.resWood[0]);
		var1.setInteger("townSizeRadius", townSizeRadius);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
    	teamID = var1.getInteger("teamID");
    	dimID = var1.getInteger("dimID");
    	spawn = CoroUtilFile.readCoords("spawn", var1);
    	resources.resWood[0] = var1.getInteger("resWood");
    	townSizeRadius = var1.getInteger("townSizeRadius");
    	
    	System.out.println("loaded rts teamid: " + teamID);
    }
	
	//this is pretty much a placeholder method, ids are kinda tedious and also unreliable once mod blocks are involved
	//like schematics future plan, this should make use of the block ids unlocalizedName
	//so this mod will eventually have a list of resources gatherable via unlocalizedname lookup, what kind of resource its classified as for our system, and possibly the efficiency factor for it
	//ie: hardened stone that gives 50-100 stone blocks before breaking, a way to mark this in our system if needed
	//depends on if the block itself manages to give off multiple blocks before breaking, or if our system does that logic, if its our system we need the above tagging usage
	public boolean canUseIDForResource(int id) {
		
		//temp lol
		if (id != 0 && (id == Block.wood.blockID || id == Block.tallGrass.blockID)) return true;
		
		/*if (id != 0 && Block.blocksList[id].blockMaterial == Material.wood && !(Block.blocksList[id] instanceof BlockChest)) {
			return true;
		} else {
			return false;
		}*/
		
		return false;
	}
    
    public ChunkCoordinates getResBaseScan() {
		
		World world = DimensionManager.getWorld(dimID);
		
		if (world != null) {
			
			int x = spawn.posX;
			int z = spawn.posZ;
			
			int stepsToDo = 15;
			int scanCurDistMax = 120;
			int scanCurYMax = 60;
			
			for (int i = 0; i < stepsToDo; i++) {
				//scan facing forward + scan offset
				int scanX = x + (int)(Math.cos((scanCurAngle) * 0.017453D) * scanCurDist);
				int scanZ = z + (int)(Math.sin((scanCurAngle) * 0.017453D) * scanCurDist);
				int scanY = spawn.posY + scanCurY - (scanCurYMax/2);
				
				//for (int yy = (int) Math.max(1, Math.floor(spawn.posY - 2)); yy < (int)spawn.posY + 6; yy++) {
				scanCurY++;
				
				
				if (scanCurY > scanCurYMax) {
					scanCurY = 0;
					
					scanCurAngle += 90/scanCurDist;
					
					if (scanCurAngle >= 360) {
						scanCurAngle = 0;
						scanCurDist++;
						System.out.println(teamID + ", scanCurDist: " + scanCurDist + ", scanCurAngle: " + scanCurAngle + ", scanY: " + scanY);
						if (scanCurDist > scanCurDistMax) {
							System.out.println("BASE SCAN COMPLETED FULL ROTATION, OUT OF RESOURCES?!");
							scanCurDist = 1;
						}
					}
				}
				
				int id = world.getBlockId(scanX, scanY, scanZ);
				
				//System.out.println("scanning: " + scanX + ", " + scanY + ", " + scanZ);

				if (canUseIDForResource(id)) {
					//System.out.println("it works #3!");
					//scanCurAngle = 0;
					//scanCurDist = 1;
					//scanCurY
					return new ChunkCoordinates(scanX, scanY, scanZ);
				}
					
				//}
			}
		}
		
		return null;
	}
    
    public ChunkCoordinates getValidBuildingSpot(String parBuildingName) {
    	Random rand = new Random();
    	World world = DimensionManager.getWorld(dimID);
    	int maxTries = 50;
    	int distBetweenBuildings = 10;
    	for (int i = 0; i < maxTries; i++) {
    		int tryRange = townSizeRadius * 2;
    		int tryX = spawn.posX - (tryRange/2) + rand.nextInt(tryRange);
    		int tryZ = spawn.posZ - (tryRange/2) + rand.nextInt(tryRange);
    		int tryY = world.getHeightValue(tryX, tryZ) - 0;
    		
    		if (Math.abs(tryY - spawn.posY) > 4) continue;
    		if (Math.sqrt(spawn.getDistanceSquared(tryX, spawn.posY, tryZ)) > townSizeRadius || Math.sqrt(spawn.getDistanceSquared(tryX, spawn.posY, tryZ)) < distBetweenBuildings) continue; //makes circle of dist limit
    		
    		int id = world.getBlockId(tryX, tryY-1, tryZ);
    		
    		if (id != 0 && Block.blocksList[id].blockMaterial == Material.water) continue;
    		
    		boolean fail = false;
    		for (int j = 0; j < listBuildings.size(); j++) {
    			BuildingBase building = listBuildings.get(j);
    			int xx = tryX - building.tEnt.xCoord;
    	        int yy = 0; // no y compare
    	        int zz = tryZ - building.tEnt.zCoord;
    	        double dist = Math.sqrt(xx * xx + yy * yy + zz * zz);
    	        
    	        if (dist < distBetweenBuildings) {
    	        	fail = true;
    	        	break;
    	        }
    		}
    		
    		if (fail) continue;
    		
    		//success!
    		return new ChunkCoordinates(tryX, tryY, tryZ);
    		
    		//int id = world.getBlockId(tryX, tryY, tryZ);
    		
    		
    		
    		//needs:
    		//if get height value is within 4 y of spawn
    		//if is within radius
    		//if is far enough from other registered buildings
    	}
    	return null;
    }
}
