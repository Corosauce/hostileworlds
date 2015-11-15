package hostileworlds;

import hostileworlds.block.TileEntityAuraCurse;
import hostileworlds.block.TileEntityAuraCurseRenderer;
import hostileworlds.block.TileEntitySourceInvasion;
import hostileworlds.block.TileEntitySourceInvasionRenderer;
import hostileworlds.client.entity.ModelWormSand;
import hostileworlds.client.entity.RenderComrade;
import hostileworlds.client.entity.RenderHWOrc;
import hostileworlds.client.entity.RenderHWZombie;
import hostileworlds.client.entity.RenderMeteorite;
import hostileworlds.client.entity.RenderMovingBlock;
import hostileworlds.client.entity.RenderNull;
import hostileworlds.client.entity.RenderWormFire;
import hostileworlds.client.entity.RenderWormSand;
import hostileworlds.commands.CommandHWClient;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.entity.MovingBlock;
import hostileworlds.entity.bt.OrcArcher;
import hostileworlds.entity.bt.OrcBoss;
import hostileworlds.entity.bt.OrcGuard;
import hostileworlds.entity.bt.OrcSeeker;
import hostileworlds.entity.comrade.EntityComradeImpl;
import hostileworlds.entity.monster.EntityBlockMonster;
import hostileworlds.entity.monster.EntityWormFire;
import hostileworlds.entity.monster.EntityWormSand;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.monster.ZombieClawer;
import hostileworlds.entity.monster.ZombieClimber;
import hostileworlds.entity.monster.ZombieHungry;
import hostileworlds.entity.monster.ZombieMiner;
import hostileworlds.entity.particle.EntityMeteorTrailFX;
import net.minecraftforge.client.ClientCommandHandler;
import CoroUtil.entity.projectile.EntityArrow;
import CoroUtil.entity.projectile.EntityFireBall;
import CoroUtil.entity.render.projectile.RenderArrow;
import CoroUtil.entity.render.projectile.RenderProjectileElementalBall;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    public ClientProxy()
    {
    	
    }

    @Override
    public void init(HostileWorlds pMod)
    {
        //pMod.texBloodCobble = ModLoader.addOverride("/terrain.png", "/coro/hw/bleedingMoss.png");
    	
        super.init(pMod);
        
        ClientCommandHandler.instance.registerCommand(new CommandHWClient());
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAuraCurse.class, new TileEntityAuraCurseRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySourceInvasion.class, new TileEntitySourceInvasionRenderer());
        //ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHWPortal.class, new RenderHWPortal());
        
        //RenderingRegistry.registerEntityRenderingHandler(EntityTestAI.class, new RenderTestAI(new ModelTest(), 0.5F));
        
        RenderingRegistry.registerEntityRenderingHandler(Zombie.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieClimber.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieHungry.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieClawer.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieMiner.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(ZombieBlockWielder.class, new RenderHWZombie());
        RenderingRegistry.registerEntityRenderingHandler(EntityComradeImpl.class, new RenderComrade());
        //RenderingRegistry.registerEntityRenderingHandler(EntityRtsBase.class, new RenderRtsEntity());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteorite.class, new RenderMeteorite());
        RenderingRegistry.registerEntityRenderingHandler(EntityWormSand.class, new RenderWormSand(new ModelWormSand(), 0.5F));
        //RenderingRegistry.registerEntityRenderingHandler(EntityItemTurretTop.class, new RenderItemTurretTop(new ModelItemTurretTop(), 0.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityWormFire.class, new RenderWormFire());
        RenderingRegistry.registerEntityRenderingHandler(MovingBlock.class, new RenderMovingBlock());
        
        RenderingRegistry.registerEntityRenderingHandler(OrcArcher.class, new RenderHWOrc());
        RenderingRegistry.registerEntityRenderingHandler(OrcGuard.class, new RenderHWOrc());
        RenderingRegistry.registerEntityRenderingHandler(OrcBoss.class, new RenderHWOrc());
        RenderingRegistry.registerEntityRenderingHandler(OrcSeeker.class, new RenderHWOrc());
        
        RenderingRegistry.registerEntityRenderingHandler(EntityArrow.class, new RenderArrow());
        
        //For particles as weather effects
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteorTrailFX.class, new RenderNull());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlockMonster.class, new RenderNull());
        RenderingRegistry.registerEntityRenderingHandler(EntityFireBall.class, new RenderProjectileElementalBall());
        
        //TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        
        //MinecraftForgeClient.registerItemRenderer(HostileWorlds.itemLaserBeam.itemID, new WeaponRenderer());
        
    }
}
