package hostileworlds.commands;

import hostileworlds.ai.WorldDirector;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.block.TileEntityHWPortal;
import hostileworlds.dimension.HWTeleporter;
import hostileworlds.entity.EntityInvader;

import java.util.ArrayList;
import java.util.List;




import modconfig.ConfigMod;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import CoroAI.componentAI.ICoroAI;

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
						}
						
						int count = 1;
						
						if (var2.length > 2) {
							count = Integer.valueOf(var2[2]);
						}
	
						for (int i = 0; i < count; i++) {
							Entity ent = EntityList.createEntityByName(prefix + mobToSpawn, player.worldObj);
							
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
								System.out.println("Spawned: " + mobToSpawn);
							} else {
								System.out.println("failed to spawn");
								break;
							}
						}
						
					} else if (var2[0].equalsIgnoreCase("invasion")) {
						if (var2[1].equalsIgnoreCase("start")) {
							WorldDirector.getPlayerNBT(player.username).setInteger("HWInvasionCooldown", 20);
						} else if (var2[1].equalsIgnoreCase("stop")) {
							for (int i = 0; i < WorldDirector.curInvasions.get(player.dimension).size(); i++) {
								WorldEvent we = WorldDirector.curInvasions.get(player.dimension).get(i);
								if (we.mainPlayerName.equals(player.username)) {
									WorldDirector.curInvasions.get(player.dimension).remove(i);
									break;
								}
							}
						} else if (var2[1].equalsIgnoreCase("next")) {
							for (int i = 0; i < WorldDirector.curInvasions.get(player.dimension).size(); i++) {
								WorldEvent we = WorldDirector.curInvasions.get(player.dimension).get(i);
								if (we.mainPlayerName.equals(player.username)) {
									we.curCooldown = 20;
								}
							}
							//if (WorldDirector.curInvasions.get(player.dimension).size() > 0) WorldDirector.curInvasions.get(player.dimension).remove(0);
						}
					} else if (var2[0].equalsIgnoreCase("waveCount")) {
						String username = player.username;
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
						WorldDirector.getPlayerNBT(username).setInteger("numOfWavesSpawned", val);
						var1.sendChatToPlayer(username + "s waveCount set to " + val);
					} else if (var2[0].equalsIgnoreCase("boss")) {
						if (var2[1].equalsIgnoreCase("reset")) {
							if (player.dimension != 0) {
								TileEntity tEnt = player.worldObj.getBlockTileEntity(HWTeleporter.portalCoord.posX, HWTeleporter.portalCoord.posY, HWTeleporter.portalCoord.posZ);
								if (tEnt instanceof TileEntityHWPortal) {
									((TileEntityHWPortal) tEnt).bossEventOccurred = false;
								}
							}
						}
					} else if (var2[0].equalsIgnoreCase("get")) {
						if (var2.length > 1) {
							Object obj = ConfigMod.getField(getCommandName(), var2[1]);
							if (obj != null) {
								var1.sendChatToPlayer(var2[1] + " = " + obj);
							} else {
								var1.sendChatToPlayer("failed to get " + var2[1]);
							}
						}
					} else if (var2[0].equalsIgnoreCase("set")) {
						if (var2.length > 2) {
							
							String val = "";
							for (int i = 2; i < var2.length; i++) val += var2[i] + (i != var2.length-1 ? " " : "");
							if (ConfigMod.updateField(getCommandName(), var2[1], val)) {
								var1.sendChatToPlayer("set " + var2[1] + " to " + val);
							} else {
								var1.sendChatToPlayer("failed to set " + var2[1]);
							}
						} else {
							var1.sendChatToPlayer("set requires 3 parameters");
						}
					} else if (var2[0].equalsIgnoreCase("derp")) {
						int size = 96;
				    	int startX = 0;
				    	int startZ = 0;
				    	
				    	int x = 0;
				    	int y = 0;
				    	int z = 0;
				    	
				    	
				    	for (y = 0; y < 50; y++) {
				    		for (x = startX; x < size; x++) {
				    			for (z = startZ; z < size; z++) {
				    				player.worldObj.setBlock((int)(player.posX + 2 + x), (int)(player.posY + y), (int)(player.posZ + 2 + z), Block.cobblestoneMossy.blockID);
				    			}
				    		}
				    		startX += 1;
				    		startZ += 1;
				    		size -= 1;
				    	}
					}
					
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

}
