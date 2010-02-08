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
import net.n0ha.sst.Transition;
import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.ExecutionFailedException;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.FlowEntityImpl;

import org.mockito.InOrder;

public class TransitionExecutorTest extends UnitTestingSupport {

	private FlowEntity request;

	public void testCallbackIsExecutedAndStateChanged() throws Exception {
		Callback mock = getCallback();
		TransitionExecutor te = getSimpleExecutor(mock);
		te.setApplicationContext(unitTestContext);

		// run the transition
		assertTrue(te.to(NEW));

		// state changed
		assertEquals(NEW.getId(), request.getState().getId());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
	}

	public void testParamsAreSanitized() throws Exception {
		Callback mock = getCallback();
		TransitionExecutor te = getSimpleExecutor(mock);
		te.setApplicationContext(unitTestContext);

		// run the transition
		assertTrue(te.withParams(null).to(NEW));

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
	}

	/**
	 * This test depends on ApplicationContext in BaseTest to contain
	 * ExampleSpringService instance.
	 */
	public void testCallbackIsConfiguredBySpring() throws Exception {
		assertNotNull(unitTestContext.getBean("exampleBean"));

		SpringConfiguredCallback mock = mock(SpringConfiguredCallback.class);
		when(mock.execute(isA(FlowEntity.class), isA(Map.class))).thenReturn(true);

		TransitionExecutor te = getSimpleExecutor(mock);
		te.setApplicationContext(unitTestContext);

		// run the transition
		assertTrue(te.to(NEW));

		verify(mock).setExampleSpringService(isA(ExampleSpringService.class));
	}

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
		te.setApplicationContext(unitTestContext);
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
		te.setApplicationContext(unitTestContext);
		assertFalse(te.to(NEW));

		// state should not change
		assertEquals(START.getId(), request.getState().getId());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
		verifyZeroInteractions(shouldNotBeExecuted);
	}

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
		te.setApplicationContext(unitTestContext);
		assertFalse(te.to(NEW));

		// state should not change
		assertEquals(START.getId(), request.getState().getId());

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
		verifyZeroInteractions(shouldNotBeExecuted);
	}

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
		te.setApplicationContext(unitTestContext);

		try {
			te.to(NEW);

			// state should not change
			assertEquals(START.getId(), request.getState().getId());

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
		te.setApplicationContext(unitTestContext);
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
		te.setApplicationContext(unitTestContext);

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
