package hostileworlds;

import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.entity.monster.ZombieMiner;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOre;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import CoroUtil.IChunkLoader;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HWEventHandler {
	
	public static List<ChunkCoordinates> listConnectablePointsVisualDebug = new ArrayList<ChunkCoordinates>();
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		//ZAUtil.blockEvent(event, 20);
	}
	
	@SubscribeEvent
	public void harvest(HarvestCheck event) {
		//ZAUtil.blockEvent(event, 1);
	}
	
	@SubscribeEvent
	public void entityEnteredChunk(EntityEvent.EnteringChunk event) {
		Entity entity = event.entity;
	    if ((entity instanceof IChunkLoader)) {
	    	if (!entity.worldObj.isRemote) {
	    		//System.out.println("update miner loaded chunks");
	    		((IChunkLoader)entity).forceChunkLoading(event.newChunkX, event.newChunkZ);
	    	}
	    }
	    
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event) {
		/*if (Minecraft.getMinecraft().theWorld.getTotalWorldTime() % (20*3) == 0) {
			listConnectablePointsVisualDebug.clear();
		}*/
		for (int i = 0; i < listConnectablePointsVisualDebug.size(); i++) {
			ChunkCoordinates cc = listConnectablePointsVisualDebug.get(i);
			if (cc != null) {
				try {
					//Overlays.renderLineFromToBlockCenter(cc.posX, cc.posY, cc.posZ, cc.posX, cc.posY + 3, cc.posZ, 0xFFFFFF/*i * 1000*/);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void breakBlockHarvest(HarvestDropsEvent event) {
		WorldDirectorMultiDim.handleHarvest(event);
	}
	
	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		if (event.entityLiving.getEntityData().getBoolean("CoroAI_HW_GravelDeath")) {
			event.entity.worldObj.setBlock(MathHelper.floor_double(event.entity.posX), MathHelper.floor_double(event.entity.posY), MathHelper.floor_double(event.entity.posZ), Blocks.gravel);
		}
	}
}
