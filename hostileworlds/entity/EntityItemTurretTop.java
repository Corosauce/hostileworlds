package hostileworlds.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroAI.componentAI.AIAgent;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.IInvUser;
import CoroAI.componentAI.jobSystem.JobHuntTurret;
import CoroAI.diplomacy.DiplomacyHelper;
import CoroAI.diplomacy.TeamTypes;
import CoroAI.entity.c_EnhAI;

public class EntityItemTurretTop extends EntityFlying implements ICoroAI, IInvUser {

	public AIAgent agent;
	
	public Vec3 spawnPos;
	
	public int rangedCooldown = 5;
	
	public float rotation = 0;
	
	public EntityItemTurretTop(World par1World) {
		super(par1World);
		//texture = "/mods/HostileWorlds/textures/blocks/bleedingMoss.png";
		setSize(0.2F, 0.3F);
		
		checkAgent();
		
		agent.jobMan.addPrimaryJob(new JobHuntTurret(agent.jobMan));
		
		agent.maxReach_Ranged = 40;
		
		agent.setSpeedFleeAdditive(0F);
		agent.setSpeedNormalBase(0.0F);
		
		agent.dipl_info = TeamTypes.getType("comrade");
		agent.collideResistClose = agent.collideResistFormation = agent.collideResistPathing = 1F;
		entityCollisionReduction = 1F;
		
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D);
	}

	@Override
	public void cleanup() {
		agent = null;
	}
	
	public void checkAgent() {
		if (agent == null) agent = new AIAgent(this, true);
	}
	
	@Override
	public void postInitFakePlayer() {
		
	}

	@Override
	public void updateAITasks() {
		agent.updateAITasks();
	}
	
	@Override
	public boolean isInRangeToRenderDist(double var1) {
        return Math.sqrt(var1) < 64;
    }
	
	@Override
	public void onLivingUpdate() {
		// TODO Auto-generated method stub
		super.onLivingUpdate();
		agent.onLivingUpdateTick();
	}
	
	@Override
	public void onUpdate() {
		
		if (spawnPos == null) {
			spawnPos = Vec3.createVectorHelper(posX, posY, posZ);
		} else {
			//this.setRotation(45, 0);
			this.setPosition(spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord);
			motionX = motionY = motionZ = 0D;
		}
		
		if (!worldObj.isRemote) {
			if (agent.entInv.fakePlayer != null) {
				rotationYaw = agent.entInv.fakePlayer.rotationYaw;
				getDataWatcher().updateObject(28, (int)rotationYaw);
			}
			//agent.updateAITasks();
			//setDead();
			//agent.entInv.rangedInUseTicksMax = 5;
		} else {
			
			
			if (getDataWatcher().getWatchableObjectInt(28) != rotation) {
				rotation = getDataWatcher().getWatchableObjectInt(28);
				//System.out.println("rotation: " + rotation);
				rotationYaw = rotation;
			} else {
				rotationYaw += 2F;
			}
			
			rotationYawHead = rotationYaw;
			
			//System.out.println(entityId + " - rotationYaw: " + rotationYaw);
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
		return 5;
	}

	@Override
	public int getCooldownRanged() {
		// TODO Auto-generated method stub
		return agent.entInv.coolDownRangedOutSource;
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
		//if (ent instanceof EntityPlayer/* && !((EntityPlayer) ent).capabilities.isCreativeMode*/) return true;
		if (ent instanceof c_EnhAI) return true;
		if (ent instanceof EntityMob && !agent.isThreat(ent)) return true;
		return DiplomacyHelper.shouldTargetEnt(this, ent);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		checkAgent();
		agent.entityInit();
		getDataWatcher().addObject(28, Integer.valueOf(0));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
		super.readEntityFromNBT(par1nbtTagCompound);
		checkAgent();
		agent.readEntityFromNBT(par1nbtTagCompound);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
		super.writeEntityToNBT(par1nbtTagCompound);
		agent.writeEntityToNBT(par1nbtTagCompound);
	}
	
	@Override
	public void despawnEntity() {
		
	}
	
	@Override
	public String getLocalizedName() {
		return "Item Turret";
	}
}
