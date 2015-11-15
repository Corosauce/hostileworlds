package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobBossCloseCombat;
import hostileworlds.ai.jobs.JobBossWielder;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityInvader;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import particleman.forge.ParticleMan;

public class ZombieBlockWielder extends EntityInvader implements IBossDisplayData {

	public JobBossCloseCombat priJob;
	
	public ZombieBlockWielder(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(priJob = new JobBossCloseCombat(agent.jobMan));
		agent.jobMan.addJob(new JobBossWielder(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(ParticleMan.itemGlove));
		this.setCurrentItemOrArmor(4, new ItemStack(Items.iron_helmet));
		
		setSize(width * 1.5F, height * 1.5F);
		ignoreFrustumCheck = true;
		
		
		for (int i = 0; i < this.equipmentDropChances.length; ++i)
        {
            this.equipmentDropChances[i] = 2;
        }
		
		func_110163_bv();
		
		agent.maxReach_Melee = 2F;
	}
	


	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(250.0D);
	}
	
	@Override
	public void attackMelee(Entity ent, float dist) {
		
		float calcDamage = 5;
		agent.maxReach_Melee = 2F;
		//easy
        if (ModConfigFields.difficulty == 0)
        {
        	calcDamage = calcDamage / 2 + 1;
        }
        
        //hard
        if (ModConfigFields.difficulty == 2)
        {
        	calcDamage = calcDamage * 3 / 2;
        }
        
        calcDamage += (HWDifficulty / 25F);
        
        //System.out.println("final damage:" + calcDamage);
        
		ent.attackEntityFrom(new EntityDamageSource("mob", this) { public boolean isDifficultyScaled() { return false; } }, (int)calcDamage);
	}
	
	@Override
	public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_,
			double p_145770_5_) {
		return true;
	}
	
	@Override
	public void onDeath(DamageSource par1DamageSource) {
		this.recentlyHit = 99; //fix for no drops due to non direct killing (particles etc)
		super.onDeath(par1DamageSource);
		
		if (!worldObj.isRemote) {
			this.entityDropItem(new ItemStack(Blocks.redstone_torch, 5, 0), 0.0F);
			this.entityDropItem(new ItemStack(Blocks.torch, 5, 0), 0.0F);
		}
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeEntityToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setBoolean("spawnedWorm", priJob.spawnedWorm);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readEntityFromNBT(par1nbtTagCompound);
		priJob.spawnedWorm = par1nbtTagCompound.getBoolean("spawnedWorm");
	}

}
