package net.n0ha.sst;

import java.util.Map;

import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.State;

public class DefaultCallback implements Callback {

	private State fromState;

	private State toState;

	public DefaultCallback() {
	}

	public DefaultCallback(State fromState, State toState) {
		this.fromState = fromState;
		this.toState = toState;
	}

	public boolean execute(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException {
		if (entity == null) {
			throw new ExecutionFailedException("Missing flow entity to execute the transition");
		}

		System.out.print("transition of entity [" + entity + "] ");
		System.out.print("with params [" + params + "] ");
		System.out.println("from [" + fromState.getName() + "] to [" + toState.getName() + "]");

		return true;
	}

	public String toString() {
		return "default callback";
	}
}
