package hostileworlds.rts;

import hostileworlds.rts.building.BuildingBase;
import hostileworlds.rts.entity.EntityRtsBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.util.CoroUtilFile;

public class Teams {

	public List<TeamObject> teamsListing = new ArrayList<TeamObject>();
	public HashMap<Integer, TeamObject> teamsLookup = new HashMap<Integer, TeamObject>();
	
	public Teams() {
		
	}
	
	//definately not nbt used method
	public TeamObject teamNew(int dimID, ChunkCoordinates coords) {
		TeamObject to = teamNew(getUnusedTeamID(), dimID, coords);
		to.initAITree();
		return to;
	}
	
	public TeamObject teamNew(int parTeam, int parDim, ChunkCoordinates coords) {
		TeamObject td = new TeamObject(parTeam, parDim, coords);
		teamsListing.add(td);
		teamsLookup.put(parTeam, td);
		return td;
	}
	
	public void teamRemoveAll() {
		for (int i = 0; i < teamsListing.size(); i++) {
			TeamObject td = teamsListing.get(i);
			System.out.println("RTSDBG: removing team: " + td.teamID);
			td.cleanup();
		}
		teamsListing.clear();
		teamsLookup.clear();
	}
	
	public void teamRemove(int parTeam) {
		for (int i = 0; i < teamsListing.size(); i++) {
			TeamObject td = teamsListing.get(i);
			if (td.teamID == parTeam) {
				System.out.println("RTSDBG: removing team: " + td.teamID);
				teamsListing.remove(i);
				teamsLookup.remove(td.teamID);
				td.cleanup();
				break;
			}
		}
	}
	
	public boolean registerWithTeam(int parTeam, String unitType, EntityRtsBase ent) {
		if (teamsLookup.containsKey(parTeam)) {
			teamsLookup.get(parTeam).registerEntity(unitType, ent);
			return true;
		}
		return false;
	}
	
	public boolean registerWithTeam(int parTeam, String buildingType, BuildingBase building) {
		if (teamsLookup.containsKey(parTeam)) {
			teamsLookup.get(parTeam).registerBuilding(building);
			return true;
		}
		return false;
	}
	
	public int getUnusedTeamID() {
		int tryID = 0;
		boolean foundFree = false;
		while (!foundFree) {
			boolean idConflict = false;
			for (int i = 0; i < teamsListing.size(); i++) {
				TeamObject td = teamsListing.get(i);
				if (td.teamID == tryID) {
					idConflict = true;
					break;
				}
			}
			
			if (idConflict) {
				tryID++;
			} else {
				RtsEngine.dbg("RTSDBG: Found unused team ID: " + tryID);
				foundFree = true;
				break;
			}
		}
		
		return tryID;
	}
	
	//what? what is this used for exactly, not entities...
	public void teamJoin(int parTeam) {
		
	}
	
	public void tickUpdate() {
		for (int i = 0; i < teamsListing.size(); i++) {
			TeamObject td = teamsListing.get(i);
			td.tickUpdate();
		}
	}
	
	public void writeToFile() {
		NBTTagCompound rtsNBT = new NBTTagCompound();
		NBTTagCompound teamDataList = new NBTTagCompound();
		for (int i = 0; i < teamsListing.size(); i++) {
			TeamObject td = teamsListing.get(i);
			NBTTagCompound teamNBT = new NBTTagCompound();
			td.writeToNBT(teamNBT);
			teamDataList.setCompoundTag("team_" + td.teamID, teamNBT);
		}
		rtsNBT.setCompoundTag("teamData", teamDataList);
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();//temp till proper init with world before ent/tile init happens is found + CoroUtilFile.getWorldFolderName();
		
		try {
			//Write out to file
			FileOutputStream fos = new FileOutputStream(saveFolder + "RTSData.dat");
	    	CompressedStreamTools.writeCompressed(rtsNBT, fos);
	    	fos.close();
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void readFromFile() {
		
		NBTTagCompound rtsNBT = new NBTTagCompound();
		
		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();//temp till proper init with world before ent/tile init happens is found + CoroUtilFile.getWorldFolderName();
		
		try {
			if ((new File(saveFolder + "RTSData.dat")).exists()) {
				rtsNBT = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "RTSData.dat"));
			}
		} catch (Exception ex) { ex.printStackTrace(); }
		
		NBTTagCompound teamDataList = rtsNBT.getCompoundTag("teamData");
		Collection teamDataListCl = teamDataList.getTags();
		Iterator it = teamDataListCl.iterator();
		
		while (it.hasNext()) {
			NBTTagCompound teamData = (NBTTagCompound)it.next();
			
			TeamObject to = new TeamObject(-1, -1, null);
			to.readFromNBT(teamData);
			to.initAITree();
			teamsListing.add(to);
			teamsLookup.put(to.teamID, to);
		}
	}
	
}
