package net.n0ha.sst.support;

import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.Role;
import net.n0ha.sst.model.User;

public interface UserToRoleMapper {

	/**
	 * Vrati rolu aktualneho uzivatela vo vztahu k entite.
	 * 
	 * @param entity
	 *            Entita ku ktorej sa vztahuje rola
	 * @return Rola
	 */
	public Role getRole(FlowEntity entity);

	public Role getBusinessRoleForUser(FlowEntity entity, User user);

}
