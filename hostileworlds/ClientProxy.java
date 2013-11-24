package hostileworlds;

import hostileworlds.block.RenderHWPortal;
import hostileworlds.block.TileEntityAuraCurse;
import hostileworlds.block.TileEntityAuraCurseRenderer;
import hostileworlds.block.TileEntityHWPortal;
import hostileworlds.client.entity.ModelItemTurretTop;
import hostileworlds.client.entity.ModelTest;
import hostileworlds.client.entity.ModelWormSand;
import hostileworlds.client.entity.RenderComrade;
import hostileworlds.client.entity.RenderHWZombie;
import hostileworlds.client.entity.RenderItemTurretTop;
import hostileworlds.client.entity.RenderMeteorite;
import hostileworlds.client.entity.RenderMovingBlock;
import hostileworlds.client.entity.RenderNull;
import hostileworlds.client.entity.RenderTestAI;
import hostileworlds.client.entity.RenderWormFire;
import hostileworlds.client.entity.RenderWormSand;
import hostileworlds.client.item.WeaponRenderer;
import hostileworlds.entity.EntityItemTurretTop;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.entity.EntityTestAI;
import hostileworlds.entity.MovingBlock;
import hostileworlds.entity.comrade.EntityComradeImpl;
import hostileworlds.entity.monster.EntityBlockMonster;
import hostileworlds.entity.monster.EntityWormFire;
import hostileworlds.entity.monster.EntityWormSand;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.monster.ZombieClimber;
import hostileworlds.entity.monster.ZombieHungry;
import hostileworlds.entity.monster.ZombieMiner;
import hostileworlds.entity.particle.EntityMeteorTrailFX;
import hostileworlds.rts.entity.EntityRtsBase;
import hostileworlds.rts.entity.client.RenderRtsEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    public ClientProxy()
    {
        mc = ModLoader.getMinecraftInstance();
    }

    @Override
    public void init(HostileWorlds pMod)
    {
        pMod.texBloodCobble = ModLoader.addOverride("/terrain.png", "/coro/hw/bleedingMoss.png");
    	
        super.init(pMod);
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAuraCurse.class, new TileEntityAuraCurseRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHWPortal.class, new RenderHWPortal());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityTestAI.class, new RenderTestAI(new ModelTest(), 0.5F));
        
        RenderingRegistry.registerEntityRenderingHandler(Zombie.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieClimber.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieHungry.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieMiner.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieBlockWielder.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(EntityComradeImpl.class, new RenderComrade());
        RenderingRegistry.registerEntityRenderingHandler(EntityRtsBase.class, new RenderRtsEntity());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteorite.class, new RenderMeteorite());
        RenderingRegistry.registerEntityRenderingHandler(EntityWormSand.class, new RenderWormSand(new ModelWormSand(), 0.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityItemTurretTop.class, new RenderItemTurretTop(new ModelItemTurretTop(), 0.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityWormFire.class, new RenderWormFire());
        RenderingRegistry.registerEntityRenderingHandler(MovingBlock.class, new RenderMovingBlock());
        
        //For particles as weather effects
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteorTrailFX.class, new RenderNull());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlockMonster.class, new RenderNull());
        
        TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        
        //MinecraftForgeClient.registerItemRenderer(HostileWorlds.itemLaserBeam.itemID, new WeaponRenderer());
        
    }
}
