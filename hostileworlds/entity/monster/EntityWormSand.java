package hostileworlds.entity.monster;

import hostileworlds.ai.jobs.JobHunt;
import hostileworlds.entity.EntityWorm;
import hostileworlds.entity.particle.EntitySandFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityWormSand extends EntityWorm {

	public EntityWormSand(World par1World) {
		super(par1World);
		
		setSize(1.5F, 1F);
		
		agent.jobMan.addPrimaryJob(new JobHunt(agent.jobMan));
		
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		double speed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		
		//if (speed > 0.015) {
            if (worldObj.isRemote) {
            	spawnParticles(speed);
            }
        //}
            
            this.motionY -= 0.02;
	}
	
	@SideOnly(Side.CLIENT)
    public void spawnParticles(double speed) {
    	
    	double vecZ = this.rotationPitch;
    	
    	Vec3 vec = Vec3.createVectorHelper(motionX, 0.2F, motionZ);
    	
    	vec = Vec3.createVectorHelper(rand.nextFloat() * 0.4F, 0.35F, rand.nextFloat() * 0.4F);
    	
		float burst = 0.7F;//rand.nextFloat();
		
		vec.rotateAroundY(90);
		EntityFX waterP;
		int iter = 5 - Minecraft.getMinecraft().gameSettings.particleSetting; 
		
		int chance = 1;//Math.max(1, 10 - (int)(speed * 15));
		//System.out.println("Chance: " + chance);
		
		for (int i = 0; i < iter; i++) {
	    	waterP = new EntitySandFX(worldObj, (double)posX, posY - 0.8D, posZ, vec.xCoord * burst, vec.yCoord, vec.zCoord * burst, 2F);
	    	if (rand.nextInt(chance) == 0) Minecraft.getMinecraft().effectRenderer.addEffect(waterP);
		}
    	
    	vec.rotateAroundY(-180);
    	
    	for (int i = 0; i < iter; i++) {
	    	waterP = new EntitySandFX(worldObj, (double)posX, posY - 0.8D, posZ, vec.xCoord * burst, vec.yCoord, vec.zCoord * burst, 2F);
	    	if (rand.nextInt(chance) == 0) Minecraft.getMinecraft().effectRenderer.addEffect(waterP);
    	}
    	
    	for (int i = 0; i < iter; i++) {
    		float range = 0.1F;
	    	waterP = new EntitySandFX(worldObj, (double)posX, posY - 0.8D, posZ, ((rand.nextFloat() * range) - (range/2)), 0, ((rand.nextFloat() * range) - (range/2)), 2F);
	    	if (rand.nextInt(chance) == 0) Minecraft.getMinecraft().effectRenderer.addEffect(waterP);
    	}
    }

}
