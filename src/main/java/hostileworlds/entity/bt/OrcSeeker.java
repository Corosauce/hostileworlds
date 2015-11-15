package hostileworlds.entity.bt;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.abilities.AbilityLeapAttack;
import hostileworlds.entity.abilities.AbilityLungeAttack;
import hostileworlds.entity.bt.ai.SeekPlayerBase;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import CoroUtil.IChunkLoader;
import CoroUtil.ability.abilities.AbilityAttackMelee;
import CoroUtil.ability.abilities.AbilityShootArrow;

public class OrcSeeker extends EnemyBase implements IChunkLoader {

	public Ticket ticket = null;
	
	public OrcSeeker(World par1World) {
		super(par1World);
		
		//marked as no despawn in initRPGStats in super class
		
		
	}
	
	@Override
	public void initExtraAI() {
		super.initExtraAI();
		
		agent.btAI.add(new SeekPlayerBase(agent.btAI, this, 16F));
	}
	
	/*@Override
	public boolean isEnemy(Entity ent) {
		if (ent instanceof EntityPlayer && !((EntityPlayer) ent).capabilities.isCreativeMode) return true;
		return super.isEnemy(ent);
	}*/
	
	@Override
	public void initRPGStats() {
		super.initRPGStats();
		
		agent.entInv.setSlotContents(agent.entInv.slot_Melee, new ItemStack(Items.iron_sword));
		agent.entInv.setSlotContents(agent.entInv.slot_Ranged, new ItemStack(Items.bow));
		//this.setCurrentItemOrArmor(0, new ItemStack(Item.swordIron));
		this.setEquipmentDropChance(0, 0);

        getAIBTAgent().profile.addAbilityMelee(new AbilityAttackMelee().init(this));
        getAIBTAgent().profile.addAbilityMelee(new AbilityLeapAttack().init(this));
        getAIBTAgent().profile.addAbilityMelee(new AbilityLungeAttack().init(this));
        getAIBTAgent().profile.addAbilityRanged(new AbilityShootArrow().init(this));
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();

		agent.setSpeedNormalBase(0.55F);
		agent.applyEntityAttributes();
		
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50);
	}
	
	/*@Override
	protected String getDeathSound() {
		return RPGMod.modID + ":death_imp";
	}*/
	
	@Override
	public String getCommandSenderName() {
		return "OrcSeeker";
	}
	
	/*@Override
	public void hookSetTargetPre(Entity target) {
		if (getAIBTAgent().blackboard.getTarget() == null && target instanceof EntityPlayer) {
			worldObj.playSoundEffect(this.posX, this.posY, this.posZ, RPGMod.modID+":sight_orc", 0.9F, 0.7F + worldObj.rand.nextFloat() * 0.3F);
		}
		super.hookSetTargetPre(target);
	}*/

	@Override
	public void updateAITasks() {
		super.updateAITasks();
		
		if (ticket == null) {
			initChunkLoad();
		}
	}
	
	public void initChunkLoad() {
		System.out.println("OrcSeeker init request ticket");
		requestTicket();
		forceChunkLoading(this.chunkCoordX, this.chunkCoordZ);
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
