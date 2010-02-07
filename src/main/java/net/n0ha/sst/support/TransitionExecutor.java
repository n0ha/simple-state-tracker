package net.n0ha.sst.support;

import java.util.HashMap;
import java.util.Map;

import net.n0ha.sst.Callback;
import net.n0ha.sst.ExecutionFailedException;
import net.n0ha.sst.FlowEntity;
import net.n0ha.sst.State;
import net.n0ha.sst.Transition;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TransitionExecutor implements ApplicationContextAware {

	private TransitionMatcher transitions;

	private FlowEntity flowEntity;

	private Map<String, Object> params;

	private ApplicationContext springContext;

	//private AuditLogDao auditLogDao;

	/*
	public List<Transition> canMoveTo() {
		return transitions.getTransitions();
	}*/

	public TransitionExecutor() {
		// springContext = new
		// ClassPathXmlApplicationContext("classpath:spring/spring-container-config.xml");
		// auditLogDao = (AuditLogDao) springContext.getBean("auditLogDao");
	}

	public TransitionExecutor withParams(Map<String, Object> params) {
		this.params = params;
		return this;
	}

	public boolean to(State state) {
		if (transitions.isEmpty()) {
			throw new IllegalStateException("Cannot execute transition, no possible path found");
		}
		sanitizeParams();
		return execute(transitions.to(state));
	}

	public void setMatcher(TransitionMatcher matcher) {
		this.transitions = matcher;
	}

	private boolean execute(Transition t) {
		for (Callback c : t.getCallbacks()) {
			if (executeCallback(c) == false) {
				return false;
			}
		}
		flowEntity.setState(t.getToState());

		return true;
	}

	private boolean executeCallback(Callback c) {
		// spring autowire & execute callback now
		springContext.getAutowireCapableBeanFactory().autowireBeanProperties(c, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);

		try {
			return c.execute(flowEntity, params);
		} catch (ExecutionFailedException e) {
			System.out.println("Catched ExecutionFailedException, won't execute other callbacks..");
			return false;
		}
	}

	private void sanitizeParams() {
		params = (params == null) ? new HashMap<String, Object>() : params;
	}

	public void setFlowEntity(FlowEntity flowEntity) {
		this.flowEntity = flowEntity;
	}

	public void setApplicationContext(ApplicationContext springContext) {
		this.springContext = springContext;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (Transition t : transitions.getTransitions()) {
			sb.append(t + " ");
		}

		return "te: " + sb.toString();
	}
}