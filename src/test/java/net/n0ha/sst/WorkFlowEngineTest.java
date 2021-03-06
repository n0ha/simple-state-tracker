package net.n0ha.sst;

import static net.n0ha.sst.MockRole.APPROVER;
import static net.n0ha.sst.MockRole.USER;
import static net.n0ha.sst.MockState.CANCELLED;
import static net.n0ha.sst.MockState.NEW;
import static net.n0ha.sst.MockState.PROCESSED;
import static net.n0ha.sst.MockState.START;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.FlowEntity;
import net.n0ha.sst.model.Transition;
import net.n0ha.sst.support.UnitTestingSupport;
import net.n0ha.sst.support.UserToRoleMapper;

import org.mockito.Mock;

public class WorkFlowEngineTest extends UnitTestingSupport {

	@Mock
	private FlowEntity request;

	@Mock
	private UserToRoleMapper roleMapper;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// mock flow entity with Start state
		when(request.getState()).thenReturn(START);
	}

	public void testNonExistantPathThrowsException() throws Exception {
		// configure the engine
		WorkFlowEngine wf = getInitializedEmptyFlow();
		Transition t1 = new Transition(START, NEW, USER);
		Transition t2 = new Transition(START, CANCELLED, APPROVER);
		wf.addTransition(t1);
		wf.addTransition(t2);
		wf.addTransition(new Transition(NEW, PROCESSED, USER));
		try {
			wf.entity(request).to(MockState.APPROVED);
			fail("Non existant path must throw IllegalStateException");
		} catch (IllegalStateException e) {
			// pass
		}
	}

	public void testNonExistantPathForTemporaryRoleThrowsException() throws Exception {
		// configure the engine
		WorkFlowEngine wf = getInitializedEmptyFlow();
		Transition t1 = new Transition(START, NEW, USER);
		Transition t2 = new Transition(START, CANCELLED, APPROVER);
		wf.addTransition(t1);
		wf.addTransition(t2);
		wf.addTransition(new Transition(NEW, PROCESSED, USER));
		try {
			wf.entity(request, APPROVER).to(NEW);
			fail("Non existant path must throw IllegalStateException");
		} catch (IllegalStateException e) {
			// pass
		}
	}

	public void testNullRoleThrowsException() throws Exception {
		// configure the engine
		WorkFlowEngine wf = getInitializedEmptyFlow();

		Transition t1 = new Transition(START, NEW, USER);
		Transition t2 = new Transition(START, CANCELLED, APPROVER);
		wf.addTransition(t1);
		wf.addTransition(t2);
		wf.addTransition(new Transition(NEW, PROCESSED, USER));
		try {
			wf.entity(request, null).to(NEW);
			fail("Role must be set");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	@SuppressWarnings("unchecked")
	public void testCallbackIsExecuted() throws Exception {
		Callback mock = getCallback();

		// configure flow
		WorkFlowEngine flow = getInitializedEmptyFlow();
		flow.addTransition(new Transition(START, NEW, USER));
		flow.whenMovesFrom(request).to(NEW).thenExecute(mock);

		// execute the transition
		assertTrue(flow.entity(request).to(NEW));

		verify(mock).execute(isA(FlowEntity.class), isA(Map.class));
	}

	public void testNullEntityThrowsException() throws Exception {
		try {
			getInitializedEmptyFlow().entity(null).to(NEW);
			fail("Must throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	public void testSubFlowIsImportedCorrectly() throws Exception {
		WorkFlowEngine subflow = getInitializedEmptyFlow();
		subflow.addTransition(new Transition(MockState.APPROVED, MockState.CANCELLED, MockRole.USER));

		WorkFlowEngine flow = getInitializedEmptyFlow();
		flow.addTransition(new Transition(MockState.START, MockState.NEW, MockRole.USER));
		flow.importSubFlow(subflow, MockState.NEW, MockRole.USER);

		// NEW --> APPROVED (crossing the border between flow and subflow)
		when(request.getState()).thenReturn(MockState.NEW);
		flow.entity(request).to(MockState.APPROVED);
		verify(request).setState(MockState.APPROVED);

		// APPROVED --> CANCELLED (in the subflow)
		when(request.getState()).thenReturn(MockState.APPROVED);
		flow.entity(request).to(MockState.CANCELLED);
		verify(request).setState(MockState.CANCELLED);
	}

	public void testCalculatesFinalStatesCorrectly() throws Exception {
		WorkFlowEngine flow = getInitializedEmptyFlow();
		flow.addTransition(new Transition(MockState.START, MockState.NEW, MockRole.USER));

		// there is one final state: NEW
		assertEquals(1, flow.getFinalStates().size());
		assertEquals(MockState.NEW, flow.getFinalStates().get(0));

		// add one more transition
		// there is going to be one more final state: APPROVED
		flow.addTransition(new Transition(MockState.START, MockState.APPROVED, MockRole.USER));
		assertEquals(2, flow.getFinalStates().size());
		assertEquals(MockState.NEW, flow.getFinalStates().get(0));
		assertEquals(MockState.APPROVED, flow.getFinalStates().get(1));
	}

	public void testRecursionFromCallback() throws Exception {
		final WorkFlowEngine flow = getInitializedEmptyFlow();
		flow.addTransition(new Transition(MockState.START, MockState.APPROVED, MockRole.SYSTEM));
		Transition t = new Transition(MockState.START, MockState.NEW, MockRole.USER);
		t.addCallback(new Callback() {
			public boolean execute(FlowEntity entity, Map<String, Object> params) {
				flow.entity(entity, MockRole.SYSTEM).to(MockState.APPROVED);
				return false;
			}
		});
		flow.addTransition(t);

		when(request.getState()).thenReturn(MockState.START);
		flow.entity(request).to(MockState.NEW);

		verify(request).setState(MockState.APPROVED);
	}

	protected WorkFlowEngine getInitializedEmptyFlow() {
		when(roleMapper.getRole(request)).thenReturn(USER);
		WorkFlowEngine wf = new WorkFlowEngine();
		wf.setMapper(roleMapper);

		return wf;
	}

	protected WorkFlowEngine getBasicFlowForApprover() {
		when(roleMapper.getRole(request)).thenReturn(APPROVER);
		WorkFlowEngine wf = new WorkFlowEngine();
		wf.setMapper(roleMapper);

		return wf;
	}

}
