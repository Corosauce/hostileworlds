package hostileworlds.block;

import hostileworlds.ai.WorldDirectorMultiDim;
import hostileworlds.ai.invasion.WorldEvent;
import hostileworlds.config.ModConfigFields;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class TileEntityAuraCurseRenderer extends TileEntitySpecialRenderer
{
    //private SignModel signModel = new SignModel();

	long firstTick = 0;
	
    public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8) {
    	
    	if (firstTick == 0) firstTick = System.currentTimeMillis();
    	
    	float scale = 0.15F;
    	int dim = var1.getWorldObj().provider.dimensionId;
    	
    	int cooldown = WorldDirectorMultiDim.clientPlayersCooldown;
    	//EntityPlayer entP = DimensionManager.getWorld(dim).getPlayerEntityByName(Minecraft.getMinecraft().thePlayer.username);
    	
    	//if (entP != null) cooldown = entP.getEntityData().getInteger("HWInvasionCooldown");
    	
    	if (WorldDirectorMultiDim.clientCurInvasions == null || WorldDirectorMultiDim.clientCurInvasions.get(dim) == null) return;
    	
    	HashMap<Integer, ArrayList<WorldEvent>> invasions = WorldDirectorMultiDim.clientCurInvasions;
    	
    	//if (MinecraftServer.getServer().isSinglePlayer()) invasions = WorldDirector.curInvasions;
    	
    	float headerExtra = 0.0F;
    	
    	float adj = 0.40F;
    	float infoBlockHeight = 0.6F;
    	float renderOffset = 2.3F - infoBlockHeight + 0.2F + headerExtra;
    	
    	int invSize = invasions.get(dim).size();
    	
    	//invSize = 1;
    	
    	renderLivingLabel("", var2, var4 + renderOffset + headerExtra + ((invSize-1) * infoBlockHeight * 1F), var6, 0, 300, (int)((invSize) * (infoBlockHeight + headerExtra) * 100));//(invasions.get(dim).size() * 80));
    	
    	float sizeOffset = -0.03F;//(invasions.get(dim).size() * adj);
    	
    	renderLivingLabel("----------", var2, var4+renderOffset-infoBlockHeight+0.075F, var6, 1);
    	if (cooldown != -1) {
    		renderLivingLabel("Invasions: " + invasions.get(dim).size() + " - Your cooldown: " + /*Minecraft.getMinecraft().thePlayer*/cooldown, var2, var4+renderOffset-infoBlockHeight+0.075F-0.1F, var6, 1);
    	} else {
    		renderLivingLabel("Invasions: " + invasions.get(dim).size() + " - Your invasion value: " + WorldDirectorMultiDim.clientPlayerInvadeValue + "/" + WorldDirectorMultiDim.getHarvestRatingInvadeThreshold(), var2, var4+renderOffset-infoBlockHeight+0.075F-0.1F, var6, 1);
    	}
    	
    	for (int i = 0; i < invSize; i++) {
    		
    		WorldEvent inv = invasions.get(dim).get(i);
    		
    		float offset = (i*infoBlockHeight) + renderOffset;
    		
    		
    		renderLivingLabel("#" + i + ", " + inv.type.eventEnumToName[inv.type.ordinal()] + " - Target: " + inv.coordDestination.posX + " : " + inv.coordDestination.posY + " : " + inv.coordDestination.posZ + ", " + inv.mainPlayerName, var2, var4+sizeOffset+offset, var6, 1);
    		if (inv.coordSource != null) renderLivingLabel("Source: " + inv.coordSource.posX + " : " + inv.coordSource.posY + " : " + inv.coordSource.posZ + ", Waves: " + (inv.currentWaveCountFromPortal), var2, var4+sizeOffset-0.1F+offset, var6, 1);
    		renderLivingLabel("Wave: " + inv.waveCount + "/" + ModConfigFields.invasionWaveCountMax + ", " + "Leaders: " + inv.lastLeaderCount + ", Last spawned: " + inv.currentWaveSpawnedInvaders, var2, var4+sizeOffset-0.2F+offset, var6, 1);
    		renderLivingLabel("Difficulty: " + inv.currentWaveDifficultyRating + " (" + inv.currentWavePlayerCount + " players) ", var2, var4+sizeOffset-0.3F+offset, var6, 1);
    		renderLivingLabel("Cooldown: " + inv.curCooldown + ", Dist: " + inv.lastLeaderDist, var2, var4+sizeOffset-0.4F+offset, var6, 1);
    		
    		if (i < invSize - 1) {
    			renderLivingLabel("----------", var2, var4+sizeOffset+offset+0.1F, var6, 1);
    		}
    	}

    	//More info ideas
    	//players skill value (weapons, armor)
    	//zombies adjusted difficulty based off of static HW difficulty and player skill value
    	
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9)
    {
    	renderLivingLabel(par2Str, par3, par5, par7, par9, 300, 180);
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height)
    {
        //float var10 = par1EntityLivingBase.getDistanceToEntity(this.renderManager.livingPlayer);

        int borderSize = 2;
    	
        //if (var10 <= (float)par9)
        //{
            FontRenderer var11 = RenderManager.instance.getFontRenderer();
            float var12 = 0.6F;
            float var13 = 0.016666668F * var12;
            GL11.glPushMatrix();
            GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            //GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-var13, -var13, var13);
            GL11.glDisable(GL11.GL_LIGHTING);
            
            if (par9 == 0) {
	            GL11.glDepthMask(false);
	            //GL11.glDisable(GL11.GL_DEPTH_TEST);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            Tessellator var14 = Tessellator.instance;
	            byte var15 = 0;
	            
	            GL11.glDisable(GL11.GL_TEXTURE_2D);
	            var14.startDrawingQuads();
	            //int width = var11.getStringWidth(par2Str) / 2;
            
	            
            
	            var14.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.draw();
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            //var11.drawString(par2Str, -var11.getStringWidth(par2Str) / 2, var15, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            //GL11.glRotatef(180, 0, 0, 1);
            var11.drawString(par2Str, -width/2+borderSize/*-var11.getStringWidth(par2Str) / 2*/, 0, 0xFFFFFF);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        //}
    }
}
