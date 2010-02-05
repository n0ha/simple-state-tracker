package net.n0ha.sst;

public interface FlowEntity {

	public Long getId();

	public State getState();

	public void setState(State state);

	public User getCreatorUser();

	public void setCreatorUser(User user);

}
