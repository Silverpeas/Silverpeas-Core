package org.silverpeas.test;

import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.control.PublicationBmEJB;
import com.stratelia.webactiv.publication.control.PublicationDAO;
import com.stratelia.webactiv.publication.control.PublicationFatherDAO;
import com.stratelia.webactiv.publication.control.PublicationI18NDAO;
import com.stratelia.webactiv.publication.control.QueryStringFactory;
import com.stratelia.webactiv.publication.control.ValidationStepsDAO;
import com.stratelia.webactiv.publication.info.SeeAlsoDAO;
import com.stratelia.webactiv.publication.model.PublicationSelection;

/**
 * This builder extends the {@link WarBuilder} in order to centralize the definition of common
 * archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4Publication extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <CLASS_TEST> WarBuilder4Publication(final Class<CLASS_TEST> classOfTest) {
    super(classOfTest);
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies for publications.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4Publication onWarForTestClass(Class<T> test) {
    WarBuilder4Publication warBuilder = new WarBuilder4Publication(test);
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
    warBuilder.createMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-node");
    warBuilder.createMavenDependenciesWithPersistence("org.silverpeas.core.services:silverpeas-core-pdc");
    warBuilder.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
    warBuilder.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-clipboard");
    warBuilder.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-formtemplate");
    warBuilder.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-calendar");
    warBuilder.testFocusedOn(war -> war
        .addClasses(PublicationBm.class, PublicationBmEJB.class, PublicationDAO.class,
            PublicationFatherDAO.class, ValidationStepsDAO.class, PublicationI18NDAO.class,
            QueryStringFactory.class)
        .addClasses(SeeAlsoDAO.class)
        .addClasses(PublicationSelection.class)
        .addPackages(true, "org.silverpeas.publication.dateReminder",
            "com.silverpeas.publication.importExport"));
    warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    warBuilder.addAsResource("org/silverpeas/publication/publicationSettings.properties");
    return warBuilder;
  }
}
