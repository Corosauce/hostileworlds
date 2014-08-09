package hostileworlds;

import hostileworlds.ai.WorldDirector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ServerTickHandler
{
	
	public static WorldDirector wd;
	//public static RtsEngine rts;
	
	public static World lastWorld = null;

    public static void onTickInGame()
    {
    	
    	if (wd == null) wd = new WorldDirector();
    	//if (rts == null) rts = new RtsEngine();
    	
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		//((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandHW());
    		//wd.resetDimData();
    	}
    	
    	wd.onTick();
    	//rts.tickUpdate();
    	
    	/*if (test == null) test = new Test();
    	if (lastWorld.getWorldTime() % 20 == 0) test.tick();*/
    }
}
