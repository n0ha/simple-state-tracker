package net.n0ha.sst;

import net.n0ha.sst.model.State;

public enum MockState implements State {

	START(0), NEW(1), PROCESSED(6, true), CANCELLED(3, true), APPROVED(4), UNVERIFIED(10, true);

	long id;

	boolean finalState;

	private MockState(long id) {
		this.id = id;
		this.finalState = false;
	}

	private MockState(long id, boolean finalState) {
		this.id = id;
		this.finalState = finalState;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name();
	}

	public boolean isFinalState() {
		return this.finalState;
	}
}
