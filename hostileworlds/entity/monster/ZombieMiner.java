package hostileworlds.entity.monster;

import hostileworlds.HostileWorlds;
import hostileworlds.ai.IChunkLoader;
import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.ai.jobs.JobPathDigger;
import hostileworlds.entity.EntityInvader;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ZombieMiner extends EntityInvader implements IChunkLoader {

	public Ticket ticket = null;
	
	public ZombieMiner(World par1World) {
		super(par1World);
		
		agent.jobMan.addPrimaryJob(new JobPathDigger(agent.jobMan));
		//agent.jobMan.addJob(new JobPathDigger(agent.jobMan)); //digger extends grouphorde
		agent.jobMan.addJob(new JobHunt(agent.jobMan));
		
		this.setCurrentItemOrArmor(0, new ItemStack(Item.pickaxeIron));
		this.setCurrentItemOrArmor(4, new ItemStack(Item.helmetIron));
		
		agent.setMoveSpeed(0.28F);
		agent.shouldFixBadYPathing = false;
		entityCollisionReduction = 1F;
		agent.collideResistClose = 1F;
		agent.collideResistPathing = 1F;
	}
	
	@Override
	public void updateAITasks() {
		super.updateAITasks();
		
		if (ticket == null) {
			System.out.println("zombie miner init request ticket");
			requestTicket();
			forceChunkLoading(this.chunkCoordX, this.chunkCoordZ);
		}
	}
	
	@Override
	public int getMaxHealth() {
		return 50;
	}

	@Override
	public void setChunkTicket(Ticket parTicket) {
		if (ticket != null && parTicket != ticket) {
			ForgeChunkManager.releaseTicket(ticket);
		}
		ticket = parTicket;
	}

	@Override
	public void forceChunkLoading(int chunkX, int chunkZ) {
		if (ticket == null) return;
		
		//System.out.println("loading chunks for miner");
		
		Set<ChunkCoordIntPair> chunks = getChunksAround(chunkX, chunkZ, 3);
		
		for (ChunkCoordIntPair chunk : chunks) {
			ForgeChunkManager.forceChunk(this.ticket, chunk);
	    }

		ChunkCoordIntPair myChunk = new ChunkCoordIntPair(chunkX, chunkZ);
		ForgeChunkManager.forceChunk(this.ticket, myChunk);
		ForgeChunkManager.reorderChunk(this.ticket, myChunk);
		
	}
	
	public Set getChunksAround(int xChunk, int zChunk, int radius) {
		Set chunkList = new HashSet();
		for (int xx = xChunk - radius; xx <= xChunk + radius; xx++) {
			for (int zz = zChunk - radius; zz <= zChunk + radius; zz++) {
				chunkList.add(new ChunkCoordIntPair(xx, zz));
			}
		}
		return chunkList;
	}
	
	public void requestTicket() {
		ForgeChunkManager.Ticket chunkTicket = ForgeChunkManager.requestTicket(HostileWorlds.instance, worldObj, ForgeChunkManager.Type.ENTITY);
	    if (chunkTicket != null)
	    {
	    	chunkTicket.getModData();
	        chunkTicket.setChunkListDepth(12);
	        chunkTicket.bindEntity(this);
	        setChunkTicket(chunkTicket);
	    }
	}
}
