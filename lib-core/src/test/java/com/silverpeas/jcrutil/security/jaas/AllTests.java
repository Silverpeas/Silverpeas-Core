package com.silverpeas.jcrutil.security.jaas;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for com.silverpeas.jcrutil.security.jaas");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestAccessAuthentified.class);
    //$JUnit-END$
    return suite;
  }

}
