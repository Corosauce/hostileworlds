package hostileworlds.commands;

import hostileworlds.client.gui.GuiQuestListing;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import cpw.mods.fml.client.FMLClientHandler;

public class CommandHWClient extends CommandBase {

	@Override
	public String getCommandName() {
		return "hwc";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
				
			String prefix = "HostileWorlds.";
			String mobToSpawn = "InvaderZombieMiner";
			
			if (var2.length > 0) {
				if (var2[0].equalsIgnoreCase("quests")) {
					FMLClientHandler.instance().getClient().displayGuiScreen(new GuiQuestListing());
					//FMLClientHandler.instance().getClient().displayGuiScreen(new GuiEZConfig());
					
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
        return true;
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
