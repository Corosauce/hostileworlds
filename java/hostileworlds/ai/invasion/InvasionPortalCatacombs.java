package hostileworlds.ai.invasion;

import hostileworlds.HostileWorlds;
import hostileworlds.ServerTickHandler;
import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.config.ModConfigFields;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;

public class InvasionPortalCatacombs extends WorldEvent {
	
	//int waveCountMax = 3;
	
	public InvasionPortalCatacombs() {
		super();
	}
	
	public InvasionPortalCatacombs(int parDim, String parName, EnumWorldEventType parType, ChunkCoordinates source, ChunkCoordinates dest) {
		super(parDim, parName, parType, source, dest);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		
		
		//Waiting on meteor
		if (coordSource == null) {
			//waaaaaaaaaaait
		} else {
			//portal good to go, use
			
			if (waveCount == 0 || !checkForActiveInvadersCached()) {
				if (curCooldown > 0) curCooldown--;
				if (waveCount == ModConfigFields.invasionWaveCountMax || curCooldown <= 0) {
					
					updateCursedPlayersList(false);
			    	updatePlayerStates();
			    	calculatePlayerRatingData();
					
					//HostileWorlds.dbg("invasion wave ended");
					TileEntity tEnt = DimensionManager.getWorld(dimensionID).getTileEntity(coordSource.posX, coordSource.posY, coordSource.posZ);
					if (tEnt == null) {
						HostileWorlds.dbg("portal broke, end!");
						WorldDirectorMultiDim.coordInvasionSources.get(dimensionID).remove(coordSource);
						invasionEnd();
						return;
					}
					if (waveCount < ModConfigFields.invasionWaveCountMax) {
						currentWaveSpawnedInvaders = (int) (ModConfigFields.invasionBaseInvaderCount + (currentWaveDifficultyRating / 3));
						HostileWorlds.dbg("trying to spawn group of: " + currentWaveSpawnedInvaders);
						if (ServerTickHandler.wd.spawnGroup(DimensionManager.getWorld(dimensionID), this.coordDestination, this.coordSource, this, currentWaveSpawnedInvaders)) {
							//((TileEntityHWPortal)tEnt).getMainTileEntity().numOfWavesSpawned++;
							if (!mainPlayerName.equals("")) {
								NBTTagCompound nbt = WorldDirectorMultiDim.getPlayerNBT(mainPlayerName);
								nbt.setInteger("numOfWavesSpawned", nbt.getInteger("numOfWavesSpawned")+1);
							}
							waveCount++;
						}
					} else {
						HostileWorlds.dbg("max waves hit, ending invasion");
						invasionEnd();
					}
				}
			}
			
			
		}
	}
	
	@Override
	public void onFirstDetectNoActiveInvaders() {
		super.onFirstDetectNoActiveInvaders();
		curCooldown = ModConfigFields.coolDownBetweenWaves;
    }
	
}
