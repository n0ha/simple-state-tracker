package net.n0ha.sst;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.n0ha.sst.support.TransitionExecutorTest;
import net.n0ha.sst.support.TransitionMatcherTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for net.n0ha.sst");
		// $JUnit-BEGIN$
		suite.addTestSuite(TransitionTest.class);
		suite.addTestSuite(WorkFlowEngineTest.class);
		suite.addTestSuite(TransitionExecutorTest.class);
		suite.addTestSuite(TransitionMatcherTest.class);
		// $JUnit-END$
		return suite;
	}

}
