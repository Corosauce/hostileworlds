package hostileworlds.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import hostileworlds.ai.jobs.JobGroupHorde;
import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityInvader;

public class ZombieHungry extends EntityInvader {

	public ZombieHungry(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Item.rottenFlesh));
		this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetChain));
		
		agent.setMoveSpeed(0.24F);
	}

}
