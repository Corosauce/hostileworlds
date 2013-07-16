package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.config.ModConfigFields;
import hostileworlds.dimension.HWTeleporter;
import hostileworlds.entity.monster.ZombieBlockWielder;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import particleman.entities.EntityParticleControllable;
import CoroAI.componentAI.ICoroAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHWPortal extends BlockContainer
{
    public BlockHWPortal(int par1, Material par2Material)
    {
        super(par1, par2Material);
        this.setLightValue(1.0F);
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public TileEntity createNewTileEntity(World par1World)
    {
        return new TileEntityHWPortal(false);
    }

    @Override
    public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
    {
    	((TileEntityHWPortal)par1World.getBlockTileEntity(par2, par3, par4)).blockBreak();
    	if (par1World.isRemote && par1World.provider.dimensionId == 0) explodeParticles(par1World, par2, par3, par4);
    }
    
    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        /*float var5 = 0.0625F;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, var5, 1.0F);*/
    	
    	float var5;
        float var6;

        if (par1IBlockAccess.getBlockId(par2 - 1, par3, par4) != this.blockID && par1IBlockAccess.getBlockId(par2 + 1, par3, par4) != this.blockID)
        {
            var5 = 0.125F;
            var6 = 0.5F;
            this.setBlockBounds(0.5F - var5, 0.0F, 0.5F - var6, 0.5F + var5, 0.0001F, 0.5F + var6);
        }
        else
        {
            var5 = 0.5F;
            var6 = 0.125F;
            this.setBlockBounds(0.5F - var5, 0.0F, 0.5F - var6, 0.5F + var5, 0.0001F, 0.5F + var6);
        }
    	
    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }
    
    /*public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
    	return null;
    }*/

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
     * coordinates.  Args: blockAccess, x, y, z, side
     */
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return par5 != 0 ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
    }

    /**
     * if the specified block is in the given AABB, add its collision bounding box to the given list
     */
    public void addCollidingBlockToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {}

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    /**
     * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
     */
    public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
    {
    	if (par5Entity instanceof EntityParticleControllable) {
	    	if (!par1World.isRemote) {
	    		TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
	    		if (te instanceof TileEntityHWPortal) {
	    			((TileEntityHWPortal)te).takeDamage((EntityParticleControllable) par5Entity);
	    			return;
	    		}
	    	} else {
	    		if (par5Entity.dimension == 0) explodeParticles(par1World, par2, par3, par4);
	    	}
    	}
    	
    	if (!par1World.isRemote && !ModConfigFields.portalTeleporting && par1World.provider.dimensionId == 0) {
    		return;
    	}
    	
        if (par5Entity.ridingEntity == null && par5Entity.riddenByEntity == null && !par1World.isRemote)
        {
        	int time = 0;
        	time = par5Entity.getEntityData().getInteger("HWPortalTime");
        	//System.out.println(time);
        	
        	if (par5Entity instanceof EntityPlayerMP) {
        		if (time++ > 300) {
        			time = 0;
        			TileEntityHWPortal portal = null;
        	    	
    	    		TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
    	    		if (te instanceof TileEntityHWPortal) {
    	    			portal = ((TileEntityHWPortal)te).getMainTileEntity();
    	    		}
        	    	
        	    	
        	    	if (portal != null) {
        	    		//travelToDimension(par5Entity, par1World.provider.dimensionId == 0 ? HostileWorlds.instance.dimIDCatacombs : 0);
        	    		if (portal.destDim == -1 && par1World.provider.dimensionId == 0) {
        	    			System.out.println("-1 dim :/");
        	    			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) par5Entity, par1World.provider.dimensionId == 0 ? 0 : 0, new HWTeleporter((WorldServer)par5Entity.worldObj));
        	    		} else {
        	    			if (DimensionManager.getWorld(portal.destDim) != null) {
        	    				MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) par5Entity, par1World.provider.dimensionId == 0 ? portal.destDim : 0, new HWTeleporter((WorldServer)par5Entity.worldObj));
        	    			}
        	    		}
        	    	} else {
        	    		System.out.println("error cant find tile entity");
        	    	}
            		
            	}
        	} else {
        		if ((!(par5Entity instanceof ICoroAI) || (par1World.provider.dimensionId != 0 && !(par5Entity instanceof ZombieBlockWielder))) && time++ > 5) {
        			time = 0;
        			
        			
        			int destDim = 0;
        			
        			if (par1World.provider.dimensionId == 0) {
	        			TileEntityHWPortal portal = null;
	        	    	
	    	    		TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
	    	    		if (te instanceof TileEntityHWPortal) {
	    	    			portal = ((TileEntityHWPortal)te).getMainTileEntity();
	    	    		}
	    	    		
	    	    		if (portal != null) {
	    	    			destDim = portal.destDim;
	    	    		}	
        	    	}
        			
        			if (destDim == -1) {
    	    			System.out.println("fail, portal.destDim is -1");
    	    		} else {
    	    			if (DimensionManager.getWorld(destDim) != null) {
    	    				if (par5Entity instanceof EntityLiving) {
    	    					 ((EntityLiving)par5Entity).getNavigator().setPath(null, 0F);
    	    				}
    	    				travelToDimension(par5Entity, par1World.provider.dimensionId == 0 ? destDim : 0);
    	    			}
    	    		}

        			//MinecraftServer.getServer().getConfigurationManager().transferEntityToWorld(par5Entity, par2, (WorldServer)par1World, DimensionManager.getWorld(par1World.provider.dimensionId == 0 ? HostileWorlds.instance.dimIDCatacombs : 0), new HWTeleporter((WorldServer)par5Entity.worldObj));
        		}
        	}
        	
        	//needs a proper reset outside this method for when they step out of a portal before it teleports
        	par5Entity.getEntityData().setInteger("HWPortalTime", time);
        	
        	
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void explodeParticles(World par1World, int par2, int par3, int par4) {
    	for (int i = 0; i < 10; i++) {
    		EntityFX entFX = new EntityReddustFX(par1World, par2, par3, par4, 1F, 1F, 0F, 0F);
    		float speed = 0.5F;
    		entFX.setVelocity(Math.random() * speed - (speed/2), Math.random() * speed, Math.random() * speed - (speed/2));
    		entFX.setRBGColorF(0, 0.5F + (float)(Math.random() * 0.5F), 0);
    		
    		if (entFX != null) {
    			Minecraft.getMinecraft().effectRenderer.addEffect(entFX);
    		}
    	}
    }
    
    public void travelToDimension(Entity ent, int par1)
    {
        if (!ent.worldObj.isRemote && !ent.isDead)
        {
        	ent.worldObj.theProfiler.startSection("changeDimension");
            MinecraftServer var2 = MinecraftServer.getServer();
            int var3 = ent.dimension;
            WorldServer var4 = var2.worldServerForDimension(var3);
            WorldServer var5 = var2.worldServerForDimension(par1);
            ent.dimension = par1;
            ent.worldObj.removeEntity(ent);
            ent.isDead = false;
            ent.worldObj.theProfiler.startSection("reposition");
            var2.getConfigurationManager().transferEntityToWorld(ent, var3, var4, var5, new HWTeleporter((WorldServer)ent.worldObj));
            ent.worldObj.theProfiler.endStartSection("reloading");
            Entity var6 = EntityList.createEntityByName(EntityList.getEntityString(ent), var5);

            if (var6 != null)
            {
                var6.copyDataFrom(ent, true);
                var5.spawnEntityInWorld(var6);
            }

            ent.isDead = true;
            ent.worldObj.theProfiler.endSection();
            var4.resetUpdateEntityTick();
            var5.resetUpdateEntityTick();
            ent.worldObj.theProfiler.endSection();
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
        double var6 = (double)((float)par2 + par5Random.nextFloat());
        double var8 = (double)((float)par3 + 0.8F);
        double var10 = (double)((float)par4 + par5Random.nextFloat());
        double var12 = 0.0D;
        double var14 = 0.0D;
        double var16 = 0.0D;
        //par1World.spawnParticle("smoke", var6, var8, var10, var12, var14, var16);
    }

    /**
     * The type of render function that is called for this block
     */
    public int getRenderType()
    {
        return -1;
    }

    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World par1World, int par2, int par3, int par4)
    {
    	if (par1World.getBlockId(par2 - 1, par3, par4) != HostileWorlds.blockBloodyCobblestone.blockID && par1World.getBlockId(par2 + 1, par3, par4) != HostileWorlds.blockBloodyCobblestone.blockID)
        {
    		par1World.setBlockMetadataWithNotify(par2, par3, par4, 0, 2);
        } else {
        	par1World.setBlockMetadataWithNotify(par2, par3, par4, 1, 2);
        }
        /*if (!bossDefeated)
        {
            if (par1World.provider.dimensionId != 0)
            {
                par1World.setBlockWithNotify(par2, par3, par4, 0);
            }
        }*/
    }
    
    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5)
    {
        byte var6 = 0;
        byte var7 = 1;

        if (par1World.getBlockId(par2 - 1, par3, par4) == this.blockID || par1World.getBlockId(par2 + 1, par3, par4) == this.blockID)
        {
            var6 = 1;
            var7 = 0;
        }

        int var8;

        for (var8 = par3; par1World.getBlockId(par2, var8 - 1, par4) == this.blockID; --var8)
        {
            ;
        }

        if (par1World.getBlockId(par2, var8 - 1, par4) != HostileWorlds.blockBloodyCobblestone.blockID)
        {
            par1World.setBlock(par2, par3, par4, 0);
        }
        else
        {
            int var9;

            for (var9 = 1; var9 < 4 && par1World.getBlockId(par2, var8 + var9, par4) == this.blockID; ++var9)
            {
                ;
            }

            if (var9 == 3 && par1World.getBlockId(par2, var8 + var9, par4) == HostileWorlds.blockBloodyCobblestone.blockID)
            {
                boolean var10 = par1World.getBlockId(par2 - 1, par3, par4) == this.blockID || par1World.getBlockId(par2 + 1, par3, par4) == this.blockID;
                boolean var11 = par1World.getBlockId(par2, par3, par4 - 1) == this.blockID || par1World.getBlockId(par2, par3, par4 + 1) == this.blockID;

                if (var10 && var11)
                {
                    par1World.setBlock(par2, par3, par4, 0);
                }
                else
                {
                    if ((par1World.getBlockId(par2 + var6, par3, par4 + var7) != HostileWorlds.blockBloodyCobblestone.blockID || par1World.getBlockId(par2 - var6, par3, par4 - var7) != this.blockID) && (par1World.getBlockId(par2 - var6, par3, par4 - var7) != HostileWorlds.blockBloodyCobblestone.blockID || par1World.getBlockId(par2 + var6, par3, par4 + var7) != this.blockID))
                    {
                        par1World.setBlock(par2, par3, par4, 0);
                    }
                }
            }
            else
            {
                par1World.setBlock(par2, par3, par4, 0);
            }
        }
        
        onBlockAdded(par1World, par2, par3, par4);
    }

    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
        return 0;
    }
    
    public boolean tryToCreatePortal(World par1World, int par2, int par3, int par4)
    {
        byte var5 = 0;
        byte var6 = 0;

        if (par1World.getBlockId(par2 - 1, par3, par4) == HostileWorlds.blockBloodyCobblestone.blockID || par1World.getBlockId(par2 + 1, par3, par4) == HostileWorlds.blockBloodyCobblestone.blockID)
        {
            var5 = 1;
        }

        if (par1World.getBlockId(par2, par3, par4 - 1) == HostileWorlds.blockBloodyCobblestone.blockID || par1World.getBlockId(par2, par3, par4 + 1) == HostileWorlds.blockBloodyCobblestone.blockID)
        {
            var6 = 1;
        }

        if (var5 == var6)
        {
            return false;
        }
        else
        {
            if (par1World.getBlockId(par2 - var5, par3, par4 - var6) == 0)
            {
                par2 -= var5;
                par4 -= var6;
            }

            int var7;
            int var8;

            for (var7 = -1; var7 <= 2; ++var7)
            {
                for (var8 = -1; var8 <= 3; ++var8)
                {
                    boolean var9 = var7 == -1 || var7 == 2 || var8 == -1 || var8 == 3;

                    if (var7 != -1 && var7 != 2 || var8 != -1 && var8 != 3)
                    {
                        int var10 = par1World.getBlockId(par2 + var5 * var7, par3 + var8, par4 + var6 * var7);

                        if (var9)
                        {
                            if (var10 != HostileWorlds.blockBloodyCobblestone.blockID)
                            {
                                return false;
                            }
                        }
                        else if (var10 != 0 && var10 != Block.fire.blockID)
                        {
                            return false;
                        }
                    }
                }
            }

            for (var7 = 0; var7 < 2; ++var7)
            {
                for (var8 = 0; var8 < 3; ++var8)
                {
                    par1World.setBlock(par2 + var5 * var7, par3 + var8, par4 + var6 * var7, blockID, 0, 2);
                }
            }
            return true;
        }
    }
}
