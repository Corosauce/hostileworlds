package hostileworlds.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemLaserBeam extends Item
{
    public static final String[] bowPullIconNameArray = new String[] {"bow_pull_0", "bow_pull_1", "bow_pull_2"};
    @SideOnly(Side.CLIENT)
    private Icon[] iconArray;
    
    //Config
  	public int ticksChargeUp = 20;
  	public int ticksCooldown = 20 * 3; //switched to dynamic instead
  	public int ticksUsage = 20*5;

    public ItemLaserBeam(int par1)
    {
        super(par1);
        this.maxStackSize = 1;
        this.setMaxDamage(100);
        this.setCreativeTab(CreativeTabs.tabCombat);
    }

    public void check(ItemStack itemstack) {
    	if (itemstack.stackTagCompound == null) itemstack.stackTagCompound = new NBTTagCompound();
    }

    /**
     * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
     */
    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4)
    {
        int j = this.getMaxItemUseDuration(par1ItemStack) - par4;
        //if (!par2World.isRemote) {
        	System.out.println("stopped: " + j);
        //}
    }
    
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World,
    		Entity par3Entity, int par4, boolean par5) {
    	super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
    	
    	//if (!(par3Entity instanceof EntityPlayer)) return;
    	check(par1ItemStack);
    	EntityPlayer entP = (EntityPlayer)par3Entity;
    	int usage = par1ItemStack.stackTagCompound.getInteger("ticksInUse");
    	int cooldown = par1ItemStack.stackTagCompound.getInteger("ticksCooldown");
    	
    	if (!par5) return;
    	
    	if (!par2World.isRemote) {
    		
    		if (cooldown > 0) {
    			cooldown--;
    			par1ItemStack.stackTagCompound.setInteger("ticksCooldown", cooldown);
    			System.out.println("Cooling down: " + cooldown);
    		}
    		
	    	if (entP.isUsingItem()) {
	    		System.out.println("sdfdfsdfsdfsdfsdfsdfds: " + (this.getMaxItemUseDuration(par1ItemStack) - entP.getItemInUseCount()));
	    		par1ItemStack.stackTagCompound.setBoolean("wasInUse", true);
	    		par1ItemStack.stackTagCompound.setInteger("ticksInUse", usage + 1);
	    		entP.setItemInUse(par1ItemStack, entP.getItemInUseCount());
	    	} else {
	    		if (par1ItemStack.stackTagCompound.getBoolean("wasInUse")) {
	    			par1ItemStack.stackTagCompound.setBoolean("wasInUse", false);
	    			par1ItemStack.stackTagCompound.setInteger("ticksCooldown", usage / 4);
	    		}
	    		par1ItemStack.stackTagCompound.setInteger("ticksInUse", -1);
	    	}
    	} else {
    		if (!isPlayerHoldingDown()) {
    			//System.out.println("not holding!");
    			if (usage != -1) {
    				System.out.println("sending!");
    				sendStopUsing();
    			}
    		} else {
    			//entP.setItemInUse(par1ItemStack, entP.getItemInUseCount());
    			fixNBTCompare();
    			
    		}
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void fixNBTCompare() {
    	
    	//c_CoroAIUtil.setPrivateValueSRGMCP(PlayerControllerMP.class, Minecraft.getMinecraft().playerController, "field_85183_f", "field_85183_f", null);
    	//c_CoroAIUtil.setPrivateValueSRGMCP(ItemRenderer.class, Minecraft.getMinecraft().entityRenderer.itemRenderer, "equippedProgress", "equippedProgress", 0.85F);
    }
    
    @SideOnly(Side.CLIENT)
    public boolean isPlayerHoldingDown() {
    	Minecraft mc = Minecraft.getMinecraft();
    	return mc.gameSettings.keyBindUseItem.pressed;
    }
    
    @SideOnly(Side.CLIENT)
    public void sendStopUsing() {
    	Minecraft mc = Minecraft.getMinecraft();
    	mc.playerController.onStoppedUsingItem(mc.thePlayer);
    }

    public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        return par1ItemStack;
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
    	return ticksChargeUp+ticksUsage;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	check(par1ItemStack);
    	if (par1ItemStack.stackTagCompound.getInteger("ticksCooldown") == 0) {
    		par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
    	}
        return par1ItemStack;
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        super.registerIcons(par1IconRegister);
        this.iconArray = new Icon[bowPullIconNameArray.length];

        for (int i = 0; i < this.iconArray.length; ++i)
        {
            this.iconArray[i] = par1IconRegister.registerIcon(bowPullIconNameArray[i]);
        }
    }
}
