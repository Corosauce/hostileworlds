package hostileworlds.entity.bt.ai;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.chunk.ChunkDataPoint;

public class SeekPlayerBase extends Selector {

	//0 = nothing to attack, 1 = attacking, 2 = sanity check says no
	//no longer forces a moveto
	
	public IBTAgent entInt;
	public EntityLiving ent;
	
	public float rangeHunt = 16;
	public int scanRate = 80;
	public int randRate = -1;
	
	public long lastBestTime = -1;
	public ChunkDataPoint lastBestChunk = null;
	
	public SeekPlayerBase(Behavior parParent, IBTAgent parEnt, float parRange) {
		super(parParent);
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
		rangeHunt = parRange;
		//rangeStray = parStray;
	}
	
	public boolean sanityCheck(Entity target) {
		/*if (ent.getHealth() < ent.getMaxHealth() / 4F * 2) {
			return false;
		}*/
		return true;
	}

	@Override
	public EnumBehaviorState tick() {
		
		//TEMP!
		//rangeHunt = 16;
		
		boolean xRay = false;
		
		EntityLivingBase protectEnt = ent;
		Random rand = new Random();
		
		AIBTAgent ai = entInt.getAIBTAgent();
		
		EntityPlayerMP entP = getPlayerToSeek();
		
		//only seek when players logged in, we could use their UUID if we wanted to do it while theyre gone
		if (entP != null) {
		
			if ((scanRate == -1 || ent.worldObj.getTotalWorldTime() % scanRate == 0) && ((randRate == -1 || rand.nextInt(randRate) == 0))) {
				
				//Chunk chunk = ent.worldObj.getChunkFromBlockCoords(MathHelper.floor_double(ent.posX), MathHelper.floor_double(ent.posZ));
				ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(ent.worldObj).getChunkData(MathHelper.floor_double(ent.posX) / 16, MathHelper.floor_double(ent.posZ) / 16);
				
				//null UUID to get the total player activity, for now!
				long thisChunkTime = cdp.getPlayerData(entP.getGameProfile().getId()).playerActivityInteraction;
				
				/*Long longObj = cdp.lookupPlayers.get(event.entityPlayer.getGameProfile().getId());
				long activityVal = 0;
				//prevent internal casting on null obj crash
				if (longObj != null) {
					
				}*/
				
				ChunkDataPoint chunkBest = cdp;
				long chunkTimeBest = thisChunkTime;
				
				ChunkDataPoint nextChunk = null;
				
				int radius = 6;
				
				for (int x = -radius; x <= radius; x++) {
					for (int z = -radius; z <= radius; z++) {
						nextChunk = WorldDirectorManager.instance().getChunkDataGrid(ent.worldObj).getChunkData(cdp.xCoord + x, cdp.zCoord + z);
						if (cdp.getPlayerData(entP.getGameProfile().getId()).playerActivityInteraction > chunkTimeBest) {
							chunkBest = nextChunk;
							chunkTimeBest = cdp.getPlayerData(entP.getGameProfile().getId()).playerActivityInteraction;
						}
					}
				}
				
				boolean forceSeekSpawn = true;
				
				//if we found a better one
				if (!forceSeekSpawn && cdp != chunkBest) {
					if (chunkTimeBest > lastBestTime) {
						lastBestTime = chunkTimeBest;
						lastBestChunk = chunkBest;
						System.out.println("found better chunk! pathing to chunk pos " + chunkBest.xCoord + " - " + chunkBest.zCoord + " - " + chunkTimeBest);
						int topY = ent.worldObj.getHeightValue(chunkBest.xCoord * 16 + 8, chunkBest.zCoord * 16 + 8) - 1;
						ai.blackboard.setMoveAndPathTo(Vec3.createVectorHelper(chunkBest.xCoord * 16 + 8, topY, chunkBest.zCoord * 16 + 8));
					} else {
						System.out.println("still pursuing best chunk - " + lastBestTime + " - moving to: x" + lastBestChunk.xCoord + " - z" + lastBestChunk.zCoord + " : " + ai.blackboard.posMoveTo);
					}
				} else {
					/*System.out.println("in best closest chunk");
					ai.blackboard.setMoveAndPathTo(null);*/
				}
				
				//if no data anywhere
				if (forceSeekSpawn || lastBestTime <= 0) {
					
					ChunkCoordinates spawnCoords = entP.getBedLocation(entP.worldObj.provider.dimensionId);
					lastBestChunk = WorldDirectorManager.instance().getChunkDataGrid(ent.worldObj).getChunkData(MathHelper.floor_double(spawnCoords.posX) / 16, MathHelper.floor_double(spawnCoords.posZ) / 16);
					
					int topY = ent.worldObj.getHeightValue(lastBestChunk.xCoord * 16 + 8, lastBestChunk.zCoord * 16 + 8) - 1;
					//if (ai.blackboard.posMoveTo == null) {
						//System.out.println("setting moveTo again");
						ai.blackboard.setMoveAndPathTo(Vec3.createVectorHelper(lastBestChunk.xCoord * 16 + 8, topY, lastBestChunk.zCoord * 16 + 8));
					//}
					
					System.out.println("seeking out players spawn coord - curpos: " + (int)ent.posX + ", " + (int)ent.posY + ", " + (int)ent.posZ + ", moving to: x: " + lastBestChunk.xCoord + " - z: " + lastBestChunk.zCoord + " : " + ai.blackboard.posMoveTo);
					
				}
				
			}
		}
		
		return super.tick();
	}
	
	//just get first player for now till we refine this class
	public EntityPlayerMP getPlayerToSeek() {
		for (int i = 0; i < ent.worldObj.playerEntities.size(); i++) {
			EntityPlayerMP entP = (EntityPlayerMP) ent.worldObj.playerEntities.get(0);
			if (!entP.isDead) {
				return entP;
			}
		}
		return null;
	}
}
