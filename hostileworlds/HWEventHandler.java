package hostileworlds;

import hostileworlds.entity.monster.ZombieMiner;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;

public class HWEventHandler {
	
	@ForgeSubscribe
	public void breakSpeed(BreakSpeed event) {
		//ZAUtil.blockEvent(event, 20);
	}
	
	@ForgeSubscribe
	public void harvest(HarvestCheck event) {
		//ZAUtil.blockEvent(event, 1);
	}
	
	@ForgeSubscribe
	public void entityEnteredChunk(EntityEvent.EnteringChunk event) {
		Entity entity = event.entity;
	    if ((entity instanceof ZombieMiner)) {
	    	if (!entity.worldObj.isRemote) {
	    		//System.out.println("update miner loaded chunks");
	    		((ZombieMiner)entity).forceChunkLoading(event.newChunkX, event.newChunkZ);
	    	}
	    }
	    
	}
}
