package com.stratelia.silverpeas.versioning;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.stratelia.silverpeas.versioning");
    //$JUnit-BEGIN$
    suite.addTestSuite(com.stratelia.silverpeas.versioning.model.TestDocumentVersion.class);
    suite.addTest(com.stratelia.silverpeas.versioning.jcr.impl.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
