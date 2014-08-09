package hostileworlds.item;

import hostileworlds.client.gui.GuiQuestListing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import particleman.element.Element;
import particleman.entities.EntityParticleControllable;
import particleman.forge.ParticleMan;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemQuestBook extends Item {
	
	public ItemQuestBook() {
		super();
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		
		if (par2World.isRemote) {
			openGUI();
		}
		
		return par1ItemStack;
	}
	
	@SideOnly(Side.CLIENT)
	public void openGUI() {
		FMLClientHandler.instance().getClient().displayGuiScreen(new GuiQuestListing());
	}

}
