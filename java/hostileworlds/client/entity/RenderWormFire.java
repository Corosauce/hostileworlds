package hostileworlds.client.entity;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderWormFire extends Render
{

    public RenderWormFire()
    {
    	
    }

    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        //GL11.glTranslatef((float)var1.posX, (float)var1.posY, (float)var1.posZ);
        
        RenderBlocks rb = new RenderBlocks(var1.worldObj);
        rb.renderBlockAsItem(Blocks.lava, 0, 0.8F);
        
        GL11.glPopMatrix();
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation("/terrain.png");
	}
}
