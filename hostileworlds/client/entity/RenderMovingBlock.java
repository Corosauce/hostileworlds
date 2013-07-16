package hostileworlds.client.entity;

import hostileworlds.entity.MovingBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMovingBlock extends Render
{
	
	boolean itemRender = false;
	
    public RenderMovingBlock()
    {
    }

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        
        MovingBlock entBlock = ((MovingBlock)var1);
        float size = 1F;
        
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        GL11.glRotatef((float)(entBlock.age * entBlock.blockNum * 0.02F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.age * entBlock.blockNum * 0.02F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(entBlock.age * entBlock.blockNum * 0.02F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        GL11.glScalef(size, size, size);
        
        this.loadTexture("/terrain.png");
        Block block = Block.blocksList[entBlock.blockID];
        
        block = Block.lavaStill;
        //block = Block.ice;
        //block = Block.sand;
        //block = Block.glass;
        
        itemRender = false;
        
        if (block != null) {
	        if (itemRender) {
		        
			        
			        RenderBlocks rb = new RenderBlocks(var1.worldObj);
			        rb.renderBlockAsItem(block, 0, 0.8F);
		        
	        } else {
	        	GL11.glDisable(GL11.GL_LIGHTING);
	        	this.renderFallingCube(entBlock, block, var1.worldObj, MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.posY), MathHelper.floor_double(var1.posZ), entBlock.blockMeta);
	        }
        }
        
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
    
    public void renderFallingCube(MovingBlock var1, Block var2, World var3, int var4, int var5, int var6, int var7)
    {
    	RenderBlocks a = new RenderBlocks(var1.worldObj);
    	
        float var8 = 0.5F;
        float var9 = 1.0F;
        float var10 = 0.8F;
        float var11 = 0.6F;
        Tessellator var12 = Tessellator.instance;
        var12.startDrawingQuads();
        //float var13 = var2.getBlockBrightness(var3, var4, var5, var6);
        //float var14 = var2.getBlockBrightness(var3, var4, var5 - 1, var6);
        var12.setBrightness(var2.getMixedBrightnessForBlock(var3, var4, var5, var6));

        float var13 = 0.8F;
        float var14 = 0.8F;
        
        var13 = (float) (var13 + Math.cos((var1.worldObj.getWorldTime() * 0.3F) - (var1.blockRow * 0.5F)) * 0.15F);
        var14 = var13;
        
        float var15 = 1.0F;
        float var16 = 1.0F;
        float var17 = 1.0F;

        if (var2.blockID == Block.leaves.blockID)
        {
            int var18 = var2.colorMultiplier(var3, (int)var1.posX, (int)var1.posY, (int)var1.posZ);
            var15 = (float)(var18 >> 16 & 255) / 255.0F;
            var16 = (float)(var18 >> 8 & 255) / 255.0F;
            var17 = (float)(var18 & 255) / 255.0F;

            if (EntityRenderer.anaglyphEnable)
            {
                float var19 = (var15 * 30.0F + var16 * 59.0F + var17 * 11.0F) / 100.0F;
                float var20 = (var15 * 30.0F + var16 * 70.0F) / 100.0F;
                float var21 = (var15 * 30.0F + var17 * 70.0F) / 100.0F;
                var15 = var19;
                var16 = var20;
                var17 = var21;
            }
        }
        
        //NEW! - set block render size
        a.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        //a.setRenderMinMax(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

        var12.setColorOpaque_F(var15 * var8 * var14, var16 * var8 * var14, var17 * var8 * var14);
        a.renderFaceYNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(0, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var9 * var14, var16 * var9 * var14, var17 * var9 * var14);
        a.renderFaceYPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(1, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(2, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var10 * var14, var16 * var10 * var14, var17 * var10 * var14);
        a.renderFaceZPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(3, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXNeg(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(4, var7));

        if (var14 < var13)
        {
            var14 = var13;
        }

        var12.setColorOpaque_F(var15 * var11 * var14, var16 * var11 * var14, var17 * var11 * var14);
        a.renderFaceXPos(var2, -0.5D, -0.5D, -0.5D, var2.getIcon(5, var7));
        var12.draw();
    }
}
