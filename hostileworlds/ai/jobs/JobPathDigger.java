package hostileworlds.ai.jobs;

import hostileworlds.HostileWorlds;
import hostileworlds.entity.monster.ZombieMiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import CoroAI.PFQueue;
import CoroAI.componentAI.jobSystem.JobManager;
import CoroAI.entity.EnumJobState;

public class JobPathDigger extends JobGroupHorde {
	
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
	
	public JobPathDigger(JobManager jm) {
		super(jm);
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return hasPathToTarget;
	}
	
	@Override
	public void onIdleTick() {
		
	}
	
	public void dbg(Object par) {
		HostileWorlds.dbg(par);
		//dbg(ent.entityId + ": " + par);
	}
	
	@Override
	public void tick() {
		
		boolean doMineTick = false;
		boolean tryDoorBreak = false;
		boolean doDoorBreak = false;
		
		//super.tick();
		//fullReset();
		//ent.setDead();
		//hasPathToTarget = false;
		//temp?
		
		
		
		//dbg(System.currentTimeMillis() % 1000);
		
		try {
			if (attackCoord != null && ent instanceof ZombieMiner && ent.worldObj.getWorldTime() % 60 == 0) dbg("dim: " + ent.dimension + " miner cur dist: " + ent.getDistance(attackCoord.posX, attackCoord.posY, attackCoord.posZ));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		//temp kill when dug out
		/*if (ent.worldObj.canBlockSeeTheSky((int)ent.posX, (int)ent.posY+2, (int)ent.posZ)) {
			dbg(ent.entityId + ": sees sky, sucess! killing");
			ent.setDead();
			return;
		}*/
		
		//weird temp kill
		if (ent.worldObj.getBlockId((int)ent.posX, (int)ent.posY-1, (int)ent.posZ) != HostileWorlds.blockBloodyCobblestone.blockID) {
			//dbg("weawewaeawe");
			//ent.setDead();
			//return;
		}
		
		if (ent.isInWater()) {
			ent.motionY += 0.05F;
			if (Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.1F) {
				ent.moveFlying(/*(ent.worldObj.rand.nextFloat() * 0.2F) - 0.1F*/0, 0.1F, 0.1F);
			}
		}
		
		//jump help - something broke default nav jumping, why?
		if (this.state == digState.IDLE || this.state == digState.W1) {
			if (ent.onGround && ent.isCollidedHorizontally/* && ent.boundingBox.minY < bestDigCoord.posY*/) {
				ent.jump();
			}
		}
		
		//dbg(Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ));
		
		//safety reset on weird idle ai
		
		
		//w1 = waiting on good angle, skipped for above target digs
		//w2 = 
		if (pfReturnTimeout > 0) {
			pfReturnTimeout--;
		} else {
			if (waitingForPath) {
				waitingForPath = false;
			}
		}
		
		checkCoord();
		
		if (((JobGroupHorde)jm.getPrimaryJob()).attackCoord == null) return;
		
		ChunkCoordinates attackCoord = ((JobGroupHorde)jm.getPrimaryJob()).attackCoord;
		
		if (this.state == EnumJobState.IDLE) {
			
			if (!hasPathToTarget) {
				if (!waitingForPath) {
					if (tryPathToTarget) {
						//if (ent.onGround) {
							waitingForPath = true;
							
							pathReturned = null;
							pfReturnTimeout = 40;
							
							dbg("requesting path test");
							waitingForThreadPath = true;
							PFQueue.getPath(ent, attackCoord.posX, attackCoord.posY, attackCoord.posZ, 512/*entInt.getAIAgent().maxPFRange*/, -1);
						//}
					}
				} else {
					if (!waitingForThreadPath) {
						if (pathReturned != null) {
							float dist = (float) Math.sqrt(attackCoord.getDistanceSquared(pathReturned.getFinalPathPoint().xCoord, pathReturned.getFinalPathPoint().yCoord, pathReturned.getFinalPathPoint().zCoord));
							
							//dbg("dist: " + dist);
							//dbg("nodes: " + pathReturned.getCurrentPathLength());
							
							if (dist < 2F && pathReturned.getCurrentPathLength() > 1) {
								hasPathToTarget = true;
								entInt.getAIAgent().setPathToEntityForce(pathReturned);
							} else {
								hasPathToTarget = false;
							}
							
							tryPathToTarget = false; //now that we know we must dig, logic later on must set this to true to reset the loop
						} else {
							dbg("no path");
							hasPathToTarget = false;
							waitingForPath = false;
						}
					}
					
					
				}
			}
			
			if (!tryPathToTarget) {
				if (hasPathToTarget) {
					super.tick();
					
					if (ent.getNavigator().noPath()) {
						hasPathToTarget = false;
					}
					
					/*if (ent.onGround && Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) < 0.02F && ent.isCollidedHorizontally) {
	    				ent.motionY = 0.42F;
	    			}*/
					
					return;
				} else {
					
					//make entity pathfind to closest spot it could get, then once he idles out at the coord, calculate dig distance till air and get the end coords for bestDigCoord
					dbg("pathing to best spot");
					this.entInt.getAIAgent().walkToMark(ent, pathReturned, 2400);
					this.entInt.getAIAgent().setPathToEntityForce(pathReturned);
					
					
					this.setJobState(EnumJobState.W1);
				}
			}
		} else if (this.state == EnumJobState.W1) {
			if (this.entInt.getAIAgent().notPathing()/*this.entInt.getAIAgent().pathToEntity == null || this.entInt.getAIAgent().pathToEntity.isFinished()*/) {
				
				dbg("pathing finish");
				
				this.setJobState(EnumJobState.W2);
				
				/*dbg("checking dig angle");
				if (isDiggableAngle()) {
					this.setJobState(EnumJobState.W2);
				} else {
					this.entInt.getAIAgent().updateWanderPath();
				}*/
			} else {
				
				double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ);
				
				if (ent.onGround && speed < 0.02F) {
					doMineTick = tryDoorBreak = true;
				}
				
				//weird fix, but needs to happen
				if (ent.onGround && noMoveTicks > 5 && Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ) < 0.02F/* && ent.isCollidedHorizontally*/) {
    				ent.motionY = 0.42F;
    				this.walkingTimeout-=20;
    			}
				
				this.walkingTimeout--;
				if (this.walkingTimeout <= 0) {
					dbg("walking timeout reached, nulling path");
					this.entInt.getAIAgent().pathToEntity = null;
					return;
				}
			}
		} else if (this.state == EnumJobState.W2) {
			//simu dig #1
			
			if (simuDigCooldownCur > 0) {
				simuDigCooldownCur--;
			} else {
				simuDigCooldownCur = simuDigCooldownMax;
			
				ChunkCoordinates coords = null;
				
				ChunkCoordinates aimCoord = ((JobGroupHorde)jm.getPrimaryJob()).attackCoord;
				
				/*if (simuDigCount == 0) {
					aimCoord = ;
				}*/
				
				//force old way
				boolean pathAndDig = false;
				//simuDigCount = 0;
				
				
				dbg("simuDigCount: " + simuDigCount);
				
				if (simuDigCount == 0 && (/*ent.worldObj.canBlockSeeTheSky((int)ent.posX, (int)ent.posY, (int)ent.posZ) || */isDiggableAngle(aimCoord))) {
					
					//strait dig test
					dbg("try strait dig");
					coords = rayTraceBlocksInverted(Vec3.createVectorHelper(ent.posX, ent.posY, ent.posZ), Vec3.createVectorHelper(aimCoord.posX, aimCoord.posY, aimCoord.posZ));
					
					
				} else if (simuDigCount < 10) {
					
					//if (simuDigCount == 0) simuDigCount = 1;
					
					double xDiff = aimCoord.posX - ent.posX;
					double zDiff = aimCoord.posZ - ent.posZ;
					double distHoriz = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
					if (distHoriz < 0) distHoriz = 1;
					
					double distVert = aimCoord.posY - ent.posY;
					
					double factor = distVert / distHoriz;
					
					if (!pathAndDig) {
						//if (isDiggableAngle(aimCoord)) {
							//dbg("good angle, but strait dig failed, try randomized");
						//} else {
							//dig up and away
							dbg("bad angle, try randomized");
							
							dbg("factor: " + factor);
							
							double adjust = (20F * factor) + distVert + ent.worldObj.rand.nextInt(20);
							
							double newX = 0;
							double newY = 0;
							double newZ = 0;
							
							if (xDiff > 0) newX = aimCoord.posX - adjust;
							if (zDiff > 0) newZ = aimCoord.posZ - adjust;
							if (xDiff <= 0) newX = aimCoord.posX + adjust;
							if (zDiff <= 0) newZ = aimCoord.posZ + adjust;
							
							newY = ent.worldObj.getHeightValue((int)newX, (int)newZ) + 1;
							
							dbg("attack coord: " + aimCoord.posX + ", " + aimCoord.posZ);
							dbg("new coord: " + newX + ", " + newZ);
							
							coords = rayTraceBlocksInverted(Vec3.createVectorHelper(ent.posX, ent.posY, ent.posZ), Vec3.createVectorHelper(newX, newY, newZ));
						//}
					} else {
						if ((simuDigCount+1) % 2 == 0) {
							int range = 80;
							
							double newX = aimCoord.posX + ent.worldObj.rand.nextInt(range) - (range/2);
							double newZ = aimCoord.posZ + ent.worldObj.rand.nextInt(range) - (range/2);
							
							double newY = ent.worldObj.getHeightValue((int)newX, (int)newZ) + 1;
							
							dbg("attack coord: " + aimCoord.posX + ", " + aimCoord.posZ);
							dbg("new coord: " + newX + ", " + newZ);
							
							entInt.getAIAgent().walkTo(ent, (int)newX, (int)newY, (int)newZ, 256, 800);
							
							bestDigCoord = new ChunkCoordinates((int)newX, (int)newY, (int)newZ);
							
							simuDigCount++;
							this.setJobState(EnumJobState.W1);
							
							dbg("walking to best random spot");
							
							return;
						} else {
							if (bestDigCoord == null) {
								bestDigCoord = aimCoord;
								dbg("bestDigCoord null, switching to aimCoord");
							}
							coords = rayTraceBlocksInverted(Vec3.createVectorHelper(ent.posX, ent.posY, ent.posZ), Vec3.createVectorHelper(bestDigCoord.posX, bestDigCoord.posY, bestDigCoord.posZ));
						}
					}
					
					
					
				}
				
				
				if (coords != null) {
					dbg("found end of solid, dist: " + ent.getDistance(coords.posX, coords.posY, coords.posZ));
					bestDigCoord = coords;
					this.setJobState(EnumJobState.W3);
				} else {
					//dig failed, try new angle if under max retries
					if (simuDigCount < 10) {
						simuDigCount++;
						//let next tick try again
					} else {
						//this.setJobState(EnumJobState.W3);
						this.setJobState(EnumJobState.IDLE);
						fullReset();
						dbg("dig failed");
						//bestDigCoord = ((JobGroupHorde)jm.getPrimaryJob()).attackCoord;
					}
				}
			}
			
			
		} else if (this.state == EnumJobState.W3) {
			//DIG!
			
			//temp weird thing
			//entInt.getAIAgent().setState(EnumActState.WALKING);
			//this.walkingTimeout = 600;
			//if (ent.worldObj.getWorldTime() % 60 == 0) dbg("cur dist: " + ent.getDistance(attackCoord.posX, attackCoord.posY, attackCoord.posZ));
			
			noDigTicks++;
			
			if (noDigTicks > noDigTicksMax) {
				dbg("NO DIG TICK RESET");
				noDigTicks = 0;
				fullReset();
			}
			
			if (bestDigCoord != null && ent.getDistance(bestDigCoord.posX, bestDigCoord.posY, bestDigCoord.posZ) >= 1F) {
			
				//dbg("cur end dig dist: " + ent.getDistance(bestDigCoord.posX, bestDigCoord.posY, bestDigCoord.posZ));
				
				//dig up check
				double xDiff = bestDigCoord.posX - ent.posX;
				double zDiff = bestDigCoord.posZ - ent.posZ;
				double distHoriz = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
				double distVert = bestDigCoord.posY - ent.posY;
				if (distHoriz == 0) distHoriz = 1;
				double factor = distVert / distHoriz;
				
				int digYDir = 0;
				
				if (factor > 0.15) {
					//dbg("should dig up!");
					digYDir = 1;
				} else if (factor < -0.15) {
					//dbg("should dig down!");
					digYDir = -1;
				}
				//final design
				
				//idle: if YDir = 1: dig above head piece, extra offset gets set to 1 
				
				//w1: use offset, dig aim head piece
				//w2: use offset, dig aim foot piece
				
				//w3: if YDir = -1: dig aim foot-1 piece
				
				int digYoffset = 0;
				if (digYDir == 1) { 
					digYoffset = 1;
				} else if (digYDir == -1) {
					digYoffset = -1;
				}
				
				//safety reset on edgecase: stuck under dig target
				if (factor > yDiffMax) {
					dbg("extreme X/Y diff detected, full reset, factor: " + factor);
					fullReset();
					return;
				}
				
				double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ);
				
				//dbg(speed);
				
				if (speed < 0.02F || ent.isInWater()) {
					if (curBlockDamage == 0F) {
						if (digState == EnumJobState.IDLE) {
							if (digYDir == 1) {
								trySetNextDigBlock(digYoffset, true, 0);
							}
						} else if (digState == EnumJobState.W1) {
							trySetNextDigBlock(1 + digYoffset);
						} else if (digState == EnumJobState.W2) {
							trySetNextDigBlock(digYoffset);
						} else if (digState == EnumJobState.W3) {
							if (digYDir == -1) { 
								trySetNextDigBlock(0, false, 0); //make sure this extra low offset works ok for the raytracer, if not, make the offset be an 'after aim' offset
							}
						}
					}
					
					doMineTick = true;
					
				} else {
					//dbg("fail speed: " + Math.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ));
				}
				
				if (ent.worldObj.canBlockSeeTheSky((int)ent.posX, (int)ent.posY+2, (int)ent.posZ)) {
					seesSkyTicks++;
					if (lastMinedCoord == null && seesSkyTicks > 100) {
						dbg("saw sky first time, reset!");
						fullReset();
						return;
					}
					
				} else {
					seesSkyTicks = 0;
				}
				
				/* else {
					if (ent.onGround && ent.isCollidedHorizontally && ent.boundingBox.minY < bestDigCoord.posY) {
						//ent.jump();
					}
				}*/
				
				/*if (lastMinedCoord != null) {
					entInt.getAIAgent().faceCoord(lastMinedCoord, 180, 180);
				} else {
					
				}*/

				
				//prevent pushing
				ent.motionX = 0F;
				ent.motionZ = 0F;
				
				entInt.getAIAgent().faceCoord(bestDigCoord, 180, 0);
				
				ent.moveFlying(/*(ent.worldObj.rand.nextFloat() * 0.2F) - 0.1F*/0, 0.3F, 0.25F);
				ent.moveEntity(0F, 0F, 0F); //makes standard move method run, that updates booleans like onGround etc
				
				//dbg(aim);
				
				entInt.getAIAgent().entityToAttack = null;
			} else {
				dbg("end of dig, full reset");
				fullReset();
			}
		} else if (this.state == EnumJobState.W4) {
			
		}
		
		
		
		if (doMineTick || lastMinedCoord != null) {
			
			if (tryDoorBreak) {
				if (curBlockDamage == 0F) {
					trySetNextDigBlock(1);
					if (lastMinedCoord != null) {
						int id = ent.worldObj.getBlockId(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
						if (id != 0) {
							Block block = Block.blocksList[id];
							if (block instanceof BlockDoor) {
								
							} else {
								lastMinedCoord = null;
							}
						}
					}
				}
			}
			
			if (lastMinedCoord != null) {
				
				
				
				float strVsBlock = 0F;
				
				int id = ent.worldObj.getBlockId(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
				if (id != 0) {
					Block block = Block.blocksList[id];
					strVsBlock = block.getBlockHardness(ent.worldObj, lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ);
					//dbg(block.getBlockName() + " - " + 4F / strVsBlock / 20F + " - " + curBlockDamage);
				}
				
				//bedrock/unbreakable safety
				if (strVsBlock == -1) {
					lastMinedCoord = null;
					return;
				}
				
				curBlockDamage += 4F / strVsBlock / 20F;
				
				 //= block.getBlockHardness(worldRef, 0, 0, 0) - (((itemStr.getStrVsBlock(block) - 1) / 4F));
				
				if (ent.worldObj.getWorldTime() % 4 == 0 && id != 0) {
					
					ent.worldObj.playSoundAtEntity(ent, "step.stone", 1F, 1F);
					
				}
				
				if (curBlockDamage > 1F) {
					
					//dbg("break block id: " + ent.worldObj.getBlockId(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ));
					
					//break, reset dig damage
					ent.worldObj.destroyBlockInWorldPartially(ent.entityId, lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, -1);
					ent.worldObj.setBlock(lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, 0);
					
					Block block = Block.blocksList[id];
					if (block != null && block.stepSound != null) ent.worldObj.playSoundAtEntity(ent, block.stepSound.getBreakSound(), 1F, 1F);
					
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
					ent.worldObj.destroyBlockInWorldPartially(ent.entityId, lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, (int)(curBlockDamage * 10F));
				}
				ent.swingItem();
			} else {
				//skip a block coord if its air, also if idle or w3 is skipped due to non angled dig
				nextDigState();
				curBlockDamage = 0;
				
			}
			
			//ent.moveEntity(0.001F, 0.1F, 0.001F);
			
			if (ent.worldObj.getWorldTime() % 20 == 0 && bestDigCoord != null && ent.motionY < 0.05F &&
					/*ent.onGround &&*/
					ent.boundingBox.minY < bestDigCoord.posY) {
				//ent.motionY = 0.35F;
				ent.jump();
			}
			
			
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
		
		//if below
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
		            int k = MathHelper.floor_double(ent.posY + (i % 2 == 0 ? (double)ent.getEyeHeight() : 0) + (double)f1);
		            int l = MathHelper.floor_double(ent.posZ + (double)f2);

		            int id = ent.worldObj.getBlockId(j, k, l);
		            
		            if (id != 0 && Block.blocksList[id].blockMaterial.isSolid())
		            {
		            	coords = new ChunkCoordinates(j, k, l);
		            }
		        }
			}
		}
		
		
		
		if (coords != null) {
			int id = ent.worldObj.getBlockId(coords.posX, coords.posY, coords.posZ);
			Block block = Block.blocksList[id];
			if ((block != null && block.blockMaterial == Material.lava)) {
				fullReset();
				return false;
			}
			if (id != 0 && (block != null && block.blockMaterial != Material.water)) {
				if (ent.worldObj.getBlockTileEntity(coords.posX, coords.posY, coords.posZ) == null) {
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
		if (lastMinedCoord != null) ent.worldObj.destroyBlockInWorldPartially(ent.entityId, lastMinedCoord.posX, lastMinedCoord.posY, lastMinedCoord.posZ, -1);
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
        	
        	int id = ent.worldObj.getBlockId((int)curX, (int)curY, (int)curZ);
        	
        	//area protection
        	if (isNoDigCoord((int)curX, (int)curY, (int)curZ)) {
        	    dbg("simudig fail: bukkit protected area");
        	    return null;
        	}
        	
        	if (id != 0) {
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
	        				id = ent.worldObj.getBlockId((int)curX, (int)curY-curIterations2, (int)curZ);
	        				
	        				//return a coord that is on the ground as long as its not too far down
	        				if (id != 0) {
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
	
	public boolean isNoDigBlock(int id) {
		if (isDangerBlock(id)) return true;
		if (simuDigCount < 5) {
			if (Block.blocksList[id].blockMaterial == Material.water) {
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
	
	public boolean isDangerBlock(int id) {
		if (Block.blocksList[id].blockMaterial == Material.lava || Block.blocksList[id].blockMaterial == Material.cactus) {
			return true;
		}
		return false;
	}
	
	public MovingObjectPosition rayTraceBlocksInvertedBleh(Vec3 par1Vec3, Vec3 par2Vec3)
    {
		boolean par3 = false;
		boolean par4 = false;
		
        if (!Double.isNaN(par1Vec3.xCoord) && !Double.isNaN(par1Vec3.yCoord) && !Double.isNaN(par1Vec3.zCoord))
        {
            if (!Double.isNaN(par2Vec3.xCoord) && !Double.isNaN(par2Vec3.yCoord) && !Double.isNaN(par2Vec3.zCoord))
            {
                int var5 = MathHelper.floor_double(par2Vec3.xCoord);
                int var6 = MathHelper.floor_double(par2Vec3.yCoord);
                int var7 = MathHelper.floor_double(par2Vec3.zCoord);
                int var8 = MathHelper.floor_double(par1Vec3.xCoord);
                int var9 = MathHelper.floor_double(par1Vec3.yCoord);
                int var10 = MathHelper.floor_double(par1Vec3.zCoord);
                int var11 = ent.worldObj.getBlockId(var8, var9, var10);
                int var12 = ent.worldObj.getBlockMetadata(var8, var9, var10);
                Block var13 = Block.blocksList[var11];

                if (var13 != null && (!par4 || var13 == null || var13.getCollisionBoundingBoxFromPool(ent.worldObj, var8, var9, var10) != null) && var11 > 0 && var13.canCollideCheck(var12, par3))
                {
                    MovingObjectPosition var14 = var13.collisionRayTrace(ent.worldObj, var8, var9, var10, par1Vec3, par2Vec3);

                    if (var14 != null)
                    {
                        return var14;
                    }
                }

                var11 = 200;

                while (var11-- >= 0)
                {
                    if (Double.isNaN(par1Vec3.xCoord) || Double.isNaN(par1Vec3.yCoord) || Double.isNaN(par1Vec3.zCoord))
                    {
                        return null;
                    }

                    if (var8 == var5 && var9 == var6 && var10 == var7)
                    {
                        return null;
                    }

                    boolean var39 = true;
                    boolean var40 = true;
                    boolean var41 = true;
                    double var15 = 999.0D;
                    double var17 = 999.0D;
                    double var19 = 999.0D;

                    if (var5 > var8)
                    {
                        var15 = (double)var8 + 1.0D;
                    }
                    else if (var5 < var8)
                    {
                        var15 = (double)var8 + 0.0D;
                    }
                    else
                    {
                        var39 = false;
                    }

                    if (var6 > var9)
                    {
                        var17 = (double)var9 + 1.0D;
                    }
                    else if (var6 < var9)
                    {
                        var17 = (double)var9 + 0.0D;
                    }
                    else
                    {
                        var40 = false;
                    }

                    if (var7 > var10)
                    {
                        var19 = (double)var10 + 1.0D;
                    }
                    else if (var7 < var10)
                    {
                        var19 = (double)var10 + 0.0D;
                    }
                    else
                    {
                        var41 = false;
                    }

                    double var21 = 999.0D;
                    double var23 = 999.0D;
                    double var25 = 999.0D;
                    double var27 = par2Vec3.xCoord - par1Vec3.xCoord;
                    double var29 = par2Vec3.yCoord - par1Vec3.yCoord;
                    double var31 = par2Vec3.zCoord - par1Vec3.zCoord;

                    if (var39)
                    {
                        var21 = (var15 - par1Vec3.xCoord) / var27;
                    }

                    if (var40)
                    {
                        var23 = (var17 - par1Vec3.yCoord) / var29;
                    }

                    if (var41)
                    {
                        var25 = (var19 - par1Vec3.zCoord) / var31;
                    }

                    boolean var33 = false;
                    byte var42;

                    if (var21 < var23 && var21 < var25)
                    {
                        if (var5 > var8)
                        {
                            var42 = 4;
                        }
                        else
                        {
                            var42 = 5;
                        }

                        par1Vec3.xCoord = var15;
                        par1Vec3.yCoord += var29 * var21;
                        par1Vec3.zCoord += var31 * var21;
                    }
                    else if (var23 < var25)
                    {
                        if (var6 > var9)
                        {
                            var42 = 0;
                        }
                        else
                        {
                            var42 = 1;
                        }

                        par1Vec3.xCoord += var27 * var23;
                        par1Vec3.yCoord = var17;
                        par1Vec3.zCoord += var31 * var23;
                    }
                    else
                    {
                        if (var7 > var10)
                        {
                            var42 = 2;
                        }
                        else
                        {
                            var42 = 3;
                        }

                        par1Vec3.xCoord += var27 * var25;
                        par1Vec3.yCoord += var29 * var25;
                        par1Vec3.zCoord = var19;
                    }

                    Vec3 var34 = ent.worldObj.getWorldVec3Pool().getVecFromPool(par1Vec3.xCoord, par1Vec3.yCoord, par1Vec3.zCoord);
                    var8 = (int)(var34.xCoord = (double)MathHelper.floor_double(par1Vec3.xCoord));

                    if (var42 == 5)
                    {
                        --var8;
                        ++var34.xCoord;
                    }

                    var9 = (int)(var34.yCoord = (double)MathHelper.floor_double(par1Vec3.yCoord));

                    if (var42 == 1)
                    {
                        --var9;
                        ++var34.yCoord;
                    }

                    var10 = (int)(var34.zCoord = (double)MathHelper.floor_double(par1Vec3.zCoord));

                    if (var42 == 3)
                    {
                        --var10;
                        ++var34.zCoord;
                    }

                    int var35 = ent.worldObj.getBlockId(var8, var9, var10);
                    int var36 = ent.worldObj.getBlockMetadata(var8, var9, var10);
                    Block var37 = Block.blocksList[var35];

                    if ((!par4 || var37 == null || var37.getCollisionBoundingBoxFromPool(ent.worldObj, var8, var9, var10) != null) && var35 > 0 && var37.canCollideCheck(var36, par3))
                    {
                        MovingObjectPosition var38 = var37.collisionRayTrace(ent.worldObj, var8, var9, var10, par1Vec3, par2Vec3);

                        if (var38 != null)
                        {
                            return var38;
                        }
                    }
                }

                return null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
	
	@Override
	public boolean canJoinFormations() {
		return false;
	}
}
