package net.n0ha.sst;

import java.util.Map;

public interface DecisionCallback {
	public State next(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException;
}