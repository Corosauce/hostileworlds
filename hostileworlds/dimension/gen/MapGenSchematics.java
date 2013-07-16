package hostileworlds.dimension.gen;

import java.io.File;

import hostileworlds.HostileWorlds;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import build.BuildServerTicks;
import build.world.BuildJob;

public class MapGenSchematics extends MapGenBase
{
    private float[] field_75046_d = new float[1024];

    /**
     * Recursively called by generate() (generate) and optionally by itself.
     */
    @Override
    protected void recursiveGenerate(World par1World, int par2, int par3, int par4, int par5, byte[] par6ArrayOfByte)
    {
    	
    	
    	
    }
    
    @Override
    public void generate(IChunkProvider par1IChunkProvider, World par2World, int par3, int par4, byte[] par5ArrayOfByte)
    {
    	int freqTemple = 8;
    	
    	if (par3 == -3 && par4 == 3) {
    		genTemple(par2World, par3, par4, par5ArrayOfByte);
    	}
    	
    	if (par3 % freqTemple == 0 && par4 % freqTemple == 0) {
    		//genTemple(par2World, par3, par4, par5ArrayOfByte);
    	}
    }
    
    public void genTemple(World par1World, int par2, int par3, byte[] par6ArrayOfByte) {
    	
    	//things to consider
    	//structure size vs frequency
    	//center the build schematic?
    	//generation on top of catacombs
    	
    	int baseY = 8;
    	
    	int x = 0;
    	int y = 0;
    	int z = 0;
    	
    	int id = Block.cobblestoneMossy.blockID;
    	
    	/*for (y = baseY; y < 50; y++) {
    		par6ArrayOfByte[x << 11 | z << 7 | y] = (byte)id;
    	}*/
    	
    	
    	
    	
    	
    	int buildID = -99;
    	
    	BuildJob bj = new BuildJob(-99, par2 * 16, baseY, par3 * 16, HostileWorlds.getSaveFolderPath() + "HWSchematics" + File.separator + "Temple");
		bj.build.dim = par1World.provider.dimensionId;
		BuildServerTicks.buildMan.addBuild(bj);
    	
    }
}
