package hostileworlds.client.entity;

import hostileworlds.entity.monster.ZombieBlockWielder;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderGiantZombie;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHWZombie extends RenderZombie
{
    public RenderHWZombie()
    {
        super(/*new ModelZombie(), 0.5F*/);
    }
    
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
    	if (par1Entity instanceof IBossDisplayData) {
    		BossStatus.func_82824_a((IBossDisplayData)par1Entity, true);
    	}
    	super.doRender(par1Entity, par2, par4, par6, par8, par9);
    	
    	//boolean debug = MinecraftServer.getServer() != null && ZCGame.instance().mapMan != null && MinecraftServer.getServer().isSinglePlayer() && ZCGame.instance().mapMan.editMode;
    	
    	//if (debug) renderDebug((Zombie)par1Entity, par2, par4, par6, par8, par9);
    	
    }
    
    @Override
    protected void preRenderCallback(EntityLiving par1EntityLiving, float par2)
    {
    	if (par1EntityLiving instanceof ZombieBlockWielder) {
    		//GL11.glScalef(par1EntityLiving.width, par1EntityLiving.height, par1EntityLiving.width);
    		GL11.glScalef(1.5F, 1.5F, 1.5F);
    		
    	}
    }
    
    public ModelBase getMainModel() {
    	return modelBipedMain;
    }
    
    /*public void renderDebug(Zombie par1Entity, double par2, double par4, double par6, float par8, float par9) {
    	
	    Entity sEnt = MinecraftServer.getServer().worldServerForDimension(ZCGame.ZCDimensionID).getEntityByID(par1Entity.entityId);
		
		if (sEnt != null && sEnt instanceof Zombie) {
			Zombie z = (Zombie)sEnt;
			PathEntity pe = ((EntityLiving)sEnt).getNavigator().getPath();
			
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
    
    protected void rotateCorpse(EntityLiving par1EntityLiving, float par2, float par3, float par4)
    {
        GL11.glRotatef(180.0F - par3, 0.0F, 1.0F, 0.0F);

        if (par1EntityLiving.deathTime > 0)
        {
            float var5 = ((float)par1EntityLiving.deathTime + par4 - 1.0F) / 20.0F * 1.6F;
            var5 = MathHelper.sqrt_float(var5);

            if (var5 > 1.0F)
            {
                var5 = 1.0F;
            }

            if (false/*((BaseEntAI)par1EntityLiving).wasHeadshot*/) {
            	GL11.glRotatef(var5 * this.getDeathMaxRotation(par1EntityLiving), 1.0F, 0.0F, 0.0F);
            } else {
            	GL11.glRotatef(var5 * this.getDeathMaxRotation(par1EntityLiving), 0.0F, 0.0F, 1.0F);
            }
        }
    }
}
