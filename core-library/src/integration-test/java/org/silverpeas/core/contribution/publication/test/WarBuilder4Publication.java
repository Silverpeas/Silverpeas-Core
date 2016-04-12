package org.silverpeas.core.contribution.publication.test;

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.admin.component.ComponentHelper;

/**
 * This builder extends the {@link WarBuilder4LibCore} in order to centralize the definition of
 * common archive part definitions.
 * @author Yohann Chastagnier
 */
public class WarBuilder4Publication extends WarBuilder4LibCore {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <CLASS_TEST> WarBuilder4Publication(final Class<CLASS_TEST> classOfTest) {
    super(classOfTest);
    addMavenDependencies("org.apache.tika:tika-core");
    addMavenDependencies("org.apache.tika:tika-parsers");
    addSilverpeasExceptionBases();
    addAdministrationFeatures();
    addIndexEngineFeatures();
    addWysiwygFeatures();
    addSilverpeasContentFeatures();
    addSilverpeasUrlFeatures();
    addClasses(FieldTemplate.class, SocialInformationPublication.class,
        AbstractSocialInformation.class, SocialInformation.class, SocialInformationType.class,
        ComponentHelper.class, FormException.class);
    addPackages(true, "org.silverpeas.core.contribution.rating",
        "org.silverpeas.core.node",
        "org.silverpeas.core.contribution.publication.model",
        "org.silverpeas.core.contribution.publication.dao",
        "org.silverpeas.core.contribution.publication.service",
        "org.silverpeas.core.contribution.publication.notification");
    addAsResource("org/silverpeas/core/contribution/publication/service");
    addAsResource("org/silverpeas/publication/publicationSettings.properties");
    addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
  }

  /**
   * Gets an instance of a war archive builder for the specified test class with the common
   * dependencies for publications.
   * @return the instance of the war archive builder.
   */
  public static <T> WarBuilder4Publication onWarForTestClass(Class<T> test) {
    return new WarBuilder4Publication(test);
  }
}
