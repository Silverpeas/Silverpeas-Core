package com.stratelia.silverpeas.versioning.jcr.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.stratelia.silverpeas.versioning.jcr.impl");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestJcrDocumentService.class);
    suite.addTestSuite(TestJcrDocumentDao.class);
    //$JUnit-END$
    return suite;
  }

}
