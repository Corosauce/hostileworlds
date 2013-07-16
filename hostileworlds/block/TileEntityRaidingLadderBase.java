package hostileworlds.block;

import hostileworlds.HostileWorlds;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRaidingLadderBase extends TileEntity {

	public int age = 0;
	public int maxAge = 3600;
	
	public TileEntityRaidingLadderBase() {
		//System.out.println("new raiding ladder base");
	}
	
	public void updateEntity()
    {
		super.updateEntity();
		if (!worldObj.isRemote) {
			age++;
		
			//System.out.println("update raiding ladder base - " + xCoord + " - " + yCoord + " - " + zCoord);
			
			if (age > maxAge) {
				breakLadders();
			}
		}
    }
	
	public void breakLadders() {
		int y = yCoord+1;
		int id = worldObj.getBlockId(xCoord, y, zCoord);
		int failCount = 0;
		System.out.println("REMOVING LADDERS!");
		while (failCount < 10 && y < 256) {
			if (id == HostileWorlds.blockRaidingLadder.blockID) {
				worldObj.setBlock(xCoord, y, zCoord, 0);
				failCount = 0;
			} else {
				failCount++;
			}
			y++;
		}
		worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
		worldObj.setBlock(xCoord, yCoord, zCoord, 0);
		
	}
}
