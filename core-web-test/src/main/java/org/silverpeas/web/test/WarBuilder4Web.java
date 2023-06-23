package org.silverpeas.web.test;

import org.silverpeas.core.test.BasicCoreWarBuilder;

public abstract class WarBuilder4Web extends BasicCoreWarBuilder {
  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   *
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> WarBuilder4Web(Class<T> test) {
    super(test);
  }

  /**
   * The test implied code that uses the JCR. So initialize the JCR schema of Silverpeas and adds the required
   * dependencies.
   */
  public WarBuilder4Web initJcr() {
    addMavenDependencies("org.silverpeas.core:silverpeas-core-jcr");
    addWebListener(SilverpeasJcrInitialization.class);
    return this;
  }
}
