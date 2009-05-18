package com.silverpeas.jcrutil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.jcrutil");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestBasicDaoFactory.class);
    suite.addTest(com.silverpeas.jcrutil.converter.AllTests.suite());
    //$JUnit-END$
    return suite;
  }

}
