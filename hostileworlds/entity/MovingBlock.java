package hostileworlds.entity;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.particle.EntityMeteorTrailFX;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MovingBlock extends Entity implements IEntityAdditionalSpawnData
{

	public boolean noCollision = false;
	public int age = 0;
	public int blockID = 0;
	public int blockMeta = 0;
	public float gravity = 0.04F;
	public float speedSlowing = 0.99F;
	public int blockifyDelay = 30;
	
	//targeting
	public EntityLivingBase target;
	public float targetTillDist = -1;
	public double targPrevPosX;
	public double targPrevPosY;
	public double targPrevPosZ;
	
	//synced data for render
	public int blockNum;
	public int blockRow;
	public boolean createParticles = false;
	
    public MovingBlock(World var1)
    {
        super(var1);
    }

    public MovingBlock(World var1, int parBlockID, int parMeta)
    {
        super(var1);
        blockID = parBlockID;
        blockMeta = parMeta;
    }

    public boolean isInRangeToRenderDist(double var1)
    {
        return true;
    }

    public boolean canTriggerWalking()
    {
        return false;
    }

    public void entityInit() {}

    public boolean canBePushed()
    {
        return !this.isDead;
    }

    public boolean canBeCollidedWith()
    {
        return !this.isDead && !this.noCollision;
    }
    
    public void moveTowards(Entity ent, Entity targ, float speed, int leadTicks) {
		double vecX = (targ.posX + ((targ.posX - targPrevPosX) * leadTicks)) - ent.posX;
		//double vecY = (targ.posY/* + ((targ.posY - targPrevPosY) * leadTicks)*/) - ent.posY;
		double vecZ = (targ.posZ + ((targ.posZ - targPrevPosZ) * leadTicks)) - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX/* + vecY * vecY*/ + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		//ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}

    public void onUpdate()
    {
        super.onUpdate();
    	++this.age;
    	
    	
    	
    	//Main movement
        this.motionX *= (double)speedSlowing;
        this.motionY *= (double)speedSlowing;
        this.motionZ *= (double)speedSlowing;
        this.motionY -= (double)gravity;
        
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        
        this.setPosition(this.posX, this.posY, this.posZ);
        
        if (!worldObj.isRemote) {
        	if (target != null && targetTillDist != -1) {
        		float var2 = (float)(this.posX - target.posX);
                //float var3 = (float)(this.posY - par1Entity.posY);
                float var4 = (float)(this.posZ - target.posZ);
                float dist = MathHelper.sqrt_float(var2 * var2 + var4 * var4);
        		if (dist > targetTillDist) {
        			//motionX = motionZ = 0F;
        			motionX *= 0.95F;
        			motionZ *= 0.95F;
        			if (motionY < 0) {
        				motionY += 0.020F;
        			}
        			if (Math.sqrt(motionX * motionX + motionZ * motionZ) < 0.20F) {
        				//moveTowards(this, target, dist * (float)(0.03F), (int) (dist * 3.5F));
        				moveTowards(this, target, dist * (float)(0.025F), (int) (dist * 2.5F));
        			}
        		}
        		
    			targPrevPosX = target.posX;
    			targPrevPosY = target.posY;
    			targPrevPosZ = target.posZ;
        		
        	}
        	
	        if (blockifyDelay != -1 && age > blockifyDelay) {
		    	float ahead = 1F;
		    	
		    	int aheadX = (int)(posX + (motionX*ahead));
		    	int aheadY = (int)(posY + (motionY*ahead));
		    	int aheadZ = (int)(posZ + (motionZ*ahead));
		    	
		    	int id = worldObj.getBlockId(aheadX, aheadY, aheadZ);
		    	
		    	if (id != 0 && worldObj.getBlockTileEntity(aheadX, aheadY, aheadZ) == null && id != HostileWorlds.blockBloodyCobblestone.blockID && Block.blocksList[id].blockMaterial != Material.water && Block.blocksList[id].blockMaterial != Material.circuits && Block.blocksList[id].blockMaterial != Material.snow && Block.blocksList[id].blockMaterial != Material.plants && Block.blocksList[id].blockMaterial.isSolid()) {
		    		blockify((int)posX, (int)posY, (int)posZ);
		    	}
	        }
	        
        	double size = 0.5D;
	        List entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(size, size, size));
	        
	        for (int i = 0; entities != null && i < entities.size(); ++i)
	        {
	            Entity var10 = (Entity)entities.get(i);
	            
	            if (var10 != null && !var10.isDead && var10 instanceof EntityLivingBase && !(var10 instanceof ZombieBlockWielder) && !(var10 instanceof EntityWorm)) {
	            	var10.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 4);
	            }
	        }
	        
        } else {
        	if (createParticles) spawnParticles();
        }
        
        //setDead();
    	
        //so temp
    	//posY = 72F;
        if (worldObj.isRemote) motionY = 0.00F;
    	
        //onGround = true;
    }
    
    public void triggerOwnerDied() {
    	blockifyDelay = 1;
    	gravity = 0.03F;
    	double speed = 0.3F;
    	motionX = rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    	motionY = 0.3F + rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    	motionZ = rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    }
    
    public void blockify(int x, int y, int z) {
    	worldObj.setBlock(x, y, z, blockID);
    	setDead();
    }
    
    //this is probably never called unless something specifically handles this block, since its not a standard living entity that can take damage
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
    	setDead();
    	return super.attackEntityFrom(par1DamageSource, par2);
    }
    
    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
    	for (int i = 0; i < 1; i++) {
    		
    		float speed = 0.1F;
    		float randPos = 8.0F;
    		float ahead = 2.5F;
    		
    		EntityMeteorTrailFX particle = new EntityMeteorTrailFX(worldObj, 
    				posX/* + (motionX*ahead) + (rand.nextFloat()*2-1) * randPos*/, 
    				posY/* + (motionY*ahead) + (rand.nextFloat()*2-1) * randPos*/, 
    				posZ/* + (motionZ*ahead) + (rand.nextFloat()*2-1) * randPos*/, motionX, 0.25F, motionZ, 0, posX, posY, posZ);
    		
    		particle.maxScale = 3F;
    		particle.setMaxAge(100);
    		particle.motionX = (rand.nextFloat()*2-1) * speed;
    		particle.motionY = (rand.nextFloat()*2-1) * 0.1F;
    		particle.motionZ = (rand.nextFloat()*2-1) * speed;
    		
        	//particles.add(particle);

    		particle.spawnAsWeatherEffect();
        	//Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    	}
    }

    public float getShadowSize()
    {
        return 0.0F;
    }

    public boolean isInRangeToRenderVec3D(Vec3 asd)
    {
        return true;
    }

    public void setEntityDead()
    {
        super.setDead();
    }

    @Override
    public void writeSpawnData(ByteArrayDataOutput data)
    {
        data.writeInt(blockID);
        data.writeInt(blockMeta);
        data.writeFloat(gravity);
        data.writeInt(blockifyDelay);
        data.writeInt(blockNum);
        data.writeInt(blockRow);
        data.writeBoolean(createParticles);
    }

    @Override
    public void readSpawnData(ByteArrayDataInput data)
    {
    	blockID = data.readInt();
    	blockMeta = data.readInt();
    	gravity = data.readFloat();
    	blockifyDelay = data.readInt();
    	blockNum = data.readInt();
    	blockRow = data.readInt();
    	createParticles = data.readBoolean();
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound data) {
		blockID = data.getInteger("blockID");
		blockMeta = data.getInteger("blockMeta");
		//blockifyDelay = data.getInteger("blockifyDelay");
		//gravity = data.getFloat("gravity");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound data) {
		data.setInteger("blockID", blockID);
		data.setInteger("blockMeta", blockMeta);
		data.setInteger("blockifyDelay", blockifyDelay);
		data.setFloat("gravity", gravity);
	}
}
