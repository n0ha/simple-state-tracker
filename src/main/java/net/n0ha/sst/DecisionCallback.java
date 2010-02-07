package net.n0ha.sst;

import java.util.Map;

import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.State;

public interface DecisionCallback {
	public State next(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException;
}