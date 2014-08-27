package hostileworlds.entity.bt.ai;

import hostileworlds.entity.bt.OrcArcher;
import hostileworlds.world.location.Stronghold;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.PersonalityProfile;
import CoroUtil.world.location.ManagedLocation;

public class PersonalityProfileStrongholdMember extends PersonalityProfile {

	public PersonalityProfileStrongholdMember(AIBTAgent parAgent) {
		super(parAgent);
	}
	
	@Override
	public boolean shouldWander() {
		return false;
	}
	
	@Override
	public boolean shouldChaseTarget() {
		if (agent.ent instanceof OrcArcher) {
			ManagedLocation ml = agent.getManagedLocation();
			if (ml != null) {
				if (ml instanceof Stronghold) {
					if (((Stronghold) ml).isPlayerInside()) {
						return true;
					} else {
						return false;
					}
				}
				
			}
		}
		return true;
	}
	
	@Override
	public void initProfile(int profileType) {
		// TODO Auto-generated method stub
		super.initProfile(profileType);
	}
	
}
