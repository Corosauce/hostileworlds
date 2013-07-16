package hostileworlds.config;

import modconfig.IConfigCategory;
import net.minecraft.block.Block;
import net.minecraftforge.common.Configuration;

public class ModConfigFields implements IConfigCategory {

	public static int difficulty = 1; //0 = easy, 1 = normal, 2 = hard
    ///echo easy: 3, normal: 4, hard: 6 - vanilla damages for zombie vs unarmored player
    public static String noInvadeWhitelist = "";
    public static int meteorCrashDistFromPlayerMin = 50;
    public static int meteorCrashDistFromPlayerMax = 400;
    public static int areaConverterReplaceBlocksWith = Block.cobblestoneMossy.blockID;
    public static boolean invadersDropNothing = true;
    public static int coolDownFirstTime = 24000 * 3;
    public static int coolDownBetweenInvasionsPortal = 24000 * 5;
    public static int coolDownBetweenInvasionsCave = 12000;
    public static int coolDownBetweenWaves = 12000;
    public static int invasionWaveCountMax = 6;
    public static int invasionBaseInvaderCount = 6;
    public static boolean portalTeleporting = true;
    public static boolean invasionManyPerPortal = false;
    public static boolean invasionCaves = true;
    public static boolean invasionPortal = true;
    public static int invasionPortalChancePercent = 30;
    public static int invasionCaveChancePercent = 70;
    public static int invasionCaveMaxDistStart = 150;
    public static boolean debugConsoleOutput = false;
    public static boolean debugTickMain = true;
    public static boolean debugDoAreaScans = true;
    public static boolean debugTurretsFreeEnergy = false;
    public static boolean warpInvadersCloser = true;
    public static int autoSaveFrequencyInTicks = 36000; //30 minutes
    
	@Override
	public String getCategory() {
		return "Hostile Worlds Settings";
	}

	@Override
	public String getConfigFileName() {
		return "HostileWorlds";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
