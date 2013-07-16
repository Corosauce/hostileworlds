package hostileworlds.ai;

import net.minecraft.entity.EntityLiving;

import java.util.ArrayList;

public class GroupAI {

	public ArrayList<EntityLiving> groupMembers;
	public EntityLiving leader;
	
	public int maxSize;
	public int anger;
	
	public GroupAI() {
		groupMembers = new ArrayList();
	}
	
	public void spawnGroup() {
		
	}
	
}
