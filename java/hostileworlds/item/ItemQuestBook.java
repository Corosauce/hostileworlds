package hostileworlds.item;

import hostileworlds.client.gui.GuiQuestListing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemQuestBook extends Item {
	
	public ItemQuestBook() {
		super();
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		
		if (par2World.isRemote) {
			openGUI();
		}
		
		return par1ItemStack;
	}
	
	@SideOnly(Side.CLIENT)
	public void openGUI() {
		FMLClientHandler.instance().getClient().displayGuiScreen(new GuiQuestListing());
	}

}
