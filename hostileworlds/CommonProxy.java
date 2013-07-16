package hostileworlds;

import hostileworlds.block.BlockAuraCurse;
import hostileworlds.block.BlockFactory;
import hostileworlds.block.BlockHWPortal;
import hostileworlds.block.BlockItemTurret;
import hostileworlds.block.BlockRaidingLadder;
import hostileworlds.block.TileEntityAuraCurse;
import hostileworlds.block.TileEntityFactory;
import hostileworlds.block.TileEntityHWPortal;
import hostileworlds.block.TileEntityItemTurret;
import hostileworlds.block.TileEntityRaidingLadderBase;
import hostileworlds.config.ModConfigBlockFields;
import hostileworlds.entity.EntityItemTurretTop;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.entity.EntityTestAI;
import hostileworlds.entity.MovingBlock;
import hostileworlds.entity.comrade.EntityComradeImpl;
import hostileworlds.entity.monster.EntityWormFire;
import hostileworlds.entity.monster.EntityWormSand;
import hostileworlds.entity.monster.Zombie;
import hostileworlds.entity.monster.ZombieBlockWielder;
import hostileworlds.entity.monster.ZombieClimber;
import hostileworlds.entity.monster.ZombieHungry;
import hostileworlds.entity.monster.ZombieMiner;
import hostileworlds.item.ItemLaserBeam;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

    public HostileWorlds mod;

    public CommonProxy()
    {
    }

    public void init(HostileWorlds pMod)
    {
        mod = pMod;
        //TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
        
        pMod.blockAuraCurse = (new BlockAuraCurse(ModConfigBlockFields.blockIDStart++)).setCreativeTab(CreativeTabs.tabMisc).setUnlocalizedName("HostileWorlds:stoneMoss");
        GameRegistry.registerBlock(pMod.blockAuraCurse, "Curse Aura");
        GameRegistry.registerTileEntity(TileEntityAuraCurse.class, "tileEntityAuraCurse");
        LanguageRegistry.addName(pMod.blockAuraCurse, "Invasion Debug Block");
        
        pMod.blockPortal = (BlockHWPortal) (new BlockHWPortal(ModConfigBlockFields.blockIDStart++, Material.portal)).setBlockUnbreakable().setResistance(6000000).setUnlocalizedName("HostileWorlds:hwPortal");
        GameRegistry.registerBlock(pMod.blockPortal, "Hostile Worlds Portal");
        GameRegistry.registerTileEntity(TileEntityHWPortal.class, "tileEntityHWPortal");
        LanguageRegistry.addName(pMod.blockPortal, "Hostile Worlds Portal");
        
        pMod.blockBloodyCobblestone = (new Block(ModConfigBlockFields.blockIDStart++, Material.rock)).setBlockUnbreakable().setResistance(6000000).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("HostileWorlds:bloodMoss");
        GameRegistry.registerBlock(pMod.blockBloodyCobblestone, "Bloody Cobblestone");
        LanguageRegistry.addName(pMod.blockBloodyCobblestone, "Bloody Cobblestone");
        
        pMod.blockRaidingLadder = (new BlockRaidingLadder(ModConfigBlockFields.blockIDStart++, pMod.texBloodCobble, false)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep).setUnlocalizedName("HostileWorlds:raidingLadder");
        GameRegistry.registerBlock(pMod.blockRaidingLadder, "Raiding Ladder");
        LanguageRegistry.addName(pMod.blockRaidingLadder, "Raiding Ladder");
        
        /*pMod.blockRaidingLadderBase = (new BlockRaidingLadder(ModConfigBlockFields.blockIDStart++, pMod.texBloodCobble, true)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep).setUnlocalizedName("HostileWorlds:raidingLadderBase");
        GameRegistry.registerBlock(pMod.blockRaidingLadderBase, "Raiding Ladder Base");
        GameRegistry.registerTileEntity(TileEntityRaidingLadderBase.class, "tileEntityRaidingLadderBase");
        LanguageRegistry.addName(pMod.blockRaidingLadderBase, "Raiding Ladder Base");*/
        
        pMod.blockFactory = (new BlockFactory(ModConfigBlockFields.blockIDStart++)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep).setUnlocalizedName("HostileWorlds:factory").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(pMod.blockFactory, "Factory");
        GameRegistry.registerTileEntity(TileEntityFactory.class, "tileEntityFactory");
        LanguageRegistry.addName(pMod.blockFactory, "Factory");
        
        pMod.blockItemTurret = (new BlockItemTurret(ModConfigBlockFields.blockIDStart++)).setHardness(50.0F).setResistance(2000.0F).setStepSound(Block.soundLadderFootstep).setUnlocalizedName("HostileWorlds:itemTurret").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerBlock(pMod.blockItemTurret, "Item Using Turret");
        GameRegistry.registerTileEntity(TileEntityItemTurret.class, "tileEntityItemTurret");
        LanguageRegistry.addName(pMod.blockItemTurret, "Item Using Turret");
        
        pMod.itemLaserBeam = (ItemLaserBeam) (new ItemLaserBeam(ModConfigBlockFields.itemIDStart)).setUnlocalizedName("HostileWorlds:itemLaserBeam").setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(pMod.itemLaserBeam, "Laser Beam");
        LanguageRegistry.addName(pMod.itemLaserBeam, "Laser Beam");
        
        GameRegistry.addRecipe(new ItemStack(pMod.blockItemTurret, 1), new Object[] {" B ", "RCR", "CCC", 'B', Item.bow, 'R', Block.blockRedstone, 'C', Block.cobblestoneMossy});
        
        String prefix = "entity.HostileWorlds.";
        String end = ".name";
        
        EntityRegistry.registerModEntity(EntityTestAI.class, "EntityTestAI", entityId++, pMod, 128, 1, true);
        
        EntityRegistry.registerModEntity(Zombie.class, "InvaderZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieMiner.class, "InvaderZombieMiner", entityId++, pMod, 256, 1, true);
        EntityRegistry.registerModEntity(ZombieClimber.class, "ClimberZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieHungry.class, "HungryZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieBlockWielder.class, "BlockWielderZombie", entityId++, pMod, 256, 1, true);
        LanguageRegistry.instance().addStringLocalization(prefix + "BlockWielderZombie" + end, "Block Wielder");
        
        EntityRegistry.registerModEntity(EntityComradeImpl.class, "EntityComrade", entityId++, pMod, 128, 1, true);
        
        
        EntityRegistry.registerModEntity(EntityMeteorite.class, "EntityMeteorite", entityId++, pMod, 512, 1, true);
        EntityRegistry.registerModEntity(EntityWormSand.class, "EntityWormSand", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(EntityWormFire.class, "EntityWormFire", entityId++, pMod, 128, 1, true);
        
        EntityRegistry.registerModEntity(MovingBlock.class, "EntityDebrisBlock", entityId++, pMod, 512, 1, true);
        EntityRegistry.registerModEntity(EntityItemTurretTop.class, "EntityItemTurretTop", entityId++, pMod, 128, 1, true);
    	//EntityRegistry.registerModEntity(EntityScent.class, "EntityScent", entityId++, pMod, 32, 20, false);
    	
    	
        //EntityRegistry.registerModEntity(EntitySurfboard.class, "EntitySurfboard", entityId++, mod, 64, 10, true);
        
		//EntityRegistry.registerGlobalEntityID(c_w_MovingBlockStructure.class, "c_w_MovingBlockStructure", entityId-1,0,0);
        //EntityRegistry.registerModEntity(EntityKoaManly.class, "Koa Man", entityId++, mod, 64, 1, true);
        //GameRegistry.registerTileEntity(TileEntityTSiren.class, "c_w_TileEntityTSiren");
    }

    public int getUniqueTextureLoc()
    {
        return 0;
    }

    public int getArmorNumber(String type)
    {
        return 0;
    }

    public int getUniqueTropicraftLiquidID()
    {
        return 0;
    }

    public void loadSounds()
    {
    }

    public void registerRenderInformation()
    {
    }

    public void registerTileEntitySpecialRenderer()
    {
    }

    public void displayRecordGui(String displayText)
    {
    }

    public World getClientWorld()
    {
        return null;
    }

    public World getServerWorld()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null)
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
        }
        else
        {
            return null;
        }
    }

    public World getSidesWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    public void weatherDbg()
    {
        // TODO Auto-generated method stub
    }

    public void windDbg()
    {
        // TODO Auto-generated method stub
    }
    
    public Entity getEntByID(int id) {
		System.out.println("common getEntByID being used, this is bad");
		return null;
	}
}
