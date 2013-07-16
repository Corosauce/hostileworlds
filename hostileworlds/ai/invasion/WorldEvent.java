package hostileworlds.ai.invasion;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.WorldDirector;
import hostileworlds.block.TileEntityHWPortal;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityInvader;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import particleman.forge.ParticleMan;
import CoroAI.componentAI.ICoroAI;
import CoroAI.entity.EnumJobState;

public class WorldEvent {

	public int dimensionID;
	public ChunkCoordinates coordSource;
	public ChunkCoordinates coordDestination;
	public String mainPlayerName = "";
	
	public boolean invasionActive = true;
	public int ticksActive;
	public int ticksMaxActive;
	
	public EnumWorldEventType type;
	
	public EnumJobState state;
	
	public ArrayList<ICoroAI> invasionEntities = new ArrayList<ICoroAI>();
	public ArrayList<String> cursedPlayers = new ArrayList<String>();
	
	public int lastCheckedInvasionCount = 1; //prevent default 0 incase cache isnt generated right away
	//public float lastWavePlayerRating;
	public float currentWaveDifficultyRating;
	public int currentWavePlayerCount;
	public int currentWaveCountFromPortal;
	public int currentWaveSpawnedInvaders;
	
	public int waveCount = 0;
	//public int maxCooldown = 12000; //half a day
	public int curCooldown = 800; //initial waiting for fire to go away cooldown, should be set only if meteor i guess
	
	//for client
	public float lastLeaderDist;
	public int lastLeaderCount;
	
	public enum EnumWorldEventType {
		INV_PORTAL_CATACOMBS, INV_PORTAL_NETHER, INV_CAVE, BOSS_CATACOMBS;
		public String[] eventEnumToName = new String[] { "Portal Invasion", "Nether Invasion", "Cave Invasion", "Boss Event" };
		
		private static final Map<Integer, EnumWorldEventType> lookup = new HashMap<Integer, EnumWorldEventType>();
	    static { for(EnumWorldEventType e : EnumSet.allOf(EnumWorldEventType.class)) { lookup.put(e.ordinal(), e); } }
	    public static EnumWorldEventType get(int intValue) { return lookup.get(intValue); }
	}
	
	public WorldEvent() {
		ticksActive = 0;
		ticksMaxActive = 168000; //more of a safety, 1 minecraft week timeout, the invasion should play out and end earlier than this
		state = EnumJobState.IDLE;
	}
	
	public WorldEvent(int parDim, String parName, EnumWorldEventType parType, ChunkCoordinates source, ChunkCoordinates dest) {
		this();
		type = parType;
		coordSource = source;
		coordDestination = dest;
		mainPlayerName = parName;
		dimensionID = parDim;
		
		//new invasion first wave ever for players name
		//starts at 0 rating
		//scans area for players, saves list for using later
		updateCursedPlayersList(false);
		
		//at end of wave
		//scan area for MORE players to add, dont clear the existing list
		//update each players rating, it sets it to player nbt
		//invasion sets currentWavePlayerRating to the calculated average across all players
		
		//next wave or invasion eventually starts, has currentWavePlayerRating to work with
	}
	
	public static WorldEvent newInvasionFromNBT(NBTTagCompound par1NBTTagCompound) {
		EnumWorldEventType type = EnumWorldEventType.get(par1NBTTagCompound.getInteger("type"));
		
		WorldEvent inv = null;
		
		if (type == EnumWorldEventType.INV_CAVE) {
			inv = new InvasionCaves();
		} else if (type == EnumWorldEventType.INV_PORTAL_CATACOMBS) {
			inv = new InvasionPortalCatacombs();
		} else if (type == EnumWorldEventType.BOSS_CATACOMBS) {
			inv = new BossCatacombs();
		}
		
		inv.readNBT(par1NBTTagCompound);
		return inv;
	}
	
	public void tick() {
		World world = DimensionManager.getWorld(dimensionID);
		EntityPlayer entP = world.getPlayerEntityByName(mainPlayerName);
		if (entP != null) WorldDirector.getPlayerNBT(entP.username).setInteger("HWInvasionCooldown", ModConfigFields.coolDownBetweenInvasionsPortal + 1);
		//if (DimensionManager.getWorld(dimensionID).getWorldTime() % 40 == 0) updatePlayerStates();
		//invasionEnd();
	}
	
	public boolean isComplete() {
		return !invasionActive;
	}
	
	public void setState(EnumJobState job) {
		state = job;
	}
    
    public boolean checkForActiveInvadersCached() {
    	if (lastCheckedInvasionCount == 0 || DimensionManager.getWorld(dimensionID).getWorldTime() % 100 == 0) {
	    	for (int i = 0; i < invasionEntities.size(); i++) {
	    		ICoroAI ent = invasionEntities.get(i);
	    		
	    		if (ent.getAIAgent().ent.isDead) {
	    			invasionEntities.remove(i);
	    		} else {
	    			//HostileWorlds.dbg("murrrrrrrr" + ((EntityLiving)ent).getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ));
	    		}
	    	}
	    	
	    	if (lastCheckedInvasionCount != invasionEntities.size() && invasionEntities.size() == 0) onFirstDetectNoActiveInvaders();
	    	
	    	lastCheckedInvasionCount = invasionEntities.size();
	    	
	    	if (invasionEntities.size() == 0) {
	    		return false;
	    		//invasionEnd();
	    	}
	    	return true;
    	} else {
    		return lastCheckedInvasionCount > 0;
    	}
    }
    
    public void onFirstDetectNoActiveInvaders() {
    	updateCursedPlayersList(false);
    	updatePlayerStates();
    	calculatePlayerRatingData();
    }
    
    public void invasionStart() {
    	//HostileWorlds.dbg("Invasion started for: " + coordSource.posX + ", " + coordSource.posY + ", " + coordSource.posZ);
    	invasionActive = true;
    	//invasionEntities.clear();
    }
    
    public void invasionEnd() {
    	HostileWorlds.dbg("Invasion ended for: " + coordSource.posX + ", " + coordSource.posY + ", " + coordSource.posZ);
    	invasionActive = false;
    	//invasionLastTime = System.currentTimeMillis();
    	//cursedPlayers.clear();
    }
    
    public void registerWithInvasion(ICoroAI ent) {
    	//HostileWorlds.dbg("ent registered with invasion: " + ent);
    	invasionEntities.add(ent);
    }
    
    public void setEntityInvasionInfo(EntityInvader ent) {
    	//HostileWorlds.dbg("ent set to difficulty scale: " + currentWaveDifficultyRating);
    	ent.HWDifficulty = this.currentWaveDifficultyRating;
    	ent.primaryTarget = mainPlayerName;
    }
    
    public EntityPlayer tryGetCursedPlayer(String username) {
    	EntityPlayer entP = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
    	
    	if (entP != null && entP.worldObj.getWorldInfo().getDimension() == DimensionManager.getWorld(dimensionID).getWorldInfo().getDimension()) {
    		return entP;
    	}
    	
    	return null;
    }
    
    public void calculatePlayerRatingData() {
		
		World worldObj = DimensionManager.getWorld(dimensionID);

		float playersFound = 0;
		float totalRating = 0;
		
		for (int i = 0; i < cursedPlayers.size(); i++) {
			EntityPlayer entP = tryGetCursedPlayer(cursedPlayers.get(i));
			
			if (entP != null) {
				playersFound++;
				totalRating += WorldDirector.getPlayerNBT(entP.username).getInteger("HWPlayerRating");
			}
		}
		
		int waveCount = 0;
		
		/*TileEntity tEnt = DimensionManager.getWorld(dimensionID).getBlockTileEntity(coordSource.posX, coordSource.posY, coordSource.posZ);
		if (tEnt instanceof TileEntityHWPortal) {
			TileEntityHWPortal portal = ((TileEntityHWPortal)tEnt).getMainTileEntity();
			
			waveCount = portal.numOfWavesSpawned;
		}*/
		
		waveCount = WorldDirector.getPlayerNBT(mainPlayerName).getInteger("numOfWavesSpawned");
		
		float playerCountAdditiveFactor = 3;
		float waveCountFactor = 2;
		
		int averagedRating = (int) ((totalRating / playersFound) + (playersFound * playerCountAdditiveFactor) + (waveCount * waveCountFactor));
		
		HostileWorlds.dbg("HW averaged rating: " + averagedRating + " for " + playersFound + " players, waveCount at: " + waveCount);
		
		currentWaveDifficultyRating = averagedRating;
		currentWavePlayerCount = (int) playersFound;
		currentWaveCountFromPortal = waveCount;
    }
    
    public void updateCursedPlayersList(boolean clearList) {
    	float maxDist = 96F;
		
		World worldObj = DimensionManager.getWorld(dimensionID);
		
		if (clearList) cursedPlayers.clear();
		
		for (int i = 0; i < worldObj.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)worldObj.playerEntities.get(i);
			
			if (!cursedPlayers.contains(entP.username) && entP.getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ) < maxDist) {
				cursedPlayers.add(entP.username);
			}
		}
    }
    
    public void updatePlayerStates() {
    	
		for (int i = 0; i < cursedPlayers.size(); i++) {
			float armorValue = 0;
			float bestWeaponValue = 0;
			boolean hasGlove = false;
			
			EntityPlayer entP = tryGetCursedPlayer(cursedPlayers.get(i));
			
			if (entP != null) {
				for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
					if (entP.inventory.armorInventory[armorIndex] != null && entP.inventory.armorInventory[armorIndex].getItem() instanceof ItemArmor) {
						armorValue += EnchantmentHelper.getEnchantmentModifierDamage(entP.inventory.armorInventory, DamageSource.generic);
					}
				}
				
				for (int slotIndex = 0; slotIndex < entP.inventory.mainInventory.length; slotIndex++) {
					if (entP.inventory.mainInventory[slotIndex] != null) {
						if (entP.inventory.mainInventory[slotIndex].itemID == ParticleMan.itemGlove.itemID) hasGlove = true;
						float dmg = entP.inventory.mainInventory[slotIndex].getItem().getDamageVsEntity(entP) + 
								EnchantmentHelper.getEnchantmentModifierLiving(entP, (EntityLiving)entP);
						if (dmg > bestWeaponValue) {
							bestWeaponValue = dmg;
						}
					}
				}
				
				WorldDirector.getPlayerNBT(entP.username).setInteger("HWPlayerRating", (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0)));
			}
		}
    }
	
	public void writeNBT(NBTTagCompound data) {
		//data.setFloat("lastWavePlayerRating", lastWavePlayerRating);
		data.setFloat("currentWaveDifficultyRating", currentWaveDifficultyRating);
		data.setInteger("dimensionID", dimensionID);
    	data.setInteger("type", type.ordinal());
    	if (coordSource != null) HostileWorlds.writeChunkCoords("source", coordSource, data);
    	if (coordDestination != null) HostileWorlds.writeChunkCoords("dest", coordDestination, data);
    	data.setInteger("ticksActive", ticksActive);
    	data.setInteger("ticksMaxActive", ticksMaxActive);
    	data.setInteger("waveCount", waveCount);
    	data.setString("mainPlayerName", mainPlayerName);
    	data.setInteger("state", state.ordinal());
    	
    	//for client syncing
    	data.setInteger("currentWaveCountFromPortal", currentWaveCountFromPortal);
    	data.setInteger("currentWaveSpawnedInvaders", currentWaveSpawnedInvaders);
    	data.setInteger("currentWavePlayerCount", currentWavePlayerCount);
    	int dist = -1;
    	if (coordDestination != null && invasionEntities.size() > 0) dist = (int) ((EntityLiving)invasionEntities.get(0)).getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ);
    	data.setInteger("lastLeaderDist", dist);
    	data.setInteger("lastLeaderCount", invasionEntities.size());
    	data.setInteger("curCooldown", curCooldown);
    }
	
	public void readNBT(NBTTagCompound data) {
		//lastWavePlayerRating = data.getFloat("lastWavePlayerRating");
		currentWaveDifficultyRating = data.getFloat("currentWaveDifficultyRating");
		dimensionID = data.getInteger("dimensionID");
    	type = EnumWorldEventType.get(data.getInteger("type"));
    	coordSource = HostileWorlds.readChunkCoords("source", data);
    	coordDestination = HostileWorlds.readChunkCoords("dest", data);
    	ticksActive = data.getInteger("ticksActive");
    	ticksMaxActive = data.getInteger("ticksMaxActive");
    	waveCount = data.getInteger("waveCount");
    	mainPlayerName = data.getString("mainPlayerName");
    	state = EnumJobState.get(data.getInteger("state"));
    	
    	//for client syncing
    	currentWaveCountFromPortal = data.getInteger("currentWaveCountFromPortal");
    	currentWaveSpawnedInvaders = data.getInteger("currentWaveSpawnedInvaders");
    	currentWavePlayerCount = data.getInteger("currentWavePlayerCount");
    	lastLeaderDist = data.getInteger("lastLeaderDist");
    	lastLeaderCount = data.getInteger("lastLeaderCount");
    	curCooldown = data.getInteger("curCooldown");
    }
}
