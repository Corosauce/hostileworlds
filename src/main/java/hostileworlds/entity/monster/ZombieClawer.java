package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.ai.jobs.JobPathClawer;
import hostileworlds.entity.EntityInvader;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;

public class ZombieClawer extends EntityInvader {

	public ZombieClawer(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobPathClawer(agent.jobMan));
		agent.jobMan.addJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_shovel));
		this.setCurrentItemOrArmor(4, new ItemStack(Items.chainmail_helmet));
		
	}
	
	@Override
	public int getMaxSpawnedInChunk() {
		return 1;//super.getMaxSpawnedInChunk();
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		agent.setSpeedNormalBase(0.55F);
		agent.applyEntityAttributes();
	}

}
