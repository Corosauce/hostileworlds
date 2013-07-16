package hostileworlds.client.entity;

import hostileworlds.entity.monster.EntityWormSand;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class RenderWormSand extends RenderLiving {
    
    protected ModelWormSand model;

    public RenderWormSand(ModelWormSand modelbase, float f) {
        super(modelbase, f);
        model = modelbase;
    }

    public void func_177_a(EntityWormSand entity, double d, double d1, double d2,
            float f, float f1) {
        super.doRenderLiving(entity, d, d1, d2, f, f1);
    }

    @Override
    public void doRenderLiving(EntityLiving entityliving, double d, double d1, double d2,
            float f, float f1) {
        super.doRenderLiving((EntityWormSand) entityliving, d, d1, d2, f, f1);
    }

    @Override
    public void doRender(Entity entity, double d, double d1, double d2,
            float f, float f1) {
    	
        doRenderLiving((EntityWormSand) entity, d, d1, d2, f, f1);
    }
}
