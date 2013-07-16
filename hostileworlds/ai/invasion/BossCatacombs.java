package hostileworlds.ai.invasion;

import hostileworlds.block.TileEntityHWPortal;
import hostileworlds.config.ModConfigFields;
import hostileworlds.dimension.HWTeleporter;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieBlockWielder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.entity.EnumJobState;

public class BossCatacombs extends WorldEvent {
	
	public int preStartCooldown = 150;
	public int destructSequenceCountdown = 20*30;
	
	public BossCatacombs() {
		super();
	}
	
	public BossCatacombs(int parDim, String parName, EnumWorldEventType parType, ChunkCoordinates source, ChunkCoordinates dest) {
		super(parDim, parName, parType, source, dest);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (preStartCooldown > 0) preStartCooldown--;
		
		World world = DimensionManager.getWorld(dimensionID);
		
		if (state == EnumJobState.W1) {
			if (!checkForActiveInvadersCached()) {
				TileEntity tEnt = world.getBlockTileEntity(HWTeleporter.portalCoord.posX, HWTeleporter.portalCoord.posY, HWTeleporter.portalCoord.posZ);
				if (tEnt instanceof TileEntityHWPortal) {
					((TileEntityHWPortal) tEnt).bossEventOccurred = true;
					
				} else {
					System.out.println("couldnt find portal to mark event of boss death");
				}
				setState(EnumJobState.W2);
			}
		}
		
		if (preStartCooldown > 0) return;
		
		//spawning stuff
		if (state == EnumJobState.IDLE) {
			ZombieBlockWielder boss = new ZombieBlockWielder(world);
			boss.setPosition(coordSource.posX, coordSource.posY, coordSource.posZ);
			
			world.spawnEntityInWorld(boss);
			registerWithInvasion(boss);
			setState(EnumJobState.W1);
		} else if (state == EnumJobState.W1) {
			
		} else if (state == EnumJobState.W2) {
			if (destructSequenceCountdown > 0) destructSequenceCountdown--;
			
			if (destructSequenceCountdown > 0) {
				if (destructSequenceCountdown % 20 == 0) {
					Zombie boss = new Zombie(world);
					boss.setPosition(coordSource.posX, coordSource.posY - 7, coordSource.posZ);
					
					world.spawnEntityInWorld(boss);
				}
				//spawn stuff
			} else {
				invasionEnd();
			}
		}
		
	}
	
	@Override
	public void onFirstDetectNoActiveInvaders() {
		super.onFirstDetectNoActiveInvaders();
		curCooldown = ModConfigFields.coolDownBetweenWaves;
    }
	
}
