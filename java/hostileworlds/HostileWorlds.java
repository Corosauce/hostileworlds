package hostileworlds;

import hostileworlds.ai.WorldDirector;
import hostileworlds.commands.CommandHW;
import hostileworlds.config.ModConfigFields;
import hostileworlds.dimension.HWDimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import modconfig.ConfigMod;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import CoroUtil.IChunkLoader;
import CoroUtil.util.CoroUtilFile;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "HostileWorlds", name="Hostile Worlds", version="0.4.3", useMetadata=false)
public class HostileWorlds {
	
	@Mod.Instance( value = "HostileWorlds" )
	public static HostileWorlds instance;
	public static String modID = "hostileworlds";
    
    @SidedProxy(clientSide = "hostileworlds.ClientProxy", serverSide = "hostileworlds.CommonProxy")
    public static CommonProxy proxy;
    
    public static Block blockAuraCurse;
    public static Block blockInvasionSource;
    public static Block blockBloodyCobblestone;
    //public static BlockHWPortal blockPortal;
    //public static Block blockRaidingLadder;
    //public static Block blockRaidingLadderBase;
    //public static Block blockItemTurret;
    //public static Block blockFactory;
    
    public static List<Block> blocksList = new ArrayList<Block>();
    
    //public static Block blockRtsBuilding;
    
    //public static Item itemLaserBeam;
    
    public static int texBloodCobble;
    
    public static boolean initProperNeededForWorld = true;
    
    public static String eventChannelName = "hostileworlds";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigMod.addConfigFile(event, "hw", new ModConfigFields());
    	//ConfigMod.addConfigFile(event, "hwblocks", new ModConfigBlockFields(), false);
    	//config.init(event);
    	
    	//generateConfigFile();
    	//updateSaveFile();
    	//test();
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	eventChannel.register(new EventHandlerPacket());
    	MinecraftForge.EVENT_BUS.register(new HWEventHandler());
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
    	//NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
    	//NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
    	/*dimIDCatacombs = DimensionManager.getNextFreeDimId();
    	
    	DimensionManager.registerProviderType(dimIDCatacombs, HWWorldProvider.class, false);
		DimensionManager.registerDimension(dimIDCatacombs, dimIDCatacombs);*/
    	
    	
    	
    	proxy.init(this);
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new PortalChunkloadCallback());
	}

	public class PortalChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {
		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				Entity ent = ticket.getEntity();
				if (ent == null) {
					int portalX = ticket.getModData().getInteger("portalX");
					int portalY = ticket.getModData().getInteger("portalY");
					int portalZ = ticket.getModData().getInteger("portalZ");

					if (portalY >= 0) {
						TileEntity te = world.getTileEntity(portalX, portalY, portalZ);
						if (te instanceof IChunkLoader) {
							((IChunkLoader) te).setChunkTicket(ticket);
							((IChunkLoader) te).forceChunkLoading(portalX/16, portalZ/16);
						}
					}
				} else if (ent instanceof IChunkLoader) {
					dbg("world load readd miner chunkloader");
					((IChunkLoader) ent).setChunkTicket(ticket);
					((IChunkLoader) ent).forceChunkLoading(ent.chunkCoordX, ent.chunkCoordZ);
				}

			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				Entity ent = ticket.getEntity();
				if (ent == null) {
					int portalX = ticket.getModData().getInteger("portalX");
					int portalY = ticket.getModData().getInteger("portalY");
					int portalZ = ticket.getModData().getInteger("portalZ");
	
					TileEntity te = world.getTileEntity(portalX, portalY, portalZ);
					if (te instanceof IChunkLoader) {
						validTickets.add(ticket);
					}
					
					/*int blId = world.getBlockId(portalX, portalY, portalZ);
					if (blId == blockPortal.blockID) {
						validTickets.add(ticket);
					}*/
				} else if (ent instanceof IChunkLoader) {
					validTickets.add(ticket);
				}
			}
			return validTickets;
		}

	}
    
    public HostileWorlds() {
    	int hm = 0;
    	//TickRegistry.registerTickHandler(new ServerTickHandler(this), Side.SERVER);
    }
    
    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
    	//so it inits before entity nbt does
    	
    }
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandHW());
    	//proper command adding
    	//((ServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerCommand(new commandAddOwner());
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	writeGameNBT();
    	//HWDimensionManager.unregisterDimensionsAndSave();
    	//ServerTickHandler.rts.writeToFile(true);
    	initProperNeededForWorld = true;
    }
    
    public static void initTry() {
    	if (initProperNeededForWorld) {
    		initProperNeededForWorld = false;
	    	//if (ServerTickHandler.rts == null) ServerTickHandler.rts = new RtsEngine();
	    	//ServerTickHandler.rts.readFromFile();
	    	CoroUtilFile.getWorldFolderName(); //make it cache the lastWorldFolder, lucky that it was cached before, as serverStop method cant update the cache, issue arrised due to new use of FMLServerAboutToStartEvent
	    	HWDimensionManager.loadAndRegisterDimensions();
    	}
    }
    
    /*public static String lastWorldFolder = "";
    
    public static String getWorldFolderName() {
		World world = DimensionManager.getWorld(0);
		
		if (world != null) {
			lastWorldFolder = ((WorldServer)world).getChunkSaveLocation().getName();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getWorldSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
    
    @SideOnly(Side.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().mcDataDir.getPath();
	}*/
	
	public static void writeGameNBT() {
		
		if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) return;
		
		dbg("Saving Hostile Worlds data");
		
		NBTTagCompound gameData = new NBTTagCompound();
		
    	try {
    		
    		HWDimensionManager.writeNBT(gameData);
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();
    		
    		//Write out to file
    		FileOutputStream fos = new FileOutputStream(saveFolder + "HostileWorlds.dat");
	    	CompressedStreamTools.writeCompressed(gameData, fos);
	    	fos.close();
	    	
	    	WorldDirector.writeAllPlayerNBT();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
	
	public static void readGameNBT() {
		
		dbg("Reading Hostile Worlds data");
		
		NBTTagCompound gameData = null;
		
		HWDimensionManager.registeredDimensions.clear();
		
		try {
			
			String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName();
			
			if ((new File(saveFolder + "HostileWorlds.dat")).exists()) {
				gameData = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + "HostileWorlds.dat"));
				
				//NBTTagList var14 = gameData.getTagList("playerData");
				HWDimensionManager.readNBT(gameData);
			}
			
			//dont read in player nbt, its read in on demand
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
	
	public static void writeChunkCoords(String prefix, ChunkCoordinates coords, NBTTagCompound nbt) {
		nbt.setInteger(prefix + "X", coords.posX);
		nbt.setInteger(prefix + "Y", coords.posY);
		nbt.setInteger(prefix + "Z", coords.posZ);
	}

	public static ChunkCoordinates readChunkCoords(String prefix, NBTTagCompound nbt) {
		return new ChunkCoordinates(nbt.getInteger(prefix + "X"), nbt.getInteger(prefix + "Y"), nbt.getInteger(prefix + "Z"));
	}
	
	public static void dbg(Object obj) {
		if (ModConfigFields.debugConsoleOutput) {
			//MinecraftServer.getServer().getLogAgent().logInfo(String.valueOf(obj));
			System.out.println(obj);
		}
	}
}
