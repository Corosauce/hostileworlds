package hostileworlds.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroUtil.componentAI.AIAgent;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.diplomacy.TeamTypes;

public class EntityWorm extends EntityFlying implements ICoroAI {

	public AIAgent agent;
	
	public EntityWorm(World par1World) {
		super(par1World);
		//texture = "/tropicalmod/test.png";
		//this.texture = "/mob/zombie.png";
		//setSize(0.6F, 2F);
	
		if (agent == null) agent = new AIAgent(this, false);
		
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		
		agent.dipl_info = TeamTypes.getType("undead");
		entityCollisionReduction = 0.9F;
	}

	@Override
	public void cleanup() {
		agent = null;
	}

	@Override
	public void updateAITasks() {
		agent.updateAITasks();
	}
	
	@Override
	public void onUpdate() {
		if (!worldObj.isRemote) {
			//agent.updateAITasks();
			//setDead();
		}
		super.onUpdate(); 
	}
	
	@Override
	protected boolean isAIEnabled()
    {
        return true;
    }
	
	@Override
	public boolean canDespawn() {
		return false;
	}

	@Override
	public AIAgent getAIAgent() {
		return agent;
	}

	@Override
	public void setPathResultToEntity(PathEntity pathentity) {
		agent.setPathToEntity(pathentity);
	}

	@Override
	public int getCooldownMelee() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCooldownRanged() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void attackMelee(Entity ent, float dist) {
		ent.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
	}

	@Override
	public void attackRanged(Entity ent, float dist) {
		//System.out.println("coroai generic ranged attack!");
	}

	@Override
	public boolean isBreaking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnemy(Entity ent) {
		// TODO Auto-generated method stub
		if (ent instanceof EntityPlayer/* && !((EntityPlayer) ent).capabilities.isCreativeMode*/) return true;
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		if (agent == null) agent = new AIAgent(this, false);
		agent.entityInit();
		
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1) {
		// TODO Auto-generated method stub
		super.readEntityFromNBT(var1);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1) {
		// TODO Auto-generated method stub
		super.writeEntityToNBT(var1);
	}
	
	@Override
	public void despawnEntity() {
		
	}
}
