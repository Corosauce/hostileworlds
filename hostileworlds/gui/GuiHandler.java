package hostileworlds.gui;

import hostileworlds.block.TileEntityFactory;
import hostileworlds.block.TileEntityItemTurret;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == 0) {
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityItemTurret){
	            return new ContainerItemTurret(player.inventory, (TileEntityItemTurret) tileEntity);
			}
		} else if (ID == 1) {
			//factory
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityFactory){
	            return new ContainerFactory(player.inventory, (TileEntityFactory) tileEntity);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (ID == 0) {
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
	        if(tileEntity instanceof TileEntityItemTurret){
	            return new GuiItemTurret(player.inventory, (TileEntityItemTurret) tileEntity);
	        }
		} else if (ID == 1) {
			//factory
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
	        if(tileEntity instanceof TileEntityFactory){
	            return new GuiFactory(player.inventory, (TileEntityFactory) tileEntity);
	        }
		}
        return null;
	}

}
