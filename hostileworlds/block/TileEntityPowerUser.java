package hostileworlds.block;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityPowerUser extends TileEntity implements IEnergySink
{
    //IC2 stuff
    public int powerEU = 0;
    public int powerEUMax = 100000;
        
	private boolean _isAddedToIC2EnergyNet;
	private boolean _addToNetOnNextTick;
	
	private boolean firstTimeSync = true;
	private boolean hasLoadedNBT = false;

    public TileEntityPowerUser() {
    }
    
	@Override
	public void updateEntity()
	{
		if(_addToNetOnNextTick)
		{
			if(!worldObj.isRemote)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
			_addToNetOnNextTick = false;
			_isAddedToIC2EnergyNet = true;
		}
		
		if (!worldObj.isRemote) {
			if (hasLoadedNBT && firstTimeSync) {
				firstTimeSync = false;
				
				initReady();
			}
		}
	}
	
	public void initReady() {
		//dummy
	}
   
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
       
        powerEU = tagCompound.getInteger("powerEU");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        
        tagCompound.setInteger("powerEU", powerEU);
    }
	
	//Stuff to help IC2 or other
	
	@Override
	public void validate()
	{
		super.validate();
		if(!_isAddedToIC2EnergyNet)
		{
			_addToNetOnNextTick = true;
		}
		
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
		if(_isAddedToIC2EnergyNet)
		{
			if(!worldObj.isRemote)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
			_isAddedToIC2EnergyNet = false;
		}
	}
	
	public void removeEnergy(int amount) {
		//System.out.println("spending energy: " + amount);
		powerEU -= amount;
	}
	
	public int addEnergy(int amount) {
		int powerTaken = 0;
		int powerLeft = amount;
		if (powerEU < powerEUMax) {
			powerTaken = amount;
			powerLeft = 0;
			if (powerEU + amount > powerEUMax) {
				powerLeft = powerEU + amount - powerEUMax;
				powerTaken = amount - powerLeft;
			}
			powerEU += powerTaken;
		}
		
		return powerLeft;
	}
	
	//IC2 methods Start \\
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet() {
		return _isAddedToIC2EnergyNet;
	}

	@Override
	public int demandsEnergy() {
		return powerEUMax - powerEU;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		return addEnergy(amount);
	}

	@Override
	public int getMaxSafeInput() {
		return 256;
	}
	
	//IC2 methods end //
}
