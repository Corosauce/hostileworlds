package hostileworlds.ai;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.invasion.WorldEvent.EnumWorldEventType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class CursedAreaCoordinates extends ChunkCoordinates {
	
	public String username;
	public int dimensionId;
	public int curMeasureCount;
	
	public CursedAreaCoordinates() {
		
	}
	
	public CursedAreaCoordinates(int par1, int par2, int par3, int parDim, String parUser, int parMeasurements)
    {
        super(par1, par2, par3);
        this.dimensionId = parDim;
        this.username = parUser;
        this.curMeasureCount = parMeasurements;
    }
	
	public void writeNBT(NBTTagCompound data) {
    	data.setInteger("dimID", dimensionId);
    	data.setString("username", username);
    	data.setInteger("curMeasureCount", curMeasureCount);
    	HostileWorlds.writeChunkCoords("coord", this, data);
    }
	
	public void readNBT(NBTTagCompound data) {
		dimensionId = data.getInteger("dimID");
		username = data.getString("username");
		curMeasureCount = data.getInteger("curMeasureCount");
		HostileWorlds.readChunkCoords("coord", data);
    }

}
