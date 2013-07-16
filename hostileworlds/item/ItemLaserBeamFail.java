package hostileworlds.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

/* extends to allow inner logic fixes to still happen */
public class ItemLaserBeamFail extends Item
{
	public Icon particles[] = new Icon[8];
	
	//Config
	public int ticksChargeUp = 20;
	public int ticksCooldown = 20 * 3;
	public int ticksUsage = 20*5;
	
    public ItemLaserBeamFail(int i)
    {
        super(i);
        maxStackSize = 1;
        setMaxDamage(100);
    }

    public void check(ItemStack itemstack) {
    	if (itemstack.stackTagCompound == null) itemstack.stackTagCompound = new NBTTagCompound();
    }
    
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
    	check(itemstack);
    	int usage = itemstack.stackTagCompound.getInteger("ticksInUse");
    	if (!entityplayer.isUsingItem() || usage == -1) {
    		entityplayer.setItemInUse(itemstack, this.getMaxItemUseDuration(itemstack));
    		if (!world.isRemote) itemstack.stackTagCompound.setInteger("ticksInUse", 0);
    	}
        return itemstack;
    }
    
    @Override
    public void onUsingItemTick(ItemStack stack, EntityPlayer player, int count) {
    	super.onUsingItemTick(stack, player, count);
    	
    	check(stack);
    	int usage = stack.stackTagCompound.getInteger("ticksInUse");
    	if (player.isUsingItem() && usage != -1) {
    		/*if (!player.worldObj.isRemote) */stack.stackTagCompound.setInteger("ticksInUse", usage + 1);
    	} else {
    		player.clearItemInUse();
    	}
    }
    
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World,
    		Entity par3Entity, int par4, boolean par5) {
    	super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
    	
    	if (!(par3Entity instanceof EntityPlayer)) return;
    	check(par1ItemStack);
    	EntityPlayer entP = (EntityPlayer)par3Entity;
    	if (!entP.isUsingItem()) {
    		if (!par2World.isRemote) par1ItemStack.stackTagCompound.setInteger("ticksInUse", -1);
    		onPlayerStoppedUsing(par1ItemStack, par2World, entP, par4);
    	}
    }
    
    @Override
    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World,
    		EntityPlayer par3EntityPlayer, int par4) {
    	super.onPlayerStoppedUsing(par1ItemStack, par2World, par3EntityPlayer, par4);
    	check(par1ItemStack);
    	par3EntityPlayer.clearItemInUse();
    	if (!par2World.isRemote) par1ItemStack.stackTagCompound.setInteger("ticksInUse", -1);
    	
    }
    
    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
    	return ticksChargeUp+ticksUsage;
    }
}
