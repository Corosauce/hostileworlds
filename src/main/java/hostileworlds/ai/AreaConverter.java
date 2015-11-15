package hostileworlds.ai;

import hostileworlds.HostileWorlds;
import hostileworlds.config.ModConfigFields;

import java.util.Random;

import CoroUtil.util.CoroUtilBlock;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class AreaConverter {
	
	public int dimID;
	public ChunkCoordinates sourceCoord;
	
	public boolean convertActive;
	public int convertRangeMax = 15;
	public int convertCache;
	public Block convertBlock = (Block)Block.blockRegistry.getObject(ModConfigFields.areaConverterReplaceBlocksWith);
	
	public int failTryCount = 0;
	public int failTryCountMax = 80;
	
	public int curRange = 5;
	public int curRangeInc = 5;
	
	public World world; 
	
	public AreaConverter(World parWorld, ChunkCoordinates parSource) {
		world = parWorld;
		sourceCoord = parSource;
	}
	
	public AreaConverter(World parWorld, NBTTagCompound par1NBTTagCompound) {
		world = parWorld;
		readFromNBT(par1NBTTagCompound);
	}
	
	public void start() {
		//System.out.println("Area converter start");
		convertActive = true;
	}
	
	public void stop() {
		//System.out.println("Area converter stopping!");
		convertActive = false;
	}
	
	public void onTick() {
		
		if (world == null) world = DimensionManager.getWorld(dimID);
		
		if (world == null) return;
		
		failTryCountMax = 80;
		
		for (int i = 0; i < 1; i++) {
			if (convertActive && sourceCoord != null) {
	
				Random rand = new Random();
				
				int tryX = sourceCoord.posX + rand.nextInt(curRange) - rand.nextInt(curRange);
				int tryZ = sourceCoord.posZ + rand.nextInt(curRange) - rand.nextInt(curRange);
				
				int tryY = sourceCoord.posY + rand.nextInt(curRange) - rand.nextInt(curRange);
				
				if (Math.sqrt(sourceCoord.getDistanceSquared(tryX, tryY, tryZ)) < curRange) {
				
					Block id = world.getBlock(tryX, tryY, tryZ);
					boolean fail = true;
					
					
					
					if (CoroUtilBlock.isAir(id)) {
						int topY = world.getHeightValue(tryX, tryZ);
						if (Math.abs(topY - sourceCoord.posY) < convertRangeMax) {
							tryY = world.getHeightValue(tryX, tryZ);
							id = world.getBlock(tryX, tryY, tryZ);
						}
					}
					if (!CoroUtilBlock.isAir(id) && id != convertBlock && id != HostileWorlds.blockBloodyCobblestone && 
							id.getMaterial().isSolid() && 
							world.getTileEntity(tryX, tryY, tryZ) == null) {
						//Block block = Block.blocksList[id];
						
						//if (block != null) {
							if (id.getMaterial().isSolid()) {
								fail = false;
								world.setBlock(tryX, tryY, tryZ, convertBlock);
								//System.out.println("converting: " + tryX + " - " + tryY + " - " + tryZ);
							}
						//}
					}
					
					if (fail) {
						failTryCount++;
					} else {
						failTryCount = 0;
					}
					
					if (failTryCount > failTryCountMax) {
						failTryCount = 0;
						if (curRange < convertRangeMax) {
							curRange += curRangeInc;
							//System.out.println("increasing spread range");
						} else {
							//System.out.println("hit max range");
							stop();
						}
					}
				}				
			}
		}
	}
	
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
		//System.out.println("READING AREA CONVERTER DATA!");
        try {
        	curRange = par1NBTTagCompound.getInteger("curRange");
        	convertActive = par1NBTTagCompound.getBoolean("convertActive");
	        sourceCoord = new ChunkCoordinates(par1NBTTagCompound.getInteger("coordX"), par1NBTTagCompound.getInteger("coordY"), par1NBTTagCompound.getInteger("coordZ"));
	        if (sourceCoord.posY == 0) sourceCoord = null;
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
	
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
    	//System.out.println("WRITING AREA CONVERTER DATA!");
        try {
	        par1NBTTagCompound.setInteger("curRange", this.curRange);
	        par1NBTTagCompound.setBoolean("convertActive", convertActive);
	        if (sourceCoord != null) {
		        par1NBTTagCompound.setInteger("coordX", sourceCoord.posX);
		        par1NBTTagCompound.setInteger("coordY", sourceCoord.posY);
		        par1NBTTagCompound.setInteger("coordZ", sourceCoord.posZ);
	        }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
	
}
