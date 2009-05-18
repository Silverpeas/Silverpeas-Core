package com.silverpeas.jcrutil.converter;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.silverpeas.jcrutil.converter");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestConverterUtil.class);
    //$JUnit-END$
    return suite;
  }

}
