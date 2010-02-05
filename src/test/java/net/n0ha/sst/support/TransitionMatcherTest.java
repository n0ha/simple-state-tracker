package net.n0ha.sst.support;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.n0ha.sst.FlowEntity;
import net.n0ha.sst.Transition;

import org.mockito.Mock;

import static net.n0ha.sst.MockButton.*;
import static net.n0ha.sst.MockState.*;
import static net.n0ha.sst.MockRole.*;

public class TransitionMatcherTest extends UnitTestingSupport {

	private List<Transition> transitions;

	private TransitionMatcher matcher;

	@Mock
	private FlowEntity entity;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		transitions = new ArrayList<Transition>();
		transitions.add(new Transition(START, NEW, SAVE, USER));
		transitions.add(new Transition(START, APPROVED, APPROVE, APPROVER));
		transitions.add(new Transition(NEW, PROCESSED, APPROVE, USER));
		transitions.add(new Transition(START, PROCESSED, APPROVE, USER));
		transitions.add(new Transition(START, UNVERIFIED, UNVERIFY, SYSTEM));

		// tests for state: START
		when(entity.getState()).thenReturn(START);
		matcher = new TransitionMatcher(transitions, USER);
	}

	public void testZeroTransitionsIsEmpty() {
		TransitionMatcher tm = new TransitionMatcher(new ArrayList<Transition>(), USER);
		assertTrue(tm.isEmpty());
	}

	public void testReferencesToTransitionsAreKept() {
		assertTrue(matcher.getTransitions().get(0) == transitions.get(0));
		assertTrue(matcher.from(entity).getTransitions().get(0) == transitions.get(0));
	}

	public void testFilteringFromStart() {
		// for USER for Start, there have to be New and Processed
		List<Transition> filteredUser = matcher.from(entity).getTransitions();

		assertTrue(filteredUser.size() == 2);
		assertTrue(((Transition) filteredUser.get(0)).getToState().equals(NEW));
		assertTrue(((Transition) filteredUser.get(1)).getToState().equals(PROCESSED));

		TransitionMatcher matcherApprover = new TransitionMatcher(transitions, APPROVER);

		// for APPROVER for Start, there have to be Approved
		List<Transition> filteredApprover = matcherApprover.from(entity).getTransitions();

		assertTrue(filteredApprover.size() == 1);
		assertTrue(((Transition) filteredApprover.get(0)).getToState().equals(APPROVED));

		TransitionMatcher matcher = new TransitionMatcher(transitions, USER);
		// for SYSTEM
		List<Transition> filteredSystem = matcher.from(entity, SYSTEM).getTransitions();

		assertEquals(1, filteredSystem.size());
		assertEquals(UNVERIFIED, ((Transition) filteredSystem.get(0)).getToState());
	}

	public void testButtonsFromStart() {
		// for Start, there have to be Save and Approve
		List<Transition> filtered = matcher.from(entity).getTransitions();

		assertTrue(filtered.size() == 2);
		assertTrue(((Transition) filtered.get(0)).getButton().equals(SAVE));
		assertTrue(((Transition) filtered.get(1)).getButton().equals(APPROVE));

		TransitionMatcher matcherApprover = new TransitionMatcher(transitions, APPROVER);

		// for APPROVER for Start, there have to be Approved
		List<Transition> filteredApprover = matcherApprover.from(entity).getTransitions();

		assertTrue(filteredApprover.size() == 1);
		assertTrue(((Transition) filteredApprover.get(0)).getButton().equals(APPROVE));
	}

	public void testFilteringFromNew() {
		// for New, only Processed
		when(entity.getState()).thenReturn(NEW);
		List<Transition> filtered = matcher.from(entity).getTransitions();

		assertTrue(filtered.size() == 1);
		assertTrue(((Transition) filtered.get(0)).getToState().equals(PROCESSED));
	}

	public void testButtonsFromNew() {
		// for New, there have to be Approve
		when(entity.getState()).thenReturn(NEW);
		List<Transition> filtered = matcher.from(entity).getTransitions();

		assertTrue(filtered.size() == 1);
		assertTrue(((Transition) filtered.get(0)).getButton().equals(APPROVE));
	}

	public void testNullTransitionsThrowsException() throws Exception {
		try {
			@SuppressWarnings("unused")
			TransitionMatcher tm = new TransitionMatcher(null, null);
			fail("Must throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

	public void testNullRoleMapperThrowsException() throws Exception {
		try {
			@SuppressWarnings("unused")
			TransitionMatcher tm = new TransitionMatcher(transitions, null);
			fail("Must throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// pass
		}
	}

}
