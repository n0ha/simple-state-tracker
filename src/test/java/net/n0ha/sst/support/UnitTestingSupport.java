package net.n0ha.sst.support;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import junit.framework.TestCase;
import net.n0ha.sst.model.Callback;
import net.n0ha.sst.model.FlowEntity;

import org.mockito.MockitoAnnotations;

public class UnitTestingSupport extends TestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// create all mocks
		MockitoAnnotations.initMocks(this);
	}

	protected Callback getCallback() throws Exception {
		Callback mock = mock(Callback.class);
		when(mock.execute(isA(FlowEntity.class), isA(Map.class))).thenReturn(true);
		return mock;
	}
}
