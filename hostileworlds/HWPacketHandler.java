package hostileworlds;

import hostileworlds.ai.WorldDirector;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.dimension.HWDimensionManager;
import hostileworlds.entity.EntityMeteorite;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HWPacketHandler implements IPacketHandler
{
    public HWPacketHandler()
    {
    }

    @SideOnly(Side.CLIENT)
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
    
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
	        if ("Meteor".equals(packet.channel)) {
                int state = dis.readInt();
                //World world = DimensionManager.getWorld(((EntityPlayer)player).dimension);
                World world = getClientWorld();
                if (state == 0) { //spawning: id, x, y, z, type  
                	EntityMeteorite ent = new EntityMeteorite(world);
                	ent.entityId = dis.readInt();
                	ent.setPosition(dis.readFloat(), dis.readFloat(), dis.readFloat());
            		ent.setVelocity(dis.readFloat(), dis.readFloat(), dis.readFloat());
                	/*ent.posX = dis.readFloat();
                	ent.posY = dis.readFloat();
                	ent.posZ = dis.readFloat();*/
                	//ent.entConf = (WeatherEntityConfig)WeatherMod.weatherEntTypes.get(dis.readInt());
                	System.out.println("spawning client meteor: " + ent.entityId);
                	world.addWeatherEffect(ent);
                } else if (state == 1) { //updating: id, posx, posy, posz, velx, vely, velz 
                	int id = dis.readInt();
                	Entity ent = null;// = WeatherMod.proxy.getEntByID(id);
                	
                	for (int i = 0; i < world.weatherEffects.size(); i++) {
                		Entity we = (Entity)world.weatherEffects.get(i);
                		if (we.entityId == id) {
                			ent = we;
                			break;
                		}
                	}
                	
                	//System.out.println("syncing client meteor: " + id);
                	//if (ent instanceof EntTornado) {
                	if (ent != null) {
                		ent.setPosition(dis.readFloat(), dis.readFloat(), dis.readFloat());
                		ent.setVelocity(dis.readFloat(), dis.readFloat(), dis.readFloat());
                	} else {
                		System.out.println("received meteor update packet for non existing id: " + id);
                	}
                	//}
                } else if (state == 2) { //kill: id
                	int id = dis.readInt();
                	Entity ent = null;// = WeatherMod.proxy.getEntByID(id);
                	
                	for (int i = 0; i < world.weatherEffects.size(); i++) {
                		Entity we = (Entity)world.weatherEffects.get(i);
                		if (we.entityId == id) {
                			ent = we;
                			break;
                		}
                	}
                	
                	if (ent != null) {
	                	System.out.println("killing client meteor: " + ent.entityId);
	                	if (ent instanceof EntityMeteorite) {
	                		((EntityMeteorite)ent).setDead();
	                	}
                	} else {
                		System.out.println("received meteor kill packet for non existing id: " + id);
                	}
                }
	        } else if ("Dimension".equals(packet.channel)) {
	            try
	            {
	            	
	            	//1 int and an int list, first is dimension list size, then the list is the id of each dimension to register, keep in mind client receive should check if it already has it registered as it processes list
	            	
	            	int dimSize = dis.readInt();
	            	
	            	System.out.println("HW Receiving " + dimSize + " dimensions to register");
	            	
	            	for (int i = 0; i < dimSize; i++) {
	            		HWDimensionManager.addDimension(dis.readInt());
	            	}
	            	
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        } else if ("InvasionData".equals(packet.channel)) {
	        	//cooldown
	        	//invasion data dimension
	        	//invasion data list size
	        	//invasion data listings
	        	WorldDirector.clientPlayersCooldown = dis.readInt();
	        	int dimID = dis.readInt();
	        	int listingSize = dis.readInt();
	        	
	        	WorldDirector.clientCurInvasions = new HashMap<Integer, ArrayList<WorldEvent>>();
	        	WorldDirector.clientCurInvasions.put(dimID, new ArrayList<WorldEvent>());
	        	
	        	//HostileWorlds.dbg("listingSize: " + listingSize);
	        	
	        	WorldDirector.clientCurInvasions.get(dimID).clear();
	        	for (int i = 0; i < listingSize; i++) {
	        		//HostileWorlds.dbg("reading: " + i);
	        		NBTTagCompound nbt = Packet.readNBTTagCompound(dis);//CompressedStreamTools.readCompressed(dis);
	        		WorldEvent event = WorldEvent.newInvasionFromNBT(nbt);
	        		WorldDirector.clientCurInvasions.get(dimID).add(event);
	        	}
	        	
	        }
        } catch (Exception ex) {
        	HostileWorlds.dbg("ERROR HANDLING HW PACKETS");
            ex.printStackTrace();
        }
    }
}
