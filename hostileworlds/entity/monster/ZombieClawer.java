package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityInvader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ZombieClawer extends EntityInvader {

	public ZombieClawer(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Item.rottenFlesh));
		this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetChain));
	}

}
