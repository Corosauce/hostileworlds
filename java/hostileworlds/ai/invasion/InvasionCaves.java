package hostileworlds.ai.invasion;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.config.ModConfigFields;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;

public class InvasionCaves extends WorldEvent {

	public InvasionCaves() {
		super();
	}
	
	public InvasionCaves(int parDim, String parName, EnumWorldEventType parType, ChunkCoordinates source, ChunkCoordinates dest) {
		super(parDim, parName, parType, source, dest);
	}
	
	@Override
	public void tick() {
		super.tick();
		//invasionEnd();
		
		if (state == state.IDLE) {
			
			updateCursedPlayersList(false);
	    	updatePlayerStates();
	    	calculatePlayerRatingData();
			
			currentWaveSpawnedInvaders = (int) (ModConfigFields.invasionBaseInvaderCount + (currentWaveDifficultyRating / 3)); //will always default to 6 for cave invasions
			if (ServerTickHandler.wd.spawnGroup(DimensionManager.getWorld(dimensionID), this.coordDestination, this.coordSource, this, currentWaveSpawnedInvaders)) {
				HostileWorlds.dbg("spawned cave group!");
				if (!mainPlayerName.equals("")) {
					NBTTagCompound nbt = WorldDirectorMultiDim.getPlayerNBT(mainPlayerName);
					nbt.setInteger("numOfWavesSpawned", nbt.getInteger("numOfWavesSpawned")+1);
				}
				setState(state.W1);
			} else {
				HostileWorlds.dbg("failed to spawn cave group, trying another cave");
				if (WorldDirectorMultiDim.coordCaves.get(dimensionID).size() > 0) {
					ArrayList<ChunkCoordinates> invasionSources = new ArrayList<ChunkCoordinates>();
					invasionSources = ServerTickHandler.wd.getUnusedInvasionSourcesInRange(WorldDirectorMultiDim.coordCaves.get(dimensionID), coordDestination, dimensionID, ModConfigFields.meteorCrashDistFromPlayerMax);
					if (invasionSources.size() > 0) {
						Random rand = new Random(invasionSources.size());
						coordSource = invasionSources.get(rand.nextInt(invasionSources.size()));
					} else {
						invasionEnd();
					}
				} else {
					invasionEnd();
				}
			}
		} else if (state == state.W1) {
			if (!checkForActiveInvadersCached()) {
				invasionEnd();
			}
		}
	}
	
	@Override
	public boolean isComplete() {
		return super.isComplete();
	}
}
