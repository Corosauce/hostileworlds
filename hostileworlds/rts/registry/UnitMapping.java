package hostileworlds.rts.registry;

import hostileworlds.rts.RtsEngine;
import hostileworlds.rts.entity.EntityRtsBase;
import hostileworlds.rts.entity.EntityRtsWorker;

import java.util.HashMap;

import net.minecraft.world.World;

public class UnitMapping {

	public static String packagePrefix = "hostileworlds.rts.entity.";
	public static HashMap<String, Class> lookupNameToUnit = new HashMap<String, Class>();
	//public static HashMap<BuildingBase, String> lookupBuildingToName = new HashMap<BuildingBase, String>();
	
	public static void initData() {
		//lookupBuildingToName.clear();
		lookupNameToUnit.clear();
		
		addMapping("worker", EntityRtsWorker.class);
		
		//lookupNameToBuilding.get("command").
	}
	
	public static void addMapping(String name, Class building) {
		lookupNameToUnit.put(name, building);
	}
	
	public static EntityRtsBase newUnit(World world, String name) {
		EntityRtsBase ub = null;
		try {
			ub = (EntityRtsBase)lookupNameToUnit.get(name).getConstructor(new Class[] {World.class}).newInstance(new Object[] {world});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (ub != null) {
			return ub;
		} else {
			RtsEngine.dbg("critical error creating new unit instance");
		}
		return null;
	}
}
