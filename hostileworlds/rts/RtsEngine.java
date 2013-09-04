package hostileworlds.rts;

import hostileworlds.rts.registry.BuildingMapping;
import hostileworlds.rts.registry.UnitMapping;
import CoroAI.diplomacy.TeamTypes;

public class RtsEngine {

	public static Teams teams;
	
	public RtsEngine() {
		BuildingMapping.initData();
		UnitMapping.initData();
		
		//diplo additions, teams 0-9, FFA mode, hopefully setting self to enemy wont have issues....
		for (int i = 0; i < 10; i++) {
			String[] enemiesArr = new String[10];
			for (int j = 0; j < 10; j++) {
				enemiesArr[j] = i != j ? "hwrts_team" + j : ""; //anti self team set
			}
			TeamTypes.addType("hwrts_team" + i, enemiesArr, new String[] { "" }, new String[]{""});
		}
	}
	
	public void tickUpdate() {
    	if (teams == null) teams = new Teams();
    	teams.tickUpdate();
	}
	
	public void writeToFile(boolean andClear) {
		teams.writeToFile();
		if (andClear) {
			teams.teamRemoveAll();
		}
	}
	
	public void readFromFile() {
		if (teams == null) teams = new Teams();
		teams.readFromFile();
	}

	public static void dbg(Object obj) {
		System.out.println(obj);
	}
}
