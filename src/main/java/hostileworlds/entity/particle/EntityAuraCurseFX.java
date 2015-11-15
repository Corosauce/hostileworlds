package hostileworlds.entity.particle;

import CoroUtil.api.weather.IWindHandler;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityAuraCurseFX extends EntitySmokeFX implements IWindHandler {
	
	float smokeParticleScale;

    public int spinState = 0;
    public int direction = 0;

	public EntityAuraCurseFX(World par1World, double par2, double par4,	double par6, double par8, double par10, double par12, float par14) {
		super(par1World, par2, par4, par6, par8, par10, par12, par14);
		smokeParticleScale = 5;
		noClip = true;
	}
	
	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
		//carefully adjusted rotations (not the same as in rotating effect renderer)
		par3 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
		par4 = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		par5 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
		par6 = -par5 * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
		par7 = par3 * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
		
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

        this.particleScale = /*this.smokeParticleScale * particleAge * 0.1F*/newSize;
        
        particleRed = 0.3F;
        particleGreen = 0.0F;
        particleBlue = 0.0F;
        
        particleRed = 0.3F + ((float) Math.sin((this.particleAge / 10F) % 60) * 0.3F);
        //particleGreen = (float) Math.cos((this.particleAge / 10F) % 20);
        //particleBlue = (float) Math.cos((this.particleAge / 10F) % 20);
        
        particleAlpha = newAlpha/* = 1F*/;
        
        float var8 = (float)this.particleTextureIndexX / 16.0F;
        float var9 = var8 + 0.0624375F;
        float var10 = (float)this.particleTextureIndexY / 16.0F;
        float var11 = var10 + 0.0624375F;
        float var12 = 0.1F * this.particleScale;
        
        if (this.particleIcon != null)
        {
        	var8 = this.particleIcon.getMinU();
        	var9 = this.particleIcon.getMaxU();
        	var10 = this.particleIcon.getMinV();
        	var11 = this.particleIcon.getMaxV();
        }
        
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
        
        if (this.rotationPitch > -90) {
        	this.rotationPitch-=2;
        }
        
        //this.rotationYaw = 0;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.motionY += 0.004D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY)
        {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
        
        this.posY = 128;
    }
	
	public int getParticleAge() {
		return this.particleAge;
	}

	@Override
	public float getWindWeight() {
		return 999;
	}

	@Override
	public int getParticleDecayExtra() {
		return 0;
	}

}
