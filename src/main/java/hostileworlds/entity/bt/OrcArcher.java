package hostileworlds.entity.bt;

import hostileworlds.entity.abilities.AbilityCastProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import CoroUtil.ability.abilities.AbilityAttackMelee;
import CoroUtil.ability.abilities.AbilityShootArrow;
import CoroUtil.inventory.AIInventory;

public class OrcArcher extends EnemyBase {

	public OrcArcher(World par1World) {
		super(par1World);
		
		//marked as no despawn in initRPGStats in super class
		
		
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
		agent.entInv.setSlotActive(AIInventory.slot_Ranged);
		//this.setCurrentItemOrArmor(0, new ItemStack(Item.swordIron));
		this.setEquipmentDropChance(0, 0);

        getAIBTAgent().profile.addAbilityMelee(new AbilityAttackMelee().init(this));
        getAIBTAgent().profile.addAbilityRanged(new AbilityShootArrow().init(this));
        getAIBTAgent().profile.addAbilityRanged(new AbilityCastProjectile().init(this));
        
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readEntityFromNBT(par1nbtTagCompound);
		
		agent.entInv.setSlotActive(AIInventory.slot_Ranged);
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
		return "Orc";
	}
	
	/*@Override
	public void hookSetTargetPre(Entity target) {
		if (getAIBTAgent().blackboard.getTarget() == null && target instanceof EntityPlayer) {
			worldObj.playSoundEffect(this.posX, this.posY, this.posZ, RPGMod.modID+":sight_orc", 0.9F, 0.7F + worldObj.rand.nextFloat() * 0.3F);
		}
		super.hookSetTargetPre(target);
	}*/

}
