package hostileworlds.rts.block;

import hostileworlds.rts.building.BuildingBase;
import hostileworlds.rts.registry.BuildingMapping;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRTSBuilding extends TileEntity
{
	
	public BuildingBase building;
	public NBTTagCompound nbtBuilding;
	
	public boolean firstTimeSync = true;
	public boolean hasLoadedNBT = false;
	public boolean waitingOnExternalInit = true; //this is to let mc do its thing to auto setup the tile entity, so then my outside code can initialize the building info for it, and then mark this false

	//important nbt
	public String buildingNameType = "command";
	
	public TileEntityRTSBuilding(String parName) {
		buildingNameType = parName;
	}
	
    public TileEntityRTSBuilding() {
    	
    }
    
    public void updateEntity()
    {
    	if (!worldObj.isRemote) {
    		if (hasLoadedNBT) {
				if (firstTimeSync && !waitingOnExternalInit) {
					firstTimeSync = false;
					
					initReady();
				}
				
				if (building != null) building.tickUpdate();
    		}
    	}
    }
    
    @Override
	public void validate()
	{
		super.validate();
		hasLoadedNBT = true;
	}
	
	@Override
	public void invalidate()
	{
		cleanup();
		super.invalidate();
	}
	
	public void onBlockBroken()
	{
		cleanup();
	}
	
	public void cleanup() {
		
	}
	
	public void setBuildingAndMarkInitReady(String parName, int parTeam) {
		setBuildingAndMarkInitReady(parName);
		nbtBuilding = new NBTTagCompound();
		nbtBuilding.setInteger("teamID", parTeam);
	}
	
	public void setBuildingAndMarkInitReady(String parName) {
		buildingNameType = parName;
		waitingOnExternalInit = false;
	}
    
    public void initReady() {
    	building = BuildingMapping.newBuilding(buildingNameType);
    	if (nbtBuilding != null) building.readFromNBT(nbtBuilding);
    	building.init(this);
    }

    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
        
        var1.setString("buildingNameType", buildingNameType);
        
        NBTTagCompound tag = new NBTTagCompound();
        if (building != null) {
	        building.writeToNBT(tag);
        }
        var1.setCompoundTag("buildingData", tag);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        buildingNameType = var1.getString("buildingNameType");
        nbtBuilding = var1.getCompoundTag("buildingData");
        
        waitingOnExternalInit = false;
    }
}
