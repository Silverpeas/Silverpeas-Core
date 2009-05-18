package com.stratelia.webactiv.util.attachment.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.stratelia.webactiv.util.attachment.model");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestAttachmentDAO.class);
    suite.addTestSuite(TestAttachmentDetail.class);
    suite.addTest(com.stratelia.webactiv.util.attachment.model.jcr.impl.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
