package hostileworlds.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSourceInvasion extends BlockContainer
{
    public BlockSourceInvasion()
    {
        super(Material.rock);
    }

    public int tickRate()
    {
        return 90;
    }

    public void updateTick(World var1, int var2, int var3, int var4, Random var5) {}

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntitySourceInvasion();
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
}
