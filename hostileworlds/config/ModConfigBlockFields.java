package hostileworlds.config;

import modconfig.IConfigCategory;
import net.minecraftforge.common.Configuration;

public class ModConfigBlockFields implements IConfigCategory {
	
	public static int blockIDStart = 3222;
	
	public static int itemIDStart = 5491;
    
	@Override
	public String getCategory() {
		return Configuration.CATEGORY_BLOCK;
	}

	@Override
	public String getConfigFileName() {
		return "HostileWorldsBlocks";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
