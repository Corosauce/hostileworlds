package hostileworlds.client.entity;

import hostileworlds.entity.monster.EntityWormSand;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class RenderWormSand extends RenderLiving {
    
    protected ModelWormSand model;

    public RenderWormSand(ModelWormSand modelbase, float f) {
        super(modelbase, f);
        model = modelbase;
    }

	@Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		// TODO Auto-generated method stub
		return null;
	}
}
