package hostileworlds.client.entity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

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
        
        this.loadTexture("/terrain.png");
        RenderBlocks rb = new RenderBlocks(var1.worldObj);
        rb.renderBlockAsItem(Block.lavaStill, 0, 0.8F);
        
        GL11.glPopMatrix();
    }
}
