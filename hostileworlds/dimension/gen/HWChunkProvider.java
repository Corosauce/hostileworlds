package hostileworlds.dimension.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

public class HWChunkProvider implements IChunkProvider
{
    private WorldServer worldObj;
    private Random random;
    private final boolean useStructures;
    private MapGenSchematics schematicGen = new MapGenSchematics();
    
    private Set chunksToUnload = new HashSet();
    private Chunk defaultEmptyChunk;
    private IChunkProvider currentChunkProvider;
    public IChunkLoader currentChunkLoader;

    /**
     * if this is false, the defaultEmptyChunk will be returned by the provider
     */
    public boolean loadChunkOnProvideRequest = true;
    private LongHashMap loadedChunkHashMap = new LongHashMap();
    private List loadedChunks = new ArrayList();
    /*private WorldServer worldObj;*/

    public HWChunkProvider(World par1World, long par2, boolean par4)
    {
        this.worldObj = (WorldServer)par1World;
        this.useStructures = par4;
        this.random = new Random(par2);
    }

    private void generate(byte[] par1ArrayOfByte)
    {
        int var2 = par1ArrayOfByte.length / 256;

        Random rand = new Random();
        
    	boolean genWallX = rand.nextInt(4) == 0;
    	boolean genWallZ = rand.nextInt(4) == 0;
    	
    	boolean makeStairs = rand.nextInt(4) == 0;
        
    	makeStairs = false;
    	
    	int layerSize = 8;
    	int layercount = 1; //dont go past max y!
    	
    	for (int var5 = 0; var5 < layerSize * layercount/*var2*/; ++var5) //y
        {
    		
    		
    		int curY = var5 % layerSize;
            
            if (curY == 0) {
            	genWallX = rand.nextInt(4) == 0;
                genWallZ = rand.nextInt(4) == 0;
            }
    		
	        for (int var3 = 0; var3 < 16; ++var3) //x
	        {
	            for (int var4 = 0; var4 < 16; ++var4) //z
	            {                
                    int var6 = 0;

                    if (var5 == 0) {
                        var6 = Block.bedrock.blockID;
                    } else if (curY == layerSize - 1) {
                    	var6 = Block.cobblestoneMossy.blockID;
                    	/*if (!makeStairs && var4 >= 6 && var4 <= 11 && var3 - curY == 0) {
                    		var6 = Block.cobblestoneMossy.blockID;
                    	}*/
                    
                    /*} else if (curY == 1) {
                        var6 = Block.cobblestoneMossy.blockID;*/
                    } else if (curY < layerSize && (var3 == 6 || var4 == 6 || var3 == 11 || var4 == 11) && (((genWallX || (var3 <= 6 || var3 >= 11)) && (genWallZ || (var4 <= 6 || var4 >= 11))))) {
                        var6 = Block.cobblestoneMossy.blockID;
                    } else if (makeStairs && var4 >= 6 && var4 <= 11 && var3 - curY == 0) {
                    	var6 = Block.cobblestoneMossy.blockID;
                    }

                    par1ArrayOfByte[var3 << 11 | var4 << 7 | var5] = (byte)var6;
                }
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int par1, int par2)
    {
        return this.provideChunk(par1, par2);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int par1, int par2)
    {
        byte[] var3 = new byte[32768];
        this.generate(var3);
        
        //System.out.println("GENERATING HW CHUNK: " + par1 + " - " + par2);

        //if (this.useStructures)
        //{
            this.schematicGen.generate(this, this.worldObj, par1, par2, var3);
        //}

        Chunk var4 = new Chunk(this.worldObj, var3, par1, par2);
            
        BiomeGenBase[] var5 = this.worldObj.getWorldChunkManager().loadBlockGeneratorData((BiomeGenBase[])null, par1 * 16, par2 * 16, 16, 16);
        byte[] var6 = var4.getBiomeArray();

        for (int var7 = 0; var7 < var6.length; ++var7)
        {
            var6[var7] = (byte)var5[var7].biomeID;
        }

        var4.generateSkylightMap();
        return var4;
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    public boolean chunkExists(int par1, int par2)
    {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3)
    {
        this.random.setSeed(this.worldObj.getSeed());
        long var4 = this.random.nextLong() / 2L * 2L + 1L;
        long var6 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)par2 * var4 + (long)par3 * var6 ^ this.worldObj.getSeed());

        if (this.useStructures)
        {
            //this.schematicGen.generateStructuresInChunk(this.worldObj, this.random, par2, par3);
        }
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean par1, IProgressUpdate par2IProgressUpdate)
    {
        return true;
    }

    /**
     * Unloads the 100 oldest chunks from memory, due to a bug with chunkSet.add() never being called it thinks the list
     * is always empty and will not remove any chunks.
     */
    public boolean unloadQueuedChunks()
    {
    	
    	if (true) return false;
    	
    	if (!this.worldObj.canNotSave)
        {
            for (ChunkCoordIntPair forced : this.worldObj.getPersistentChunks().keySet())
            {
                this.chunksToUnload.remove(ChunkCoordIntPair.chunkXZ2Int(forced.chunkXPos, forced.chunkZPos));
            }

            for (int var1 = 0; var1 < 100; ++var1)
            {
                if (!this.chunksToUnload.isEmpty())
                {
                    Long var2 = (Long)this.chunksToUnload.iterator().next();
                    Chunk var3 = (Chunk)this.loadedChunkHashMap.getValueByKey(var2.longValue());
                    var3.onChunkUnload();
                    this.safeSaveChunk(var3);
                    this.safeSaveExtraChunkData(var3);
                    this.chunksToUnload.remove(var2);
                    this.loadedChunkHashMap.remove(var2.longValue());
                    this.loadedChunks.remove(var3);
                    ForgeChunkManager.putDormantChunk(ChunkCoordIntPair.chunkXZ2Int(var3.xPosition, var3.zPosition), var3);
                    if(loadedChunks.size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.worldObj).size() == 0 && !DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId)) {
                        DimensionManager.unloadWorld(this.worldObj.provider.dimensionId);
                        return false;//currentChunkProvider.unload100OldestChunks();
                    }
                }
            }

            if (this.currentChunkLoader != null)
            {
                this.currentChunkLoader.chunkTick();
            }
        }

        return false;//this.currentChunkProvider.unload100OldestChunks();
    }
    
    private void safeSaveExtraChunkData(Chunk par1Chunk)
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                this.currentChunkLoader.saveExtraChunkData(this.worldObj, par1Chunk);
            }
            catch (Exception var3)
            {
                var3.printStackTrace();
            }
        }
    }

    /**
     * used by saveChunks, but catches any exceptions if the save fails.
     */
    private void safeSaveChunk(Chunk par1Chunk)
    {
        if (this.currentChunkLoader != null)
        {
            try
            {
                par1Chunk.lastSaveTime = this.worldObj.getTotalWorldTime();
                this.currentChunkLoader.saveChunk(this.worldObj, par1Chunk);
            }
            catch (IOException var3)
            {
                var3.printStackTrace();
            }
            catch (MinecraftException var4)
            {
                var4.printStackTrace();
            }
        }
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "FlatLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4)
    {
        BiomeGenBase var5 = this.worldObj.getBiomeGenForCoords(par2, par4);
        return var5 == null ? null : var5.getSpawnableList(par1EnumCreatureType);
    }

    /**
     * Returns the location of the closest structure of the specified type. If not found returns null.
     */
    public ChunkPosition findClosestStructure(World par1World, String par2Str, int par3, int par4, int par5)
    {
        return null;
    }
    
    public void unloadChunksIfNotNearSpawn(int par1, int par2)
    {
        if (this.worldObj.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(this.worldObj.provider.dimensionId))
        {
            ChunkCoordinates var3 = this.worldObj.getSpawnPoint();
            int var4 = par1 * 16 + 8 - var3.posX;
            int var5 = par2 * 16 + 8 - var3.posZ;
            short var6 = 128;

            if (var4 < -var6 || var4 > var6 || var5 < -var6 || var5 > var6)
            {
                this.chunksToUnload.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(par1, par2)));
            }
        }
        else
        {
            this.chunksToUnload.add(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(par1, par2)));
        }
    }

    public int getLoadedChunkCount()
    {
        return 0;
    }

	@Override
	public void recreateStructures(int var1, int var2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void func_104112_b()
    {
        if (this.currentChunkLoader != null)
        {
            this.currentChunkLoader.saveExtraData();
        }
    }
	
	
}
