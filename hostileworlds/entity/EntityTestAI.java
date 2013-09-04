package hostileworlds.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroAI.componentAI.AIAgent;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.jobSystem.JobHunt;

public class EntityTestAI extends EntityLiving implements ICoroAI {

	public AIAgent agent;
	
	public EntityTestAI(World par1World) {
		super(par1World);
		setSize(.5F, .8F);
	
		agent = new AIAgent(this, false);
		
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
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
	protected boolean isAIEnabled()
    {
        return true;
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
		System.out.println("coroai generic ranged attack!");
	}

	@Override
	public boolean isBreaking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnemy(Entity ent) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//special mob methods
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		
		if (worldObj.isRemote) {
			//EntityRotFX firefly = new EntityFireflyFX(worldObj, (double)posX, (double)posY + 0.5, (double)posZ, 0D, 0D, 0D, 60D, 1);
	        //c_CoroWeatherUtil.setParticleGravity((EntityFX)firefly, 0.1F);
	        
	        //Minecraft.getMinecraft().effectRenderer.addEffect(firefly);
	        //((EntityRotFX) firefly).spawnAsWeatherEffect();
		}
	}

}
