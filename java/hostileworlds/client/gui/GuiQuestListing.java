package hostileworlds.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import CoroUtil.client.GuiSlotImpl;
import CoroUtil.client.IScrollingElement;
import CoroUtil.client.IScrollingGUI;
import CoroUtil.forge.CoroAI;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiQuestListing extends GuiScreen implements IScrollingGUI {

	//TileEntitySession tEnt;
	
	//see paper notes for most of design, random extras here
	//game state: NONE/LOBBY/ACTIVE
	
	//dont forget, you semi have a map listing gui already, it scrolls...... copy modconfig gui or the zombiecraft level select gui?
	public int gameState = 0; //0 = inactive, 1 = lobby, 2 = active
	
	public int selectedWorld;
    public List<IScrollingElement> listElements = new ArrayList<IScrollingElement>();
    public GuiSlotImpl guiScrollable;
    public GuiButton guiSelectMap;
    
    public ResourceLocation resGUI = new ResourceLocation(CoroAI.modID + ":textures/gui/gui512.png");
    
    protected int xSize = 176;
    protected int ySize = 166;
	
    class SlotEntry implements IScrollingElement {

    	String title = "";
    	String info = "";
    	List<String> listInfo;
    	
    	public SlotEntry(String parName, String parInfo, List<String> parListInfo) {
    		title = parName;
    		info = parInfo;
    		listInfo = parListInfo;
    	}
    	
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getExtraInfo() {
			return info;
		}
		
		@Override
		public List<String> getExtraInfo2() {
			return listInfo;
		}
    	
    }
    
	public GuiQuestListing () {
		super();
		//tEnt = tileEntity;
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3)
    {
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    //this.mc.renderEngine.bindTexture("/mods/ZombieCraft/textures/gui/gui512.png");
	    mc.getTextureManager().bindTexture(resGUI);
	    int x = (width - xSize) / 2;
	    int y = (height - ySize) / 2;
	    this.drawTexturedModalRect(x, y, 0, 0, 512, 512);
		
		super.drawScreen(par1, par2, par3);
		
		this.guiScrollable.drawScreen(par1, par2, par3);
        
        //TEMP!!!!!!
        //initGui();
    }
	
	@Override
    public void initGui()
    {

		xSize = 372;
    	ySize = 250;
		
		super.initGui();
		ScaledResolution var8 = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaledWidth = var8.getScaledWidth();
        int scaledHeight = var8.getScaledHeight();
        
        //System.out.println(scaledWidth);

        int xCenter = scaledWidth / 2;
        int yCenter = scaledHeight / 2;
        
        int xStart = xCenter - xSize/2;
        int yStart = yCenter - ySize/2;
        
        int xStartPadded = xStart + 8 - 1;
        int yStartPadded = yStart + 8 - 1;
        
        int btnWidth = 80;
        int btnHeight = 20;
        int padding = 1;
        int btnSpacing = 22;
        
        
        
        if (listElements.size() == 0) {
        	
        	Minecraft mc = FMLClientHandler.instance().getClient();
        	
        	PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(mc.thePlayer);
        	
        	for (int i = 0; i < quests.activeQuests.size(); i++) {
    			ActiveQuest quest = quests.activeQuests.get(i);
    			//System.out.println("client side active quest id: " + activeQuests.get(i).questID);
    			
    			String qStr = "";
    			List<String> qStr2 = new ArrayList<String>();
    			List<String> qStr3 = new ArrayList<String>();
    			
    			qStr = quest.getTitle();
    			qStr2 = quest.getInstructions(qStr2);
    			qStr3 = quest.getInfoProgress(qStr3);
    			
    			List<String> listMerge = new ArrayList<String>();
    			listMerge.addAll(qStr2);
    			listMerge.addAll(qStr3);
    			
    			listElements.add(new SlotEntry(qStr, "", listMerge));
    		}
	    }
        
        this.guiScrollable = new GuiSlotImpl(this, listElements, 180, this.height, 32, this.height - 32, 12*3 + 10);
        
        
        xStart += 96;
        yStart += 16;
        
        int guiWidth = (/*guiScrollable.slotSizeHalf * 2*/40) + 80 + 30;
        
        guiScrollable.slotSizeHalf = guiWidth;
        
        guiScrollable.width = guiWidth;
        guiScrollable.height = 140;
        guiScrollable.top = yStart;
        guiScrollable.bottom = yStart + guiScrollable.height;
        //guiScrollable.slotHeight = 24;
        guiScrollable.left = xStart;
        guiScrollable.right = xStart + guiWidth;
        
        
    }
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		
        
	}
	
	@Override
	protected void actionPerformed(GuiButton var1)
    {
		String username = "";
        if (mc.thePlayer != null) username = CoroUtilEntity.getName(mc.thePlayer);
        
        NBTTagCompound nbt = new NBTTagCompound();
        //nbt.setString("username", username); //irrelevant, overriden server side for safety
        String mapName = "";
        if (listElements.size() > 0 && selectedWorld > -1) mapName = listElements.get(selectedWorld).getTitle();
        nbt.setString("mapName", mapName);
        nbt.setInteger("cmdID", var1.id);
        
        //PacketHelper.sendClientPacket(ZCPacketHandler.getSessionPacket(nbt));
    }
	
	public int sanitize(int val) {
		return sanitize(val, 0, 9999);
	}
	
	public int sanitize(int val, int min, int max) {
		if (val > max) val = max;
        if (val < min) val = min;
		return val;
	}
    
    public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float f = 0.00390625F / 2F;
        float f1 = 0.00390625F / 2F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + 0) * f1));
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + 0) * f1));
        tessellator.draw();
    }

	@Override
	public void onElementSelected(int par1) {
		selectedWorld = par1;
	}

	@Override
	public int getSelectedElement() {
		return selectedWorld;
	}

	@Override
	public GuiButton getSelectButton() {
		return guiSelectMap;
	}

	@Override
	public void drawBackground() {
		
	}

}
