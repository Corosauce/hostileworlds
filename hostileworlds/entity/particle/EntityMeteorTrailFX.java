package hostileworlds.entity.particle;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import extendedrenderer.particle.entity.EntityRotFX;

public class EntityMeteorTrailFX extends EntityRotFX {
	
	float smokeParticleScale;

    public int spinState = 0;
    public int direction = 0;
    public double sourceX;
    public double sourceY;
    public double sourceZ;
    public float maxScale = 15F;

	public EntityMeteorTrailFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float par14, double parSourceX, double parSourceY, double parSourceZ) {
		super(par1World, par2, par4, par6, par8, par10, par12);
		particleMaxAge = 1000;
		
		rotationPitch = par1World.rand.nextInt(90);
		rotationYaw = par1World.rand.nextInt(360);
		sourceX = parSourceX;
		sourceY = parSourceY;
		sourceZ = parSourceZ;
	}
	
	public void setMaxAge(int var) {
		particleMaxAge = var;
	}
	
	@Override
	public int getFXLayer()
    {
        return 0;
    }
	
	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
		//carefully adjusted rotations (not the same as in rotating effect renderer)
		/*par3 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
		par4 = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		par5 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
		par6 = -par5 * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
		par7 = par3 * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);*/
		
        float var88 = ((float)this.particleAge + par2) / (float)this.particleMaxAge * 32.0F;

        if (var88 < 0.0F)
        {
            var88 = 0.0F;
        }

        if (var88 > 1.0F)
        {
            var88 = 1.0F;
        }
        
        float newSize = particleAge * 0.3F;
        if (newSize > 50) newSize = 50;
        
        float newAlpha = particleAge * 0.006F;
        if (newAlpha > 1) newAlpha = 1;
        if (newAlpha < 0) newAlpha = 0;

        //this.particleScale = /*this.smokeParticleScale * particleAge * 0.1F*/newSize;
        
        if (particleAge < 10) {
	        particleScale = particleAge * 5F;
        } else {
        	if (particleMaxAge - particleAge < 400) {
            	int count = (particleMaxAge - particleAge);
            	particleScale = (float)count * 0.01F;
        	}
        	//particleScale = (particleMaxAge - particleAge) * 5F;
        }
        
        if (particleScale > maxScale) {
        	this.particleScale = maxScale;
        }
        
        particleRed = 0.9F;
        particleGreen = 0.0F + (particleAge * 0.03F);
        particleBlue = 0.0F + (particleAge * 0.005F);
        
        particleGreen = 0.0F + (particleAge * 0.15F);
        particleBlue = 0.0F + (particleAge * 0.03F);
        
        //System.out.println(particleBlue);
        
        //particleRed = 0.3F + ((float) Math.sin((this.particleAge / 10F) % 60) * 0.3F);
        //particleGreen = (float) Math.cos((this.particleAge / 10F) % 20);
        //particleBlue = (float) Math.cos((this.particleAge / 10F) % 20);
        
        particleAlpha = newAlpha/* = 1F*/;
        
        particleAlpha = 0.7F;
        
        if (particleMaxAge - particleAge < 70) {
        	int count = (particleMaxAge - particleAge);
        	particleAlpha = (float)count * 0.01F;
        	//gamma += ((float)Math.abs(particleMaxAge - particleAge) * 0.02F);
        	//System.out.println("gamma: " + gamma);
        }
        
        float var8 = (float)(this.getParticleTextureIndex() % 16) / 16.0F;
        float var9 = var8 + 0.0624375F;
        float var10 = (float)(this.getParticleTextureIndex() / 16) / 16.0F;
        float var11 = var10 + 0.0624375F;
        float var12 = 0.1F * this.particleScale;
        float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)par2 - interpPosX);
        float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)par2 - interpPosY);
        float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)par2 - interpPosZ);
        float var16 = 1.0F;
        par1Tessellator.setColorRGBA_F(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha);
        par1Tessellator.addVertexWithUV((double)(var13 - par3 * var12 - par6 * var12), (double)(var14 - par4 * var12), (double)(var15 - par5 * var12 - par7 * var12), (double)var9, (double)var11);
        par1Tessellator.addVertexWithUV((double)(var13 - par3 * var12 + par6 * var12), (double)(var14 + par4 * var12), (double)(var15 - par5 * var12 + par7 * var12), (double)var9, (double)var10);
        par1Tessellator.addVertexWithUV((double)(var13 + par3 * var12 + par6 * var12), (double)(var14 + par4 * var12), (double)(var15 + par5 * var12 + par7 * var12), (double)var8, (double)var10);
        par1Tessellator.addVertexWithUV((double)(var13 + par3 * var12 - par6 * var12), (double)(var14 - par4 * var12), (double)(var15 + par5 * var12 - par7 * var12), (double)var8, (double)var11);
        
        //super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
    }
	
	public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        /*if (this.rotationPitch > -90) {
        	this.rotationPitch-=2;
        }*/
        
        //this.rotationYaw = 0;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        this.setParticleTextureIndex(7/* - this.particleAge * 8 / this.particleMaxAge*/);
        //this.motionY += 0.004D;
        //this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (particleAge < 40) {
	        float speed = 0.01F;
	        if (this.posX > this.sourceX) {
	        	this.motionX -= speed;
	        } else {
	        	this.motionX += speed;
	        }
	        if (this.posY > this.sourceY) {
	        	this.motionY -= speed;
	        } else {
	        	this.motionY += speed;
	        }
	        if (this.posZ > this.sourceZ) {
	        	this.motionZ -= speed;
	        } else {
	        	this.motionZ += speed;
	        }
        }
        
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;

        if (this.posY == this.prevPosY)
        {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;
        
        //this.posY = 128;
    }
	
	public int getParticleAge() {
		return this.particleAge;
	}
	
	public float maxRenderRange() {
    	return 9999F;
    }
	
	public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
        return true;
    }

}
