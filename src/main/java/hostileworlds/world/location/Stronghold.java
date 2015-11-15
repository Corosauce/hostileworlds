package hostileworlds.world.location;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.bt.EnemyBase;
import hostileworlds.entity.bt.OrcArcher;
import hostileworlds.entity.bt.OrcBoss;
import hostileworlds.entity.bt.OrcGuard;
import hostileworlds.entity.bt.OrcSeeker;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.town.TownObject;
import build.BuildServerTicks;
import build.ICustomGen;
import build.world.Build;
import build.world.BuildJob;

public class Stronghold extends TownObject implements ICustomGen {

	public int areaRadius = 10;
	public int areaHeight = 10;
	public int wallHeight = 5;
	
	public int playerScanRange = 64; //should be much bigger then longest distance from spawn to outside stronghold
	
	public boolean cachePlayerInside;
	
	public OrcSeeker entitySeeker = null;
	
	public Stronghold() {
		super();
	}
	
	@Override
	public void tickUpdate() {
		super.tickUpdate();
		
		if (getWorld().getTotalWorldTime() % 20 == 0) {
			System.out.println("stronghold tick - " + spawn);
			
			if (shouldRemoveStronghold()) {
				removeStronghold();
			}
		}
		
		tickManageSeeker();
		
		if (getWorld().getTotalWorldTime() % 40 == 0) {
			updatePlayerInfoCache();
		}
		//System.out.println("koa village tick");
	}
	
	@Override
	public void initFirstTime() {
		super.initFirstTime();
		
		System.out.println("first time gen stronghold");
		
		genStructure();
	}
	
	public void genStructure() {
		
		areaRadius = 15;
		areaHeight = 30;
		
		clearGenArea();
		
		//safety check for regen testing
		if (getWorld().getBlock(this.spawn.posX, this.spawn.posY, this.spawn.posZ) != HostileWorlds.blockSourceStructure) {
			getWorld().setBlock(this.spawn.posX, this.spawn.posY, this.spawn.posZ, HostileWorlds.blockSourceStructure);
		}
		
		/*int height = wallHeight;
		int radius = areaRadius;
		
		Block blockWalls = HostileWorlds.blockBloodyCobblestone;
		blockWalls = Blocks.stonebrick;
		
		//flooring
		for (int x = -areaRadius; x <= areaRadius; x++) {
			for (int z = -areaRadius; z <= areaRadius; z++) {
				setBlockRel(x, 0, z, blockWalls);
			}
		}
		
		for (int y = 0; y < height; y++) {
			
			
			//outer wall
			for (int x = -radius; x <= radius; x++) {
				setBlockRel(x, y, radius, blockWalls);
				setBlockRel(x, y, -radius, blockWalls);
			}
			
			//outer wall
			for (int z = -radius; z <= radius; z++) {
				setBlockRel(radius, y, z, blockWalls);
				setBlockRel(-radius, y, z, blockWalls);
			}
		}*/
		
		genSchematic();
	}
	
	public void genSchematic() {
		
		int yOffset = 0;//-1;
		
		Build mainStructureData = new Build(spawn.posX, spawn.posY + yOffset, spawn.posZ, CoroUtilFile.getSaveFolderPath() + "HWSchematics" + File.separator + "stronghold");
		/*ChunkCoordinates coords = getBuildingCornerCoord();
		mainStructureData.map_coord_minX = coords.posX;
		mainStructureData.map_coord_minY = coords.posY;
		mainStructureData.map_coord_minZ = coords.posZ;*/
		
		
    	BuildJob bj = new BuildJob(-99, spawn.posX, spawn.posY + yOffset, spawn.posZ, mainStructureData);
    	/*coords = getBuildingCornerCoord();
		bj.build_startX = coords.posX;
		bj.build_startY = coords.posY;
		bj.build_startZ = coords.posZ;*/
		bj.build.dim = getWorld().provider.dimensionId;
		bj.useFirstPass = false; //skip air setting pass
		bj.useRotationBuild = false;
		bj.build_rate = 1600;
		bj.setDirection(0);
		bj.customGenCallback = this;
		bj.blockIDsNoBuildOver.add(HostileWorlds.blockSourceStructure);
		
		//set stronghold height size to height of schematic
		areaHeight = mainStructureData.map_sizeY;
		
		BuildServerTicks.buildMan.addBuild(bj);
	}
	
	public ChunkCoordinates getBuildingCornerCoord() {
    	return new ChunkCoordinates(MathHelper.floor_double(((double)spawn.posX - (double)areaRadius)), spawn.posY/*MathHelper.floor_double(((double)pos.posY - (double)size.posY/2D))*/, MathHelper.floor_double(((double)spawn.posZ - (double)areaRadius)));
    }
	
	public void clearGenArea() {
		for (int y = 0; y < areaHeight; y++) {
			for (int x = -areaRadius; x <= areaRadius; x++) {
				for (int z = -areaRadius; z <= areaRadius; z++) {
					setBlockRel(x, y, z, Blocks.air);
				}
			}
		}
	}
	
	public void setBlockRel(int x, int y, int z, Block parBlock) {
		setBlockRel(x, y, z, parBlock, 0);
	}
	
	public void setBlockRel(int x, int y, int z, Block parBlock, int parMeta) {
		//air check to prevent source block not overwritten for now
		if (getWorld().getBlock(this.spawn.posX+x, this.spawn.posY+y, this.spawn.posZ+z) != HostileWorlds.blockSourceStructure) {
			getWorld().setBlock(this.spawn.posX+x, this.spawn.posY+y, this.spawn.posZ+z, parBlock, parMeta, 3);
		}
	}
	
	public boolean shouldRemoveStronghold() {
		return getWorld().getBlock(this.spawn.posX, this.spawn.posY, this.spawn.posZ) != HostileWorlds.blockSourceStructure;
	}
	
	public void removeStronghold() {
		
		System.out.println("removing HW stronghold");
		
		WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(getWorld());
		
		wd.removeTickingLocation(this);
	}
	
	public void spawnEntities() {
		//test code
		
		int spawnRadius = areaRadius-1;
		
		//stuff spawned in the relative positive directions needs a -1 for z, but not ALL of them, wtf?
		
		int fix = 0;
		
		//archers in posts
		spawnEntityRel("archer", Vec3.createVectorHelper(spawnRadius, wallHeight + 1, spawnRadius - fix));
		spawnEntityRel("archer", Vec3.createVectorHelper(spawnRadius, wallHeight + 1, -spawnRadius));
		spawnEntityRel("archer", Vec3.createVectorHelper(-spawnRadius, wallHeight + 1, spawnRadius - fix));
		spawnEntityRel("archer", Vec3.createVectorHelper(-spawnRadius, wallHeight + 1, -spawnRadius));
		
		//guards at gates
		spawnEntityRel("guard", Vec3.createVectorHelper(-13, 1, -2));
		spawnEntityRel("guard", Vec3.createVectorHelper(-13, 1, 2));
		
		spawnEntityRel("guard", Vec3.createVectorHelper(13, 1, -2));
		spawnEntityRel("guard", Vec3.createVectorHelper(13, 1, 2));
		
		spawnEntityRel("guard", Vec3.createVectorHelper(-2, 1, -13));
		spawnEntityRel("guard", Vec3.createVectorHelper(+2, 1, -13));
		
		spawnEntityRel("guard", Vec3.createVectorHelper(-2, 1, 13 - fix));
		spawnEntityRel("guard", Vec3.createVectorHelper(+2, 1, 13 - fix));
		
		//upstairs guards
		spawnEntityRel("guard", Vec3.createVectorHelper(-5, 5, 0));
		spawnEntityRel("guard", Vec3.createVectorHelper(+5, 5, 0));
		
		spawnEntityRel("guard", Vec3.createVectorHelper(+5, 5, -5));
		spawnEntityRel("guard", Vec3.createVectorHelper(-5, 5, +5));
		
		//boss
		spawnEntityRel("boss", Vec3.createVectorHelper(0, 9, -8));
	}
	
	public void spawnEntityRel(String parType, Vec3 parCoords) {
		
		//fix centering innacuracy for our stronghold
		//parCoords.xCoord -= 1;
		//parCoords.zCoord += 1;
		
		//weird fix
		/*if (parCoords.zCoord > 0) {
			parCoords.zCoord--;
		}*/
		
		parCoords.xCoord += spawn.posX;
		parCoords.zCoord += spawn.posZ;
		parCoords.yCoord += spawn.posY;
		
		EnemyBase ent = null;
		
		if (parType.equals("archer")) {
			ent = new OrcArcher(getWorld());
		} else if (parType.equals("guard")) {
			ent = new OrcGuard(getWorld());
		} else if (parType.equals("boss")) {
			ent = new OrcBoss(getWorld());
		}
		
		if (ent != null) {
			ent.getAIBTAgent().setManagedLocation(this);
			ent.setPosition(parCoords.xCoord + 0.5F, parCoords.yCoord, parCoords.zCoord + 0.5F);
			getWorld().spawnEntityInWorld(ent);
			ent.onSpawnWithEgg(null);
		}
		
		//TODO: register entities with managedlocation, how are ids managed?
	}

	@Override
	public void genPassPre(World world, BuildJob parBuildJob, int parPass) {
		if (parPass == -1) {
			spawnEntities();
		}
	}

	@Override
	public NBTTagCompound getInitNBTTileEntity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isEntityInsideStructure(Entity ent) {
		if (ent.posX > spawn.posX - areaRadius && ent.posX <= spawn.posX + areaRadius) {
			if (ent.posZ > spawn.posZ - areaRadius && ent.posZ <= spawn.posZ + areaRadius) {
				//for now this part of the code assumes spawn is at the bottom most position of structure
				if (ent.posY >= spawn.posY) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void updatePlayerInfoCache() {
		cachePlayerInside = false;
		EntityPlayer entP = getWorld().getClosestPlayer(spawn.posX, spawn.posY, spawn.posZ, playerScanRange);
		if (entP != null) {
			cachePlayerInside = isEntityInsideStructure(entP);
		}
	}
	
	public boolean isPlayerInside() {
		//System.out.println("isPlayerInside: " + cachePlayerInside);
		return cachePlayerInside;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound var1) {
		super.readFromNBT(var1);
		areaHeight = var1.getInteger("areaHeight");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound var1) {
		super.writeToNBT(var1);
		var1.setInteger("areaHeight", areaHeight);
	}
	
	public void tickManageSeeker() {
		if (getWorld().getTotalWorldTime() % 80 == 0) {
			if (entitySeeker == null || entitySeeker.isDead || entitySeeker.getHealth() == 0 || !getWorld().loadedEntityList.contains(entitySeeker)) {
				System.out.println("SPAWN NEW ORC SEEKER");
				
				entitySeeker = new OrcSeeker(getWorld());
				entitySeeker.getAIBTAgent().setManagedLocation(this);
				entitySeeker.setPosition(spawn.posX, spawn.posY + 2, spawn.posZ);
				
				//Chunk chunk = getWorld().getChunkFromBlockCoords(spawn.posX, spawn.posZ);
				
				/*IChunkProvider ichunkprovider = getWorld().getChunkProvider();
		        ichunkprovider.loadChunk(spawn.posX - 3 >> 4, spawn.posZ - 3 >> 4);
		        ichunkprovider.loadChunk(spawn.posX + 3 >> 4, spawn.posZ - 3 >> 4);
		        ichunkprovider.loadChunk(spawn.posX - 3 >> 4, spawn.posZ + 3 >> 4);
		        ichunkprovider.loadChunk(spawn.posX + 3 >> 4, spawn.posZ + 3 >> 4);*/
				
				entitySeeker.initChunkLoad();
				
				getWorld().spawnEntityInWorld(entitySeeker);
				entitySeeker.onSpawnWithEgg(null);
			}
		}
	}

}
