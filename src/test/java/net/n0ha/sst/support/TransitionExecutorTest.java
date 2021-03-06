package net.n0ha.sst.support;

import static net.n0ha.sst.MockRole.USER;
import static net.n0ha.sst.MockState.NEW;
import static net.n0ha.sst.MockState.START;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.n0ha.sst.MockRole;
import net.n0ha.sst.MockState;
import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.ExecutionFailedException;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.FlowEntityImpl;
import net.n0ha.sst.model.Transition;

import org.mockito.InOrder;

public class TransitionExecutorTest extends UnitTestingSupport {

	private FlowEntity request;

	@SuppressWarnings("unchecked")
	public void testCallbackIsExecutedAndStateChanged() throws Exception {
		Callback mock = getCallback();
		TransitionExecutor te = getSimpleExecutor(mock);

		// run the transition
		assertTrue(te.to(NEW));

		// state changed
		assertEquals(NEW, request.getState());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
	}

	@SuppressWarnings("unchecked")
	public void testParamsAreSanitized() throws Exception {
		Callback mock = getCallback();
		TransitionExecutor te = getSimpleExecutor(mock);

		// run the transition
		assertTrue(te.withParams(null).to(NEW));

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
	}

	@SuppressWarnings("unchecked")
	public void testCallbacksAreExecutedInOrderOfInsertion() throws Exception {
		Callback first = getCallback();
		Callback second = getCallback();
		Callback third = getCallback();

		// insert callbacks in order
		Transition t = new Transition(START, NEW, USER);
		t.addCallback(first);
		t.addCallback(second);
		t.addCallback(third);

		// run the transition
		TransitionExecutor te = createExecutor(t);
		assertTrue(te.to(NEW));

		// verify the order in which mocks were called
		InOrder inOrder = inOrder(first, second, third);
		inOrder.verify(first).execute(isA(FlowEntity.class), isA(Map.class));
		inOrder.verify(second).execute(isA(FlowEntity.class), isA(Map.class));
		inOrder.verify(third).execute(isA(FlowEntity.class), isA(Map.class));

		// just in case, it cannot be the same instance
		assertFalse(first.equals(second));
		assertFalse(first.equals(third));
		assertFalse(second.equals(third));
	}

	@SuppressWarnings("unchecked")
	public void testFalseReturnValueStopsExecution() throws Exception {
		// create callback that returns false, and thus prevents other callbacks
		// from being executed
		Callback mock = mock(Callback.class);
		when(mock.execute(isA(FlowEntity.class), isA(Map.class))).thenReturn(false);

		// and one standard callback..
		Callback shouldNotBeExecuted = getCallback();

		Transition t = new Transition(START, NEW, USER);
		t.addCallback(mock);
		t.addCallback(shouldNotBeExecuted);

		// run the transition
		TransitionExecutor te = createExecutor(t);
		assertFalse(te.to(NEW));

		// state should not change
		assertEquals(START, request.getState());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
		verifyZeroInteractions(shouldNotBeExecuted);
	}

	@SuppressWarnings("unchecked")
	public void testExecutionFailedExceptionStopsExecution() throws Exception {
		// create callback that throws exception, and thus prevents other
		// callbacks from being executed
		Callback mock = mock(Callback.class);
		when(mock.execute(isA(FlowEntity.class), isA(Map.class))).thenThrow(new ExecutionFailedException(""));

		// and one standard callback..
		Callback shouldNotBeExecuted = getCallback();

		Transition t = new Transition(START, NEW, USER);
		t.addCallback(mock);
		t.addCallback(shouldNotBeExecuted);

		// run the transition
		TransitionExecutor te = createExecutor(t);
		assertFalse(te.to(NEW));

		// state should not change
		assertEquals(START, request.getState());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
		verifyZeroInteractions(shouldNotBeExecuted);
	}

	@SuppressWarnings("unchecked")
	public void testRuntimeExceptionIsPropagatedAndStopsExecution() throws Exception {
		// create callback that throws runtime exception, and thus prevents
		// other callbacks from being executed
		Callback mock = mock(Callback.class);
		when(mock.execute(isA(FlowEntity.class), isA(Map.class))).thenThrow(new RuntimeException());

		// and one standard callback..
		Callback shouldNotBeExecuted = getCallback();

		Transition t = new Transition(START, NEW, USER);
		t.addCallback(mock);
		t.addCallback(shouldNotBeExecuted);

		// run the transition
		TransitionExecutor te = createExecutor(t);

		try {
			te.to(NEW);

			// state should not change
			assertEquals(START, request.getState());

			fail("Must not swallow RuntimeException");
		} catch (RuntimeException e) {
			// pass
		}

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
		verifyZeroInteractions(shouldNotBeExecuted);
	}

	public void testNonExistantPathThrowsException() throws Exception {
		Callback mock = mock(Callback.class);
		TransitionExecutor te = getSimpleExecutor(mock);

		try {
			te.to(MockState.APPROVED);
			fail("Non existant path must throw IllegalStateException");
		} catch (IllegalStateException e) {
			// pass
		}
	}

	private TransitionExecutor createExecutor(Transition t) {
		List<Transition> transitionList = new ArrayList<Transition>();
		transitionList.add(t);

		return createExecutor(transitionList);
	}

	private TransitionExecutor createExecutor(List<Transition> transitionList) {
		TransitionExecutor te = new TransitionExecutor();
		request = new FlowEntityImpl();
		request.setState(START);
		te.setFlowEntity(request);

		TransitionMatcher tm = new TransitionMatcher(transitionList, USER);
		te.setMatcher(tm);
		return te;
	}

	private TransitionExecutor getSimpleExecutor(Callback mockCallback) throws Exception {
		// create transition
		Transition t1 = new Transition(MockState.START, MockState.NEW, MockRole.USER);
		t1.addCallback(mockCallback);

		// package it for TransitionExecutor
		List<Transition> transitionList = new ArrayList<Transition>();
		transitionList.add(t1);

		// create TransitionExecutor instance
		return createExecutor(transitionList);
	}
}
