package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.AreaConverter;
import hostileworlds.dimension.HWDimensionManager;
import hostileworlds.dimension.HWTeleporter;
import hostileworlds.entity.monster.ZombieBlockWielder;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import particleman.entities.EntityParticleControllable;
import CoroAI.PFQueue;
import CoroAI.componentAI.ICoroAI;

import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityHWPortal extends TileEntity
{
	//Portal linking
	public ChunkCoordinates overWorldCoord = null; //used for catacomb tile ent
	public int destDim = -1; //used for overworld tile ent
	
	//Multi tile entity linking
	public boolean mainTE = false;
	public AreaConverter areaConverter = null;
	public ChunkCoordinates mainTECoord = null;
	public int numOfWavesSpawned = 0; //counts how many waves have spawned from portal, used for difficulty scaling
	public int age = 0;
	
	public int rotation = 0;
	public int type = 0;
	
	public boolean waitingForNBT = true;
	public boolean init = false;
	public boolean hasRegisteredDimension = false;
	
	//Used for overworld portal heath for destruction
	public int health = 50;
	
	//User for catacombs boss event marking
	public boolean bossEventOccurred = false;
	
	private Ticket chunkTicket;
	
	public boolean readyToPlacePortal = false;
	public boolean placedPortal = false;
	
	public TileEntityHWPortal(Boolean parWaitingOnNBT) {
		waitingForNBT = parWaitingOnNBT;
		
	}
	
	public TileEntityHWPortal() {
		/*if (worldObj.provider.dimensionId == 0) {
			overWorldCoord = new ChunkCoordinates(xCoord, yCoord, zCoord);
		}*/
	}
	
	public void takeDamage(EntityParticleControllable ent) {
		
		if (mainTE) {
		
			//if being shot
			if (ent.state == 1 && worldObj.provider.dimensionId == 0) {
				health--;
				ent.setDead();
				if (health <= 0) {
					worldObj.setBlock(xCoord, yCoord, zCoord, 0);
					worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
					invalidate();
				}
			}
		} else {
			getMainTileEntity().takeDamage(ent);
		}
	}
	
	public void blockBreak() {
		if (mainTE) {
			System.out.println("releasing chunkload ticket for dim: " + worldObj.provider.dimensionId);
			ForgeChunkManager.releaseTicket(chunkTicket);
			if (worldObj.provider.dimensionId == 0) {
				HWDimensionManager.deleteDimension(destDim);
				
				World world = DimensionManager.getWorld(destDim);
				
				if (world != null) {
					
					TileEntity tEnt = world.getBlockTileEntity(HWTeleporter.portalCoord.posX, HWTeleporter.portalCoord.posY, HWTeleporter.portalCoord.posZ);
					
					if (tEnt instanceof TileEntityHWPortal) {
						TileEntityHWPortal portal = ((TileEntityHWPortal) tEnt).getMainTileEntity();
						
						world.setBlock(portal.xCoord, portal.yCoord, portal.zCoord, 0);
						world.removeBlockTileEntity(portal.xCoord, portal.yCoord, portal.zCoord);
					}
					
				}
			}
			chunkTicket = null;
		}
		
		//System.out.println("removing tile entity");
		worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		blockBreak();
	}
	
	@Override
	public void updateEntity()
    {
		
		//World world = DimensionManager.getWorld(destDim);
		
		//if (!waitingOnPF) {
		/*if (world != null) {
			((WorldServer)world).theChunkProviderServer.provideChunk(0, 0);
		}*/
		
		if (mainTE && worldObj.provider.dimensionId != 0) {
			//System.out.println("dim: " + worldObj.provider.dimensionId);
			
			if (chunkTicket != null) {
				//ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
				
			}
		}
		
		if (!worldObj.isRemote) {
			
			if (!placedPortal && readyToPlacePortal) {
				World world = DimensionManager.getWorld(destDim);
				
				//if (!waitingOnPF) {
				if (world != null/* && !((WorldServer)world).theChunkProviderServer.chunkExists(0, 0)*/) {
					
					//System.out.println("loading chunk 0,0 for dim " + world.provider.dimensionId);
					//((WorldServer)world).theChunkProviderServer.loadChunk(0, 0);
					((WorldServer)world).theChunkProviderServer.provideChunk(0, 0);
					
					((WorldServer)world).theChunkProviderServer.loadChunk(1, 0);
					((WorldServer)world).theChunkProviderServer.loadChunk(0, 1);
					
					System.out.println("CREATING PORTAL FOR: " + destDim);
					HWTeleporter tele = new HWTeleporter((WorldServer)worldObj);
					tele.worldDest = (WorldServer)world;
					//send useless coords because they're overridden anyways
					tele.makePortal(0, 0, 0, this, null);
					
					placedPortal = true;
					readyToPlacePortal = false;
				} else {
					System.out.println("SERIOUS ERROR, DEST DIM IS NULL, CANT MAKE CATACOMB PORTAL IN: " + destDim);
				}
			}
			
			age++;
			if (worldObj.provider.dimensionId == 0) {
				if (mainTE) {
					//System.out.println(xCoord);
				}
				//System.out.println("ditto");
			}
			if (mainTE) {
				//System.out.println("dim: " + worldObj.provider.dimensionId + " - " + xCoord + " - " + zCoord);
				if (!init) {
					init = true;
					
					if (chunkTicket == null) {
						System.out.println(this + " - REQUESTING CHUNKLOAD TICKET");
						chunkTicket = ForgeChunkManager.requestTicket(HostileWorlds.instance, worldObj, Type.NORMAL);
					}
					if (chunkTicket == null) {
						System.out.println("FAILED TO GET CHUNKLOAD TICKET, ABORTING! - HW Portal");
						return;
					}
					chunkTicket.getModData().setInteger("portalX", xCoord);
					chunkTicket.getModData().setInteger("portalY", yCoord);
					chunkTicket.getModData().setInteger("portalZ", zCoord);
					ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
					
					forceChunkLoading(chunkTicket);
				}
				
				if (!hasRegisteredDimension && !waitingForNBT && worldObj.provider.dimensionId == 0) {
					
					if (destDim == -1) {
						destDim = DimensionManager.getNextFreeDimId();
						try {
							DimensionManager.initDimension(destDim);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
					if (DimensionManager.getWorld(destDim) == null) {
						
						System.out.println(this + ": registering dimension: " + destDim);
						
						HWDimensionManager.addDimension(destDim);
						//world = null;
						
						readyToPlacePortal = true;
					}
					
					hasRegisteredDimension = true;
				}
				
				if (areaConverter == null) {
					if (mainTECoord != null && worldObj.provider.dimensionId == 0) {
						areaConverter = new AreaConverter(worldObj, mainTECoord);
						areaConverter.start();
					}
				} else {
					areaConverter.onTick();
				}
				
				if (worldObj.getWorldTime() % 100 == 0) {
					if (worldObj.provider.dimensionId != 0) {
						List entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord).expand(120D, 120D, 120D));
						
						for (int i = 0; i < entities.size(); i++) {
							EntityLiving ent = (EntityLiving)entities.get(i);
							if (ent.getNavigator().noPath() && (!(ent instanceof ICoroAI) || (!(ent instanceof ZombieBlockWielder) && ((ICoroAI)ent).getAIAgent().entityToAttack == null))) {
								ent.entityAge = 0;
								PFQueue.getPath(ent, xCoord, yCoord-1, zCoord, 64F);
							}
						}
						 
					}
					if (chunkTicket != null) forceChunkLoading(chunkTicket);
				}
			}
			if (mainTECoord == null && age > 10) {
				getMainTileEntity();
			}
		}
    }
	
	public void forceChunkLoading(Ticket ticket) {
		if (chunkTicket == null) {
			chunkTicket = ticket;
		}

		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
		ChunkCoordIntPair portalChunk = new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4);
		chunks.add(portalChunk);
		ForgeChunkManager.forceChunk(ticket, portalChunk);

		int radius = 1;
		
		for (int chunkX = (xCoord >> 4) - radius; chunkX <= (xCoord >> 4) + radius; chunkX++) {
			for (int chunkZ = (zCoord >> 4) - radius; chunkZ <= (zCoord >> 4) + radius; chunkZ++) {
				//System.out.println("dim: " + worldObj.provider.dimensionId + ", force loading chunk: " + chunkX + ", " + chunkZ);
				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
				ForgeChunkManager.forceChunk(ticket, chunk);
				chunks.add(chunk);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 64*64;
    }
	
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
		
        super.readFromNBT(par1NBTTagCompound);
        try {
        	//notice how you're not setting mainTE, intentional?
        	if (par1NBTTagCompound.getBoolean("isMainTE")) {
        		
        		bossEventOccurred = par1NBTTagCompound.getBoolean("bossEventOccurred");
        		
	        	int dim = par1NBTTagCompound.getInteger("destDim");
	        	//anti set to 0 protection
		        if (dim != 0) this.destDim = dim;
		        
		        overWorldCoord = new ChunkCoordinates(par1NBTTagCompound.getInteger("overWorldCoordX"), par1NBTTagCompound.getInteger("overWorldCoordY"), par1NBTTagCompound.getInteger("overWorldCoordZ"));
		        if (overWorldCoord.posY == 0 && dim == -1) {
		        	System.out.println("CRITICAL READ ERROR, overWorldCoord.posY = 0!!!");
		        	overWorldCoord = null;
		        }
		        
		        
		        NBTTagCompound converter = par1NBTTagCompound.getCompoundTag("areaConverter");
		        
		        if (converter != null) {
		        	areaConverter = new AreaConverter(worldObj, converter);
		        }
		        waitingForNBT = false;
        	}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
	
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        try {
        	if (mainTE) {
        		par1NBTTagCompound.setBoolean("isMainTE", true);
		        par1NBTTagCompound.setInteger("destDim", this.destDim);
		        par1NBTTagCompound.setBoolean("bossEventOccurred", bossEventOccurred);
		        
		        
		        if (overWorldCoord != null) {
			        par1NBTTagCompound.setInteger("overWorldCoordX", overWorldCoord.posX);
			        par1NBTTagCompound.setInteger("overWorldCoordY", overWorldCoord.posY);
			        par1NBTTagCompound.setInteger("overWorldCoordZ", overWorldCoord.posZ);
		        } else {
		        	if (worldObj.provider.dimensionId != 0) {
		        		System.out.println("CRITICAL write error, overWorldCoord is null - " + this);
		        	}
		        }
		        
		        
		        if (worldObj.provider.dimensionId == 0) {
			        if (areaConverter != null) {
				        NBTTagCompound data = new NBTTagCompound();
				        areaConverter.writeToNBT(data);
				        par1NBTTagCompound.setCompoundTag("areaConverter", data);
			        } else {
			        	System.out.println("critical write error, area converter is null - " + this);
			        }
		        }
        	} else {
        		par1NBTTagCompound.setBoolean("isMainTE", false);
        	}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    public TileEntityHWPortal getMainTileEntity() {
    	
    	TileEntityHWPortal portal = null;
    	
    	if (mainTECoord != null) {
    		TileEntity te = worldObj.getBlockTileEntity(mainTECoord.posX, mainTECoord.posY, mainTECoord.posZ);
    		if (te instanceof TileEntityHWPortal) {
    			portal = (TileEntityHWPortal)te;
    		}
    	}
    	
    	if (portal != null) {
    		return portal;
    	} else {
    		return findMainTileEntity();
    	}
    }
    
    private TileEntityHWPortal findMainTileEntity() {
    	int curX = xCoord;
    	int curY = yCoord;
    	int curZ = zCoord;
    	
    	TileEntityHWPortal portal = this;
    	
    	while (portal != null) {
    		if (worldObj.getBlockTileEntity(curX+1, curY, curZ) instanceof TileEntityHWPortal) {
    			portal = (TileEntityHWPortal)worldObj.getBlockTileEntity(++curX, curY, curZ);
    			continue;
    		} else if (worldObj.getBlockTileEntity(curX, curY+1, curZ) instanceof TileEntityHWPortal) {
    			portal = (TileEntityHWPortal)worldObj.getBlockTileEntity(curX, ++curY, curZ);
    			continue;
    		} if (worldObj.getBlockTileEntity(curX, curY, curZ+1) instanceof TileEntityHWPortal) {
    			portal = (TileEntityHWPortal)worldObj.getBlockTileEntity(curX, curY, ++curZ);
    			continue;
    		} else {
    			break;
    		}
    	}
    	
    	if (curX == xCoord && curY == yCoord && curZ == zCoord) mainTE = true;
    	mainTECoord = new ChunkCoordinates(curX, curY, curZ);
    	
    	return portal;
    }
   
    /*@Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
        this.readFromNBT(pkt.customParam1);
    }
   
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, var1);
    }*/
	
}
