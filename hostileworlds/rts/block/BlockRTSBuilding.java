package hostileworlds.rts.block;

import hostileworlds.HostileWorlds;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRTSBuilding extends BlockContainer
{
    public BlockRTSBuilding(int var1)
    {
        super(var1, Material.circuits);
        setBlockBounds(0.05F, 0F, 0.05F, 0.95F, 0.1F, 0.95F);
    }

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityRTSBuilding();
    }
    
    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    @Override
    public boolean canCollideCheck(int par1, boolean par2) {
    	
    	return true;
    }
    
    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4,
    		EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
    	super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase,
    			par6ItemStack);
    	//if (!par1World.isRemote) {
	    	TileEntity tEnt = par1World.getBlockTileEntity(par2, par3, par4);
	    	if (tEnt instanceof TileEntityRTSBuilding) {
	    		((TileEntityRTSBuilding) tEnt).setBuildingAndMarkInitReady("command");
	    	}
    	//}
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                    EntityPlayer player, int idk, float what, float these, float are) {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        if (tileEntity == null || player.isSneaking()) {
        	return false;
        }
	    player.openGui(HostileWorlds.instance, 2, world, x, y, z);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
        super.breakBlock(world, x, y, z, par5, par6);
    }
}
