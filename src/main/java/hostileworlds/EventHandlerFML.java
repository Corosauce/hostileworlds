package hostileworlds;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.formation.Manager;
import CoroUtil.quest.PlayerQuestManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class EventHandlerFML {

	public static World lastWorld = null;
	
	@SubscribeEvent
	public void tickWorld(WorldTickEvent event) {
		if (event.phase == Phase.START) {
			HostileWorlds.initTry();
		}
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			ServerTickHandler.onTickInGame();
		}
		
	}
}
