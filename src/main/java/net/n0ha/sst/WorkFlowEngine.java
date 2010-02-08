package net.n0ha.sst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.ExecutionFailedException;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.Role;
import net.n0ha.sst.model.State;
import net.n0ha.sst.support.TransitionExecutor;
import net.n0ha.sst.support.TransitionMatcher;
import net.n0ha.sst.support.UserToRoleMapper;

public class WorkFlowEngine {

	private List<Transition> transitions;

	private Map<State, DecisionCallback> onSubmitCallbacks;

	private Map<State, Callback> onEnterCallbacks;

	private UserToRoleMapper mapper;

	private State initialState;

	public WorkFlowEngine() {
		transitions = new ArrayList<Transition>();
		onSubmitCallbacks = new HashMap<State, DecisionCallback>();
		onEnterCallbacks = new HashMap<State, Callback>();
	}

	public TransitionExecutor entity(FlowEntity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Flow entity must be set");
		}

		TransitionExecutor executor = new TransitionExecutor();
		executor.setMatcher(getMatcher(entity).from(entity));
		executor.setFlowEntity(entity);
		executor.setOnEnterCallbacks(onEnterCallbacks);

		return executor;
	}

	public TransitionExecutor entity(FlowEntity entity, Role role) {
		if (entity == null || role == null) {
			throw new IllegalArgumentException("Flow entity and role must be set");
		}

		TransitionExecutor executor = new TransitionExecutor();
		executor.setMatcher(getMatcher(entity).from(entity, role));
		executor.setFlowEntity(entity);
		executor.setOnEnterCallbacks(onEnterCallbacks);

		return executor;
	}

	private TransitionMatcher getMatcher(FlowEntity entity) {
		TransitionMatcher tm = new TransitionMatcher(transitions, mapper.getRole(entity));
		return tm;
	}

	public TransitionMatcher whenMovesFrom(FlowEntity entity) {
		return getMatcher(entity).from(entity);
	}

	public void addTransition(Transition transition) {
		if (transitions.size() == 0) {
			initialState = transition.getFromState();
		}
		transitions.add(transition);
	}

	public State getInitialState() {
		return initialState;
	}

	private List<Transition> getTransitions() {
		return new ArrayList<Transition>(transitions);
	}

	public void importSubFlow(WorkFlowEngine subFlow, State connectTo, Role role) {
		// first, connect the subflows' initial state to our state
		State subFlowInitialState = subFlow.getInitialState();
		this.addTransition(new Transition(connectTo, subFlowInitialState, role));

		// now, copy over all subflow transitions
		// TODO think of better way then using private getter
		for (Transition t : subFlow.getTransitions()) {
			addTransition(t);
		}
	}

	public List<State> getFinalStates() {
		List<State> result = new ArrayList<State>();

		// first, include all final states from all transitions
		// all of them are possible final states
		for (Transition t : transitions) {
			result.add(t.getToState());
		}

		// now, exclude all start states from the list
		// they are obviously not final
		for (Transition t : transitions) {
			result.remove(t.getFromState());
		}

		return result;
	}

	public void setMapper(UserToRoleMapper mapper) {
		this.mapper = mapper;
	}

	public void addOnSubmitCallback(State state, DecisionCallback callback) {
		onSubmitCallbacks.put(state, callback);
	}

	public void addOnEnterCallback(State state, Callback callback) {
		onEnterCallbacks.put(state, callback);
	}

	public void submit(FlowEntity request, Map<String, Object> params) {
		if (onSubmitCallbacks.containsKey(request.getState())) {
			try {
				State state = onSubmitCallbacks.get(request.getState()).next(request, params);
				entity(request).to(state);
			} catch (ExecutionFailedException e) {
				// pass
			}
		}
	}

}
