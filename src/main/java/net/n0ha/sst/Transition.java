package net.n0ha.sst;

import java.util.ArrayList;
import java.util.List;

import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.Role;
import net.n0ha.sst.model.State;

public class Transition {
	private final State fromState;

	private final State toState;

	private final Role role;

	private List<Callback> callbacks;

	public Transition(final State fromState, final State toState, final Role role) {
		this.callbacks = new ArrayList<Callback>();
		this.fromState = fromState;
		this.toState = toState;
		this.role = role;

		// default action
		addCallback(new DefaultCallback(fromState, toState));
	}

	public List<Callback> getCallbacks() {
		return callbacks;
	}

	public void thenExecute(Callback c) {
		addCallback(c);
	}

	public void addCallback(Callback c) {
		callbacks.add(c);
	}

	public State getFromState() {
		return fromState;
	}

	public State getToState() {
		return toState;
	}

	public Role getRole() {
		return role;
	}

	public String toString() {
		return "[" + fromState.getClass().getSimpleName() + " --> " + toState.getClass().getSimpleName() + "]" + getCallbacks().size() + " callbacks]";
	}

}