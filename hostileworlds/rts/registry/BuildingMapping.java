package hostileworlds.rts.registry;

import hostileworlds.rts.RtsEngine;
import hostileworlds.rts.building.BuildingBase;
import hostileworlds.rts.building.BuildingCommand;
import hostileworlds.rts.building.BuildingHouse;

import java.util.HashMap;

public class BuildingMapping {

	public static String packagePrefix = "hostileworlds.rts.building."; // not needed since using a mapping?
	public static HashMap<String, Class> lookupNameToBuilding = new HashMap<String, Class>();
	//public static HashMap<BuildingBase, String> lookupBuildingToName = new HashMap<BuildingBase, String>();
	
	public static void initData() {
		//lookupBuildingToName.clear();
		lookupNameToBuilding.clear();
		
		addMapping("command", BuildingCommand.class);
		addMapping("house", BuildingHouse.class);
		
		//lookupNameToBuilding.get("command").
	}
	
	public static void addMapping(String name, Class building) {
		lookupNameToBuilding.put(name, building);
	}
	
	public static BuildingBase newBuilding(String name) {
		BuildingBase bb = null;
		try {
			bb = (BuildingBase)lookupNameToBuilding.get(name).getConstructor(new Class[] {}).newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (bb != null) {
			bb.name = name; //to make building name match the mapping name
			return bb;
		} else {
			RtsEngine.dbg("critical error creating new building instance");
		}
		return null;
	}
}
