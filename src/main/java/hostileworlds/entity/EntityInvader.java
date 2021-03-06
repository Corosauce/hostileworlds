package hostileworlds.entity;

import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.monster.ZombieMiner;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroUtil.componentAI.AIAgent;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.util.CoroUtilBlock;

public class EntityInvader extends EntityZombie implements ICoroAI {

	public AIAgent agent;
	
	public float HWDifficulty;
	public String primaryTarget = "";
	
	public EntityInvader(World par1World) {
		super(par1World);
		//texture = "/tropicalmod/test.png";
		//this.texture = "/mob/zombie.png";
		//setSize(0.6F, 2F);
		
		//agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		entityCollisionReduction = 0.9F;
		agent.dipl_info = TeamTypes.getType("undead");
		
		if (ModConfigFields.invadersDropNothing) {
			for (int i = 0; i < this.equipmentDropChances.length; ++i)
	        {
	            this.equipmentDropChances[i] = 0;
	        }
		}
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		agent.setSpeedFleeAdditive(0.1F);
		agent.setSpeedNormalBase(0.6F);
		agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
	}
	
	@Override
	protected void entityInit()
    {
		super.entityInit();
		agent = new AIAgent(this, false);
		agent.entityInit();
		
    }
	
	@Override
	public boolean getCanSpawnHere()
    {
		return this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox);
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
		if (agent != null) {
			agent.setPathToEntity(pathentity);
		} else {
			System.out.println("agent is null for entity invader when returning threaded path");
		}
	}

	@Override
	public int getCooldownMelee() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public int getCooldownRanged() {
		// TODO Auto-generated method stub
		return 40;
	}

	@Override
	public void attackMelee(Entity ent, float dist) {
		
		float calcDamage = 2;
		
		if (this.getEquipmentInSlot(0) != null) {
			
			calcDamage = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
			
			//System.out.println("attackMelee calcDamage val: " + calcDamage);
			//calcDamage = this.getCurrentItemOrArmor(0).getItem().getDamageVsEntity(ent);
			//this might cast crash, how to use multimap?
			//calcDamage = (float) ((BaseAttribute) this.getCurrentItemOrArmor(0).getAttributeModifiers().get("generic.attackDamage")).getDefaultValue();
		}
		
		calcDamage *= 0.75F;
		
		if (calcDamage < 1F) calcDamage = 1F;
		
        calcDamage += getBonusDamage(ent, dist);
		
		//easy
        if (ModConfigFields.difficulty == 0)
        {
        	calcDamage = calcDamage / 2 + 1;
        }
        
        //hard
        if (ModConfigFields.difficulty == 2)
        {
        	calcDamage = calcDamage * 3 / 2;
        }
        
        calcDamage += (HWDifficulty / 25F);
        
        //System.out.println("final damage:" + calcDamage);
        
		ent.attackEntityFrom(new EntityDamageSource("mob", this) { public boolean isDifficultyScaled() { return false; } }, (int)calcDamage);
	}
	
	public float getBonusDamage(Entity targetEnt, float dist) {
		return 0;
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
		if (ent instanceof EntityPlayer && !((EntityPlayer) ent).capabilities.isCreativeMode) return true;
		return false;
	}
	
	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
		if (par1DamageSource != DamageSource.inWall) {
			return super.attackEntityFrom(par1DamageSource, par2);
		} else {
			return false;
		}
    }
	
	//special mob methods
	@Override
	public void onLivingUpdate() {
		if (agent != null) agent.onLivingUpdateTick();
		fallDistance = 0;
		
		//force non child zombies
		setChild(false);
		
		//client y movement fix and health sync
		
		if (worldObj.isRemote) {
			//EntityRotFX firefly = new EntityFireflyFX(worldObj, (double)posX, (double)posY + 0.5, (double)posZ, 0D, 0D, 0D, 60D, 1);
	        //c_CoroWeatherUtil.setParticleGravity((EntityFX)firefly, 0.1F);
	        
	        //Minecraft.getMinecraft().effectRenderer.addEffect(firefly);
	        //((EntityRotFX) firefly).spawnAsWeatherEffect();
		} else {
			if (isInWater()) {
			   if (!(this instanceof ZombieMiner)) fixBadYPathing();
			}
			//setDead();
		}
		
		super.onLivingUpdate();
	}
	
	@Override
	public boolean isValidLightLevel() {
		return true;
	}

	//note, AIAgent doesnt use this anymore, it uses the method that checks isPersistanceRequired field now
	@Override
	public boolean canDespawn() {
		return false;
	}
	
	@Override
	public boolean isInRangeToRenderDist(double par1)
    {
		return Math.sqrt(par1) < 512F;
    }
	
	public boolean isNearWall(AxisAlignedBB par1AxisAlignedBB)
    {
        int var3 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int var4 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int var5 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int var6 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int var7 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int var8 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int var9 = var3-1; var9 < var4+1; ++var9)
        {
            for (int var10 = var5; var10 < var6+1; ++var10)
            {
                for (int var11 = var7-1; var11 < var8+1; ++var11)
                {
                    Block var12 = worldObj.getBlock(var9, var10, var11);

                    if (!CoroUtilBlock.isAir(var12)/* && var12.blockMaterial == par2Material*/)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
	
	public void fixBadYPathing() {
		if (this.getNavigator().getPath() != null) {
			PathEntity pEnt = this.getNavigator().getPath();
			int index = pEnt.getCurrentPathIndex();
			//index--;
			if (index < 0) index = 0;
			if (index >= pEnt.getCurrentPathLength()) index = pEnt.getCurrentPathLength()-1;
			Vec3 var1 = null;
			try {
				var1 = pEnt.getVectorFromIndex(this, index);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("errrrrrrrrrrrrrrr");
				var1 = pEnt.getVectorFromIndex(this, pEnt.getCurrentPathLength()-1);
			}

            if (var1 != null)
            {
            	//fast water pathing
            	//this.getMoveHelper().setMoveTo(var1.xCoord, var1.yCoord, var1.zCoord, 0.53F);
            	double dist = this.getDistance(var1.xCoord, this.boundingBox.minY, var1.zCoord);
            	//System.out.println(dist);
            	if (dist <= 0.5F) {
            		this.getNavigator().getPath().incrementPathIndex();
            	}
            }
		}
	}

	@Override
	public void cleanup() {
		agent = null;
	}
}
