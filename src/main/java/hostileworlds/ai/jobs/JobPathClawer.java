package hostileworlds.ai.jobs;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.monster.ZombieMiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.jobSystem.JobManager;
import CoroUtil.entity.EnumJobState;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilPath;

public class JobPathClawer extends JobGroupHorde {
	
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! new AI has inventory, make miners use it, make them gather ONLY ores it mines through, no cobble, drops on death, pinata fun times!
	
	//Zombie Miner AI layout
	//job states: mainly for managing positioning and method of approach
	//-- IDLE: determines if he can pathfind to the curse area, if has path reroutes to super job and returns
	//-- W1: verifies it is a diggable angle (not directly above or below), repathfinds and waits, should perhaps be merged with simudig, incase pathfinding cant get a diggable angle 
	//-- W2: needs to dig, determines best angle depending on current simudig attempt
	//--- 1: strait shot to attackCoord simudig
	//--- 2+: simudigs towards the general area with increasing randomness as attempts fail
	//-- W3: in position, performs dig until end dig destination reached, needs more logic
	
	//Notes:
	// MAYBE simudig needs to over shoot the vertical direction, to solve issues of redundant digs just under surface, unless that could be more suprising....
	// better detection for being on the surface, to stop digging of 1 block deep groove towards target
	
	//Threading and pathfind simulation variables
	public int pfReturnTimeout = 0;
	public boolean waitingForPath = false;
	public boolean waitingForThreadPath = false;
	public PathEntity pathReturned = null;
	public boolean tryPathToTarget = true;
	public boolean hasPathToTarget = false;

	//Dig state variables
	public int simuDigCount;
	public int simuDigCooldownCur = 0;
	public int simuDigCooldownMax = 40;
	public ChunkCoordinates lastMinedCoord = null; //last block he broke
	public EnumJobState digState = EnumJobState.IDLE; //used for upper/lower block dig, may account for angled dig later
	public ChunkCoordinates bestDigCoord = null; //this is the endpoint of the dig
	public float curBlockDamage;
	public float tickBlockDamage = 0.1F;
	public int seesSkyTicks = 0;
	public int noMoveTicks = 0;
	public int noDigTicks = 0;
	public int noDigTicksMax = 300;
	
	public float yDiffMax = 2F;
	
	public JobPathClawer(JobManager jm) {
		super(jm);
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return true;
	}
	
	@Override
	public void onIdleTickAct() {
		
	}
	
	public void dbg(Object par) {
		HostileWorlds.dbg(par);
		//dbg(ent.entityId + ": " + par);
	}
	
	@Override
	public void tick() {
		
		boolean doMineTick = false;
		boolean tryDoorBreak = true;
		boolean doDoorBreak = true;
		
		//force 1 node path (strait move to)
		if (ai.entityToAttack != null) {
			//dbg("forcing path");
			
			//setPathToEntity(CoroUtilPath.getSingleNodePath(new ChunkCoordinates(MathHelper.floor_double(ai.entityToAttack.posX), MathHelper.floor_double(ai.entityToAttack.posY), MathHelper.floor_double(ai.entityToAttack.posZ))));
			
			if (ent.getDistanceToEntity(ai.entityToAttack) >= 2F && Math.abs(ent.posY-ai.entityToAttack.posY) < 3) {
				doMineTick = true;
				ent.getMoveHelper().setMoveTo(ai.entityToAttack.posX, ai.entityToAttack.posY, ai.entityToAttack.posZ, OldUtil.getMoveSpeed(ent));
				setPathToEntity(null);
			}
		}
		
		if (lastMinedCoord != null) {
			if (ent.getDistance(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ) > 3) {
				lastMinedCoord = null;
			}
		}
		
		
		if (doMineTick || lastMinedCoord != null) {
			
			if (tryDoorBreak) {
				if (curBlockDamage == 0F) {
					trySetNextDigBlock(1);
					if (lastMinedCoord != null) {
						/*Block id = ent.worldObj.getBlock(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
						if (!CoroUtilBlock.isAir(id)) {
							//Block block = Block.blocksList[id];
							if (id instanceof BlockDoor) {
								
							} else {
								lastMinedCoord = null;
							}
						}*/
					} else {
						trySetNextDigBlock(0);
					}
				}
			}
			
			if (lastMinedCoord != null) {
				
				
				
				float strVsBlock = 0F;
				
				Block id = ent.worldObj.getBlock(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
				if (!CoroUtilBlock.isAir(id)) {
					//Block block = Block.blocksList[id];
					strVsBlock = id.getBlockHardness(ent.worldObj, lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
					//dbg(block.getBlockName() + " - " + 4F / strVsBlock / 20F + " - " + curBlockDamage);
				}
				
				//bedrock/unbreakable safety
				if (strVsBlock == -1) {
					lastMinedCoord = null;
					return;
				}
				
				curBlockDamage += 0.5F / strVsBlock / 20F;
				
				 //= block.getBlockHardness(worldRef, 0, 0, 0) - (((itemStr.getStrVsBlock(block) - 1) / 4F));
				
				if (ent.worldObj.getWorldTime() % 4 == 0 && !CoroUtilBlock.isAir(id)) {
					
					ent.worldObj.playSoundAtEntity(ent, "step.stone", 1F, 1F);
					
				}
				
				if (curBlockDamage > 1F) {
					
					//dbg("break block id: " + ent.worldObj.getBlockId(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ));
					
					//break, reset dig damage
					ent.worldObj.destroyBlockInWorldPartially(ent.getEntityId(), lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, -1);
					ent.worldObj.setBlock(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, Blocks.air);
					
					//Block block = Block.blocksList[id];
					if (!CoroUtilBlock.isAir(id) && id.stepSound != null) ent.worldObj.playSoundAtEntity(ent, id.stepSound.getBreakSound(), 1F, 1F);
					
					curBlockDamage = 0F;
					noDigTicks = 0;
					
					//jump once to get up the step, test if works consistantly
					//if (digYDir == 1 && digState == EnumJobState.W2) {
						//ent.jump();
					//}
					
					
					nextDigState();
					lastMinedCoord = null;
				} else {
					//dbg("partial dig: " + (int)(curBlockDamage * 10F));
					ent.worldObj.destroyBlockInWorldPartially(ent.getEntityId(), lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, (int)(curBlockDamage * 10F));
				}
				ent.swingItem();
			} else {
				//skip a block coord if its air, also if idle or w3 is skipped due to non angled dig
				nextDigState();
				curBlockDamage = 0;
				
			}
			
			//ent.moveEntity(0.001F, 0.1F, 0.001F);
			
			
			
			
		}
		
		
		if (ent.worldObj.getWorldTime() % 20 == 0 && ent.motionY < 0.05F
				/*ent.onGround &&*/
				) {
			if (lastMinedCoord != null && ent.boundingBox.minY < lastMinedCoord.posY) {
				OldUtil.jump(ent);
			} else if (ai.entityToAttack != null && ent.isCollidedHorizontally) {
				OldUtil.jump(ent);
			}
			//ent.motionY = 0.35F;
			
			//ent.jump();
		}
		
		if (!doMineTick && lastMinedCoord == null) {
			if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.02F) {
				noMoveTicks++;
			} else {
				noMoveTicks = 0;
			}
		}
		
		//dbg("no move ticks: " + noMoveTicks);
		
		if (noMoveTicks > 200) {
			if (this.state == EnumJobState.W1) {
				setJobState(EnumJobState.W2);
				noMoveTicks = 0;
				dbg("idle out set to W2");
				
			} else {
				dbg("idle out reset");
				fullReset();
			}
		}
		
	}
	
	public void nextDigState() {
		if (digState == EnumJobState.IDLE) {
			digState = EnumJobState.W1;
		} else if (digState == EnumJobState.W1) {
			digState = EnumJobState.W2;
		} else if (digState == EnumJobState.W2) {
			digState = EnumJobState.W3;
		} else if (digState == EnumJobState.W3) {
			digState = EnumJobState.IDLE;
		}
		//dbg("set state: " + digState);
	}
	
	public boolean trySetNextDigBlock(int offset) {
		return trySetNextDigBlock(offset, false, 0);
	}
	
	public boolean trySetNextDigBlock(int offset, boolean usePosition, int afterTraceOffset) {
		
		ChunkCoordinates coords = null;
		
		if (usePosition) {
			coords = new ChunkCoordinates((int)ent.posX, (int)ent.posY + 1 + offset, (int)ent.posZ); //testing & tweaking required
		} else {
			MovingObjectPosition aim = null;
			
			int randSize = 30;
			
			ent.rotationPitch = 0;
			ent.rotationYaw += ent.worldObj.rand.nextInt(randSize) - (randSize/2);
			aim = entInt.getAIAgent().rayTrace(1F, offset, null);
			
			if (aim == null && bestDigCoord != null && bestDigCoord.posY < ent.posY) {
				//coords = new ChunkCoordinates((int)ent.posX, (int)ent.boundingBox.minY - 3, (int)ent.posZ); //testing & tweaking required
			}
			
			if (aim != null) {
				coords = new ChunkCoordinates(aim.blockX, aim.blockY + afterTraceOffset, aim.blockZ);
			} else {
				for (int i = 0; i < 8; ++i)
		        {
		            float f = ((float)((i >> 0) % 2) - 0.5F) * ent.width * 0.8F;
		            float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
		            float f2 = ((float)((i >> 2) % 2) - 0.5F) * ent.width * 0.8F;
		            int j = MathHelper.floor_double(ent.posX + (double)f);
		            int k = MathHelper.floor_double(ent.posY + (i % 2 == 0 ? (double)ent.getEyeHeight() : 0.5) + (double)f1);
		            int l = MathHelper.floor_double(ent.posZ + (double)f2);

		            Block id = ent.worldObj.getBlock(j, k, l);
		            
		            if (!CoroUtilBlock.isAir(id) && id.getMaterial().isSolid())
		            {
		            	coords = new ChunkCoordinates(j, k, l);
		            }
		        }
			}
		}
		
		
		
		if (coords != null) {
			Block id = ent.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
			//Block block = Block.blocksList[id];
			if ((!CoroUtilBlock.isAir(id) && id.getMaterial() == Material.lava)) {
				fullReset();
				return false;
			}
			if (!CoroUtilBlock.isAir(id) && (id.getMaterial() != Material.water)) {
				if (ent.worldObj.getTileEntity(coords.posX, coords.posY, coords.posZ) == null) {
					lastMinedCoord = coords;
					return true;
				}
			}
		}
		
		//dbg("found nothing for " + digState);
		
		return false;
	}
	
	public void fullReset() {
		noMoveTicks = 0;
		seesSkyTicks = 0;
		/*if (simuDigCount >= 10) */simuDigCount = 0;
		pfReturnTimeout = 0;
		waitingForPath = false;
		waitingForThreadPath = false;
		pathReturned = null;
		tryPathToTarget = true;
		hasPathToTarget = false;
		if (lastMinedCoord != null) ent.worldObj.destroyBlockInWorldPartially(ent.getEntityId(), lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, -1);
		lastMinedCoord = null;
		simuDigCooldownCur = 0;
		digState = EnumJobState.IDLE;
		//bestDigCoord = null;
		setJobState(EnumJobState.IDLE);
	}
	
	public boolean isDiggableAngle(ChunkCoordinates coord) {
		if (ent.posY > ((JobGroupHorde)jm.getPrimaryJob()).attackCoord.posY) {
			return true;
		} else if (getVerticalAngle(coord) < yDiffMax) {/*} else if (getVerticalAngle(coord) < 0.9F) {*/
			return true;
		}
		return false;
	}
	
	public float getVerticalAngle(ChunkCoordinates coord) {
		
		double xDiff = coord.posX - ent.posX;
		double zDiff = coord.posZ - ent.posZ;
		double distHoriz = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		double distVert = coord.posY - ent.posY;
		if (distHoriz == 0) distHoriz = 1;
		return (float)(distVert / distHoriz);
		
		//return 60F; //fake a safe angle for now
	}
	
	public void setPathToEntity(PathEntity pathentity)
    {
		if (waitingForThreadPath) {
			pathReturned = pathentity;
			waitingForThreadPath = false;
		} else {
			entInt.getAIAgent().setPathToEntityForce(pathentity);
		}
    }
	
	public ChunkCoordinates rayTraceBlocksInverted(Vec3 par1Vec3, Vec3 par2Vec3) {
		
		int maxAirBlocksIntoSolid = 3;
		int curAirBlocksIntoSolid = 0;
		
		int maxAirBlocksBeforeSolid = 4;
		int curAirBlocksBeforeSolid = 0;
		
		double var1 = par2Vec3.xCoord - par1Vec3.xCoord;
        double var3 = par2Vec3.yCoord - par1Vec3.yCoord;
        double var5 = par2Vec3.zCoord - par1Vec3.zCoord;
        double var7 = MathHelper.sqrt_double(var1 * var1 + var3 * var3 + var5 * var5);
        
        float stepSize = 1F;

        double scanDirX = var1 / var7 * stepSize;
        double scanDirY = var3 / var7 * stepSize;
        double scanDirZ = var5 / var7 * stepSize;
        
        int maxIterations = 800;
        int curIterations = 0;
        
        double curX = par1Vec3.xCoord;
        double curY = par1Vec3.yCoord;
        double curZ = par1Vec3.zCoord;
        
        boolean foundFirstSolid = false;
        
        while (curIterations++ < maxIterations) {
        	curX += var1 / var7 * stepSize;
        	curY += var3 / var7 * stepSize;
        	curZ += var5 / var7 * stepSize;
        	
        	Block id = ent.worldObj.getBlock((int)curX, (int)curY, (int)curZ);
        	
        	//area protection
        	if (isNoDigCoord((int)curX, (int)curY, (int)curZ)) {
        	    dbg("simudig fail: bukkit protected area");
        	    return null;
        	}
        	
        	if (!CoroUtilBlock.isAir(id)) {
        		foundFirstSolid = true;
        		curAirBlocksIntoSolid = 0;
        		if (isNoDigBlock(id)) {
        			dbg("simudig fail: no dig block");
        			return null;
        		} else if (ent.worldObj.getHeightValue((int)curX, (int)curZ) <= (int)curY) {
        			dbg("simudig success: target coord is at surface");
        			return new ChunkCoordinates((int)curX, (int)curY, (int)curZ);
        		}
        	} else {
        		if (foundFirstSolid) {
	        		curAirBlocksIntoSolid++;
	        		if (curAirBlocksIntoSolid > maxAirBlocksIntoSolid) {
	        			
	        			int curIterations2 = 0;
	        			int maxIterations2 = 5;
	        			
	        			while (curIterations2++ < maxIterations2) {
	        				id = ent.worldObj.getBlock((int)curX, (int)curY-curIterations2, (int)curZ);
	        				
	        				//return a coord that is on the ground as long as its not too far down
	        				if (!CoroUtilBlock.isAir(id)) {
	        					dbg("simudig success: safe drop");
	        					return new ChunkCoordinates((int)curX, (int)curY-curIterations2+1, (int)curZ);
	        				}
	        			}
	        			
	        			//big drop, fail it
	        			dbg("simudig fail: bad drop");
	        			return null;//new ChunkCoordinates((int)curX, (int)curY, (int)curZ);
	        			
	        		}
        		} else {
        			curAirBlocksBeforeSolid++;
        			if (curAirBlocksBeforeSolid > maxAirBlocksBeforeSolid) {
        				dbg("simudig fail: too much air at start");
        				return null;
        			}
        		}
        	}
        }
        
        dbg("simudig fail: max iterations");
		return null;
	}
	
	public boolean isNoDigBlock(Block id) {
		if (isDangerBlock(id)) return true;
		if (simuDigCount < 5) {
			if (id.getMaterial() == Material.water) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isNoDigCoord(int x, int y, int z) {

        // MCPC start
          /*org.bukkit.entity.Entity bukkitentity = ent.getBukkitEntity();
          if ((bukkitentity instanceof Player)) {
            Player player = (Player)bukkitentity;
            BlockBreakEvent breakev = new BlockBreakEvent(player.getWorld().getBlockAt(x, y, z), player);
            Bukkit.getPluginManager().callEvent(breakev);
            if (breakev.isCancelled()) {
                return true;
            }
            breakev.setCancelled(true);
          }*/
          // MCPC end
          
          return false;
	}
	
	public boolean isDangerBlock(Block id) {
		if (id.getMaterial() == Material.lava || id.getMaterial() == Material.cactus) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canJoinFormations() {
		return false;
	}
}
