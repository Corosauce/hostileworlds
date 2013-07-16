package hostileworlds.entity.comrade;

import hostileworlds.block.TileEntityFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroAI.componentAI.AIAgent;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.IInvUser;
import CoroAI.componentAI.jobSystem.JobHunt;
import CoroAI.diplomacy.DiplomacyHelper;
import CoroAI.diplomacy.TeamTypes;
import CoroAI.entity.c_EnhAI;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityComradeBase extends EntityLiving implements ICoroAI, IInvUser {

	public AIAgent agent;
	
	public float HWDifficulty;
	public String primaryTarget = "";
	
	//Factory related stuff
	public boolean building = false;
	public TileEntityFactory factory = null;
	
	public EntityComradeBase(World par1World) {
		super(par1World);
		//texture = "/tropicalmod/test.png";
		this.texture = "/mods/ZombieCraft/textures/entities/comrade/skin0.png";
		//setSize(0.6F, 2F);
		
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		this.health = getMaxHealth();
		agent.setMoveSpeed(0.35F);
		agent.fleeSpeed = 0.4F;
		agent.PFRangePathing = 128;
		//agent.entInv.shouldLookForPickups = true;
		//agent.entInv.grabItems = true;
		agent.entInv.grabXP = true;
		agent.maxReach_Ranged = 25;
		agent.maxReach_Melee = 2.2F;
		agent.dipl_info = TeamTypes.getType("comrade");
		
		agent.collideResistClose = agent.collideResistFormation = agent.collideResistPathing = entityCollisionReduction = 0.9F;
		
	}

	@Override
	public void cleanup() {
		agent = null;
	}
	
	public void checkAgent() {
		if (agent == null) agent = new AIAgent(this, true);
	}
	
	@Override
	protected void entityInit()
    {
		super.entityInit();
		checkAgent();
		agent.entityInit();
    }

	@Override
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
	}
	
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
	protected boolean isAIEnabled()
    {
        return true;
    }

	@Override
	public int getMaxHealth() {
		return 40;
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
		agent.entInv.attackMelee(ent, dist);
	}

	@Override
	public void attackRanged(Entity ent, float dist) {
		//System.out.println("coroai generic ranged attack!");
		checkAgent();
		agent.entInv.attackRanged(ent, dist);
	}

	@Override
	public boolean isBreaking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnemy(Entity ent) {
		// TODO Auto-generated method stub
		if (ent instanceof EntityPlayer && !((EntityPlayer) ent).capabilities.isCreativeMode) return true;
		if (ent instanceof c_EnhAI) return true;
		if ((ent instanceof EntityMob) && !agent.isThreat(ent)) return true;
		return DiplomacyHelper.shouldTargetEnt(this, ent);
	}
	
	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
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
		
		
		if (agent.jobMan.getPrimaryJob().hookHit(par1DamageSource, par2)) {
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
		
		//temp
		agent.meleeUseRightClick = false;
		
		//tameify!
		if (!worldObj.isRemote && agent.jobMan.getPrimaryJob().tamable.owner.equals("")) {
			EntityPlayer entP = worldObj.getClosestPlayerToEntity(this, -1);
			if (entP != null) {
				agent.jobMan.getPrimaryJob().tamable.tameBy(entP.username);
			}
		}
		
		fallDistance = 0;
		
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
	
	@Override
	public String getLocalizedName() {
		return "Comrade";
	}
}
