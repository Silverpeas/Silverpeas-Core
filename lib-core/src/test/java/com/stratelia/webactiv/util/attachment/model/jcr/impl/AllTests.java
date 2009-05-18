package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.stratelia.webactiv.util.attachment.model.jcr.impl");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestJcrAttachmentDao.class);
    suite.addTestSuite(TestJcrAttachmentService.class);
    //$JUnit-END$
    return suite;
  }

}
