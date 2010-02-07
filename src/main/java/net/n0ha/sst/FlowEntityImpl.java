package net.n0ha.sst;

import java.util.Date;

public class FlowEntityImpl implements FlowEntity {

	private Long id;

	private State state;

	private User creatorUser;

	private Date created;

	public FlowEntityImpl() {
		// pass
	}

	public FlowEntityImpl(State state, User creatorUser) {
		this.state = state;
		this.creatorUser = creatorUser;
		this.created = new Date();
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public User getCreatorUser() {
		return this.creatorUser;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCreatorUser(User creatorUser) {
		this.creatorUser = creatorUser;
	}

}
