package net.n0ha.sst.support;

import java.util.Map;

import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.FlowEntity;

public class SpringConfiguredCallback implements Callback {

	@SuppressWarnings("unchecked")
	public boolean execute(FlowEntity entity, Map params) {
		return true;
	}

	public void setExampleSpringService(ExampleSpringService service) {
		// pass
	}
}
