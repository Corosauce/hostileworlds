package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobEatEntities;
import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityInvader;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ZombieHungry extends EntityInvader {

	//eats other zombies, grows when eating them
	public float sizeAmp = 1F;
	public float sizeAmpMax = 2F;
	public float baseWidth;
	public float baseHeight;
	public float baseOffset;
	
	public ZombieHungry(World par1World) {
		super(par1World);
		
		baseWidth = width;
		baseHeight = height;
		baseOffset = yOffset;

		updateSize();
        
		
		
        agent.jobMan.addPrimaryJob(new JobEatEntities(agent.jobMan));
		agent.jobMan.addJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Items.rotten_flesh));
		this.setCurrentItemOrArmor(4, new ItemStack(Items.chainmail_helmet));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		getDataWatcher().addObject(28, Float.valueOf(0)); //entity size
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readEntityFromNBT(par1nbtTagCompound);
		sizeAmp = par1nbtTagCompound.getFloat("sizeAmp");
		updateSize();
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeEntityToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setFloat("sizeAmp", sizeAmp);
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (!worldObj.isRemote) {
			getDataWatcher().updateObject(28, sizeAmp);
		} else {
			sizeAmp = getDataWatcher().getWatchableObjectFloat(28);
			updateSize();
		}
	}
	
	public void addSize(float parFloat) {
		sizeAmp += parFloat;
		if (sizeAmp > sizeAmpMax) {
			sizeAmp = sizeAmpMax;
		}
		
		updateSize();
		
		//System.out.println("Hungry Zombie size: " + sizeAmp);
		
		
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
	}
	
	public void updateSize() {
		this.yOffset = baseOffset * sizeAmp;
		this.width = baseWidth * sizeAmp;
		this.height = baseHeight * sizeAmp;
        this.setSize(baseWidth * sizeAmp, baseHeight * sizeAmp);
        this.setPosition(posX, posY, posZ);
        agent.setSpeedNormalBase(Math.max(0.45F, (0.55F / Math.max(1F, (sizeAmp * 0.5F)))));
        //System.out.println("new speed: " + (0.55F / (sizeAmp * 0.5F)));
        agent.applyEntityAttributes();
	}
	
	@Override
	public float getBonusDamage(Entity targetEnt, float dist) {
		return 2 * sizeAmp;
	}

}
