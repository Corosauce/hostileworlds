package hostileworlds.block;

import hostileworlds.HostileWorlds;
import hostileworlds.client.sound.MovingSoundStreamingSource;
import hostileworlds.entity.particle.EntityAuraCurseFX;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySourceStructure extends TileEntity
{

	//central point for a structure generated in overworld, should be tracked and managed like source invasion via world director, this is more of a marker block
	//maybe do ent spawning from this block
	//eventually use the sound part too once we have something for it
	
	@SideOnly(Side.CLIENT)
    public MovingSoundStreamingSource sound;
	
	public ArrayList<EntityAuraCurseFX> particles = new ArrayList<EntityAuraCurseFX>();
	
    public TileEntitySourceStructure() {
    	
    }
    
    public void updateEntity()
    {
    	if (worldObj.isRemote) {
    		herp();
    		//tickSound();
    	} else {
    		
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickSound() {
    	//if (this.lastPlayTime < System.currentTimeMillis())
        //{
    		float range = 120;
    		EntityPlayer player = worldObj.getClosestPlayer(xCoord, yCoord, zCoord, range);

            if (player != null)
            {
                playNonMovingSound(Vec3.createVectorHelper(xCoord, yCoord, zCoord), HostileWorlds.modID + ":bossfight", 1.0F, 1.0F, range);
            
            }
        //}
    }
    
    @SideOnly(Side.CLIENT)
    public void playNonMovingSound(Vec3 parPos, String var1, float var5, float var6, float parCutOffRange)
    {
    	if (sound == null) {
    		System.out.println("PLAY!!!!!!!!!!!!!!!!!!!!!");
	    	ResourceLocation res = new ResourceLocation(var1);
	    	sound = new MovingSoundStreamingSource(parPos, res, var5, var6, parCutOffRange);
	    	FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    	}
    	
    	/*if (sound.isDonePlaying()) {
    		System.out.println("PLAY!!!!!!!!!!!!!!!!!!!!!");
    		FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    	}*/
    }
    
    @SideOnly(Side.CLIENT)
    public void stopSound() {
    	if (sound != null) {
    		sound.stopPlaying();
    	}
    }
    
    @Override
    public void invalidate() {
    	super.invalidate();
    	if (worldObj.isRemote) {
    		stopSound();
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void herp() {
    	
    	
    	if (Minecraft.getMinecraft().thePlayer.getDistance(xCoord, yCoord, zCoord) < 64) {
    		for (int i = 0; i < 3; i++) {
	    		Random rand = new Random();
	    		double range = 1D;
	    		//EntityAuraCurseFX particle = new EntityAuraCurseFX(worldObj, this.xCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, yCoord + rand.nextDouble()*range, this.zCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, 0, 0.25F, 0, 30);
	    		EntityReddustFX particle = new EntityReddustFX(worldObj, this.xCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, yCoord + rand.nextDouble()*range, this.zCoord + 0.5F + rand.nextDouble()*range - rand.nextDouble()*range, 2F, rand.nextFloat(), 0F, 0F);
		    	//particles.add(particle);
		    	Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    		}
    	}
    }

    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
}
