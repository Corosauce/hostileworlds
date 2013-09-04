package hostileworlds.client.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class WeaponRenderer implements IItemRenderer {
	
    public static final ResourceLocation resTex = new ResourceLocation("/mods/HostileWorlds/textures/items/blueLaserRay.png");
	
	public WeaponRenderer() {
	}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch(type) {
			case EQUIPPED: return true;
			default: return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}
	
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		int swingTimeMax = 10;
		int ticksInUse = 0;
		
		Minecraft mc = Minecraft.getMinecraft();
		//boolean swinging = item != null && item.stackTagCompound != null && item.stackTagCompound.getInteger("ticksInUse") > 0;
		if (item != null && item.stackTagCompound != null) {
			ticksInUse = item.stackTagCompound.getInteger("ticksInUse");
		}
		
		if (type == ItemRenderType.EQUIPPED) {
			
			boolean isFirstPerson = false;
			if(data[1] != null && data[1] instanceof EntityPlayer) {
				
				
				
				if(!((EntityPlayer)data[1] == Minecraft.getMinecraft().renderViewEntity && 
						Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 &&
						!((Minecraft.getMinecraft().currentScreen instanceof GuiInventory ||
								Minecraft.getMinecraft().currentScreen instanceof GuiContainerCreative) && 
								RenderManager.instance.playerViewY == 180.0F))) {
					
				} else {
					isFirstPerson = true;
				}
			}
			
			if (ticksInUse >= -10) {
				GL11.glPopMatrix();
				GL11.glPushMatrix();
				GL11.glPushMatrix();
				//GL11.glBindTexture(GL11.GL_TEXTURE_2D, var9);
	            //Tessellator var10 = Tessellator.instance;
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            GL11.glDepthMask(false);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
	            GL11.glDisable(GL11.GL_CULL_FACE);
	            FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(resTex);
				Tessellator tessellator = Tessellator.instance;
				
				float f = 0F;//par2Icon.getMinU();
		        float f1 = 1F;//par2Icon.getMaxU();
		        float f2 = 0F;//par2Icon.getMinV();
		        float f3 = 1F;//par2Icon.getMaxV();
		        float f4 = 1.0F;
		        float f5 = 0.5F;
		        float f6 = 0.25F;
		        //GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		        //GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		        
		        float scale = 40F;
		        float scaleY = (ticksInUse-10) / 5F;
		        
		        GL11.glTranslatef(0F, -scaleY/4, 0F);

		        if (isFirstPerson) {
		        	//GL11.glRotatef(20F, 0F, 0F, 1F);
		        	//GL11.glRotatef(20F, 1F, 0F, 0F);
		        	GL11.glRotatef(30F, 0F, 1F, 0F);
		        } else {
		        	
		        	GL11.glRotatef(-20F, 0F, 0F, 1F);
		        	GL11.glRotatef(-25F, 0F, 1F, 0F);
		        	
		        }
		        
		        GL11.glTranslatef(scale / 2, 0, 0);
	        	GL11.glScalef(scale, scaleY, scale);
		        
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 1.0F, 0.0F);
		        tessellator.addVertexWithUV((double)(0.0F - f5), (double)(0.0F - f6), 0.0D, (double)f, (double)f3);
		        tessellator.addVertexWithUV((double)(f4 - f5), (double)(0.0F - f6), 0.0D, (double)f1, (double)f3);
		        tessellator.addVertexWithUV((double)(f4 - f5), (double)(f4 - f6), 0.0D, (double)f1, (double)f2);
		        tessellator.addVertexWithUV((double)(0.0F - f5), (double)(f4 - f6), 0.0D, (double)f, (double)f2);
		        tessellator.draw();
				//System.out.println("ticksInUse: " + ticksInUse);
		        GL11.glPopMatrix();
		        GL11.glEnable(GL11.GL_CULL_FACE);
	            GL11.glDisable(GL11.GL_BLEND);
	            GL11.glDepthMask(true);
	            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			} else {
				
			}
		}
	}
}
