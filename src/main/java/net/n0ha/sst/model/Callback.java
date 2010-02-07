package net.n0ha.sst.model;

import java.util.Map;

import net.n0ha.sst.ExecutionFailedException;

public interface Callback {
	public boolean execute(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException;
}