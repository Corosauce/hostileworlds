package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.comrade.EntityComradeImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import CoroAI.IPacketNBT;
import CoroAI.componentAI.ICoroAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityIconFX;
import extendedrenderer.particle.entity.EntityRotFX;

public class TileEntityFactory extends TileEntityPowerUser implements IInventory, IPacketNBT
{
    
    private ItemStack[] inv;
    
    //Fuel converting
    public double fuelToEURate = 2.5D;
    public int slotFuelStart = 3;
    public int fuelRefillPoint = powerEUMax / 4 * 3;
    
    //Building
    public int ticksBuildCur = 0;
    public int ticksBuildMax = 100;
    public boolean waitingToSpawn = true;
    
	// Entity / AI / FakePlayer integration
	public ICoroAI entInt;
	public EntityLiving entLiving;
	
	public int repairTimeCur = 0;
	public int repairTimeMax = 400;
	
	//Client fields
	public int clTicksBuild = 0;
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> particles = new ArrayList<EntityRotFX>();

    public TileEntityFactory() {
    	inv = new ItemStack[9];
    	
    	powerEUMax = 1000000;
    }
    
	@Override
	public void updateEntity()
	{
		super.updateEntity();

		//TEMP!!!
		ticksBuildMax = 2400;
		
		if (!worldObj.isRemote) {
			
			manageEntity();
			manageFuel();
			tickBuild();
			
			if (ModConfigFields.debugTurretsFreeEnergy) addEnergy(256);
			
		} else {
			tickAnimate();
		}
	}
	
	public void manageEntity() {
		if (entLiving != null) {
			if (entLiving.isDead) {
				cleanupEntity();
				return;
			}
			//check building status here, lock in place while building etc
			
			
			//entLiving.setPosition(xCoord+0.5D, yCoord+1D, zCoord+0.5D);
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
	
	public void tickBuild() {
		if (entInt != null) {
			
			
			entLiving.setPosition(xCoord+0.5D, yCoord+0.1D/* - 2D + (2D * ((double)ticksBuildCur / (double)ticksBuildMax))*/, zCoord+0.5D);
			
			entLiving.setRotationYawHead(entLiving.rotationYaw += ticksBuildCur * 0.3);
			//System.out.println(yCoord+0.1D - 2D + (2D * ((double)ticksBuildCur / (double)ticksBuildMax)));
			
			ticksBuildCur++;
			
			if (waitingToSpawn && ((double)ticksBuildCur / (double)ticksBuildMax) > 0.8D) {
				waitingToSpawn = false;
				worldObj.spawnEntityInWorld(entLiving);
				entInt.getAIAgent().spawnedOrNBTReloadedInit();
			}
			
			if (ticksBuildCur % 5 == 0) sync();
			
			removeEnergy(500000 / ticksBuildMax);
			
			if (ticksBuildCur > ticksBuildMax) {
				buildComplete();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickAnimate() {
		
		//debug
		//clTicksBuild = 0;
		
		if (clTicksBuild > 0) {
			int amount = 1 + (int)(10D * ((double)clTicksBuild / (double)ticksBuildMax) / (Minecraft.getMinecraft().gameSettings.particleSetting+1));
			
			//System.out.println(amount);
			
			for (int i = 0; i < amount; i++)
	        {
	        	double speed = 0.15D;
	        	double speedInheritFactor = 0.5D;
	        	
	        	Random rand = new Random();
	        	
	        	EntityRotFX entityfx = new EntityIconFX(worldObj, xCoord + rand.nextDouble(), yCoord + 0.0D + rand.nextDouble() * 1.5D, zCoord + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey, Minecraft.getMinecraft().renderEngine);
	        	
				//entityfx.particleGravity = 0.5F;
				entityfx.rotationYaw = rand.nextInt(360);
				//entityfx.rotationPitch = rand.nextInt(360);
				entityfx.setMaxAge(50+rand.nextInt(30));
				entityfx.setGravity(0F);
				entityfx.setRBGColorF(72F/255F, 239F/255F, 8F/255F);
				//red
				entityfx.setRBGColorF(0.4F + (rand.nextFloat() * 0.4F), 0, 0);
				//green
				entityfx.setRBGColorF(0, 0.4F + (rand.nextFloat() * 0.4F), 0);
				//tealy blue
				entityfx.setRBGColorF(0, 0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F));
				//entityfx.setRBGColorF(0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F));
				entityfx.particleScale = 0.25F + 0.2F * rand.nextFloat();
				entityfx.brightness = 1F;
				entityfx.setSize(0.1F, 0.1F);
				entityfx.spawnY = (float) entityfx.posY;
				
				//entityfx.spawnAsWeatherEffect();
				ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
				
				particles.add(entityfx);
				
	        }
		}
		
		for (int i = 0; i < particles.size(); i++) {
			EntityRotFX particle = particles.get(i);
			
			if (clTicksBuild == 0 || particle.isDead) {
				particles.remove(particle);
			} else {
				double centerX = xCoord + 0.5D;
				double centerY = yCoord + 0.5D;
				double centerZ = zCoord + 0.5D;
				
				double vecX = centerX - particle.posX;
				double vecZ = centerZ - particle.posZ;
				double rotYaw = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
				rotYaw -= 75D;// + (15D * clTicksBuild / ticksBuildMax);
				double speed = 0.01D + (0.50D * clTicksBuild / ticksBuildMax);
				particle.motionX = Math.cos(rotYaw * 0.017453D) * speed;
				particle.motionZ = Math.sin(rotYaw * 0.017453D) * speed;
				int cycle = 60;
				
				if (/*clTicksBuild > 100 && */clTicksBuild + 20 < ticksBuildMax) {
					if (particle.getAge() % cycle < cycle/2) {
						particle.setGravity(-0.02F);
					} else {
						particle.setGravity(0.09F);
					}
				} else {
					if (particle.posY > (double)yCoord + 1D) {
						particle.setGravity(0.15F);
					} else {
						particle.setGravity(-0.15F);
					}
					
					//particle.posY = yCoord + 1D;
					//particle.motionY = 0D;
					//particle.setGravity(0F);
				}
			}
		}
	}
	
	public void buildStart() {
		System.out.println("factory: build start");
		EntityComradeImpl comrade = new EntityComradeImpl(worldObj);
		entInt = comrade;
		entLiving = comrade;
		comrade.setPosition(xCoord+0.5D, yCoord+0.1D, zCoord+0.5D);
		comrade.building = true;
		comrade.factory = this;
		waitingToSpawn = true;
	}
	
	public void buildComplete() {
		System.out.println("factory: build complete");
		((EntityComradeImpl)entInt).building = false;
		entInt = null;
		entLiving = null;
		waitingToSpawn = false;
		buildReset();
		sync();
	}
	
	public void buildReset() {
		ticksBuildCur = 0;
	}
	
	public void injectStartingInventory(ICoroAI coroAI) {
		coroAI.getAIAgent().entInv.inventory.mainInventory[0] = inv[0];
		coroAI.getAIAgent().entInv.inventory.mainInventory[1] = inv[1];
		coroAI.getAIAgent().entInv.inventory.mainInventory[2] = inv[2];
	}
	
	public void initReady() {
		/*List entities = this.worldObj.getEntitiesWithinAABB(EntityComradeImpl.class, AxisAlignedBB.getBoundingBox((double)this.xCoord, (double)this.yCoord + 1.5D, (double)this.zCoord, (double)(this.xCoord + 1), (double)(this.yCoord + 1), (double)(this.zCoord + 1)).expand(0.0D, 2.0D, 0.0D));
		if (entities.size() > 0) {
			entInt = (ICoroAI) entities.get(0);
			HostileWorlds.dbg("Set BUILD piece entity to existing entity: " + ((EntityLiving)entities.get(0)).entityId);
			if (entities.size() > 1) {
				HostileWorlds.dbg("Duplicate BUILD piece detected, error? killing extra entities");
				for (int i = 1; i < entities.size(); i++) {
					HostileWorlds.dbg("killing: " + ((EntityLiving)entities.get(i)).entityId);
					((EntityLiving)entities.get(i)).setDead();
				}
			}
			
			entLiving = (EntityLiving)entInt;
			entLiving.setPosition(xCoord+0.5D, yCoord+1D, zCoord+0.5D);
			//entInt.getAIAgent().entInv.inventoryOutsourced = this;
			if (entities.size() == 0) {
				worldObj.spawnEntityInWorld(entLiving);
				entInt.getAIAgent().spawnedOrNBTReloadedInit();
			}
		}*/
		
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
        
        writeToNBTPacket(tagCompound);
    }

	@Override
	public void handleClientSentNBT(NBTTagCompound par1nbtTagCompound) {
		
		if (entInt == null && par1nbtTagCompound.hasKey("buildStart")) {
			buildStart();
			sync();
		}
		
		//technically i should be doing some sanity checking here
		//readFromNBTPacket(par1nbtTagCompound);
		
		//System.out.println("handled client send data to " + this);
	}
    
    public void readFromNBTPacket(NBTTagCompound tagCompound) {
    	clTicksBuild = tagCompound.getInteger("ticksBuild");
    }
    
    public void writeToNBTPacket(NBTTagCompound tagCompound) {
    	tagCompound.setInteger("ticksBuild", ticksBuildCur);
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

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return true;
	}
	
	//Stuff to help IC2 or other
	
	@Override
	public void validate()
	{
		super.validate();
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
	}
	
	public void cleanupEntity() {
		buildReset();
		sync();
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
		super.cleanup();
		cleanupEntity();
	}
}
