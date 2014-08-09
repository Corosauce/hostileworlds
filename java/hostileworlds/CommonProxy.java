package hostileworlds;

import particleman.forge.ParticleMan;
import particleman.items.ItemParticleGlove;
import hostileworlds.block.BlockAuraCurse;
import hostileworlds.block.BlockBloodMoss;
import hostileworlds.block.BlockInvasionSource;
import hostileworlds.block.TileEntityAuraCurse;
import hostileworlds.block.TileEntityInvasionSource;
import hostileworlds.config.ModConfigFields;
import hostileworlds.entity.EntityMeteorite;
import hostileworlds.entity.MovingBlock;
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
import hostileworlds.item.ItemQuestBook;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
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
        
        addBlock(pMod.blockAuraCurse = (new BlockAuraCurse()).setCreativeTab(CreativeTabs.tabMisc), TileEntityAuraCurse.class, "curseAura", "Invasion Debug Block");
        
        addBlock(pMod.blockInvasionSource = (new BlockInvasionSource()).setCreativeTab(CreativeTabs.tabMisc), TileEntityInvasionSource.class, "invasionSource", "Invasion Source Block");
        
        /*CoroUtilBlock.setUnlocalizedNameAndTexture(pMod.blockPortal = (BlockHWPortal) (new BlockHWPortal(ModConfigBlockFields.blockIDStart++, Material.portal)).setBlockUnbreakable().setResistance(6000000), HostileWorlds.modID + ":hwPortal");
        GameRegistry.registerBlock(pMod.blockPortal, "Hostile Worlds Portal");
        GameRegistry.registerTileEntity(TileEntityHWPortal.class, "tileEntityHWPortal");
        LanguageRegistry.addName(pMod.blockPortal, "Hostile Worlds Portal");*/
        
        addBlock(pMod.blockBloodyCobblestone = (new BlockBloodMoss()).setBlockUnbreakable().setResistance(6000000), "bloodMoss", "Bloody Cobblestone");
        
        /*CoroUtilBlock.setUnlocalizedNameAndTexture(pMod.blockRaidingLadder = (new BlockRaidingLadder(ModConfigBlockFields.blockIDStart++, pMod.texBloodCobble, false)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep), HostileWorlds.modID + ":raidingLadder");
        GameRegistry.registerBlock(pMod.blockRaidingLadder, "Raiding Ladder");
        LanguageRegistry.addName(pMod.blockRaidingLadder, "Raiding Ladder");*/
        
        /*pMod.blockRaidingLadderBase = (new BlockRaidingLadder(ModConfigBlockFields.blockIDStart++, pMod.texBloodCobble, true)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep).setUnlocalizedName("HostileWorlds:raidingLadderBase");
        GameRegistry.registerBlock(pMod.blockRaidingLadderBase, "Raiding Ladder Base");
        GameRegistry.registerTileEntity(TileEntityRaidingLadderBase.class, "tileEntityRaidingLadderBase");
        LanguageRegistry.addName(pMod.blockRaidingLadderBase, "Raiding Ladder Base");*/
        
        /*CoroUtilBlock.setUnlocalizedNameAndTexture(pMod.blockFactory = (new BlockFactory(ModConfigBlockFields.blockIDStart++)).setHardness(0.4F).setStepSound(Block.soundLadderFootstep).setCreativeTab(CreativeTabs.tabMisc), HostileWorlds.modID + ":factory");
        GameRegistry.registerBlock(pMod.blockFactory, "Factory");
        GameRegistry.registerTileEntity(TileEntityFactory.class, "tileEntityFactory");
        LanguageRegistry.addName(pMod.blockFactory, "Factory");
        
        CoroUtilBlock.setUnlocalizedNameAndTexture(pMod.blockItemTurret = (new BlockItemTurret(ModConfigBlockFields.blockIDStart++)).setHardness(50.0F).setResistance(2000.0F).setStepSound(Block.soundLadderFootstep).setCreativeTab(CreativeTabs.tabMisc), HostileWorlds.modID + ":itemTurret");
        GameRegistry.registerBlock(pMod.blockItemTurret, "Item Using Turret");
        GameRegistry.registerTileEntity(TileEntityItemTurret.class, "tileEntityItemTurret");
        LanguageRegistry.addName(pMod.blockItemTurret, "Item Using Turret");*/
        
        /*CoroUtilBlock.setUnlocalizedNameAndTexture(pMod.blockRtsBuilding = (new BlockRTSBuilding(ModConfigBlockFields.blockIDStart++)).setHardness(50.0F).setResistance(2000.0F).setStepSound(Block.soundLadderFootstep).setCreativeTab(CreativeTabs.tabMisc), HostileWorlds.modID + ":rtsBuilding");
        GameRegistry.registerBlock(pMod.blockRtsBuilding, "RTS Building");
        GameRegistry.registerTileEntity(TileEntityRTSBuilding.class, "TileEntityRTSBuilding");
        LanguageRegistry.addName(pMod.blockRtsBuilding, "RTS Building");*/
        
        //CoroUtilItem.setUnlocalizedNameAndTexture(pMod.itemLaserBeam = (ItemLaserBeam) (new ItemLaserBeam(ModConfigBlockFields.itemIDStart)).setCreativeTab(CreativeTabs.tabMisc), HostileWorlds.modID + ":itemLaserBeam");
        //GameRegistry.registerItem(pMod.itemLaserBeam, "Laser Beam");
        //LanguageRegistry.addName(pMod.itemLaserBeam, "Laser Beam");
        
        //GameRegistry.addRecipe(new ItemStack(pMod.blockItemTurret, 1), new Object[] {" B ", "RCR", "CCC", 'B', Item.bow, 'R', Block.blockRedstone, 'C', Block.cobblestoneMossy});
        
        Item questBook = new ItemQuestBook();
        questBook.setCreativeTab(CreativeTabs.tabMisc);
        
        questBook.setUnlocalizedName(HostileWorlds.modID + ":questBook");
        questBook.setTextureName(HostileWorlds.modID + ":questBook");
        
        GameRegistry.registerItem(questBook, "hwQuestBook");
        LanguageRegistry.addName(questBook, "Hostile Worlds Quest Book");
        
        //GameRegistry.addRecipe(new ItemStack(questBook), new Object[] {" LL", "LRR", "LRD", Character.valueOf('L'), Items.leather, Character.valueOf('R'), Items.redstone, Character.valueOf('D'), Items.diamond});
        
        String prefix = "entity.HostileWorlds.";
        String end = ".name";
        
        //EntityRegistry.registerModEntity(EntityTestAI.class, "EntityTestAI", entityId++, pMod, 128, 1, true);
        
        EntityRegistry.registerModEntity(Zombie.class, "InvaderZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieMiner.class, "InvaderZombieMiner", entityId++, pMod, 256, 1, true);
        EntityRegistry.registerModEntity(ZombieClimber.class, "ClimberZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieHungry.class, "HungryZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieClawer.class, "ClawingZombie", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(ZombieBlockWielder.class, "BlockWielderZombie", entityId++, pMod, 256, 1, true);
        LanguageRegistry.instance().addStringLocalization(prefix + "BlockWielderZombie" + end, "Block Wielder");
        
        EntityRegistry.registerModEntity(EntityComradeImpl.class, "EntityComrade", entityId++, pMod, 128, 1, true);
        
        EntityRegistry.registerModEntity(EntityMeteorite.class, "EntityMeteorite", entityId++, pMod, 512, 1, true);
        EntityRegistry.registerModEntity(EntityWormSand.class, "EntityWormSand", entityId++, pMod, 128, 1, true);
        EntityRegistry.registerModEntity(EntityWormFire.class, "EntityWormFire", entityId++, pMod, 128, 1, true);
        
        EntityRegistry.registerModEntity(MovingBlock.class, "EntityDebrisBlock", entityId++, pMod, 512, 1, true);
        //EntityRegistry.registerModEntity(EntityItemTurretTop.class, "EntityItemTurretTop", entityId++, pMod, 128, 1, true);

        //EntityRegistry.registerModEntity(EntityRtsWorker.class, "EntityRtsWorker", entityId++, pMod, 128, 1, true);
    	//EntityRegistry.registerModEntity(EntityScent.class, "EntityScent", entityId++, pMod, 32, 20, false);
        EntityRegistry.registerModEntity(EntityBlockMonster.class, "EntityBlockMonster", entityId++, pMod, 128, 1, true);
    	
        //EntityRegistry.registerModEntity(EntitySurfboard.class, "EntitySurfboard", entityId++, mod, 64, 10, true);
        
		//EntityRegistry.registerGlobalEntityID(c_w_MovingBlockStructure.class, "c_w_MovingBlockStructure", entityId-1,0,0);
        //EntityRegistry.registerModEntity(EntityKoaManly.class, "Koa Man", entityId++, mod, 64, 1, true);
        //GameRegistry.registerTileEntity(TileEntityTSiren.class, "c_w_TileEntityTSiren");
        
        BiomeGenBase[] biomes = {BiomeGenBase.desert, BiomeGenBase.taiga, BiomeGenBase.forest, BiomeGenBase.jungle, BiomeGenBase.plains, BiomeGenBase.swampland, BiomeGenBase.mesa, BiomeGenBase.roofedForest};
        
        //isnt navigating right, remove for now
        //EntityRegistry.addSpawn(ZombieClimber.class, 1, 1, 2, EnumCreatureType.monster, biomes);
        if (ModConfigFields.spawnHungryZombie) EntityRegistry.addSpawn(ZombieHungry.class, 10, 1, 1, EnumCreatureType.monster, biomes);
        if (ModConfigFields.spawnClawerZombie) EntityRegistry.addSpawn(ZombieClawer.class, 10, 1, 1, EnumCreatureType.monster, biomes);
    }
    
    public static void addBlock(Block block, Class tEnt, String unlocalizedName, String blockNameBase) {
		addBlock(block, unlocalizedName, blockNameBase);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
		HostileWorlds.blocksList.add(block);
	}
	
	public static void addBlock(Block parBlock, String unlocalizedName, String blockNameBase) {
		//vanilla calls
		GameRegistry.registerBlock(parBlock, unlocalizedName);
		parBlock.setBlockName(HostileWorlds.modID + ":" + unlocalizedName);
		parBlock.setBlockTextureName(HostileWorlds.modID + ":" + unlocalizedName);
		parBlock.setCreativeTab(CreativeTabs.tabMisc);
		LanguageRegistry.addName(parBlock, blockNameBase);
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
}
