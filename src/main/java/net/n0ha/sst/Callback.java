package net.n0ha.sst;

import java.util.Map;

public interface Callback {
	public boolean execute(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException;
}