package net.n0ha.sst.support;

import java.util.HashMap;
import java.util.Map;

import net.n0ha.sst.Transition;
import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.ExecutionFailedException;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.State;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TransitionExecutor implements ApplicationContextAware {

	private TransitionMatcher transitions;

	private FlowEntity flowEntity;

	private Map<String, Object> params;

	private ApplicationContext springContext;

	private Map<State, Callback> onEnterCallbacks;

	public TransitionExecutor() {
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
		if (onEnterCallbacks != null && onEnterCallbacks.containsKey(flowEntity.getState())) {
			executeCallback(onEnterCallbacks.get(flowEntity.getState()));
		}

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

	public void setOnEnterCallbacks(Map<State, Callback> callbacks) {
		this.onEnterCallbacks = callbacks;
	}
}