package hostileworlds.rts.ai;

import hostileworlds.rts.TeamObject;
import hostileworlds.rts.ai.behaviors.ActionBuildBuildingTemp;
import hostileworlds.rts.ai.behaviors.ActionOptimizeOrBuildWorkers;
import CoroAI.bt.Behavior;
import CoroAI.bt.actions.Delay;
import CoroAI.bt.selector.Selector;
import CoroAI.bt.selector.SelectorConcurrent;
import CoroAI.bt.selector.SelectorThreshold;

/* This class manages economy and base building, or at least started out here */
public class BehaviorTreeBuild {

	TeamObject team;
	Selector trunk;
	
	public BehaviorTreeBuild(TeamObject parTeam) {
		team = parTeam;
		
		trunk = new SelectorConcurrent(null);//SelectorThreshold(null, parTeam.resources.resWood, 300); //design flaw, once enough resources, eco ai dies - or, this design good, putting order giving in ActionOptimize bad?
		
		Selector branch_l1_0 = new SelectorThreshold(trunk, parTeam.resources.resWoodRate, 50);
		Behavior branch_l1_1 = new ActionBuildBuildingTemp(trunk, team);//new Delay(trunk, 3, 1); //change this to spend resources or increase army size value for testing once resource threshold val is economy size adapty
		
		trunk.add(branch_l1_0);
		trunk.add(branch_l1_1);
		
		Behavior branch_l2_0 = new ActionOptimizeOrBuildWorkers(branch_l1_0, team); //make this build behavior, 
		Behavior branch_l2_1 = new Delay(branch_l1_0, 3, 1); //to fill a dead end? ehhhh
		
		branch_l1_0.add(branch_l2_0);
		branch_l1_0.add(branch_l2_1);
				
		//helpfull debug
		trunk.dbgName = "res_Wood";
		branch_l1_0.dbgName = "res_Rate";
		branch_l2_0.dbgName = "buildworker";
	}
	
	public void tick() {
		//System.out.println("-------------");
		trunk.tick();
	}
	
}
