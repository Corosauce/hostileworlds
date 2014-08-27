package hostileworlds.client.entity;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.monster.ZombieHungry;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHWOrc extends RenderBiped
{
    public RenderHWOrc()
    {
        super(new ModelBiped(), 0.5F, 1.0F);
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return new ResourceLocation(HostileWorlds.modID + ":textures/entities/orc.png");
	}
    
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
    	if (par1Entity instanceof IBossDisplayData) {
    		BossStatus.setBossStatus((IBossDisplayData)par1Entity, true);
    	}
    	super.doRender(par1Entity, par2, par4, par6, par8, par9);
    	
    	//boolean debug = MinecraftServer.getServer() != null && ZCGame.instance().mapMan != null && MinecraftServer.getServer().isSinglePlayer() && ZCGame.instance().mapMan.editMode;
    	
    	//if (debug) renderDebug((Zombie)par1Entity, par2, par4, par6, par8, par9);
    	
    }
    
    @Override
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
    	if (par1EntityLivingBase instanceof ZombieBlockWielder) {
    		//GL11.glScalef(par1EntityLivingBase.width, par1EntityLivingBase.height, par1EntityLivingBase.width);
    		GL11.glScalef(1.5F, 1.5F, 1.5F);
    		
    	} else if (par1EntityLivingBase instanceof ZombieHungry) {
    		ZombieHungry hungryZombie = (ZombieHungry) par1EntityLivingBase;
    		GL11.glScalef(hungryZombie.sizeAmp, hungryZombie.sizeAmp, hungryZombie.sizeAmp);
    	}
    }
    
    public ModelBase getMainModel() {
    	return modelBipedMain;
    }
    
    /*public void renderDebug(Zombie par1Entity, double par2, double par4, double par6, float par8, float par9) {
    	
	    Entity sEnt = MinecraftServer.getServer().worldServerForDimension(ZCGame.ZCDimensionID).getEntityByID(par1Entity.entityId);
		
		if (sEnt != null && sEnt instanceof Zombie) {
			Zombie z = (Zombie)sEnt;
			PathEntity pe = ((EntityLivingBase)sEnt).getNavigator().getPath();
			
			if (pe != null && pe.getCurrentPathLength() > 1) {
				for (int i = 0; i < pe.getCurrentPathLength() - 1; i++) {
					Vec3 vec = pe.getVectorFromIndex(par1Entity, i);
					Vec3 vec2 = pe.getVectorFromIndex(par1Entity, i+1);
					Overlays.renderLineFromToBlock(vec.xCoord, vec.yCoord+0.5F, vec.zCoord, vec2.xCoord, vec2.yCoord+0.5F, vec2.zCoord, 0x00FF00);
				}
			}
		
		
			if (pe != null && z.job != null && z.job.getJobClass() != nullpar1Entity.currentAction != null) {
				//if () {
					this.renderLivingLabel(par1Entity, String.valueOf(pe.getCurrentPathIndex() + " - " + z.job.getJobClass().state), par2, par4, par6, 999);
				//}
			} else {
				//this.renderLivingLabel(par1Entity, "wat", par2, par4, par6, 999);
			}
		}
		
    }*/
    
    protected void rotateCorpse(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
    {
        GL11.glRotatef(180.0F - par3, 0.0F, 1.0F, 0.0F);

        if (par1EntityLivingBase.deathTime > 0)
        {
            float var5 = ((float)par1EntityLivingBase.deathTime + par4 - 1.0F) / 20.0F * 1.6F;
            var5 = MathHelper.sqrt_float(var5);

            if (var5 > 1.0F)
            {
                var5 = 1.0F;
            }

            if (false/*((BaseEntAI)par1EntityLivingBase).wasHeadshot*/) {
            	GL11.glRotatef(var5 * this.getDeathMaxRotation(par1EntityLivingBase), 1.0F, 0.0F, 0.0F);
            } else {
            	GL11.glRotatef(var5 * this.getDeathMaxRotation(par1EntityLivingBase), 0.0F, 0.0F, 1.0F);
            }
        }
    }
}
