package hostileworlds.entity.bt;

import hostileworlds.entity.bt.ai.PersonalityProfileStrongholdMember;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import CoroUtil.bt.entity.EntityMobBase;
import CoroUtil.bt.nodes.TargetEnemy;
import CoroUtil.diplomacy.TeamTypes;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EnemyBase extends EntityMobBase/* implements IMob*/ {
	
	public EnemyBase(World par1World) {
		super(par1World);
		agent.dipl_info = TeamTypes.getType("hostile");


		
		
	}
	
	@Override
	public void initExtraAI() {
		super.initExtraAI();
		
		agent.btAttack.add(new TargetEnemy(null, this, 16F, null, -1, 20));
	}
	
	@Override
	public void updateAITasks() {
		super.updateAITasks();

		//leap out of water with block infront of it, for path nav fix
		if (isInWater() && isCollidedHorizontally) {
			motionX *= 1.2F;
			motionZ *= 1.2F;
			
			motionY = 0.84F;
		}
	}
	
	@Override
	public void initAIProfile() {
		//super.initAIProfile();
		agent.profile = new PersonalityProfileStrongholdMember(agent);
		agent.profile.init();
		agent.profile.initProfile(-1);
		agent.profile.setFearless();
	}
	
	/*@Override
	public void initAIProfile() {
		super.initAIProfile();
		//agent.profile = new PersonalityProfileEpoch(agent);
		agent.profile.initProfile(PersonalityProfileEpoch.TYPE_NPCENEMY);
        //agent.profile.init();
        //agent.profile.initDefaultProfile();
	}*/
	
	/*public void checkLocationRegistrations() {
		WorldDirector director = WorldDirectorManager.instance().getWorldDirector(worldObj);
		if (director instanceof WorldDirectorEpochDungeon) {
			((WorldDirectorEpochDungeon)director).checkDungeonInstance();
			((WorldDirectorEpochDungeon)director).dungeon.addEntity("enemy", this);
			//remove despawning for dungeons
			agent.canDespawn = false;
		}
	}*/
	
	@Override
	public void initRPGStats() {
		super.initRPGStats();
		agent.canDespawn = false;
		//checkLocationRegistrations();
		
		/*WorldDirector director = WorldDirectorManager.instance().getWorldDirector(worldObj);
		if (director instanceof WorldDirectorVanillaOverworld) {
			((WorldDirectorVanillaOverworld)director).markEntitySpawnedFirstTime(this);
		}*/
	}
	
	@Override
	public void setDead() {
		super.setDead();
		
		/*if (!worldObj.isRemote) {
			WorldDirector director = WorldDirectorManager.instance().getWorldDirector(worldObj);
			if (director instanceof WorldDirectorVanillaOverworld) {
				((WorldDirectorVanillaOverworld)director).markEntityDied(this);
			}
		}*/
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readEntityFromNBT(par1nbtTagCompound);
		
		//checkLocationRegistrations();
	}
	
	@Override
	public boolean getCanSpawnHere() {
		// TODO Auto-generated method stub
		return super.getCanSpawnHere();
	}

}
