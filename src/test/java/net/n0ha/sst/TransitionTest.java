package net.n0ha.sst;

import static net.n0ha.sst.MockRole.USER;
import static net.n0ha.sst.MockState.NEW;
import static net.n0ha.sst.MockState.START;

import java.util.Map;

import net.n0ha.sst.support.UnitTestingSupport;

public class TransitionTest extends UnitTestingSupport {

	public void testCorrectStatesAreReported() throws Exception {
		Transition t = new Transition(START, NEW, USER);
		assertEquals(START, t.getFromState());
		assertEquals(NEW, t.getToState());
		assertEquals(USER, t.getRole());
	}

	public void testCallbackIsAdded() throws Exception {
		Transition t = new Transition(START, NEW, USER);
		Callback c = new Callback() {
			public boolean execute(FlowEntity entity, Map<String, Object> params) throws ExecutionFailedException {
				return true;
			}
		};

		t.addCallback(c);

		// there has to be 2 callbacks, one default, and one added
		assertEquals(2, t.getCallbacks().size());

		// the added callback has to be the same instance
		assertTrue(t.getCallbacks().get(1) == c);
	}
}
