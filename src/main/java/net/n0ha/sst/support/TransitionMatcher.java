package net.n0ha.sst.support;

import java.util.ArrayList;
import java.util.List;

import net.n0ha.sst.Button;
import net.n0ha.sst.FlowEntity;
import net.n0ha.sst.Role;
import net.n0ha.sst.State;
import net.n0ha.sst.Transition;

public class TransitionMatcher {

	private State fromState;

	private List<Transition> transitions;

	private Role role;

	public TransitionMatcher(List<Transition> transitions, Role role) {
		if (transitions == null) {
			throw new IllegalArgumentException("List of transitions must be set");
		}

		if (role == null) {
			throw new IllegalArgumentException("User role must be set");
		}

		this.transitions = new ArrayList<Transition>(transitions);
		this.role = role;
	}

	public TransitionMatcher from(FlowEntity flowEntity) {
		return from(flowEntity, this.role);
	}

	public TransitionMatcher from(FlowEntity flowEntity, Role role) {
		this.fromState = flowEntity.getState();
		List<Transition> possibleTransitions = new ArrayList<Transition>();
		for (Transition t : transitions) {
			if (t.getFromState().equals(fromState)) {
				if (t.getRole().equals(role)) {
					possibleTransitions.add(t);
				}
			}
		}

		this.transitions = possibleTransitions;
		return this;
	}

	public Transition to(Button button) {
		for (Transition t : transitions) {
			if (t.getButton().equals(button)) {
				return t;
			}
		}

		throw new IllegalStateException("No transition exists from state: " + fromState + " for button : " + button);
	}

	public List<Transition> getTransitions() {
		return new ArrayList<Transition>(transitions);
	}

	public boolean isEmpty() {
		return (transitions.size() == 0);
	}

	public String toString() {
		return transitions.toString();
	}
}
