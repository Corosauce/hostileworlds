package hostileworlds.dimension;

import net.minecraft.world.biome.BiomeGenBase;

public class HWBiomeGen extends BiomeGenBase
{
	public static final HWBiomeGen base = (HWBiomeGen) (new HWBiomeGen(77)).setBiomeName("HWCatacombs");
	
    public HWBiomeGen(int par1)
    {
        super(par1);
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        
        //this.spawnableCreatureList.add(new SpawnListEntry(EntityBat.class, 100, 10, 10));
        //this.spawnableMonsterList.add(new SpawnListEntry(EntityZombie.class, 100, 1, 10));
        //this.spawnableMonsterList.add(new SpawnListEntry(Zombie.class, 100, 1, 10));
    }
}
