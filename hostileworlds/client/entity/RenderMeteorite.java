package hostileworlds.client.entity;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.EntityMeteorite;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMeteorite extends Render
{
	
    public RenderMeteorite()
    {
    }

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
    	this.bindEntityTexture(var1);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        
        float size = 15 - (((EntityMeteorite)var1).age * 0.03F);
        
        if (size < 5) size = 5;
        
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        Block block = HostileWorlds.blockBloodyCobblestone;
        World var11 = var1.worldObj;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glRotatef((float)(((EntityMeteorite)var1).age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(((EntityMeteorite)var1).age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(((EntityMeteorite)var1).age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        RenderBlocks rb = new RenderBlocks(var1.worldObj);
        GL11.glScalef(size, size, size);
        //Tessellator tess = Tessellator.instance;
        //tess.setBrightness(255);
        //tess.setColorOpaque_F(255, 255, 255);
        rb.renderBlockAsItem(block, 0, 0.8F);
        
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}
}
