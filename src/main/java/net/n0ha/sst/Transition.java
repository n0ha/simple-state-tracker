package net.n0ha.sst;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ClassUtils;

public class Transition {
	private final State fromState;

	private final State toState;

	private final Button button;

	private final Role role;

	private List<Callback> callbacks;

	public Transition(final State fromState, final State toState, final Button button, final Role role) {
		this.callbacks = new ArrayList<Callback>();
		this.fromState = fromState;
		this.toState = toState;
		this.button = button;
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

	public Button getButton() {
		return button;
	}

	public Role getRole() {
		return role;
	}

	public String toString() {
		return "[" + ClassUtils.getShortName(fromState.getClass()) + " --> " + ClassUtils.getShortName(toState.getClass()) + "]  [button: " + ClassUtils.getShortName(button.getClass()) + "]  ["
		        + getCallbacks().size() + " callbacks]";
	}

}