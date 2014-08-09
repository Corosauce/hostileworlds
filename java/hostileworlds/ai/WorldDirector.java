package hostileworlds.ai;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.invasion.InvasionCaves;
import hostileworlds.ai.invasion.InvasionPortalCatacombs;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.ai.invasion.WorldEvent.EnumWorldEventType;
import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityInvader;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieMiner;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.packet.PacketHelper;
import CoroUtil.pathfinding.IPFCallback;
import CoroUtil.pathfinding.PFCallbackItem;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class WorldDirector implements IPFCallback {

	
	
	//1.7.2 BUG!! LIST DATA WAS REFILLED VIA DIMENSION MANAGER, MUST FIX! WE LOSE INVASION SOURCES NOW!
	
	
	
	
	
	
	//curses!
	
	//public World world;
	//public World lastWorld;
	
	public int detectedIDCatacombs; //for thread
	
	//Per World fields
	public static HashMap<Integer, ArrayList<WorldEvent>> curInvasions = new HashMap<Integer, ArrayList<WorldEvent>>();
	public static HashMap<Integer, ArrayList<CursedAreaCoordinates>> coordCurses = new HashMap<Integer, ArrayList<CursedAreaCoordinates>>();
	public static HashMap<Integer, ArrayList<ChunkCoordinates>> coordSurfaceCaves = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
	public static HashMap<Integer, ArrayList<ChunkCoordinates>> coordCaves = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
	public static HashMap<Integer, ArrayList<ChunkCoordinates>> coordInvasionSources = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
	//public ArrayList<ChunkCoordinates> coordCurses;
	
	//Client sycned copies for visual info
	public static HashMap<Integer, ArrayList<WorldEvent>> clientCurInvasions = new HashMap<Integer, ArrayList<WorldEvent>>();
	public static int clientPlayersCooldown;
	public static float clientPlayerInvadeValue;
	
	//Curse tracking
	public HashMap<String, ChunkCoordinates> lastCursePlayerLocations;
	
	//General
	public HashMap<String, ChunkCoordinates> lastScanPlayerLocations;
	
	//Persistant Player NBT
	public static HashMap<String, NBTTagCompound> playerNBT = new HashMap<String, NBTTagCompound>();
	
	//Threading
	public AreaScanner areaScanner;
	public boolean scanning;
	
	//Configs
	//ticks
	//public int timeBetweenInvasions = 120000; //5 days
	public int maxActiveInvasionsPerPlayer = 1;
	
	public WorldDirector() {
		
		//new HWDimensionManager();
		
		lastScanPlayerLocations = new HashMap<String, ChunkCoordinates>();
		//world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
		//areaScanner = new AreaScanner(this, world);
	}
	
	public void onTick() {
		
		if (!ModConfigFields.debugTickMain) return;
		
		WorldServer[] worlds = DimensionManager.getWorlds();
    	for (int i = 0; i < worlds.length; i++) {
    		WorldServer world = worlds[i];
    		onTickWorld(world);
    	}
    	
    	//1.7.2 BUG!! LIST DATA WAS REFILLED VIA DIMENSION MANAGER, MUST FIX! WE LOSE INVASION SOURCES NOW!
    	//SAVEDATABROKEN
    	
	}
	
	public static void resetDimData() {
		dbg("Resetting HW Data");
		curInvasions = new HashMap<Integer, ArrayList<WorldEvent>>();
		coordCurses = new HashMap<Integer, ArrayList<CursedAreaCoordinates>>();
		coordSurfaceCaves = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
		coordCaves = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
		coordInvasionSources = new HashMap<Integer, ArrayList<ChunkCoordinates>>();
	}
	
	public static void initDimData(World world) {
		initDimData(world.provider.dimensionId);
	}
	
	public static void initDimData(int dimID) {
		dbg("Initializing HW Data for dim: " + dimID);
		curInvasions.put(dimID, new ArrayList<WorldEvent>());
		coordCurses.put(dimID, new ArrayList<CursedAreaCoordinates>());
		coordSurfaceCaves.put(dimID, new ArrayList<ChunkCoordinates>());
		coordCaves.put(dimID, new ArrayList<ChunkCoordinates>());
		coordInvasionSources.put(dimID, new ArrayList<ChunkCoordinates>());
	}
	
	public static NBTTagCompound getPlayerNBT(String username) {
		if (!playerNBT.containsKey(username)) {
			tryLoadPlayerNBT(username);
		}
		return playerNBT.get(username);
	}
	
	public static void tryLoadPlayerNBT(String username) {
		//try read from hw/playerdata/player.dat
		//init with data, if fail, init default blank
		
		NBTTagCompound playerData = new NBTTagCompound();
		
		try {
			String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "HWPlayerData" + File.separator + username + ".dat";
			
			if ((new File(fileURL)).exists()) {
				playerData = CompressedStreamTools.readCompressed(new FileInputStream(fileURL));
			}
		} catch (Exception ex) {
			HostileWorlds.dbg("no saved data found for " + username);
		}
		
		playerNBT.put(username, playerData);
	}
	
	public static void writeAllPlayerNBT() {
		HostileWorlds.dbg("writing out all player nbt");
		
		String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "HWPlayerData";
		if (!new File(fileURL).exists()) new File(fileURL).mkdir();
		
		Iterator it = playerNBT.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        HostileWorlds.dbg(pairs.getKey() + " = " + pairs.getValue());
	        writePlayerNBT((String)pairs.getKey(), (NBTTagCompound)pairs.getValue());
	    }
	}
	
	public static void writePlayerNBT(String username, NBTTagCompound parData) {
		HostileWorlds.dbg("writing " + username);
		
		String fileURL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + File.separator + "HWPlayerData" + File.separator + username + ".dat";
		
		try {
			FileOutputStream fos = new FileOutputStream(fileURL);
	    	CompressedStreamTools.writeCompressed(parData, fos);
	    	fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			HostileWorlds.dbg("Error writing HW player data for " + username);
		}
	}
	
	public void onTickWorld(World world) {
		/*lastWorld = world;
		if (world != lastWorld) {
			world = lastWorld;
			/reaScanner = ;
		}*/
		
		//IM COLD, HALP COMPUTER
		//if (true) while (true) dbg("WARMTH!");
		
		int dim = world.provider.dimensionId;
		
		if (coordSurfaceCaves.get(dim) == null) {
			initDimData(world);
		}
		
		for (int i = 0; i < curInvasions.get(dim).size(); i++) {
			WorldEvent invasion = curInvasions.get(dim).get(i);
			invasion.tick();
			if (invasion.isComplete()) {
				curInvasions.get(dim).remove(i);
				dbg("removing complete invasion: " + invasion + " - user: " + invasion.mainPlayerName);
			}
		}
		
		if (world.getWorldTime() % 40 == 0) {
			for (int i = 0; i < world.playerEntities.size(); i++) {
				EntityPlayer entP = (EntityPlayer)world.playerEntities.get(i);
				
				if (entP != null && !entP.isDead) {
					HostileWorlds.eventChannel.sendTo(getInvasionDataPacketForPlayer(dim, CoroUtilEntity.getName(entP)), (EntityPlayerMP)entP);
					//((EntityPlayerMP)entP).playerNetServerHandler.sendPacketToPlayer(getInvasionDataPacketForPlayer(dim, CoroUtilEntity.getName(entP)));
				}
			}
		}
		
		
		
		//ooooooooooooollld
		/*if (coordCaves.get(dim).size() > 0) {
			for (int i = 0; i < coordCurses.get(dim).size(); i++) {
				TileEntity tEnt = world.getBlockTileEntity(coordCurses.get(dim).get(i).posX, coordCurses.get(dim).get(i).posY, coordCurses.get(dim).get(i).posZ);
				if (tEnt instanceof TileEntityAuraCurse) {
					
					//uhhhh, should players be in the area for an invasion to happen????
					
					if (!((TileEntityAuraCurse) tEnt).invasionActive && ((TileEntityAuraCurse) tEnt).invasionLastTime + timeBetweenInvasions < System.currentTimeMillis() && isSafeQuickScan(entP)) {
						//if (triggerEvent(world, coordCurses.get(dim).get(i))) ((TileEntityAuraCurse) tEnt).invasionStart();
					}
				} else {
					coordCurses.get(dim).remove(i);
					return;
				}
				
			}
		}*/
		
		
		
		if (world.provider.dimensionId == 0) {
			onTickOverworld(world);
		}
		
		/*if (world.provider.getDimensionName().equalsIgnoreCase("Catacombs")) {
			detectedIDCatacombs = world.provider.dimensionId;
			onTickCatacombs(world);
		}*/
	}
	
	public static void dbg(Object obj) {
		HostileWorlds.dbg(obj);
	}
	
	public boolean isCoordAndNearAreaNaturalBlocks(World parWorld, int x, int y, int z, int range) {
		if (isNaturalSurfaceBlock(parWorld.getBlock(x, y, z)) && 
				isNaturalSurfaceBlock(parWorld.getBlock(x+range, y, z)) && 
				isNaturalSurfaceBlock(parWorld.getBlock(x-range, y, z)) &&
				isNaturalSurfaceBlock(parWorld.getBlock(x, y, z+range)) &&
				isNaturalSurfaceBlock(parWorld.getBlock(x, y, z-range))) {
			return true;
		}
		return false;
	}
	
	public boolean isNaturalSurfaceBlock(Block id) {
		if (id == Blocks.snow || id == Blocks.grass || id == Blocks.dirt || id == Blocks.sand || id == Blocks.stone || id == Blocks.gravel || id == Blocks.tallgrass) {
			return true;
		}
		if (isLogOrLeafBlock(id)) return true;
		return false;
	}
	
	public boolean isLogOrLeafBlock(Block id) {
		//Block block = Block.blocksList[id];
		if (id == null) return false;
		if (id.getMaterial() == Material.leaves) return true;
		if (id.getMaterial() == Material.plants) return true;
		if (id.getMaterial() == Material.wood) return true;
		return false;
	}
	
	public int getInvasionCountForPlayer(EntityPlayer entP) {
		int count = 0;
		for (int i = 0; i < curInvasions.get(entP.dimension).size(); i++) {
			WorldEvent invasion = curInvasions.get(entP.dimension).get(i);
			if (invasion.mainPlayerName.equalsIgnoreCase(CoroUtilEntity.getName(entP))) {
				count++;
			}
		}
		return count;
	}
	
	//placeholder
	public boolean isCursed(EntityPlayer entP) {
		return true;
	}
	
	public ArrayList<ChunkCoordinates> getUnusedInvasionSourcesInRange(ArrayList<ChunkCoordinates> collection, EntityPlayer entP, float range) {
		return getUnusedInvasionSourcesInRange(collection, new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ), entP.dimension, range);
	}
	
	public ArrayList<ChunkCoordinates> getUnusedInvasionSourcesInRange(ArrayList<ChunkCoordinates> collection, ChunkCoordinates parCoords, int dim, float range) {
		ArrayList<ChunkCoordinates> coordList = new ArrayList<ChunkCoordinates>();
		
		for (int i = 0; i < collection.size(); i++) {
			ChunkCoordinates coords = collection.get(i);
			
			if (isValid(dim, coords)) {
				if (OldUtil.getDistanceXZ(parCoords, coords) < range && (ModConfigFields.invasionManyPerPortal || !isInUse(dim, coords))) {
					coordList.add(coords);
				}
			} else {
				collection.remove(coords);
			}
		}
		
		return coordList;
	}
	
	public boolean isInUse(int dim, ChunkCoordinates coords) {
		for (int i = 0; i < curInvasions.get(dim).size(); i++) {
			if (curInvasions.get(dim).get(i).coordSource == coords) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isValid(int dim, ChunkCoordinates coords) {
		return getSourceType(dim, coords) != null;
	}
	
	public boolean isValidSourceBlockID(Block id) {
		if (id == HostileWorlds.blockInvasionSource || id == Blocks.portal) return true;
		return false;
	}
	
	public EnumWorldEventType getSourceType(int dim, ChunkCoordinates coords) {
		Block id = DimensionManager.getWorld(dim).getBlock(coords.posX, coords.posY, coords.posZ);
		
		if (id == HostileWorlds.blockInvasionSource) {
			return EnumWorldEventType.INV_PORTAL_CATACOMBS;
		} else if (id == Blocks.portal) {
			return EnumWorldEventType.INV_PORTAL_NETHER;
		} else {
			if (coordCaves.get(dim).contains(coords)) {
				return EnumWorldEventType.INV_CAVE;
			}
		}
		return null;
	}
	
	public void onTickOverworld(World world) {
		
		if (ModConfigFields.autoSaveFrequencyInTicks > 0 && world.getWorldTime() % ModConfigFields.autoSaveFrequencyInTicks == 0) {
			HostileWorlds.instance.writeGameNBT();
		}
		
		for (int i = 0; i < world.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)world.playerEntities.get(i);
			
			if (entP != null && !entP.isDead && !ModConfigFields.noInvadeWhitelist.contains(CoroUtilEntity.getName(entP))) {
				
				NBTTagCompound playerData = getPlayerNBT(CoroUtilEntity.getName(entP));
				
				//Portal -- counter, needs proper ++ counter outside of the portal collide method
				int time = playerData.getInteger("HWPortalTime");
	        	if (time > 0) playerData.setInteger("HWPortalTime", --time);
	        	
	        	//First time checker (might trigger for deaths too)
	        	boolean initialized = playerData.getBoolean("HWFirstCooldownInitialized");
	        	
	        	//First time run 3 day cooldown
	        	if (!initialized) {
	        		playerData.setInteger("HWInvasionCooldown", ModConfigFields.coolDownFirstTime);
	        		playerData.setBoolean("HWFirstCooldownInitialized", true);
	        	}
	        	
	        	//playerData.setInteger("HWInvasionCooldown", 80);
	        	
	        	boolean canInvade = false;
	        	
	        	//Invasion cooldown
	        	if (ModConfigFields.timeBasedInvasionsInstead) {
		        	int cooldown = playerData.getInteger("HWInvasionCooldown");
		        	
		        	if (cooldown > 0) playerData.setInteger("HWInvasionCooldown", --cooldown);
		        	if (cooldown <= 0) canInvade = true; 
	        	} else {
	        		if (isInvadeable(entP)) {
	        			canInvade = true;
	        		}
	        	}
	        	//TEMP!!!!!!!!!!!!!!!!
	    		//debug
	    		//timeBetweenInvasions = 80;
	        	//cooldown = 0;
	        	
	        	int maxDistance = ModConfigFields.meteorCrashDistFromPlayerMax;
	        	int minDistance = ModConfigFields.meteorCrashDistFromPlayerMin;
	        	
				//Updates usable invasion sources as they explore the world
				if (world.getWorldTime() % 100 == 0) {
					if (ModConfigFields.debugDoAreaScans) tryAreaScan(entP);
				}
				
				//Invasion candidate checking
				if (canInvade && isCursed(entP) && getInvasionCountForPlayer(entP) < maxActiveInvasionsPerPlayer) {
					//to check if in use, make method to iterate over invasions sources
					dbg("start try invasion logic");
					Random rand = new Random();
					
					//random chance logic of invasion type
					int[] typeWeights = new int[2];
					typeWeights[0] = 70; //Caves
					typeWeights[1] = 30; //HW Portal
					//typeWeights[2] = 30; //Nether Portal, not used for now
					int totalWeight = 0;
					for (int ii = 0; ii < typeWeights.length; ii++) {
						totalWeight += typeWeights[ii];
					}
					int randomIndex = -1;
					double random = Math.random() * totalWeight;
					for (int ii = 0; ii < typeWeights.length; ii++) {
						random -= typeWeights[ii];
						if (random <= 0) {
							randomIndex = ii;
							break;
						}
					}
					
					ArrayList<ChunkCoordinates> invasionSources = new ArrayList<ChunkCoordinates>();
					
					ChunkCoordinates finalSource = null;
					boolean spawnInvasion = false;
					boolean keepTry = true;
					EnumWorldEventType invType = EnumWorldEventType.INV_CAVE;
					
					//If caves, and if enabled, and if at least 1 portal invasion source exists already
					if (ModConfigFields.invasionCaves && coordInvasionSources.get(entP.dimension).size() > 0 && randomIndex == 0) {
						if (coordCaves.get(entP.dimension).size() > 0) {
							invasionSources = getUnusedInvasionSourcesInRange(coordCaves.get(entP.dimension), entP, ModConfigFields.invasionCaveMaxDistStart);
							dbg("cave source size: " + invasionSources.size());
							if (invasionSources.size() > 0) {
								invType = EnumWorldEventType.INV_CAVE;
								keepTry = false;
							}
						}
					}
					
					//If HW portal
					if (keepTry || randomIndex == 1) {
						dbg("trying portal source");
						if (coordInvasionSources.get(entP.dimension).size() > 0) {
							invasionSources = getUnusedInvasionSourcesInRange(coordInvasionSources.get(entP.dimension), entP, maxDistance);
							if (invasionSources.size() > 0) {
								invType = EnumWorldEventType.INV_PORTAL_CATACOMBS;
								keepTry = false;
							}
						}
					}
					
					
					dbg("keeptry: " + keepTry);
					
					boolean spawnMeteor = false;
					
					//int choice = 
					if (invasionSources.size() > 0) {
						
						while (finalSource == null && invasionSources.size() > 0) {
							spawnInvasion = true;
							if (invasionSources.size() == 1) {
								finalSource = invasionSources.get(0);
							} else {
								//Randomize between each possible source
								
								int choice = rand.nextInt(invasionSources.size());
								
								finalSource = invasionSources.get(choice);
							}
							if (invType == EnumWorldEventType.INV_PORTAL_CATACOMBS) {
								TileEntity tEnt = world.getTileEntity(finalSource.posX, finalSource.posY, finalSource.posZ);
								if (tEnt == null) {
									invasionSources.remove(finalSource);
									finalSource = null;
									spawnInvasion = false;
								}
							}
						}
					}
					
					if (finalSource == null) {
						//new meteor event!
						dbg("couldnt find a source, meteorite based invasion!");
						invType = EnumWorldEventType.INV_PORTAL_CATACOMBS;
						//Get a chunk coords that:
						// -- Only has natural surface blocks in the damage area 
						// -- is within maxDistance range of player
						
						//in the actual invasion class, figure out best angle of approach for meteor, raytrace from sky to crash point until no collide or close to crash collide
						
						//quick lazy code
						
						
						spawnInvasion = true;
						spawnMeteor = true;
						
						//eventMeteorite(world, new ChunkCoordinates((int)entP.posX, 300, (int)entP.posZ), null);
						
						//DONT ADD to sources here, let the portal get made and let it register it, just make sure the player cooldown gets set here
						//coordInvasionSources.get(entP.dimension).add(e)
					}
					
					if (spawnInvasion) {
						
						
						
						//temp
						ChunkCoordinates curseCoord = new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ);
						
						//keep in mind finalSource could be null if new meteor event must happen
						WorldEvent invasion = null;
						if (invType == invType.INV_PORTAL_CATACOMBS) {
							dbg("spawn hw portal invasion!");
							invasion = new InvasionPortalCatacombs(world.provider.dimensionId, CoroUtilEntity.getName(entP), invType, finalSource, curseCoord);
							if (ModConfigFields.timeBasedInvasionsInstead) {
								playerData.setInteger("HWInvasionCooldown", ModConfigFields.coolDownBetweenInvasionsPortal);
							} else {
								decreaseInvadeRating(entP, getHarvestRatingInvadeThreshold()/2F);
							}
						} else if (invType == invType.INV_CAVE) {
							dbg("spawn cave invasion!");
							invasion = new InvasionCaves(world.provider.dimensionId, CoroUtilEntity.getName(entP), invType, finalSource, curseCoord);
							if (ModConfigFields.timeBasedInvasionsInstead) {
								playerData.setInteger("HWInvasionCooldown", ModConfigFields.coolDownBetweenInvasionsCave);
							} else {
								decreaseInvadeRating(entP, getHarvestRatingInvadeThreshold()/2F);
							}
						}
						
						boolean foundSafeSpot = false;
						
						if (spawnMeteor) {
							ChunkCoordinates dest = null;
							
							int randX = -1;
							int randZ = -1;
							
							for (int tries = 0; !foundSafeSpot && tries < 100; tries++) {
								
								randX = (int)entP.posX - (maxDistance/2) + entP.worldObj.rand.nextInt(maxDistance);
								randZ = (int)entP.posZ - (maxDistance/2) + entP.worldObj.rand.nextInt(maxDistance);
								
								//dbg("trying " + randX + " - " + randZ);
								
								//if far away enough
								if (entP.getDistance(randX, entP.posY, randZ) > minDistance) {
									int tryY = entP.worldObj.getHeightValue(randX, randZ) - 1;
									
									Block id = world.getBlock(randX, tryY, randZ);

									//dbg("first id: " + id);
									
									//if area is natural blocks
									if (isCoordAndNearAreaNaturalBlocks(world, randX, tryY, randZ, 8)) {
										while (isLogOrLeafBlock(id)) {
											tryY -= 2;
											id = world.getBlock(randX, tryY, randZ);
										}
										dbg("Found safe crash site! " + randX + " - " + tryY + " - " + randZ);
										foundSafeSpot = true;
										dest = new ChunkCoordinates(randX, tryY, randZ);
									}
								} else {
									//dbg("too close");
								}
							}
							
							if (foundSafeSpot) {
								ChunkCoordinates spawn = new ChunkCoordinates((int)entP.posX - (maxDistance/2) + entP.worldObj.rand.nextInt(maxDistance), 500, (int)entP.posZ - (maxDistance/2) + entP.worldObj.rand.nextInt(maxDistance));
								//ChunkCoordinates dest = new ChunkCoordinates(randX, , randZ);
								
								eventMeteorite(world, spawn, dest, invasion);
							} else {
								dbg("couldnt find a safe crash spot D:");
							}
						}
						
						if (!spawnMeteor || foundSafeSpot) curInvasions.get(entP.dimension).add(invasion);
					}
				}
				
				
			}
		}
		
		for (int i = 0; i < world.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)world.playerEntities.get(i);

			if (entP != null && !entP.isDead) {
					
				//Random rand = new Random();
				//int range = 128;
				
				if (world.getWorldTime() % 400 == 0) {
					//dbg("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					tryCurseUpdate(entP);
					//eventMeteorite(world, new ChunkCoordinates((int)entP.posX, 300, (int)entP.posZ), new ChunkCoordinates((int)entP.posX, world.getHeightValue((int)entP.posX, (int)entP.posZ), (int)entP.posZ));
				}
			}
		}
		
		
	}
	
	public boolean tryCurseUpdate(EntityPlayer entP) {
		
		//TEMPORARY LIST CLEAR!!!!!
		//surfaceCaves.clear();
		//scanning = false;
		//if (!scanning) {
		
		//if ((!lastScanPlayerLocations.containsKey(CoroUtilEntity.getName(entP)) || Math.sqrt(lastScanPlayerLocations.get(CoroUtilEntity.getName(entP)).getDistanceSquared((int)entP.posX, (int)entP.posY, (int)entP.posZ)) > 8)) {
			//lastScanPlayerLocations.put(CoroUtilEntity.getName(entP), new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ));
			//dbg("Starting area scan on player: " + CoroUtilEntity.getName(entP));
			
		//}
		return false;
	}
	
	public void eventMeteorite(World world, ChunkCoordinates spawn, ChunkCoordinates impact, WorldEvent parInvasion) {
		EntityMeteorite ent = new EntityMeteorite(world, impact, parInvasion);
		
		//Zombie ent = new Zombie(world);
		
		Random rand = new Random();
		
		ent.setPosition(spawn.posX, spawn.posY, spawn.posZ);
		//ent.motionX = (rand.nextFloat()*2-1) * 6F;
        //ent.motionY = rand.nextFloat()*2-1;
        //ent.motionZ = (rand.nextFloat()*2-1) * 6F;
		
		HostileWorlds.eventChannel.sendToDimension(WorldDirector.getMeteorPacket(ent, 0), ent.dimension);
        //MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(getMeteorPacket(ent, 0), ent.dimension);
        ent.worldObj.weatherEffects.add(((Entity)ent));
        
		//world.spawnEntityInWorld(ent);
	}
	
	//type: 0 = spawn, 1 = pos/motion update, 2 = death animation
	public static FMLProxyPacket getMeteorPacket(Entity ent, int type) {
		
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setString("packetCommand", "Meteor");
		nbt.setInteger("state", type);
		nbt.setInteger("entityID", ((Entity)ent).getEntityId());
		nbt.setFloat("x", (float)((Entity)ent).posX);
		nbt.setFloat("y", (float)((Entity)ent).posY);
		nbt.setFloat("z", (float)((Entity)ent).posZ);
		nbt.setFloat("vecX", (float)((Entity)ent).motionX);
		nbt.setFloat("vecY", (float)((Entity)ent).motionY);
		nbt.setFloat("vecZ", (float)((Entity)ent).motionZ);
        
        return PacketHelper.getNBTPacket(nbt, HostileWorlds.eventChannelName);
	}
	
	public static FMLProxyPacket getInvasionDataPacketForPlayer(int dim, String player) {
		
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setString("packetCommand", "InvasionData");
		
		EntityPlayer entP = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
		if (ModConfigFields.timeBasedInvasionsInstead) {
			nbt.setInteger("cooldown", getPlayerNBT(CoroUtilEntity.getName(entP)).getInteger("HWInvasionCooldown"));
		} else {
			nbt.setInteger("cooldown", -1);
		}
		
		nbt.setFloat("invadeValue", getPlayerNBT(CoroUtilEntity.getName(entP)).getFloat("harvested_Rating"));
		
		nbt.setInteger("dimID", dim);
		
		NBTTagCompound nbtListing = new NBTTagCompound();
		
		for (int j = 0; j < WorldDirector.curInvasions.get(dim).size(); j++) {
        	WorldEvent invasion = WorldDirector.curInvasions.get(dim).get(j);
			NBTTagCompound nbtEntry = new NBTTagCompound();
			invasion.writeNBT(nbtEntry);
			nbtListing.setTag("entry_" + j, nbtEntry);
		}
		
		nbt.setTag("invasionListing", nbtListing);
		
		return PacketHelper.getNBTPacket(nbt, HostileWorlds.eventChannelName);
		
		/*int bytes = 0;
		NBTTagCompound[] tags = new NBTTagCompound[WorldDirector.curInvasions.get(dim).size()];
		for (int j = 0; j < WorldDirector.curInvasions.get(dim).size(); j++) {
        	WorldEvent invasion = WorldDirector.curInvasions.get(dim).get(j);
			NBTTagCompound invasionData = new NBTTagCompound();
			invasion.writeNBT(invasionData);
			tags[j] = invasionData;
			try {
				bytes += CompressedStreamTools.compress(invasionData).length;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//CompressedStreamTools.writeCompressed(invasionData, dos);
			//writeNBTTagCompound();
    	}
		//System.out.flush();
		//HostileWorlds.dbg("eat a dick eclipse");
		
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream((Integer.SIZE * 3) + (Byte.SIZE * bytes)); //ehhhhhh? how to get size of nbt data?
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
        	EntityPlayer entP = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
        	dos.writeInt(getPlayerNBT(CoroUtilEntity.getName(entP)).getInteger("HWInvasionCooldown"));
        	dos.writeInt(dim);
        	dos.writeInt(WorldDirector.curInvasions.get(dim).size()+0);
        	for (int j = 0; j < WorldDirector.curInvasions.get(dim).size(); j++) {
	        	//WorldEvent invasion = WorldDirector.curInvasions.get(dim).get(j);
				//NBTTagCompound invasionData = new NBTTagCompound();
				//invasion.writeNBT(invasionData);
        		//HostileWorlds.dbg("writing: " + j);
				//CompressedStreamTools.writeCompressed(tags[j], dos);
				writeNBTTagCompound(tags[j], dos);
				//writeNBTTagCompound(tags[j], dos);
        	}
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "InvasionData";
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();
        
        return pkt;*/
	}
	
	public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutputStream par1DataOutputStream) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutputStream.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutputStream.writeShort((short)abyte.length);
            par1DataOutputStream.write(abyte);
        }
    }
	
	public boolean triggerHordeEvent(World world, ChunkCoordinates curseCoord, ChunkCoordinates destCoord) {
		
		int dim = world.provider.dimensionId;
		
		ChunkCoordinates bestCave = null;
		float closest = 99999F;
		
		/*for (int i = 0; i < surfaceCaves.size(); i++) {
			float dist = surfaceCaves.get(i).getDistanceSquared((int)curseCoord.posX, (int)curseCoord.posY, (int)curseCoord.posZ);
			if (dist < closest) {
				closest = dist;
				bestCave = surfaceCaves.get(i);
			}
		}*/
		
		Random rand = new Random(world.getWorldTime());
		
		int i = rand.nextInt(coordSurfaceCaves.get(dim).size());
		int tries = 0;
		
		int min = 10;
		int max = 100;
		
		//for (int i = 0; i < caves.size(); i++) {
		while (closest > max && tries++ < 200) {
			i = rand.nextInt(coordSurfaceCaves.get(dim).size());
			//i = rand.nextInt(caves.size());
			if (world.getChunkProvider().chunkExists(coordSurfaceCaves.get(dim).get(i).posX / 16, coordSurfaceCaves.get(dim).get(i).posZ / 16)) {
				float dist = (float)Math.sqrt(coordSurfaceCaves.get(dim).get(i).getDistanceSquared((int)curseCoord.posX, (int)curseCoord.posY, (int)curseCoord.posZ));
				if (dist < closest && dist > min) {
					closest = dist;
					bestCave = coordSurfaceCaves.get(dim).get(i);
				}
			}
		}
		
		if (closest <= max && closest > min && bestCave != null) {
			dbg(world.provider.dimensionId + ": Horde Spawn Event: " + bestCave.posX + ", " + bestCave.posY + ", " + bestCave.posZ);
			
			//temp override!
			//spawnGroup(curseCoord, new ChunkCoordinates(curseCoord.posX, curseCoord.posY-15, curseCoord.posZ));
			
			//spawnGroup(world, curseCoord, bestCave);
			spawnHorde(world, curseCoord, bestCave);
			
			
		} else {
			dbg("failed to find close cave, closest: " + closest + ", " + tries);
			return false;
		}
		
		return true;
		
		//ent.agent.initJobs();
		
		
		
	}
	
	public float distanceTo(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        float f = x2 - x1;
        float f1 = y2 - y1;
        float f2 = z2 - z1;
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }
	
	public void spawnHorde(World world, ChunkCoordinates attackCoord, ChunkCoordinates spawnCoord) {
		
		dbg(world.provider.dimensionId + ": Horde Spawn Event: " + spawnCoord.posX + ", " + spawnCoord.posY + ", " + spawnCoord.posZ);
		
		Zombie zombie = null;
		
		for (int i = 0; i < 10; i++) {
			zombie = new Zombie(world);
			
			zombie.setPosition(spawnCoord.posX, spawnCoord.posY + 2, spawnCoord.posZ);
			
			if (attackCoord != null) {
				((JobGroupHorde)((ICoroAI) zombie).getAIAgent().jobMan.priJob).attackCoord = attackCoord;
			} else {
				EntityPlayer entP = world.getClosestPlayerToEntity(zombie, -1);
				
				if (entP != null) {
					dbg("setting player coords");
					((JobGroupHorde)((ICoroAI) zombie).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ);
				}
			}
			
			world.spawnEntityInWorld(zombie);
		}
	}
	
	public boolean tryAreaScan(EntityPlayer entP) {
		
		//TEMPORARY LIST CLEAR!!!!!
		//surfaceCaves.clear();
		//scanning = false;
		//if (!scanning) {
		
		boolean debug = false;
		
		if (!scanning && entP.worldObj.getTotalWorldTime() % (20*60) == 0 && (debug || (!lastScanPlayerLocations.containsKey(CoroUtilEntity.getName(entP)) || Math.sqrt(lastScanPlayerLocations.get(CoroUtilEntity.getName(entP)).getDistanceSquared((int)entP.posX, (int)entP.posY, (int)entP.posZ)) > 64))) {
			//HWEventHandler.listConnectablePointsVisualDebug.clear();
			lastScanPlayerLocations.put(CoroUtilEntity.getName(entP), new ChunkCoordinates((int)entP.posX, (int)entP.posY, (int)entP.posZ));
			//dbg("Starting area scan on player: " + CoroUtilEntity.getName(entP));
			if (entP.worldObj.provider.getDimensionName().equalsIgnoreCase("catacombs")) {
				areaScanner = new AreaScanner(this, entP.worldObj);
				areaScanner.pfToPlayer = true;
				new Thread(areaScanner , "HW Area Scanner").start();
			} else {
				new Thread(areaScanner = new AreaScanner(this, entP.worldObj), "HW Area Scanner").start();
			}
			scanning = true;
		}
		return false;
	}
	
	public synchronized void areaScanCompleteCallback() {
		coordSurfaceCaves.put(areaScanner.world.provider.dimensionId, areaScanner.tempSurfaceCaves);
		coordCaves.put(areaScanner.world.provider.dimensionId, areaScanner.tempCaves);
		scanning = false;
		dbg("Hostile worlds dim " + areaScanner.world.provider.dimensionId + ": Area scan complete, surface caves: " + coordSurfaceCaves.get(areaScanner.world.provider.dimensionId).size() + ", caves: " + coordCaves.get(areaScanner.world.provider.dimensionId).size());
	}
	
	public boolean triggerEvent(World world, ChunkCoordinates curseCoord) {
		
		int dim = world.provider.dimensionId;
		
		ChunkCoordinates bestCave = null;
		float closest = 99999F;
		
		/*for (int i = 0; i < surfaceCaves.size(); i++) {
			float dist = surfaceCaves.get(i).getDistanceSquared((int)curseCoord.posX, (int)curseCoord.posY, (int)curseCoord.posZ);
			if (dist < closest) {
				closest = dist;
				bestCave = surfaceCaves.get(i);
			}
		}*/
		
		Random rand = new Random(world.getWorldTime());
		
		int i = rand.nextInt(coordCaves.get(dim).size());
		int tries = 0;
		
		//for (int i = 0; i < caves.size(); i++) {
		while (closest > 160 && tries++ < 100) {
			i = rand.nextInt(coordCaves.get(dim).size());
			//i = rand.nextInt(caves.size());
			if (world.getChunkProvider().chunkExists(coordCaves.get(dim).get(i).posX / 16, coordCaves.get(dim).get(i).posZ / 16)) {
				float dist = (float)Math.sqrt(coordCaves.get(dim).get(i).getDistanceSquared((int)curseCoord.posX, (int)curseCoord.posY, (int)curseCoord.posZ));
				if (dist < closest) {
					closest = dist;
					bestCave = coordCaves.get(dim).get(i);
				}
			}
		}
		
		if (closest <= 160 && bestCave != null) {
			dbg(world.provider.dimensionId + ": Cave Spawn Event: " + bestCave.posX + ", " + bestCave.posY + ", " + bestCave.posZ);
			
			//temp override!
			//spawnGroup(curseCoord, new ChunkCoordinates(curseCoord.posX, curseCoord.posY-15, curseCoord.posZ));
			
			spawnGroup(world, curseCoord, bestCave, null, 1);
			
			
		} else {
			dbg("failed to find close cave, closest: " + closest);
			return false;
		}
		
		return true;
		
		//ent.agent.initJobs();
		
		
		
	}
	
	public boolean spawnGroup(World world, ChunkCoordinates attackCoord, ChunkCoordinates spawnCoord, WorldEvent invasion, int spawnCount) {
		
		
		
		//dbg(difficultyFactor);
		
		//TEMP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		//if (true) return;
		
		int spawned = 0;
		int amountToSpawn = spawnCount;
		int tries = 0;
		
		String prefix = "HostileWorlds.";
		String mobToSpawnLeader = "InvaderZombieMiner";
		String mobToSpawn = "InvaderZombie";
		
		EntityLivingBase leader = null;
		
		//MCPC+ fix
		if (ModConfigFields.warpInvadersCloser) {
			/*if (world.blockExists(spawnCoord.posX, spawnCoord.posY, spawnCoord.posZ)) {
				HostileWorlds.dbg("chunk exists at spawn point!");
			} else {*/
				ChunkCoordinates newCoord = spawnCoord;
				//HostileWorlds.dbg("chunk DOES NOT exist at spawn point! moving closer");
				HostileWorlds.dbg("forcing spawn point closer");
				double var1 = attackCoord.posX - spawnCoord.posX;
		        double var3 = attackCoord.posY - spawnCoord.posY;
		        double var5 = attackCoord.posZ - spawnCoord.posZ;
		        double var7 = MathHelper.sqrt_double(var1 * var1 + var3 * var3 + var5 * var5);
		        
		        float stepSize = 16F;

		        int scanDirX = (int)(var1 / var7 * stepSize);
		        int scanDirY = (int)(var3 / var7 * stepSize);
		        int scanDirZ = (int)(var5 / var7 * stepSize);
		        
		        int tryCount = 0;
		        int maxDist = 150;
		        
		        //this was a quick code switch, loop is pointless/inefficient now really
		        while (tryCount < 200/* && !world.blockExists(newCoord.posX, newCoord.posY, newCoord.posZ)*/) {
		        	newCoord = new ChunkCoordinates(newCoord.posX + scanDirX, newCoord.posY + scanDirY, newCoord.posZ + scanDirZ);
		        	//HostileWorlds.dbg("trying: " + newCoord.posX + ", " + newCoord.posY + ", " + newCoord.posZ);
		        	if (Math.sqrt(newCoord.getDistanceSquaredToChunkCoordinates(attackCoord)) < maxDist) {
		        		break;
		        	}
		        }
		        
		        
		        
		        spawnCoord = new ChunkCoordinates(newCoord.posX, world.getHeightValue(newCoord.posX, newCoord.posZ), newCoord.posZ);
		        
		        HostileWorlds.dbg("Done, trycount: " + tryCount + ", newCoord on surface, dist: " + Math.sqrt(newCoord.getDistanceSquaredToChunkCoordinates(attackCoord)));
			//}
		}
		
		while (spawned < amountToSpawn && tries++ < 100) {
			Entity ent = EntityList.createEntityByName(prefix + (spawned < 1 ? mobToSpawnLeader : /*spawned < 4 ? "ClimberZombie" : */mobToSpawn), world);

			int range = 8;
			
            if (ent instanceof EntityLiving)
            {
            	EntityLiving ent2 = (EntityLiving)ent;
            	double var5 = (double)spawnCoord.posX + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)range;
                double var7 = (double)(spawnCoord.posY - 1 + world.rand.nextInt(6) - 3);
                double var9 = (double)spawnCoord.posZ + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)range;
                //EntityLivingBase var11 = var13 instanceof EntityLivingBase ? (EntityLivingBase)var13 : null;
                ent2.setLocationAndAngles(var5, var7, var9, ent2.worldObj.rand.nextFloat() * 360.0F, 0.0F);

                if (ent2.getCanSpawnHere())
                {
                	if (ent instanceof ICoroAI) {
                		if (((ICoroAI) ent).getAIAgent().jobMan.priJob instanceof JobGroupHorde) {
                			((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = attackCoord;
                			
                			/*TileEntity tEnt = world.getBlockTileEntity(attackCoord.posX, attackCoord.posY, attackCoord.posZ);
                			if (tEnt instanceof TileEntityAuraCurse) {
                				((TileEntityAuraCurse) tEnt).registerWithInvasion((ICoroAI)ent);
                			} else {
                				dbg("HW - this should never happen!");
                			}*/
                			
                			if (invasion != null) {
                				if (ent instanceof EntityInvader) {
                					invasion.setEntityInvasionInfo((EntityInvader) ent);
                				}
                				if (ent instanceof ZombieMiner) {
                    				
                        			invasion.registerWithInvasion((ICoroAI)ent);
                        			System.out.println("zombie miner SPAWN request ticket");
                        			((ZombieMiner)ent).requestTicket();
                        			((ZombieMiner)ent).forceChunkLoading(ent.chunkCoordX, ent.chunkCoordZ);
                            		leader = (ZombieMiner)ent;
                            		//dbg("Spawned Zombie Miner");
                            	} else {
                            		//dbg("Spawned Zombie Invader");
                            	}
                			}
                			
                			
                			
                			if (leader != null) ((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).leader = leader;
                		}
                	}
                	
                	
                	
                	world.spawnEntityInWorld(ent2);
                	spawned++;
                	tries = 0;
                }
            } else {
            	HostileWorlds.dbg("invalid entity, aborting");
            	return false;
            }
		}
		
		if (tries >= 100) {
			HostileWorlds.dbg("hit max tries, aborted");
			if (spawned > 0) {
				HostileWorlds.dbg("spawned some at least, returning true");
				return true;
			}
			return false;
		}
		
		return true;
		/*
		Random rand = new Random();
		
		int x = (int)entP.posX + rand.nextInt(32) - 16;
		int z = (int)entP.posZ + rand.nextInt(32) - 16;
		
		//EntityTestAI ent = new EntityTestAI(entP.worldObj);
		EntityZombie ent = new EntityZombie(entP.worldObj);
		
		ent.setPosition(x, entP.worldObj.getHeightValue(x, z) + 1, z);
		//ent.setPosition(coord.posX, coord.posY, coord.posZ);
		
		
		
		entP.worldObj.spawnEntityInWorld(ent);
		
		PFQueue.tryPath(ent, coord.posX, entP.worldObj.getHeightValue(coord.posX, coord.posZ), coord.posZ, 256F, 0);*/
	}
	
	
	
	
	
	
	public ArrayList<PFCallbackItem> queue = new ArrayList<PFCallbackItem>();
	public boolean waitingOnPF = false;

	@Override
	public void pfComplete(PFCallbackItem ci) {
		getQueue().add(ci);
	}

	@Override
	public void manageCallbackQueue() {
		ArrayList<PFCallbackItem> list = getQueue();
		
		try {
			for (int i = 0; i < list.size(); i++) {
				PFCallbackItem item = list.get(i);
				dbg("processing queue");
				waitingOnPF = false;
				
				float dist = distanceTo(item.pe.getFinalPathPoint().xCoord, item.pe.getFinalPathPoint().yCoord, item.pe.getFinalPathPoint().zCoord, item.pe.getPathPointFromIndex(0).xCoord, item.pe.getPathPointFromIndex(0).yCoord, item.pe.getPathPointFromIndex(0).zCoord);
				
				dbg(dist);
				
				if (dist < 7F) {
				//if (item.foundEnd) {
					dbg("found a pathable spot!");
					
					//cache the spot!!!!! - use while they're close enough, find others while using this one
					
					spawnHorde(DimensionManager.getWorld(detectedIDCatacombs), new ChunkCoordinates(item.pe.getFinalPathPoint().xCoord, item.pe.getFinalPathPoint().yCoord, item.pe.getFinalPathPoint().zCoord), new ChunkCoordinates(item.pe.getPathPointFromIndex(0).xCoord, item.pe.getPathPointFromIndex(0).yCoord, item.pe.getPathPointFromIndex(0).zCoord));
				}
				
				//if (!item.ent.isDead && c_CoroAIUtil.chunkExists(item.ent.worldObj, (int)item.ent.posX / 16, (int)item.ent.posZ / 16)) item.ent.getNavigator().setPath(item.pe, item.speed);
			}
		} catch (Exception ex) {
			dbg("Crash in HW Callback PF manager");
			ex.printStackTrace();
		}
		
		//if (list.size() > 0) System.out.println("cur list size: "  + list.size());
		
		list.clear();
	}

	@Override
	public ArrayList<PFCallbackItem> getQueue() {
		return queue;
	}
	
	public static float getBlockImportanceValue(Block block) {
		
		boolean test = false;
		if (test) {
			System.out.println("TEST INVADE IS ON!");
			return 30;
		}
		
		float scaleBase = 1F;
		float defaultIron = scaleBase * 0.3F;
		
		if (block instanceof BlockLog) {
			return scaleBase * 0.1F;
		} else if (block instanceof BlockSapling) {
			return scaleBase * 0.3F;
		} else if (block instanceof BlockOre) {
			if (block == Blocks.coal_ore) {
				return scaleBase * 0.2F;
			} else if (block == Blocks.iron_ore) {
				return defaultIron;
			} else if (block == Blocks.gold_ore) {
				return scaleBase * 0.4F;
			} else if (block == Blocks.lit_redstone_ore || block == Blocks.redstone_ore) {
				return scaleBase * 0.5F;
			} else if (block == Blocks.lapis_ore) {
				return scaleBase * 0.6F;
			} else if (block == Blocks.diamond_ore) {
				return scaleBase * 1F;
			} else if (block == Blocks.emerald_ore) {
				return scaleBase * 1.2F;
			} else {
				return defaultIron;
			}
		} else if (OreDictionary.getOres(Block.blockRegistry.getNameForObject(block)).size() > 0) {
			return defaultIron;
		} else {
			return 0;
		}
	}
	
	public static void handleHarvest(HarvestDropsEvent event) {
		if (event.harvester != null) {
			if (event.world.playerEntities.contains(event.harvester)) {
				
				NBTTagCompound nbt = WorldDirector.getPlayerNBT(CoroUtilEntity.getName(event.harvester));
				if (event.block instanceof BlockOre) {
					int curVal = nbt.getInteger("harvested_Ore");
					curVal++;
					nbt.setInteger("harvested_Ore", curVal);
					//System.out.println("increment!");
				} else if (event.block instanceof BlockLog) {
					int curVal = nbt.getInteger("harvested_Log");
					curVal++;
					nbt.setInteger("harvested_Log", curVal);
				}
				
				/*float curVal = nbt.getFloat("harvested_Rating");
				curVal += getBlockImportanceValue(event.block);
				nbt.setFloat("harvested_Rating", curVal);*/
				increaseInvadeRating(event.harvester, getBlockImportanceValue(event.block));
				
				//System.out.println("harvested block for " + event.harvester.username + " - " + event.block);
			}
		}
	}
	
	public static void increaseInvadeRating(EntityPlayer parPlayer, float parVal) {
		NBTTagCompound nbt = WorldDirector.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat("harvested_Rating");
		curVal += parVal;
		nbt.setFloat("harvested_Rating", curVal);
		
		System.out.println("curVal: " + curVal);
	}
	
	public static void decreaseInvadeRating(EntityPlayer parPlayer, float parVal) {
		NBTTagCompound nbt = WorldDirector.getPlayerNBT(CoroUtilEntity.getName(parPlayer));
		float curVal = nbt.getFloat("harvested_Rating");
		curVal -= parVal;
		nbt.setFloat("harvested_Rating", curVal);
	}
	
	public static float getHarvestRatingInvadeThreshold() {
		return 30F;
	}
	
	public static boolean isInvadeable(EntityPlayer parPlayer) {
		return WorldDirector.getPlayerNBT(CoroUtilEntity.getName(parPlayer)).getFloat("harvested_Rating") >= getHarvestRatingInvadeThreshold();
	}
}
