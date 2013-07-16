package hostileworlds.client.entity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;

import CoroAI.componentAI.ICoroAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderComrade extends RenderBiped
{
    public RenderComrade()
    {
        super(new ModelBiped(0.0F), 0.5F);
    }
    
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
    	super.doRender(par1Entity, par2, par4, par6, par8, par9);
    	
    	if (!(par1Entity instanceof ICoroAI) || !(par1Entity instanceof EntityLiving)) return;
    	
    	if (renderManager.livingPlayer.getDistanceToEntity(par1Entity) > 12) return;
    	
    	ICoroAI entInt = (ICoroAI)par1Entity;
    	
    	GL11.glPushMatrix();
    	
    	float scale = 0.02F;
    	float yOffset = 0.15F;
    	int health = par1Entity.getDataWatcher().getWatchableObjectInt(23);
    	int healthMax = ((EntityLiving)par1Entity).getMaxHealth();
    	String s = "hue";
    	
    	FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
        
        GL11.glTranslatef((float)par2 + 0.0F, (float)par4 + par1Entity.height + 0.5F + yOffset, (float)par6);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        int width = 30;//fontrenderer.getStringWidth(s) / 2;
        
        GL11.glTranslatef(-width/2, 0.25F / scale, 0.0F);
        GL11.glDepthMask(false);
        //GL11.glEnable(GL11.GL_BLEND);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        
        int height = 2;
        
        //red bg
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex((double)(0), 0.0D, 0.0D);
        tessellator.addVertex((double)(0), height, 0.0D);
        tessellator.addVertex((double)(width), height, 0.0D);
        tessellator.addVertex((double)(width), 0.0D, 0.0D);
        tessellator.draw();
        
        width = 30 * health / healthMax;
        
        //green health
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0F, 1F, 0.0F, 0.25F);
        tessellator.addVertex((double)(0), 0.0D, 0.0D);
        tessellator.addVertex((double)(0), height, 0.0D);
        tessellator.addVertex((double)(width), height, 0.0D);
        tessellator.addVertex((double)(width), 0.0D, 0.0D);
        tessellator.draw();
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        //fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
        GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
    	
	    GL11.glPopMatrix();
    }
}
