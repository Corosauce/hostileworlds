package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityItemTurretTop;
import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import CoroAI.ITilePacket;
import CoroAI.componentAI.AIFakePlayer;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.IInvOutsourced;
import CoroAI.componentAI.jobSystem.JobHuntTurret;
import CoroAI.tile.TileHandler;

public class TileEntityItemTurret extends TileEntity implements IInventory, IEnergySink, IInvOutsourced, ITilePacket
{
    
	//!!!!!!!!
	//Use an existing implementable living ICoroAI inventory using entity
	//It is a part of the tile, the top piece, it can be targetted and destroyed if it cant defend
	//Bottom tile entity piece will remain, look destroyed, needs repair, preserves items but they get damaged a bit?
	//Special inventory link handling, so that entity can die and be remade without entity loss, so main inventory storage should be in this class
	
	//global mod config ideas:
	//- rate of recharge per LV EU packet
	
	//per turret config ideas:
	// - rate of fire: costs more EU for more
	// - accuracy: costs more EU for more accurate
	// - targetting range
	// - rotationPitch offset
	// - right click mode for melee
	
	
	
	//SUPPORT NBT!
	// simply check if the nbt changed, then apply a cost to reclone the item?
	
    private ItemStack[] inv;
    private ItemStack lastAmmo;
    
    //IC2 stuff
    
    public int powerEU = 0;
    public int powerEUMax = 100000;
    
    //Fuel converting
    public double fuelToEURate = 2.5D;
    public int slotFuelStart = 3;
    public int fuelRefillPoint = powerEUMax / 4 * 3;
    
    //Costs
    public int powerEUCostPerRepairMelee = 50;
    public int powerEUCostPerRepairRanged = 50;
    public int powerEUCostPerRefillAmmo = 50;
    public int powerEUCostPerShot = 50;
    public int cooldownTaxBase = 40;
    
	private boolean _isAddedToIC2EnergyNet;
	private boolean _addToNetOnNextTick;
	
	// Entity / AI / FakePlayer integration
	public ICoroAI entInt;
	public EntityLivingBase entLiving;
	
	public boolean firstTimeSync = true;
	public boolean hasLoadedNBT = false;
	
	public int repairTimeCur = 0;
	public int repairTimeMax = 400;
	
	public int lastDamageMelee = 0;
	public int lastDamageRanged = 0;
	
	//Configurables
	public int shootTicksBetweenShots = 40;
	public int shootTicksToCharge = 15;
	public int shootRange = 20;
	public boolean meleeRightClick = false;
	public boolean shootModuleSpeedHack = false;
	

    public TileEntityItemTurret() {
    	inv = new ItemStack[9];
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
			
			manageEntity();
			manageFuel();
			
			if (ModConfigFields.debugTurretsFreeEnergy) addEnergy(100);

			//ItemStack is = new ItemStack(Item.coal);
			//System.out.println(TileEntityFurnace.getItemBurnTime(is));
			
		}
	}
	
	public void manageEntity() {
		if (entLiving != null) {
			if (entLiving.isDead) {
				cleanupEntity();
				return;
			}
			entLiving.setPosition(xCoord+0.5D, yCoord+1D, zCoord+0.5D);
		} else {
			repairTimeCur++;
			if (repairTimeCur < repairTimeMax) {
				//sync or animate or something?
			} else {
				initReady();
			}
		}
	}
	
	public void manageFuel() {
		if (powerEU < fuelRefillPoint) {
			for (int i = 0; i < 3; i++) {
				ItemStack is = inv[slotFuelStart+i];
				if (is != null) {
					int fuelAmount = (int)((double)TileEntityFurnace.getItemBurnTime(is) * fuelToEURate);
					if (fuelAmount > 0) {
						is.stackSize--;
						if (is.stackSize < 1) inv[slotFuelStart+i] = null;
						addEnergy(fuelAmount);
						return;
					}
				}
			}
		}
	}
	
	public void injectFuelToEUStorage() {
		
	}
	
	public boolean canAffordSlot(int slot, boolean performSpend) {
		if (slot == 0) { 
			if (powerEU >= powerEUCostPerRepairMelee) {
				if (performSpend) removeEnergy(powerEUCostPerRepairMelee);
				return true;
			}
		} else if (slot == 1) {
			if (powerEU >= powerEUCostPerRepairRanged) {
				if (performSpend) removeEnergy(powerEUCostPerRepairRanged);
				return true;
			}
		} else if (slot == 2) {
			if (powerEU >= powerEUCostPerRefillAmmo) {
				if (performSpend) removeEnergy(powerEUCostPerRefillAmmo);
				return true;
			}
		}
		return false;
	}
	
	public void syncConfigToEnt(AIFakePlayer parAIFakePlayer) {
		
		//temp
		//meleeRightClick = false;
		/*
		shootTicksBetweenShots = 0;
		shootTicksToCharge = 10;*/
		((JobHuntTurret)parAIFakePlayer.ai.jobMan.getPrimaryJob()).huntRange = shootRange;
		parAIFakePlayer.rangedInUseTicksMax = this.shootTicksToCharge;
		parAIFakePlayer.speedChargeHack = this.shootModuleSpeedHack;
		parAIFakePlayer.coolDownRangedOutSource = this.shootTicksBetweenShots;
		parAIFakePlayer.ai.meleeUseRightClick = this.meleeRightClick;
	}
	
	public int getCostForNextShot() {
		double cost = powerEUCostPerShot * 2;
		if (shootTicksBetweenShots < cooldownTaxBase) {
			cost = cost * (double)Math.max(1, (cooldownTaxBase - shootTicksBetweenShots+1) * 0.3F);
		}
		cost += powerEUCostPerRepairRanged + powerEUCostPerRefillAmmo;
		return (int)Math.ceil(cost);
	}

	@Override
	public boolean canAttack() {
		return powerEU >= getCostForNextShot();
	}
	
	@Override
	public void syncOutsourcedToEntInventory(AIFakePlayer parAIFakePlayer) {
		//System.out.println("syncOutsourcedToEntInventory - pre");
		syncConfigToEnt(parAIFakePlayer);
		
		removeEnergy(getCostForNextShot());
		
		if (parAIFakePlayer.inventory != null && inv != null) {
			
			if (lastAmmo == null && inv[2] != null) lastAmmo = inv[2].copy();
			
			ItemStack is = parAIFakePlayer.inventory.mainInventory[1];
			
			if (inv[1] == null || ((is != null && is.itemID != inv[1].itemID) || (is != null && (is.isStackable() || is.stackSize != inv[1].stackSize)))) {
				parAIFakePlayer.inventory.mainInventory[1] = inv[1];
				is = parAIFakePlayer.inventory.mainInventory[1];
			}
			
			if (inv[1] != null) {
				if (is == null || (is.isStackable() && is.stackSize < inv[1].stackSize)) {
					if (canAffordSlot(1, true)) {
						//System.out.println("replenish stacksize for ranged");
						parAIFakePlayer.inventory.mainInventory[1] = inv[1].copy();
					}
				}
				
			}
			
			is = parAIFakePlayer.inventory.mainInventory[2]; //ammo
			if (lastAmmo != null) {
				if (is == null || is.stackSize < lastAmmo.stackSize) {
					if (canAffordSlot(2, true)) {
						parAIFakePlayer.inventory.mainInventory[2] = lastAmmo.copy();
					}
				}
			}
			

			if (parAIFakePlayer.inventory.mainInventory[2] != null) lastAmmo = parAIFakePlayer.inventory.mainInventory[2].copy();
			
			is = parAIFakePlayer.inventory.mainInventory[parAIFakePlayer.slot_Melee];
			if (is != null) {
				lastDamageMelee = is.getItemDamage();
			} else {
				parAIFakePlayer.inventory.mainInventory[parAIFakePlayer.slot_Melee] = inv[parAIFakePlayer.slot_Melee];
			}
			is = parAIFakePlayer.inventory.mainInventory[parAIFakePlayer.slot_Ranged];
			if (is != null) lastDamageRanged = is.getItemDamage();
			
			
		}
	}
	
	@Override
	public void syncEntToOutsourcedInventory(AIFakePlayer parAIFakePlayer) {
		//System.out.println("syncEntToOutsourcedInventory - post");
		if (parAIFakePlayer.inventory != null && inv != null) {
			
			//should i even sync back for anything really?
			/*for (int i = 0; i < inv.length; i++) {
				inv[i] = parAIFakePlayer.inventory.mainInventory[i];
			}*/
			
			//Cleanup for some oddities
			for (int i = 0; i < 3; i++) {
				if (parAIFakePlayer.inventory.mainInventory[i] != null && parAIFakePlayer.inventory.mainInventory[i].stackSize < 0) parAIFakePlayer.inventory.mainInventory[i] = null;
			}
			
			ItemStack is = parAIFakePlayer.inventory.mainInventory[parAIFakePlayer.slot_Melee];
			if (is != null && lastDamageMelee < is.getItemDamage()) {
				if (canAffordSlot(parAIFakePlayer.slot_Melee, true)) {
					//System.out.println("lastDamageMelee - " + lastDamageMelee + " - " + is.getItemDamage());
					is.damageItem(lastDamageMelee - is.getItemDamage(), parAIFakePlayer.fakePlayer);
				}
			}
			
			is = parAIFakePlayer.inventory.mainInventory[parAIFakePlayer.slot_Ranged];
			if (is != null && lastDamageRanged < is.getItemDamage()) {
				if (canAffordSlot(parAIFakePlayer.slot_Ranged, true)) {
					//System.out.println("lastDamageRanged - " + lastDamageRanged + " - " + is.getItemDamage());
					is.damageItem(lastDamageRanged - is.getItemDamage(), parAIFakePlayer.fakePlayer);
				}
			}
			
			ItemStack is2 = parAIFakePlayer.inventory.mainInventory[2]; //ammo
			//System.out.println(is2);
			int what = 2;
			//if (inv[2] != null && inv[2]) 
			
		}
	}
	
	public void initReady() {
		List entities = this.worldObj.getEntitiesWithinAABB(EntityItemTurretTop.class, AxisAlignedBB.getBoundingBox((double)this.xCoord, (double)this.yCoord + 1.5D, (double)this.zCoord, (double)(this.xCoord + 1), (double)(this.yCoord + 1), (double)(this.zCoord + 1)).expand(0.0D, 2.0D, 0.0D));
		if (entities.size() > 0) {
			entInt = (ICoroAI) entities.get(0);
			HostileWorlds.dbg("Set top piece entity to existing entity: " + ((EntityLivingBase)entities.get(0)).entityId);
			if (entities.size() > 1) {
				HostileWorlds.dbg("Duplicate EntityItemTurretTop detected, error? killing extra entities");
				for (int i = 1; i < entities.size(); i++) {
					HostileWorlds.dbg("killing: " + ((EntityLivingBase)entities.get(i)).entityId);
					((EntityLivingBase)entities.get(i)).setDead();
				}
			}
		} else {
			entInt = new EntityItemTurretTop(worldObj);
		}
		entLiving = (EntityLivingBase)entInt;
		entLiving.setPosition(xCoord+0.5D, yCoord+1D, zCoord+0.5D);
		entInt.getAIAgent().entInv.inventoryOutsourced = this;
		if (entities.size() == 0) {
			worldObj.spawnEntityInWorld(entLiving);
			entInt.getAIAgent().spawnedOrNBTReloadedInit();
		}
		repairTimeCur = 0;
	}
    
    @Override
    public int getSizeInventory() {
        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inv[slot];
    }
   
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inv[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
        }              
    }

    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
                if (stack.stackSize <= amt) {
                        setInventorySlotContents(slot, null);
                } else {
                        stack = stack.splitStack(amt);
                        if (stack.stackSize == 0) {
                                setInventorySlotContents(slot, null);
                        }
                }
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
                setInventorySlotContents(slot, null);
        }
        return stack;
    }
   
    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
        player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}
   
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
       
        NBTTagList tagList = tagCompound.getTagList("Inventory");
        for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
                byte slot = tag.getByte("Slot");
                if (slot >= 0 && slot < inv.length) {
                        inv[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
        }
        
        powerEU = tagCompound.getInteger("powerEU");
        
        readFromNBTPacket(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
                       
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inv.length; i++) {
                ItemStack stack = inv[i];
                if (stack != null) {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setByte("Slot", (byte) i);
                        stack.writeToNBT(tag);
                        itemList.appendTag(tag);
                }
        }
        tagCompound.setTag("Inventory", itemList);
        tagCompound.setInteger("powerEU", powerEU);
        
        writeToNBTPacket(tagCompound);
    }

	@Override
	public void handleClientSentNBT(NBTTagCompound par1nbtTagCompound) {
		//technically i should be doing some sanity checking here
		readFromNBTPacket(par1nbtTagCompound);
		sync();
		//System.out.println("handled client send data to " + this);
	}
    
    public void readFromNBTPacket(NBTTagCompound tagCompound) {
    	if (tagCompound.hasKey("shootTicksBetweenShots")) shootTicksBetweenShots = tagCompound.getInteger("shootTicksBetweenShots");
    	if (tagCompound.hasKey("shootTicksToCharge")) shootTicksToCharge = tagCompound.getInteger("shootTicksToCharge");
    	if (tagCompound.hasKey("shootRange")) shootRange = tagCompound.getInteger("shootRange");
    	if (tagCompound.hasKey("meleeRightClick")) meleeRightClick = tagCompound.getBoolean("meleeRightClick");
    }
    
    public void writeToNBTPacket(NBTTagCompound tagCompound) {
    	tagCompound.setInteger("shootTicksBetweenShots", shootTicksBetweenShots);
    	tagCompound.setInteger("shootTicksToCharge", shootTicksToCharge);
    	tagCompound.setInteger("shootRange", shootRange);
    	tagCompound.setBoolean("meleeRightClick", meleeRightClick);
    }
    
    public void sync() {
    	MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayersInDimension(getDescriptionPacket(), worldObj.provider.dimensionId);
    }
	
	@Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
    	this.readFromNBTPacket(pkt.customParam1);
    }
    
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBTPacket(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, var1);
    }

    @Override
    public String getInvName() {
        return "tco.tileentitytiny";
    }

	@Override
	public boolean isInvNameLocalized() {
		// TODO Auto-generated method stub
		return false;
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
	
	public void cleanupEntity() {
		if(!worldObj.isRemote && entLiving != null)
		{
			entLiving.setDead();
			//not needed since automatic method added into CoroAI
			//entInt.getAIAgent().cleanup();
			entInt = null;
			entLiving = null;
		}
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
		
		cleanupEntity();
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

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void handleServerSentDataWatcherList(List parList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientSentDataWatcherList(List parList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TileHandler getTileHandler() {
		// TODO Auto-generated method stub
		return null;
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
