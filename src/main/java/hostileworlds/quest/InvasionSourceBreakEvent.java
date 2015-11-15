package hostileworlds.quest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import CoroUtil.quest.quests.BreakBlockQuest;

public class InvasionSourceBreakEvent extends BreakBlockQuest {

	public InvasionSourceBreakEvent() {
		questType = "breakInvasionBlock";
		modOwner = "hw";
	}
	
	@Override
	public void initCustomData(ChunkCoordinates parCoords, Block parBlock) {
		super.initCustomData(parCoords, parBlock);
	}
	
	@Override
	public String getTitle() {
		return "Stop Future Invasions";
	}
	
	@Override
	public List<String> getInstructions(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		/*String itemName = blockType;
		Block item = CoroUtilBlock.getBlockByName(itemName);
		if (item != null) {
			ItemStack is = new ItemStack(item);
			itemName = is.getDisplayName();
		}*/
		//String str = "";
		EntityPlayer entP = playerQuests.getPlayer();
		if (entP != null) {
			int relX = (blockCoords.posX-MathHelper.floor_double(entP.posX));
			int relY = (blockCoords.posY-MathHelper.floor_double(entP.posY));
			int relZ = (blockCoords.posZ-MathHelper.floor_double(entP.posZ));
			parList.add("Destroy the block holding the portal open ");
			parList.add(Math.abs(relX) + " blocks to the " + ((relX > 0) ? "East" : "West")
					+ ", " + Math.abs(relZ) + " to the " + ((relZ > 0) ? "South" : "North")
					+ ", " + Math.abs(relY) + " " + ((relY > 0) ? "Up" : "Down"));
		} else {
			parList.add("Destroy the block holding the portal open at ");
			parList.add(blockCoords.posX + ", " + blockCoords.posY + ", " + blockCoords.posZ);
		}
		return parList;
	}
	
	@Override
	public List<String> getInfoProgress(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		String str = "";
		parList.add(str);
		return parList;
	}
	
}
