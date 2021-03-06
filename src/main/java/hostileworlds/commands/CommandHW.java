package hostileworlds.commands;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.ai.tasks.TaskCallForHelp;
import hostileworlds.ai.tasks.TaskDigTowardsTarget;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityInvader;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.world.location.Stronghold;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import modconfig.ConfigMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import CoroPets.ai.BehaviorModifier;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.util.CoroUtil;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ManagedLocation;

public class CommandHW extends CommandBase {

	@Override
	public String getCommandName() {
		return "hw";
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
		List<String> list = new ArrayList<String>(ConfigMod.configLookup.get(getCommandName()).valsBoolean.keySet());
		list.addAll(ConfigMod.configLookup.get(getCommandName()).valsInteger.keySet());
		list.addAll(ConfigMod.configLookup.get(getCommandName()).valsString.keySet());
        return list;
    }

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
	
				String prefix = "HostileWorlds.";
				String mobToSpawn = "InvaderZombieMiner";
				
				if (var2.length > 0) {
					if (var2[0].equalsIgnoreCase("spawn")) {
						if (var2[1].equalsIgnoreCase("miner")) {
							mobToSpawn = "InvaderZombieMiner";
						} else if (var2[1].equalsIgnoreCase("zombie")) {
							mobToSpawn = "InvaderZombie";
						} else if (var2[1].equalsIgnoreCase("fireworm")) {
							mobToSpawn = "EntityWormFire";
						} else if (var2[1].equalsIgnoreCase("sandworm")) {
							mobToSpawn = "EntityWormSand";
						} else if (var2[1].equalsIgnoreCase("boss")) {
							mobToSpawn = "BlockWielderZombie";
						} else {
							mobToSpawn = var2[1];
							prefix = "";
						}
						
						int count = 1;
						
						if (var2.length > 2) {
							count = Integer.valueOf(var2[2]);
						}
	
						for (int i = 0; i < count; i++) {
							Entity ent = EntityList.createEntityByName(prefix + mobToSpawn, player.worldObj);
							
							if (ent == null) ent = EntityList.createEntityByName(mobToSpawn, player.worldObj);
							
							if (ent != null) {
								
								double dist = 4D;
								
								double finalX = player.posX - (Math.sin(player.rotationYaw * 0.01745329F) * dist);
								double finalZ = player.posZ + (Math.cos(player.rotationYaw * 0.01745329F) * dist);
								
								double finalY = player.posY;
								
								ent.setPosition(finalX, finalY, finalZ);
					
								if (ent instanceof EntityInvader && ((ICoroAI) ent).getAIAgent().jobMan.priJob instanceof JobGroupHorde) {
									((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates((int)player.posX, (int)player.boundingBox.minY, (int)player.posZ);
								}
								
								if (ent instanceof ICoroAI) ((ICoroAI) ent).getAIAgent().spawnedOrNBTReloadedInit();
								
								//temp
								//ent.setPosition(69, player.worldObj.getHeightValue(69, 301), 301);
								//((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates(44, player.worldObj.getHeightValue(44, 301), 301);
								
								player.worldObj.spawnEntityInWorld(ent);
								if (ent instanceof EntityLiving) {
									((EntityLiving) ent).onSpawnWithEgg(null);
								}
								System.out.println("Spawned: " + mobToSpawn);
							} else {
								System.out.println("failed to spawn");
								break;
							}
						}
						
					} else if (var2[0].equalsIgnoreCase("invasion")) {
						if (var2[1].equalsIgnoreCase("start")) {
							if (ModConfigFields.timeBasedInvasionsInstead) {
								WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(player)).setInteger("HWInvasionCooldown", 20);
							} else {
								WorldDirectorMultiDim.getPlayerNBT(CoroUtilEntity.getName(player)).setFloat("harvested_Rating", WorldDirectorMultiDim.getHarvestRatingInvadeThreshold());
							}
						} else if (var2[1].equalsIgnoreCase("stop") || var2[1].equalsIgnoreCase("end")) {
							for (int i = 0; i < WorldDirectorMultiDim.curInvasions.get(player.dimension).size(); i++) {
								WorldEvent we = WorldDirectorMultiDim.curInvasions.get(player.dimension).get(i);
								if (we.mainPlayerName.equals(CoroUtilEntity.getName(player))) {
									WorldDirectorMultiDim.curInvasions.get(player.dimension).remove(i);
									break;
								}
							}
						} else if (var2[1].equalsIgnoreCase("next")) {
							for (int i = 0; i < WorldDirectorMultiDim.curInvasions.get(player.dimension).size(); i++) {
								WorldEvent we = WorldDirectorMultiDim.curInvasions.get(player.dimension).get(i);
								if (we.mainPlayerName.equals(CoroUtilEntity.getName(player))) {
									we.curCooldown = 20;
								}
							}
							//if (WorldDirector.curInvasions.get(player.dimension).size() > 0) WorldDirector.curInvasions.get(player.dimension).remove(0);
						}
					} else if (var2[0].equalsIgnoreCase("waveCount")) {
						String username = CoroUtilEntity.getName(player);
						int val = 0;
						if (var2.length > 3) {
							username = var2[2];
							val = Integer.valueOf(var2[3]);
						} else if (var2.length > 2) {
							val = Integer.valueOf(var2[2]);
						}
						if (var2[1].equalsIgnoreCase("reset")) {
							
						} else if (var2[1].equalsIgnoreCase("set")) {
							
						}
						WorldDirectorMultiDim.getPlayerNBT(username).setInteger("numOfWavesSpawned", val);
						CoroUtil.sendPlayerMsg((EntityPlayerMP) player, username + "s waveCount set to " + val);
					/*} else if (var2[0].equalsIgnoreCase("boss")) {
						if (var2[1].equalsIgnoreCase("reset")) {
							if (player.dimension != 0) {
								TileEntity tEnt = player.worldObj.getBlockTileEntity(HWTeleporter.portalCoord.posX, HWTeleporter.portalCoord.posY, HWTeleporter.portalCoord.posZ);
								if (tEnt instanceof TileEntityHWPortal) {
									((TileEntityHWPortal) tEnt).bossEventOccurred = false;
								}
							}
						}*/
					} else if (var2[0].equalsIgnoreCase("get")) {
						if (var2.length > 1) {
							Object obj = ConfigMod.getField(getCommandName(), var2[1]);
							if (obj != null) {
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, var2[1] + " = " + obj);
							} else {
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "failed to get " + var2[1]);
							}
						}
					} else if (var2[0].equalsIgnoreCase("set")) {
						if (var2.length > 2) {
							
							String val = "";
							for (int i = 2; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
							if (ConfigMod.updateField(getCommandName(), var2[1], val)) {
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "set " + var2[1] + " to " + val);
							} else {
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "failed to set " + var2[1]);
							}
						} else {
							CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "set requires 3 parameters");
						}
					} else if (var2[0].equalsIgnoreCase("derp")) {
						
						EntityMeteorite.genBuilding(player.worldObj, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY + 2), MathHelper.floor_double(player.posZ), 22);
						
					} else if (var2[0].equalsIgnoreCase("derp2")) {
						
						
						
						int size = 22;
						int origSize = size;
				    	int startX = 0;
				    	int startZ = 0;
				    	
				    	int x = 0;
				    	int y = 0;
				    	int z = 0;
				    	
				    	
				    	
				    	for (y = 0; y < 50; y++) {
				    		for (x = startX; x <= size; x++) {
				    			for (z = startZ; z <= size; z++) {
				    				player.worldObj.setBlock((int)(player.posX + x - origSize/2), (int)(player.posY + y + 5), (int)(player.posZ + z - origSize/2), HostileWorlds.blockBloodyCobblestone);
				    			}
				    		}
				    		startX += 1;
				    		startZ += 1;
				    		size -= 1;
				    		
				    		if (size - startX < 5) {
				    			break;
				    		}
				    	}
					} else if (var2[0].equals("stronghold")) {
						
						//for now, to speed up production, use CoroUtil world director for its managed locations, and HW multi dimensional WorldDirector for ... for what?
						
						int x = MathHelper.floor_double(player.posX);
						int z = MathHelper.floor_double(player.posZ);
						int y = player.worldObj.getHeightValue(x, z);
						Stronghold village = new Stronghold();
						
						WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(player.worldObj);
						//questionable ID setting
						int newID = wd.lookupTickingManagedLocations.size();
						village.initData(newID, player.worldObj.provider.dimensionId, new ChunkCoordinates(x, y, z));
						village.initFirstTime();
						wd.addTickingLocation(village);
						//StructureObject bb = StructureMapping.newTown(player.worldObj.provider.dimensionId, "command", new ChunkCoordinates(x, y, z));
						//bb.init();
						//bb.location.initFirstTime();
					} else if (var2[0].equals("regen")) {
						WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(player.worldObj);
						Iterator it = wd.lookupTickingManagedLocations.values().iterator();
						while (it.hasNext()) {
							ManagedLocation ml = (ManagedLocation) it.next();
							ml.initFirstTime();
						}
					} else if (var2[0].equals("infotest")) {
						Chunk chunk = player.worldObj.getChunkFromBlockCoords(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posZ));
						
						System.out.println("inhabited time: " + chunk.inhabitedTime);
					} else if (var2[0].equals("testdig")) {
						TaskDigTowardsTarget task = new TaskDigTowardsTarget();
						
						System.out.println("ENHANCE!");
						BehaviorModifier.enhanceZombiesToDig(DimensionManager.getWorld(0), Vec3.createVectorHelper(player.posX, player.posY, player.posZ), new Class[] { TaskDigTowardsTarget.class, TaskCallForHelp.class }, 5);
					}
					
					
					/* else if (var2[0].equalsIgnoreCase("rts")) {
						if (var2[1].equalsIgnoreCase("new")) {
							RtsEngine.teams.teamNew(player.worldObj.provider.dimensionId, new ChunkCoordinates((int)(player.posX), (int)(player.posY), (int)(player.posZ)));
						} else if (var2[1].equalsIgnoreCase("reset") || var2[1].equalsIgnoreCase("clear") ) {
							RtsEngine.teams.teamRemoveAll();
						}
					}*/
					
				}
				
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Hostile Worlds command");
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
