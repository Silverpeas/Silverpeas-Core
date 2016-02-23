package org.silverpeas.test;

import com.silverpeas.subscribe.stub.StubbedNodeService;
import com.silverpeas.subscribe.stub.StubbedOrganizationController;
import com.stratelia.webactiv.node.model.NodeOrderComparator;
import com.stratelia.webactiv.node.model.NodeRuntimeException;
import com.stratelia.webactiv.node.model.NodeSelection;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4Node extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <CLASS_TEST> WarBuilder4Node(final Class<CLASS_TEST> classOfTest) {
    super(classOfTest);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies for publications.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4Node onWarForTestClass(Class<T> test) {
    WarBuilder4Node warBuilder = new WarBuilder4Node(test);
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
    warBuilder.createMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:pdc");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:publication");
    warBuilder.createMavenDependencies("org.silverpeas.core.ejb-core:calendar");
    warBuilder.testFocusedOn(war -> war
        .addClasses(NodeOrderComparator.class, NodeRuntimeException.class, NodeSelection.class)
        .addPackages(true, "com.stratelia.webactiv.node.control", "com.silverpeas.node",
            "com.silverpeas.coordinates", "com.stratelia.webactiv.coordinates",
            "com.silverpeas.subscribe")
        .addAsResource("create-database.sql"));
    warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");

    // Stubbed classes which must be set explicitly
    warBuilder.applyManually(
        war -> war.deleteClasses(StubbedNodeService.class, StubbedOrganizationController.class));

    return warBuilder;
  }

  /**
   * Sets into classpath a stubbed implementation of StubbedNodeService service<br/>.
   * The aim is to avoid the setting of all the date into db.
   * @return the builder instance itself.
   */
  public WarBuilder4Node useStubbedNodeService() {
    addClasses(StubbedNodeService.class);
    return this;
  }

  /**
   * Sets into classpath a stubbed implementation of StubbedOrganizationController service<br/>.
   * The aim is to avoid the setting of all the date into db.
   * @return the builder instance itself.
   */
  public WarBuilder4Node useStubbedOrganizationController() {
    addClasses(StubbedOrganizationController.class);
    return this;
  }
}
