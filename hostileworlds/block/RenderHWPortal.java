package hostileworlds.block;

import java.nio.FloatBuffer;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHWPortal extends TileEntitySpecialRenderer
{
    FloatBuffer field_76908_a = GLAllocation.createDirectFloatBuffer(16);
    
    public boolean generateTexture = true;

    public ResourceLocation resTunnel = new ResourceLocation("textures/environment/end_sky.png");
    public ResourceLocation resField = new ResourceLocation("textures/entity/end_portal.png");
    
    /**
     * Renders the End Portal.
     */
    public void renderEndPortalTileEntity(TileEntityHWPortal par1TileEntityEndPortal, double par2, double par4, double par6, float par8)
    {
    	
    	//if (true) return;
    	
        float var9 = (float)this.tileEntityRenderer.playerX;
        float var10 = (float)this.tileEntityRenderer.playerY;
        float var11 = (float)this.tileEntityRenderer.playerZ;
        GL11.glDisable(GL11.GL_LIGHTING);
        Random var12 = new Random(31100L);
        float var13 = 0.75F;
        
        var13 = 0F;
        
        
        //GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
        
        for (int var14 = 0; var14 < 16; ++var14)
        {
            GL11.glPushMatrix();
            float var15 = (float)(16 - var14);
            float var16 = 0.0625F;
            float var17 = 1.0F / (var15 + 1.0F);

            if (var14 == 0)
            {
            	Minecraft.getMinecraft().renderEngine.bindTexture(resTunnel);
                var17 = 0.1F;
                var15 = 65.0F;
                var16 = 0.125F;
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }

            if (var14 == 1)
            {
            	Minecraft.getMinecraft().renderEngine.bindTexture(resField);
                //this.bindTextureByName("/misc/particlefield.png");
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                var16 = 0.5F;
            }

            float var18 = (float)(-(par4 + (double)var13));
            float var19 = var18 + ActiveRenderInfo.objectY;
            float var20 = var18 + var15 + ActiveRenderInfo.objectY;
            float var21 = var19 / var20;
            var21 += (float)(par4 + (double)var13);
            //GL11.glTranslatef(var9, var21, var11);
            
            int revIndex = 16-var14;
            float time = (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F;
            float time2 = (float) Math.sin(par1TileEntityEndPortal.worldObj.getWorldTime() / 200F);
            float time3 = (float) Math.sin(par1TileEntityEndPortal.worldObj.getWorldTime() / 40F);
            float time4 = (float) Math.cos(par1TileEntityEndPortal.worldObj.getWorldTime() / 40F);
            
            //System.out.println(time2 * 0.3F);
            
            GL11.glRotatef((time2 * 0.15F) - 0.25F, 1.0F, 0.0F, 0.0F);
            
            GL11.glTexGeni(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
            GL11.glTexGeni(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
            GL11.glTexGeni(GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
            GL11.glTexGeni(GL11.GL_Q, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
            
            //if (!generateTexture) {
            	//generateTexture = false;
	            GL11.glTexGen(GL11.GL_S, GL11.GL_OBJECT_PLANE, this.func_76907_a(1.0F, 0.0F, 0.0F, 0.0F));
	            GL11.glTexGen(GL11.GL_T, GL11.GL_OBJECT_PLANE, this.func_76907_a(0.0F, 0.0F, 1.0F, 0.0F));
	            GL11.glTexGen(GL11.GL_R, GL11.GL_OBJECT_PLANE, this.func_76907_a(0.0F, 0.0F, 0.0F, 1.0F));
	            GL11.glTexGen(GL11.GL_Q, GL11.GL_EYE_PLANE, this.func_76907_a((float)Math.cos((float)par1TileEntityEndPortal.worldObj.getWorldTime() / 40F) * 0.2F, 0.7F, 0.0F, 0.0F));
            //}
            GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
            GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
            GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
            GL11.glEnable(GL11.GL_TEXTURE_GEN_Q);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            //GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
            
            
            
            
            
            //time = 0F;
            
            //GL11.glTranslatef(0.0F, time, 0.0F);
            GL11.glRotatef(time2, 1.0F, 1.0F, 1.0F);
            GL11.glScalef(var16, var16, var16);
            GL11.glTranslatef(0.5F, 0.5F, 0.0F);
            GL11.glRotatef((float)(var14 * var14 * 4321 + var14 * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            //GL11.glRotatef((float)(var14 * var14 * 4321 + var14 * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            
            //GL11.glRotatef((float)(Math.sin(var14 * time) * 0.1F) * 2.0F, 0.0F, 0.0F, 1.0F);
            
            //GL11.glRotatef((float) (var14 * 0.1F * Math.cos(par1TileEntityEndPortal.worldObj.getWorldTime() / 100F)), 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (var14 / 1.5F * Math.cos(par1TileEntityEndPortal.worldObj.getWorldTime() / 50F)), 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, (float) (var14 / 1.5F * Math.sin(par1TileEntityEndPortal.worldObj.getWorldTime() / 50F)), 0.0F);
            
            
            
            float scale = 0.3F;
            
            GL11.glTranslatef((float) ((float)(revIndex * scale) * time3), (float) ((float)(revIndex * scale) * time4), 0F);
            
            //GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
            //GL11.glTranslatef(-var9, -var11, -var10);
            
            var19 = var18 + ActiveRenderInfo.objectY;
            GL11.glTranslatef(ActiveRenderInfo.objectX * var15 / var19, ActiveRenderInfo.objectZ * var15 / var19, -var10);
            Tessellator var24 = Tessellator.instance;
            var24.startDrawingQuads();
            
            var21 = var12.nextFloat() * 0.5F + 0.1F;
            float var22 = var12.nextFloat() * 0.5F + 0.4F;
            float var23 = var12.nextFloat() * 0.5F + 0.5F;
            
            //temp debug
            par1TileEntityEndPortal.type = 2;
            par1TileEntityEndPortal.rotation = par1TileEntityEndPortal.getBlockMetadata();
            
            //0 = varying color, 1 = dark, 2 = red, 3 = green, 4 = blue, 5 = purple awesome, 6 = time based color shifting;
            
            if (par1TileEntityEndPortal.type == 0) {
            	var21 = var12.nextFloat() * 0.5F + 0.1F;
	            var22 = var12.nextFloat() * 0.5F + 0.4F;
	            var23 = var12.nextFloat() * 0.5F + 0.5F;
            } else if (par1TileEntityEndPortal.type == 1) {
            	var21 = 1F;
	            var22 = 0F;
	            var23 = 0F;
            } else if (par1TileEntityEndPortal.type == 2) {
            	var21 = 0F;
	            var22 = 0.7F;
	            var23 = 0F;
            } else if (par1TileEntityEndPortal.type == 3) {
            	var21 = 0.2F;
	            var22 = 0.2F;
	            var23 = 1F;
            } else if (par1TileEntityEndPortal.type == 4) {
            	var21 = 0F;
	            var22 = 0F;
	            var23 = 1F;
            } else if (par1TileEntityEndPortal.type == 5) {
            	var21 = var12.nextFloat();
	            var22 = var12.nextFloat();
	            var23 = var12.nextFloat() + 0.6F;
            } else if (par1TileEntityEndPortal.type == 6) {
            	
            	float shift = (float)Math.cos((float)par1TileEntityEndPortal.worldObj.getWorldTime() / 50F);
            	float shift2 = (float)Math.cos((float)par1TileEntityEndPortal.worldObj.getWorldTime() / 100F);
            	float shift3 = (float)Math.cos((float)par1TileEntityEndPortal.worldObj.getWorldTime() / 60F);
            	
            	var21 = shift * 0.2F + 0.4F;
	            var22 = shift2 * 0.2F + 0.4F;
	            var23 = 0.8F;//shift3 * 0.2F + 0.2F;
            	
            }

            if (var14 == 0)
            {
                /*var23 = 1.0F;
                var22 = 1.0F;
                var21 = 1.0F;*/
            }

            GL11.glDisable(GL11.GL_CULL_FACE);
            
            float fade = (float)Math.cos((float)par1TileEntityEndPortal.worldObj.getWorldTime() / 20F) * 0.02F;
            
            //var17 = 0.1F;
            
            var24.setColorRGBA_F(var21 * var17, var22 * var17, var23 * var17, 1F + fade);
            
            double x = 0D;
            double z = 0D;
            double x2 = 0D;
            double z2 = 0D;
            
            int angle = par1TileEntityEndPortal.rotation;
            
            double xx = (angle == 0 ? 0.5D : 0D);
            double zz = (angle == 1 ? 0.5D : 0D);
            
            var24.addVertex(par2 + xx, par4 + (double)var13, par6 + zz);
            var24.addVertex(par2 + xx + (angle == 0 ? 0D : 1D), par4 + (double)var13, par6 + zz + (angle == 1 ? 0D : 1D));
            var24.addVertex(par2 + xx + (angle == 0 ? 0D : 1D), par4 + (double)var13 + 1D, par6 + zz + (angle == 1 ? 0D : 1D));
            var24.addVertex(par2 + xx, par4 + (double)var13 + 1D, par6 + zz);
            
            /*var24.addVertex(par2, par4 + (double)var13, par6);
            var24.addVertex(par2, par4 + (double)var13, par6 + 1.0D);
            var24.addVertex(par2 + 1.0D, par4 + (double)var13 + 1D, par6 + 1.0D);
            var24.addVertex(par2 + 1.0D, par4 + (double)var13 + 1D, par6);*/
            var24.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
        GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
        GL11.glDisable(GL11.GL_TEXTURE_GEN_R);
        GL11.glDisable(GL11.GL_TEXTURE_GEN_Q);
        GL11.glEnable(GL11.GL_LIGHTING);
        
        //GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
        
    }

    private FloatBuffer func_76907_a(float par1, float par2, float par3, float par4)
    {
        this.field_76908_a.clear();
        this.field_76908_a.put(par1).put(par2).put(par3).put(par4);
        this.field_76908_a.flip();
        return this.field_76908_a;
    }

    public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8)
    {
        this.renderEndPortalTileEntity((TileEntityHWPortal)par1TileEntity, par2, par4, par6, par8);
    }
}
