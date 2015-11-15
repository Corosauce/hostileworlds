package hostileworlds;

import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.entity.EntityMeteorite;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerPacket {
	
	//1.6.4 original usage was PMGloveCommand channel, but we only have 1 type of packet, so a packetCommand lookup isnt needed
	
	@SideOnly(Side.CLIENT)
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
	
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String packetCommand = nbt.getString("packetCommand");
			
			if (packetCommand.equals("Meteor")) {
				int state = nbt.getInteger("state");
				World world = getClientWorld();
				if (state == 0) {
					EntityMeteorite ent = new EntityMeteorite(world);
					ent.setEntityId(nbt.getInteger("entityID"));
	            	ent.setPosition(nbt.getFloat("x"), nbt.getFloat("y"), nbt.getFloat("z"));
	            	ent.setVelocity(nbt.getFloat("vecX"), nbt.getFloat("vecY"), nbt.getFloat("vecZ"));
	            	System.out.println("spawning client meteor: " + ent.getEntityId());
	            	world.addWeatherEffect(ent);
				} else if (state == 1) {
					int id = nbt.getInteger("entityID");
                	Entity ent = null;
                	
                	for (int i = 0; i < world.weatherEffects.size(); i++) {
                		Entity we = (Entity)world.weatherEffects.get(i);
                		if (we.getEntityId() == id) {
                			ent = we;
                			break;
                		}
                	}
                	
                	if (ent != null) {
                		ent.setPosition(nbt.getFloat("x"), nbt.getFloat("y"), nbt.getFloat("z"));
    	            	ent.setVelocity(nbt.getFloat("vecX"), nbt.getFloat("vecY"), nbt.getFloat("vecZ"));
                	} else {
                		System.out.println("received meteor update packet for non existing id: " + id);
                	}
				} else if (state == 2) {
					int id = nbt.getInteger("entityID");
                	Entity ent = null;
                	
                	for (int i = 0; i < world.weatherEffects.size(); i++) {
                		Entity we = (Entity)world.weatherEffects.get(i);
                		if (we.getEntityId() == id) {
                			ent = we;
                			break;
                		}
                	}
                	
                	if (ent != null) {
	                	System.out.println("killing client meteor: " + ent.getEntityId());
	                	if (ent instanceof EntityMeteorite) {
	                		((EntityMeteorite)ent).setDead();
	                	}
                	} else {
                		System.out.println("received meteor kill packet for non existing id: " + id);
                	}
				}
			} else if (packetCommand.equals("InvasionData")) {
				//cooldown
	        	//invasion data dimension
	        	//invasion data list size
	        	//invasion data listings
	        	WorldDirectorMultiDim.clientPlayersCooldown = nbt.getInteger("cooldown");
	        	WorldDirectorMultiDim.clientPlayerInvadeValue = nbt.getFloat("invadeValue");
	        	int dimID = nbt.getInteger("dimID");
	        	//int listingSize = nbt.getInteger("listSize");
	        	NBTTagCompound nbtData = nbt.getCompoundTag("invasionListing");
	        	
	        	WorldDirectorMultiDim.clientCurInvasions = new HashMap<Integer, ArrayList<WorldEvent>>();
	        	WorldDirectorMultiDim.clientCurInvasions.put(dimID, new ArrayList<WorldEvent>());
	        	
	        	//HostileWorlds.dbg("listingSize: " + listingSize);
	        	
	        	WorldDirectorMultiDim.clientCurInvasions.get(dimID).clear();
	        	
	        	Iterator it = nbtData.func_150296_c().iterator();
	            while (it.hasNext()) {
	            	String tagName = (String) it.next();
	            	NBTTagCompound entry = nbtData.getCompoundTag(tagName);
	            	WorldEvent wEvent = WorldEvent.newInvasionFromNBT(entry);
	        		WorldDirectorMultiDim.clientCurInvasions.get(dimID).add(wEvent);
	            }
	        	
	        	/*//REWORK TO ITERATE DATA IN NBT
	        	for (int i = 0; i < listingSize; i++) {
	        		//HostileWorlds.dbg("reading: " + i);
	        		NBTTagCompound nbt = Packet.readNBTTagCompound(dis);//CompressedStreamTools.readCompressed(dis);
	        		WorldEvent wEvent = WorldEvent.newInvasionFromNBT(nbt);
	        		WorldDirector.clientCurInvasions.get(dimID).add(wEvent);
	        	}*/
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP entP = ((NetHandlerPlayServer)event.handler).playerEntity;
		
		try {
			
			ByteBuf buffer = event.packet.payload();
			
	        //if ("PMGloveCommand".equals(packet.channel)) {
	        	int commandID = buffer.readInt();
	        	int slotID = buffer.readInt();
			//}
        	
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return CoroUtilEntity.getName(Minecraft.getMinecraft().thePlayer);
    }
	
}
