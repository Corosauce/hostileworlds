package hostileworlds.dimension;

import hostileworlds.entity.monster.Zombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

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
