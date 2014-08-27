package hostileworlds.dimension;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.ai.CursedAreaCoordinates;
import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.ai.invasion.WorldEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;

public class HWDimensionManager {

	public static HWDimensionManager instance; 
	public static List<Integer> registeredDimensions = new ArrayList<Integer>();
	
	public HWDimensionManager() {
		if (instance != null) System.out.println("Duplicate HWDimensionManager() creation detected");
		instance = this;
		
		/*registeredDimensions.add(6);
		registeredDimensions.add(15);
		
		writeGameNBT();
		
		readGameNBT();
		
		System.out.println(registeredDimensions.get(0));*/
	}
	
	/*public static void addDimension(int id) {
		System.out.println("Adding Dimension: " + id);
		if (!registeredDimensions.contains(id)) {
			registeredDimensions.add(id);
			registerDimension(id, true);
		} else {
			System.out.println("Dimension " + id + " already added, aborting registration. (should only happen in singleplayer)");
		}
		
		HostileWorlds.instance.writeGameNBT();
	}

	//Used on tile entity portal break
	public static void deleteDimension(int id) {
		if (registeredDimensions.contains(id)) {
			System.out.println("Deleting dimension: " + id);
			//To fix dimension cleanup crash
			//unregisterDimension(id);
			registeredDimensions.remove(new Integer(id));
		} else {
			System.out.println("Dimension " + id + " not found for removal");
		}
		
		HostileWorlds.instance.writeGameNBT();
	}
	
	public static void registerDimension(int id, boolean init) {
		DimensionManager.registerProviderType(id, HWWorldProvider.class, true);
		DimensionManager.registerDimension(id, id);
		try {
			DimensionManager.initDimension(id);
		} catch (Exception ex) {
			System.out.println("Crash trying to init dimension, might be because client is trying to?");
			ex.printStackTrace();
		}
		if (init) WorldDirector.initDimData(id);
	}*/

	public static void unregisterDimension(int id) {
		//A crash is happening for internal chunk cleanup code a while after this line of code is called
		//not unregistering might fix it, the dimension will not reload on server restart, however this could technically be a memory leak issue if too many dimensions made in active server instance
		//ok bad idea, this happened on world exit and reload without closing mc
		//java.lang.IllegalArgumentException: Failed to register dimension for id 27, One is already registered
		//at net.minecraftforge.common.DimensionManager.registerDimension(DimensionManager.java:127)
		//move this code to unregister ONLY on server shutdown, should do the cleanup proper?
		//nm, just prevent our method from being called when block is break, go 1 method up 
		DimensionManager.unregisterDimension(id);
		//DimensionManager.unloadWorld(id);
	}
	
	//server side only
	public static void loadAndRegisterDimensions() {
		ServerTickHandler.wd.resetDimData();
		HostileWorlds.instance.readGameNBT();
    	
    	/*for (int i = 0; i < registeredDimensions.size(); i++) {
    		registerDimension(registeredDimensions.get(i), false);
    	}*/
    }
    
    /*public static void unregisterDimensionsAndSave() {
    	System.out.println("unregistering all HW dimensions");
    	for (int i = 0; i < registeredDimensions.size(); i++) {
    		unregisterDimension(registeredDimensions.get(i));
    	}
    	
    	HostileWorlds.instance.writeGameNBT();
    	
    	registeredDimensions.clear();
    }*/
    
  	/*public static Packet250CustomPayload getDimensionsPacket() {
  		ByteArrayOutputStream bos = new ByteArrayOutputStream(Integer.SIZE * (1 + registeredDimensions.size()));
          DataOutputStream dos = new DataOutputStream(bos);

          try
          {
        	  dos.writeInt(registeredDimensions.size());
        	  HostileWorlds.dbg("sending dim packet register info");
        	  HostileWorlds.dbg("dim size: " + registeredDimensions.size());
              for (int i = 0; i < registeredDimensions.size(); i++) {
            	  dos.writeInt(registeredDimensions.get(i));
            	  HostileWorlds.dbg("writing: " + registeredDimensions.get(i));
              }
          }
          catch (Exception ex)
          {
              ex.printStackTrace();
          }

          Packet250CustomPayload pkt = new Packet250CustomPayload();
          pkt.channel = "Dimension";
          pkt.data = bos.toByteArray();
          pkt.length = bos.size();
          
          return pkt;
  	}*/
  	
  	public static void readNBT(NBTTagCompound data) {
    	
  		//Per world data
    	NBTTagCompound playerData = data.getCompoundTag("registeredDimensions");
    	Iterator it = playerData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound dimData = playerData.getCompoundTag(tagName);
			int dim = dimData.getInteger("dimID");
			
			readDimension(dim, dimData);
			
			//if not overworld
			/*if (dim != 0) {
				registeredDimensions.add(dim);
			}*/
		}
		
		//Player NBT, not per world --- per player..... lets save to separate files!
		//WorldDirector.playerNBT = data.getCompoundTag("registeredDimensions");
    }
	
    public static void writeNBT(NBTTagCompound data) {
    	NBTTagCompound dimList = new NBTTagCompound();
		
		/*for (int dimIndex = 0; dimIndex < HWDimensionManager.registeredDimensions.size(); dimIndex++) {
			int dimID = HWDimensionManager.registeredDimensions.get(dimIndex);

			NBTTagCompound dimData = new NBTTagCompound();
			
			writeDimension(dimID, dimData);
			
			dimList.setCompoundTag("dim_" + dimID, dimData);
		}*/
		
		//World 0
		NBTTagCompound dimData = new NBTTagCompound();			
		writeDimension(0, dimData);
		dimList.setTag("dim_0", dimData);
		
		data.setTag("registeredDimensions", dimList);
    }
    
    
    public static void readDimension(int dimID, NBTTagCompound dimData) {
    	if (ServerTickHandler.wd == null) {
    		ServerTickHandler.wd = new WorldDirectorMultiDim();
    	}
    	ServerTickHandler.wd.initDimData(dimID);
    	
    	//Invasions
    	NBTTagCompound listData = dimData.getCompoundTag("invasionData");
    	
    	Iterator it = listData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound data = listData.getCompoundTag(tagName);
			WorldEvent invasion = WorldEvent.newInvasionFromNBT(data);
			
			HostileWorlds.dbg("starting loaded invasion: " + invasion.type + " - player name: " + invasion.mainPlayerName);
			WorldDirectorMultiDim.curInvasions.get(dimID).add(invasion);
		}
		
		//Curses
    	listData = dimData.getCompoundTag("curseData");
    	it = listData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound data = listData.getCompoundTag(tagName);
			CursedAreaCoordinates curse = new CursedAreaCoordinates();
			curse.readNBT(data);
			WorldDirectorMultiDim.coordCurses.get(dimID).add(curse);
		}
    	
    	//Surface Caves
    	listData = dimData.getCompoundTag("surfaceCaves");
    	it = listData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound data = listData.getCompoundTag(tagName);
			WorldDirectorMultiDim.coordSurfaceCaves.get(dimID).add(HostileWorlds.readChunkCoords("coord", data));
		}
		
		//Caves
		listData = dimData.getCompoundTag("caves");
		it = listData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound data = listData.getCompoundTag(tagName);
			WorldDirectorMultiDim.coordCaves.get(dimID).add(HostileWorlds.readChunkCoords("coord", data));
		}
		
		//Invasion Sources
		listData = dimData.getCompoundTag("invasionSources");
		it = listData.func_150296_c().iterator();
		while (it.hasNext()) {
			String tagName = (String) it.next();
			NBTTagCompound data = listData.getCompoundTag(tagName);
			WorldDirectorMultiDim.coordInvasionSources.get(dimID).add(HostileWorlds.readChunkCoords("coord", data));
		}
		
		HostileWorlds.dbg("dim: " + dimID + " - LOADED coordInvasionSources size: " + WorldDirectorMultiDim.coordInvasionSources.get(dimID).size());
    }
    
    public static void writeDimension(int dimID, NBTTagCompound dimData) {
    	
    	//no data, bail
    	if (WorldDirectorMultiDim.curInvasions.get(dimID) == null) return;
    	
    	//Invasions
    	NBTTagCompound invasionList = new NBTTagCompound();
		for (int j = 0; j < WorldDirectorMultiDim.curInvasions.get(dimID).size(); j++) {
			WorldEvent invasion = WorldDirectorMultiDim.curInvasions.get(dimID).get(j);
			NBTTagCompound invasionData = new NBTTagCompound();
			invasion.writeNBT(invasionData);
			invasionList.setTag("invasion_" + j, invasionData);
		}
    	
		
		//Curses
		NBTTagCompound curseList = new NBTTagCompound();
		for (int j = 0; j < WorldDirectorMultiDim.coordCurses.get(dimID).size(); j++) {
			CursedAreaCoordinates curse = WorldDirectorMultiDim.coordCurses.get(dimID).get(j);
			NBTTagCompound curseData = new NBTTagCompound();
			curse.writeNBT(curseData);
			curseList.setTag("curse_" + j, curseData);
		}
		
		//Surface Caves
		NBTTagCompound surfaceCaveList = new NBTTagCompound();
		for (int j = 0; j < WorldDirectorMultiDim.coordSurfaceCaves.get(dimID).size(); j++) {
			ChunkCoordinates coords = WorldDirectorMultiDim.coordSurfaceCaves.get(dimID).get(j);
			NBTTagCompound coordData = new NBTTagCompound();
			HostileWorlds.writeChunkCoords("coord", coords, coordData);
			//curse.writeNBT(coordData);
			surfaceCaveList.setTag("surfaceCave_" + j, coordData);
		}
		
		//Caves - this might not need to be written out, its a lot of data too
		NBTTagCompound caveList = new NBTTagCompound();
		for (int j = 0; j < WorldDirectorMultiDim.coordCaves.get(dimID).size(); j++) {
			ChunkCoordinates coords = WorldDirectorMultiDim.coordCaves.get(dimID).get(j);
			NBTTagCompound coordData = new NBTTagCompound();
			HostileWorlds.writeChunkCoords("coord", coords, coordData);
			//curse.writeNBT(coordData);
			caveList.setTag("cave_" + j, coordData);
		}
		
		//Invasion Sources
		NBTTagCompound invasionSourceList = new NBTTagCompound();
		for (int j = 0; j < WorldDirectorMultiDim.coordInvasionSources.get(dimID).size(); j++) {
			ChunkCoordinates coords = WorldDirectorMultiDim.coordInvasionSources.get(dimID).get(j);
			NBTTagCompound coordData = new NBTTagCompound();
			HostileWorlds.writeChunkCoords("coord", coords, coordData);
			//curse.writeNBT(coordData);
			invasionSourceList.setTag("invasionSource_" + j, coordData);
		}
		
		dimData.setInteger("dimID", dimID);
		dimData.setTag("invasionData", invasionList);
		dimData.setTag("curseData", curseList);
		dimData.setTag("surfaceCaves", surfaceCaveList);
		dimData.setTag("caves", caveList);
		dimData.setTag("invasionSources", invasionSourceList);
    }
}
