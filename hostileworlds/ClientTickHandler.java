package hostileworlds;

import hostileworlds.item.ItemLaserBeam;

import java.util.EnumSet;

import modconfig.gui.GuiConfigEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
	public static Minecraft mc = null;
	public static World worldRef = null;
	public static EntityPlayer player = null;
	public static World lastWorld;
	
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
    	if (type.equals(EnumSet.of(TickType.PLAYER))) {
        	EntityPlayer entP = Minecraft.getMinecraft().thePlayer;
        	ItemStack is = entP.inventory.getCurrentItem();
        	if (is != null && entP.isUsingItem()) {
        		if (is.getItem() instanceof ItemLaserBeam) {
        			entP.setItemInUse(is, entP.getItemInUseCount());
        		}
        	}
        	
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.RENDER)))
        {
            onRenderTick();
        }
        else if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
            GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;

            if (guiscreen != null)
            {
                onTickInGUI(guiscreen);
            }
            else
            {
                
            }
            
            onTickInGame();
        }
    }

	@Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT, TickType.PLAYER);
        // In my testing only RENDER, CLIENT, & PLAYER did anything on the client side.
        // Read 'cpw.mods.fml.common.TickType.java' for a full list and description of available types
    }

    @Override
    public String getLabel()
    {
        return null;
    }

    public void onRenderTick()
    {
    	
    	if (mc == null) mc = ModLoader.getMinecraftInstance();
    	if (worldRef == null) worldRef = ModLoader.getMinecraftInstance().theWorld;
        if (player == null) player = ModLoader.getMinecraftInstance().thePlayer;
        
        if (worldRef == null || player == null) {
            return;
        }
        
        //super crappy temp gui open code
        if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT)) {
        	if (!(mc.currentScreen instanceof GuiConfigEditor)) mc.displayGuiScreen(new GuiConfigEditor());
        }
        
        if(timeout > 0 && msg != null) {
            ScaledResolution var8 = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int var4 = var8.getScaledWidth();
            int var10 = var8.getScaledHeight();
            int var6 = mc.fontRenderer.getStringWidth(msg);
            mc.fontRenderer.drawStringWithShadow(msg, 3, 105, 16777215);
            
        }
        
    }

    public void onTickInGUI(GuiScreen guiscreen)
    {
        //TODO: Your Code Here
    }
    
    public void onTickInGame() {
		// TODO Auto-generated method stub
		

        if (worldRef == null || player == null) {
            return;
        }
        
        if (timeout > 0) --timeout;
	}
    
    public static int timeout;
    public static String msg;
    public static int color;
    public static int defaultColor = 16777215;
    public static boolean ingui;
    
    public static void displayMessage(String var0, int var1) {
        msg = var0;
        timeout = 100;
        color = var1;
    }

    public static void displayMessage(String var0) {
        displayMessage(var0, defaultColor);
    }
}
