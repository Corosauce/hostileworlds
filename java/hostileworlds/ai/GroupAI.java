package hostileworlds.ai;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;

public class GroupAI {

	public ArrayList<EntityLivingBase> groupMembers;
	public EntityLivingBase leader;
	
	public int maxSize;
	public int anger;
	
	public GroupAI() {
		groupMembers = new ArrayList();
	}
	
	public void spawnGroup() {
		
	}
	
}
