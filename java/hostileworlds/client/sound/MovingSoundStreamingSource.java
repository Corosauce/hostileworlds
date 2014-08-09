package hostileworlds.client.sound;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import cpw.mods.fml.client.FMLClientHandler;

public class MovingSoundStreamingSource extends MovingSound {

	public float cutOffRange = 128;
	public Vec3 realSource = null;

	//constructor for non moving sounds
    public MovingSoundStreamingSource(Vec3 parPos, ResourceLocation parRes, float parVolume, float parPitch, float parCutOffRange)
    {
        super(parRes);
        this.repeat = true;
        this.volume = parVolume;
        this.field_147663_c = parPitch;
        cutOffRange = parCutOffRange;
        realSource = parPos;
        
        //sync position
        update();
    }
    
    public void stopPlaying() {
    	donePlaying = true;
    }

    public void update()
    {
    	EntityPlayer entP = FMLClientHandler.instance().getClient().thePlayer;
    	
    	if (entP != null) {
    		this.xPosF = (float) entP.posX;
    		this.yPosF = (float) entP.posY;
    		this.zPosF = (float) entP.posZ;
    	}
    	
    	float var3 = (float)((cutOffRange - (double)MathHelper.sqrt_double(getDistanceFrom(realSource, Vec3.createVectorHelper(entP.posX, entP.posY, entP.posZ)))) / cutOffRange);

        if (var3 < 0.0F)
        {
        	//donePlaying = true;
            var3 = 0.0F;
        }
        
        volume = var3;
    }
    
    public double getDistanceFrom(Vec3 source, Vec3 targ)
    {
        double d3 = (double)source.xCoord - targ.xCoord;
        double d4 = (double)source.yCoord - targ.yCoord;
        double d5 = (double)source.zCoord - targ.zCoord;
        return d3 * d3 + d4 * d4 + d5 * d5;
    }

}
