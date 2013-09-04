package hostileworlds;

import hostileworlds.ai.WorldDirector;
import hostileworlds.commands.CommandHW;
import hostileworlds.rts.RtsEngine;

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
	public static RtsEngine rts;
	
	public static World lastWorld = null;
	
	public static Test test;
	
    public ServerTickHandler(HostileWorlds mod_ZombieAwareness) {
    	mod = mod_ZombieAwareness;
    	
	}

	@Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.WORLDLOAD))) {
        	//System.out.println("RTSDBG: WORLDLOAD CALLED");
        	World world = (World)tickData[0];
        	if (world.provider.dimensionId == 0) {
        		//System.out.println("is remote? " + world.isRemote);
        		HostileWorlds.initTry();
        	}
        }
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
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD);
    }

    @Override
    public String getLabel() { return null; }
    

    public void onTickInGame()
    {
    	
    	if (wd == null) wd = new WorldDirector();
    	if (rts == null) rts = new RtsEngine();
    	
    	if (lastWorld != DimensionManager.getWorld(0)) {
    		lastWorld = DimensionManager.getWorld(0);
    		((ServerCommandManager)FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(new CommandHW());
    		//wd.resetDimData();
    	}
    	
    	wd.onTick();
    	rts.tickUpdate();
    	
    	/*if (test == null) test = new Test();
    	if (lastWorld.getWorldTime() % 20 == 0) test.tick();*/
    }
}
