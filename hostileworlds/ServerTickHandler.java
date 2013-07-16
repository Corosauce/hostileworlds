package hostileworlds;

import hostileworlds.ai.WorldDirector;
import hostileworlds.commands.CommandHW;

import java.util.EnumSet;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.bt.Test;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ServerTickHandler implements ITickHandler
{
	
	public static HostileWorlds mod;
	public static WorldDirector wd;
	
	public static World lastWorld = null;
	
	public static Test test;
	
    public ServerTickHandler(HostileWorlds mod_ZombieAwareness) {
    	mod = mod_ZombieAwareness;
    	
	}

	@Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    	
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.SERVER)))
        {
        	onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.SERVER);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	
    	if (wd == null) wd = new WorldDirector();
    	
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandHW());
    		//wd.resetDimData();
    	}
    	
    	wd.onTick();
    	
    	/*if (test == null) test = new Test();
    	if (lastWorld.getWorldTime() % 20 == 0) test.tick();*/
    }
}
