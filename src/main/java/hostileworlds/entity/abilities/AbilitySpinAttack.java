package hostileworlds.entity.abilities;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import CoroUtil.ability.Ability;
import CoroUtil.bt.IBTAgent;
import CoroUtil.inventory.AIInventory;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;

public class AbilitySpinAttack extends Ability {
	
	public EntityLivingBase target;
	public boolean switchToMeleeSlot = true;
	
	public AbilitySpinAttack() {
		super();
		this.name = "SpinAttack";
		this.ticksToCharge = 40;
		this.ticksToPerform = 120;
		this.ticksToCooldown = 150;
		
		//max dist of 3
		this.bestDist = 5;
		this.bestDistRange = 10;
	}
	
	@Override
	public boolean canHitCancel(DamageSource parSource) {
		return false;
	}
	
	@Override
	public void setTarget(Entity parTarget) {
		if (parTarget instanceof EntityLivingBase) {
			target = (EntityLivingBase)parTarget;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void tickRender(Render parRender) {
		super.tickRender(parRender);
		
		
	}
	
	@Override
	public boolean canActivate() {
		return owner.onGround && super.canActivate();
	}
	
	@Override
	public void tickChargeUp() {
		super.tickChargeUp();
		
		if (owner.worldObj.isRemote) {
			Random rand = new Random();
			if (curTickCharge > 0) {
				int amount = 1 + (int)(10D * ((double)curTickCharge / (double)ticksToCharge) / (Minecraft.getMinecraft().gameSettings.particleSetting+1));
				
				//System.out.println(amount);
				
				for (int i = 0; i < amount; i++)
		        {
		        	double speed = 0.15D;
		        	double speedInheritFactor = 0.5D;
		        	
		        	//EntityRotFX entityfx = new EntityIconFX(Minecraft.getMinecraft().theWorld, owner.posX + rand.nextDouble(), owner.boundingBox.minY+0.2, owner.posZ + rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed, ParticleRegistry.squareGrey);
		        	EntityRotFX entityfx = particleBehavior.spawnNewParticleIconFX(Minecraft.getMinecraft().theWorld, ParticleRegistry.squareGrey, owner.posX + rand.nextDouble() - rand.nextDouble(), owner.boundingBox.minY+0.8, owner.posZ + rand.nextDouble() - rand.nextDouble(), (rand.nextDouble() - rand.nextDouble()) * speed, 0.03D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed);
		        	particleBehavior.initParticle(entityfx);
		        	float f = 0.0F + (rand.nextFloat() * 0.9F);
		        	entityfx.setRBGColorF(f, 0, 0);
		        	entityfx.callUpdatePB = false;
					ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
					particleBehavior.particles.add(entityfx);
					
		        }
			}
		}
	}

	@Override
	public void tickPerform() {
		
		if (target == null) {
			setFinishedPerform();
			return;
		}
		
		//System.out.println("isRemote: " + owner.worldObj.isRemote);
		if (owner.worldObj.isRemote) {
			
			/*this.owner.rotationYaw += 5;
			if (this.owner.rotationYaw > 360) {
				this.owner.rotationYaw -= 360;
			}*/
			
			Random rand = new Random();
			//owner.worldObj.spawnParticle("largesmoke", owner.posX + (rand.nextDouble() - 0.5D) * (double)owner.width, owner.posY + rand.nextDouble() * (double)owner.height, owner.posZ + (rand.nextDouble() - 0.5D) * (double)owner.width, 0.0D, 0.0D, 0.0D);
		} else {
			
			((IBTAgent)owner).getAIBTAgent().blackboard.setMoveAndPathTo(null);
			
			boolean cancel = false;
			if (target == null || (target != null && (target.isDead || /*target.getHealth() <= 0 || */(target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime > 0)))) {
				this.setFinishedPerform();
				cancel = true;
			}
			
			if (!cancel) {
				if (switchToMeleeSlot) {
					//System.out.println("melee slot use!");
					if (owner instanceof IBTAgent) {
						((IBTAgent)owner).getAIBTAgent().entInv.setSlotActive(AIInventory.slot_Melee);
					}
				}
				
				//((EntityLiving)owner).faceEntity(target, 180, 180);
				
				double moveSpeed = 0.2D;
				
				//double dashX = (Math.cos((owner.rotationYaw+90) * 0.01745329D) * moveSpeed);
				//double posY = (owner.posY/* - 0.3D - Math.sin((center.rotationPitch) / 180.0F * 3.1415927F) * dist*/);
				//double dashZ = (Math.sin((owner.rotationYaw+90) * 0.01745329D) * moveSpeed);
				
				Vec3 targetVec = CoroUtilEntity.getTargetVector(owner, target);
				
				owner.motionX = targetVec.xCoord * moveSpeed;
				owner.motionZ = targetVec.zCoord * moveSpeed;
				
				this.owner.swingItem();
				
				this.owner.rotationYaw += 60;
				if (this.owner.rotationYaw > 360) {
					this.owner.rotationYaw -= 360;
				}
				
				/*if (curTickPerform == 3 || curTickPerform == 23) {
					this.owner.swingItem();
				}*/
				
				
				
				int ticksHitTarg = 8;
				int ticksHitRange = 3;
				
				double speed = 0.8D;
				double hitRange = 1.5D;
				
				/*if (curTickPerform == 1) {
					this.owner.swingItem();
				}*/
				
				double dist = this.owner.getDistanceToEntity(target);
				
				if (dist <= hitRange) {
					
					//if (curTickPerform >= ticksHitTarg - ticksHitRange && curTickPerform <= ticksHitTarg + ticksHitRange) {
						//System.out.println("hit");
						this.target.attackEntityFrom(new EntityDamageSource("mob", owner), 4);
						
						//only set finished on second hit
						/*if (curTickPerform >= 20) {
							this.setFinishedPerform();
						}*/
					//}
				}
			}
		}
		
		
		super.tickPerform();
	}

}
