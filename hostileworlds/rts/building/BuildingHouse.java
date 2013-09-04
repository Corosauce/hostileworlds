package hostileworlds.rts.building;

import hostileworlds.rts.RtsEngine;
import hostileworlds.rts.block.TileEntityRTSBuilding;

public class BuildingHouse extends BuildingBase {
	public BuildingHouse() {
		super();
	}
	
	@Override
	public void init(TileEntityRTSBuilding parTEnt) {
		super.init(parTEnt);
		
		//main command building, assuming we only have 1 of these per entire team:
		//look for team instance, if null, make it, else, try to join it, for non command blocks it would have its team var set via what made it and it would try to join the team
		
		dbg("trying to register with team: " + this.teamID);
		if (!RtsEngine.teams.registerWithTeam(this.teamID, this.name, this)) {
			//make new team
			dbg("failed to find team: " + this.teamID + ", panic!");
		}
		
		if (!isBuilt) {
			isBuilt = true;
			buildSchematic();
			//build
			
		}
		
	}
	
	@Override
	public void tickUpdate() {
		// TODO Auto-generated method stub
		super.tickUpdate();
	}
}
