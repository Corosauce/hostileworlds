package hostileworlds.rts.building;

import hostileworlds.rts.block.TileEntityRTSBuilding;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import CoroAI.util.CoroUtilFile;
import build.BuildServerTicks;
import build.world.BuildJob;

public class BuildingBase {

	//should be able to implement the orders taking interface
	
	public String name = ""; //this is automatically set when created via BuildingMapping, including when tile entity recreates it from nbt
	public int teamID = -1;
	public TileEntityRTSBuilding tEnt = null;
	
	public boolean isBuilt = false;
	
	public BuildingBase() {
		
	}
	
	public void dbg(Object obj) {
		System.out.println("RTSDBG " + name + ": " + obj);
	}
	
	public void init(TileEntityRTSBuilding parTEnt) {
		tEnt = parTEnt;
	}
	
	public void tickUpdate() {
		
	}
	
	public void writeToNBT(NBTTagCompound var1)
    {
        var1.setInteger("teamID", teamID);
        var1.setBoolean("isBuilt", isBuilt);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        if (var1.hasKey("teamID")) teamID = var1.getInteger("teamID");
        isBuilt = var1.getBoolean("isBuilt");
    }
    
    public void buildSchematic() {
    	int yOffset = -2;
    	BuildJob bj = new BuildJob(-99, tEnt.xCoord, tEnt.yCoord + yOffset, tEnt.zCoord, CoroUtilFile.getSaveFolderPath() + "RTSSchematics" + File.separator + name);
		bj.build.dim = tEnt.worldObj.provider.dimensionId;
		bj.useFirstPass = false; //skip air setting pass
		bj.useRotationBuild = true;
		bj.build_rate = 2;
		bj.setDirection(1);
		
		BuildServerTicks.buildMan.addBuild(bj);
    }
	
}
