package hostileworlds.gui;

import hostileworlds.block.TileEntityItemTurret;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerItemTurret extends Container {

	protected TileEntityItemTurret tileEntity;
	
	public int lastEUAmount = 0;
	public int lastShootTicksToCharge = 0;
	public int lastShootTicksBetweenShots = 0;
	public int lastShootRange = 0;
	public boolean lastMeleeRightClick = false;

    public ContainerItemTurret (InventoryPlayer inventoryPlayer, TileEntityItemTurret te){
        tileEntity = te;

        //the Slot constructor takes the IInventory and the slot number in that it binds to
        //and the x-y coordinates it resides on-screen
        /*for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlotToContainer(new Slot(tileEntity, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }*/
        
        int offsetX = 126;
        
        addSlotToContainer(new Slot(tileEntity, 0, 8, 17));
        addSlotToContainer(new Slot(tileEntity, 1, 8, 17 + (18 * 1)));
        addSlotToContainer(new Slot(tileEntity, 2, 8, 17 + (18 * 2)));
        addSlotToContainer(new Slot(tileEntity, 3, 8 + offsetX, 17 + (18 * 0)));
        addSlotToContainer(new Slot(tileEntity, 4, 8 + offsetX, 17 + (18 * 1)));
        addSlotToContainer(new Slot(tileEntity, 5, 8 + offsetX, 17 + (18 * 2)));

        //commonly used vanilla code that adds the player's inventory
        bindPlayerInventory(inventoryPlayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.isUseableByPlayer(player);
    }


    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
                                    8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        ItemStack stack = null;
        Slot slotObject = (Slot) inventorySlots.get(slot);

        //null checks and checks if the item can be stacked (maxStackSize > 1)
        if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            try {
	            //merges the item into player inventory since its in the tileEntity
	            if (slot < 9) {
	                if (!this.mergeItemStack(stackInSlot, 9, 39, true)) {
	                    return null;
	                }
	            }
	            //places it into the tileEntity is possible since its in the player inventory
	            else if (!this.mergeItemStack(stackInSlot, 0, 9, false)) {
	                return null;
	            }
	
	            if (stackInSlot.stackSize == 0) {
	                slotObject.putStack(null);
	            } else {
	                slotObject.onSlotChanged();
	            }
	
	            if (stackInSlot.stackSize == stack.stackSize) {
	                return null;
	            }
	            slotObject.onPickupFromSlot(player, stackInSlot);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        }
        return stack;
    }
    
    //Overrides for handling custom data syncing
    
    @Override
    public void addCraftingToCrafters(ICrafting par1ICrafting)
    {
        super.addCraftingToCrafters(par1ICrafting);
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.lastEUAmount != this.tileEntity.powerEU)
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tileEntity.powerEU/100);
            }
            
            if (this.lastShootTicksToCharge != this.tileEntity.shootTicksToCharge)
            {
                icrafting.sendProgressBarUpdate(this, 1, this.tileEntity.shootTicksToCharge);
            }
            
            if (this.lastShootTicksBetweenShots != this.tileEntity.shootTicksBetweenShots)
            {
                icrafting.sendProgressBarUpdate(this, 2, this.tileEntity.shootTicksBetweenShots);
            }
            
            if (this.lastMeleeRightClick != this.tileEntity.meleeRightClick)
            {
                icrafting.sendProgressBarUpdate(this, 3, this.tileEntity.meleeRightClick ? 1 : 0);
            }
            
            if (this.lastShootRange != this.tileEntity.shootRange)
            {
                icrafting.sendProgressBarUpdate(this, 4, this.tileEntity.shootRange);
            }
            
            
            
        }

        this.lastEUAmount = this.tileEntity.powerEU;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2)
    {
        if (par1 == 0)
        {
        	this.tileEntity.powerEU = par2*100;
        } else if (par1 == 1)
        {
        	this.tileEntity.shootTicksToCharge = par2;
        } else if (par1 == 2)
        {
        	this.tileEntity.shootTicksBetweenShots = par2;
        } else if (par1 == 3)
        {
        	this.tileEntity.meleeRightClick = par2 == 1;
        } else if (par1 == 4)
        {
        	this.tileEntity.shootRange = par2;
        }
    }
}
