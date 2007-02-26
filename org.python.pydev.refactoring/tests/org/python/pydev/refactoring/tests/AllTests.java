package org.python.pydev.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("PEPTIC Unit tests");

		// $JUnit-BEGIN$
		suite.addTest(org.python.pydev.refactoring.tests.rewriter.AllTests
				.suite());
		suite.addTest(org.python.pydev.refactoring.tests.core.AllTests.suite());
		suite.addTest(org.python.pydev.refactoring.tests.adapter.AllTests
				.suite());
		suite.addTest(org.python.pydev.refactoring.tests.visitors.AllTests
				.suite());
		suite.addTest(org.python.pydev.refactoring.tests.codegenerator.AllTests
				.suite());
		suite
				.addTest(org.python.pydev.refactoring.tests.coderefactoring.AllTests
						.suite());
		// suite.addTest(JythonTestSuite.suite());
		// $JUnit-END$
		return suite;
	}

}
