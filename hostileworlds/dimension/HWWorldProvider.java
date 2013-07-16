package hostileworlds.dimension;

import hostileworlds.HostileWorlds;
import hostileworlds.dimension.gen.HWChunkProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HWWorldProvider extends WorldProvider
{
    /**
     * creates a new world chunk manager for WorldProvider
     */
	@Override
    public void registerWorldChunkManager()
    {
        this.worldChunkMgr = new HWWorldChunkManager(HWBiomeGen.base, 0.5F, 0.0F);
        //this.dimensionId = HostileWorlds.instance.dimIDCatacombs;
        this.hasNoSky = false;
    }

    /**
     * Returns the chunk provider back for the world provider
     */
    @Override
    public IChunkProvider createChunkGenerator()
    {
        return new HWChunkProvider(this.worldObj, this.worldObj.getSeed(), false);
    }

    /**
     * Calculates the angle of sun and moon in the sky relative to a specified time (usually worldTime)
     */
    /*public float calculateCelestialAngle(long par1, float par3)
    {
        return 0.0F;
    }*/
    
    @SideOnly(Side.CLIENT)

    /**
     * Returns array with sunrise/sunset colors
     */
    @Override
    public float[] calcSunriseSunsetColors(float par1, float par2)
    {
        return null;
    }
    
    protected void generateLightBrightnessTable()
    {
        float var1 = -0.05F;

        worldObj.lastLightningBolt = 0;
        
        for (int var2 = 0; var2 <= 15; ++var2)
        {
            float var3 = /*1.0F - */(float)var2 / 15.0F;
            
            //normal
            this.lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
            
            //nether
            lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
            
            //negative light
            lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F + var1) + var1;
            
            float wat = this.worldObj.getWorldTime();
            
            
            
            float smooth = this.worldObj.getWorldTime() % 200 / 60F;
            
            //System.out.println(smooth + " - " + wat);
            
            //awesome
            //float smooth = System.currentTimeMillis() % 100000 / 10000F;
            
            
            
            //good control on darkening the torches
            lightBrightnessTable[var2] = 0.0F + (float)Math.sin(var2 * 0.02F);
            
            //wacky
            //lightBrightnessTable[var2] = -0.5F + (float)Math.sin(var2 * smooth);
            
            //System.out.println("FINAL: " + lightBrightnessTable[var2]);
            
            //lightBrightnessTable[var2] = (float)Math.cos(var2 * 0.11F);//var3 * 0.02F;
            
            //static negative light
            //lightBrightnessTable[var2] = 0.0F;
            
            //(float) Math.sin(System.currentTimeMillis());
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public float calculateCelestialAngle(long par1, float par3)
    {
        int var4 = (int)(par1 % 24000L);
        float var5 = ((float)var4 + par3) / 24000.0F - 0.25F;

        if (var5 < 0.0F)
        {
            ++var5;
        }

        if (var5 > 1.0F)
        {
            --var5;
        }

        float var6 = var5;
        var5 = 1.0F - (float)((Math.cos((double)var5 * Math.PI) + 1.0D) / 2.0D);
        var5 = var6 + (var5 - var6) / 3.0F;
        return 0.21F;//var5;
    }
    
    @SideOnly(Side.CLIENT)

    /**
     * Return Vec3D with biome specific fog color
     */
    @Override
    public Vec3 getFogColor(float par1, float par2)
    {
    	//temp, wat
    	hasNoSky = false;
    	//generateLightBrightnessTable();
    	
        int var3 = 10518688;
        
        var3 = 0x000000;
        
        float var4 = MathHelper.cos(par1 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;

        
        
        if (var4 < 0.0F)
        {
            var4 = 0.0F;
        }

        if (var4 > 1.0F)
        {
            var4 = 1.0F;
        }

        float var5 = (float)(var3 >> 16 & 255) / 255.0F;
        float var6 = (float)(var3 >> 8 & 255) / 255.0F;
        float var7 = (float)(var3 & 255) / 255.0F;
        var5 *= var4 * 0.0F + 0.15F;
        var6 *= var4 * 0.0F + 0.15F;
        var7 *= var4 * 0.0F + 0.15F;
        return Vec3.createVectorHelper((double)var5, (double)var6, (double)var7);
    }
    
    @Override
    public boolean getWorldHasVoidParticles()
    {
        return false;
    }
    
    @Override
    public double getVoidFogYFactor()
    {
    	
    	//double adj = (/*FMLClientHandler.instance().getClient().thePlayer.getHealth()*/20 * 0.0005);
    	
    	//System.out.println(adj);
    	
    	//adj = 0.01D;
    	
    	
    	
        return 0D;//this.terrainType.voidFadeMagnitude();
    }
    
    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the given X,Z coordinate should show environmental fog.
     */
    @Override
    public boolean doesXZShowFog(int par1, int par2)
    {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSkyColored()
    {
        return true;
    }

    /**
     * True if the player can respawn in this dimension (true = overworld, false = nether).
     */
    @Override
    public boolean canRespawnHere()
    {
        return true;
    }
    
    public ChunkCoordinates getSpawnPoint()
    {
    	//ZCGame zcGame = ZCGame.instance();
    	
    	//return super.getSpawnPoint();
    	
    	return new ChunkCoordinates(0, 3, 0);
    	
    	/*if (zcGame != null && zcGame.zcLevel != null) {
    		//this code doesnt appear to return a very accurate result, so its not depended on, watchForPlayerRespawn still manages the final teleport
    		return new ChunkCoordinates(zcGame.zcLevel.lobby_coord_playerX, zcGame.zcLevel.lobby_coord_playerY, zcGame.zcLevel.lobby_coord_playerZ);
    	} else {
    		
    	}*/
        
    }

    /**
     * Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions.
     */
    @Override
    public boolean isSurfaceWorld()
    {
        return false;
    }
    @SideOnly(Side.CLIENT)

    /**
     * the y level at which clouds are rendered.
     */
    @Override
    public float getCloudHeight()
    {
        return 256.0F;
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    @Override
    public boolean canCoordinateBeSpawn(int par1, int par2)
    {
        int var3 = this.worldObj.getFirstUncoveredBlock(par1, par2);
        return var3 == 0 ? false : Block.blocksList[var3].blockMaterial.blocksMovement();
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension.
     */
    @Override
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return new ChunkCoordinates(0, 3, 0);
    }

    @Override
    public int getAverageGroundLevel()
    {
        return 3;
    }

    /**
     * Returns the dimension's name, e.g. "The End", "Nether", or "Overworld".
     */
    @Override
    public String getDimensionName()
    {
        return "Catacombs";
    }
    
    //fail
    @Override
    public boolean canMineBlock(EntityPlayer player, int x, int y, int z)
    {
    	//return ZCUtil.areBlocksMineable;
        return worldObj.canMineBlockBody(player, x, y, z);
    }
}
