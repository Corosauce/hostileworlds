package hostileworlds.rts.entity;

import hostileworlds.HostileWorlds;
import hostileworlds.rts.RtsEngine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroAI.bt.OrdersHandler;
import CoroAI.bt.actions.Delay;
import CoroAI.bt.actions.OrdersUser;
import CoroAI.bt.selector.Selector;
import CoroAI.bt.selector.SelectorSequence;
import CoroAI.componentAI.AIAgent;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.jobSystem.JobBehavior;
import CoroAI.diplomacy.DiplomacyHelper;
import CoroAI.diplomacy.TeamTypes;
import CoroAI.entity.c_EnhAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityRtsBase extends EntityLiving implements ICoroAI {

	public AIAgent agent;
	
	//For team management
	public int teamID = -1;
	public String unitType = "worker";
	//Building progress stuff
	public boolean building = false;
	
	public JobBehavior bh;

	public boolean wasInWater;
	
	public EntityRtsBase(World par1World) {
		super(par1World);
		//texture = "/tropicalmod/test.png";
		//this.texture = "/mods/ZombieCraft/textures/entities/comrade/skin3.png";
		//setSize(0.6F, 2F);
		
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		agent.jobMan.addPrimaryJob(bh = new JobBehavior(agent.jobMan));
		//agent.jobMan.addJob(new JobHunt(agent.jobMan));
		bh.ordersHandler = new OrdersHandler(this);
		
		//super simple orders using behavior tree
		Selector sel0 = new SelectorSequence(null);
		sel0.add(new OrdersUser(sel0, bh.ordersHandler, "gather"));
		sel0.add(new Delay(sel0, 1, 0));
		bh.trunk = sel0;
		agent.PFRangePathing = 64;
		//agent.entInv.shouldLookForPickups = true;
		//agent.entInv.grabItems = true;
		//agent.entInv.grabXP = true;
		agent.maxReach_Ranged = 25;
		agent.maxReach_Melee = 2.2F;
		agent.dipl_info = TeamTypes.getType("comrade");
		agent.shouldAvoid = false;
		
		agent.collideResistClose = agent.collideResistFormation = agent.collideResistPathing = entityCollisionReduction = 0.9F;
		
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		agent.setSpeedFleeAdditive(0.1F);
		agent.setSpeedNormalBase(0.6F);
		agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D);
	}

	@Override
	public void cleanup() {
		agent = null;
	}
	
	public void checkAgent() {
		if (agent == null) agent = new AIAgent(this, false);
	}
	
	@Override
	protected void entityInit()
    {
		super.entityInit();
		checkAgent();
		agent.entityInit();
		getDataWatcher().addObject(25, Integer.valueOf(0)); //teamID
    }

	/*@Override
	public void postInitFakePlayer() {
		if (agent.entInv.inventory.mainInventory[0] == null) {
			if (factory != null) {
				factory.injectStartingInventory(this);
			} else {
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.swordDiamond));
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.bow));
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 64));
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 64));
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 64));
				agent.entInv.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 64));
			}
		}
	}*/
	
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer) {
		if (building) return false;
		checkAgent();
		return agent.hookInteract(par1EntityPlayer);
	}
	
	@Override
	public boolean getCanSpawnHere()
    {
		return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox);
    }
	
	@Override
	public void updateAITasks() {
		if (building) return;
		agent.updateAITasks();
		//this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(1D);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
		super.readEntityFromNBT(par1nbtTagCompound);
		teamID = par1nbtTagCompound.getInteger("teamID");
		unitType = par1nbtTagCompound.getString("unitType");
		checkAgent();
		agent.readEntityFromNBT(par1nbtTagCompound);
		HostileWorlds.initTry();
		RtsEngine.teams.registerWithTeam(teamID, unitType, this);
	}
	
	@Override
	public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingBaseData)
    {
		//make it try to register with team 0 if has no team because it was spawned outside standard rts methods
		if (teamID == -1) {
			teamID = 0;
			System.out.println("Rts Unit has no team, trying to join team 0");
			RtsEngine.teams.registerWithTeam(teamID, unitType, this);
		}
		return super.onSpawnWithEgg(par1EntityLivingBaseData);
    }
	
	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
		super.writeEntityToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("teamID", teamID);
		par1nbtTagCompound.setString("unitType", unitType);
		agent.writeEntityToNBT(par1nbtTagCompound);
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
		return 20;
	}

	@Override
	public int getCooldownRanged() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public void attackMelee(Entity ent, float dist) {
		checkAgent();
		ent.attackEntityFrom(DamageSource.causeMobDamage(this), 6);
		//agent.entInv.attackMelee(ent, dist);
	}

	@Override
	public void attackRanged(Entity ent, float dist) {
		//System.out.println("coroai generic ranged attack!");
		checkAgent();
		//agent.entInv.attackRanged(ent, dist);
	}

	@Override
	public boolean isBreaking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnemy(Entity ent) {
		// TODO Auto-generated method stub
		//if (ent instanceof EntityPlayer && !((EntityPlayer) ent).capabilities.isCreativeMode) return true;
		if (ent instanceof c_EnhAI) return true;
		if ((ent instanceof EntityMob) && !agent.isThreat(ent)) return true;
		return DiplomacyHelper.shouldTargetEnt(this, ent);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
		if (!worldObj.isRemote && (agent == null || agent.jobMan == null)) return false;
		//TEMP INTERACT WAY
		/*if (!worldObj.isRemote && par1DamageSource.getEntity() instanceof EntityPlayer && !par1DamageSource.isProjectile()) {
			EntityPlayer entP = (EntityPlayer)par1DamageSource.getEntity();
			
			if (agent.entInv.inventory != null && entP.getCurrentEquippedItem() != null) {
				agent.dbg("giving comrade melee item");
				agent.entInv.inventory.setInventorySlotContents(0, entP.getCurrentEquippedItem().copy());
				agent.entInv.sync();
			}
			
			//no damage
			return false;
		}*/
		
		
		if (agent.jobMan.getPrimaryJob().hookHit(par1DamageSource, (int)par2)) {
			if (par1DamageSource != DamageSource.inWall) {
				return super.attackEntityFrom(par1DamageSource, par2);
			} else {
				return false;
			}
		} else {
			return false;
		}
    }
	
	//special mob methods
	@Override
	public void onLivingUpdate() {
		agent.onLivingUpdateTick();
		
		if (!isInWater() && wasInWater == true && isCollidedHorizontally) motionY = 0.44F; 
		
		wasInWater = isInWater();
		//tameify!
		/*if (!worldObj.isRemote && agent.jobMan.getPrimaryJob().tamable.owner.equals("")) {
			EntityPlayer entP = worldObj.getClosestPlayerToEntity(this, -1);
			if (entP != null) {
				agent.jobMan.getPrimaryJob().tamable.tameBy(entP.username);
			}
		}*/
		
		//fallDistance = 0;
		
		if (worldObj.isRemote) { //in render file now
			//teamID = getDataWatcher().getWatchableObjectInt(25);
			//this.texture = "/mods/ZombieCraft/textures/entities/comrade/skin" + teamID + ".png";
		} else {
			getDataWatcher().updateObject(25, teamID);
			//System.out.println(agent.moveSpeed);
			//System.out.println(getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
			//getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(1);
			
		}
		
		super.onLivingUpdate();
	}

	@Override
	public boolean canDespawn() {
		return false;
	}
	
	@Override
	public boolean isInRangeToRenderDist(double par1)
    {
		return Math.sqrt(par1) < 512F;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
    {
		super.setPositionAndRotation2(par1, par3, par5, par7, par8, par9);
        /*this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);*/        
    }
}
