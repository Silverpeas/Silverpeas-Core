/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.test;

import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.publication.social.SocialInformationPublication;
import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.test.WarBuilder4LibCore;

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
    addSilverpeasExceptionBases();
    addAdministrationFeatures();
    addIndexEngineFeatures();
    addWysiwygFeatures();
    addSilverpeasContentFeatures();
    addSilverpeasUrlFeatures();
    addPublicationTemplateFeatures();
    addClasses(FieldTemplate.class, SocialInformationPublication.class,
        AbstractSocialInformation.class, SocialInformation.class, SocialInformationType.class,
        FormException.class);
    addClasses(ContributionModificationContextHandler.class, AttachmentException.class,
        ContributionOperationContextPropertyHandler.class);
    addPackages(true, "org.silverpeas.core.contribution.rating",
        "org.silverpeas.core.node",
        "org.silverpeas.core.contribution.publication.model",
        "org.silverpeas.core.contribution.publication.dao",
        "org.silverpeas.core.contribution.publication.service",
        "org.silverpeas.core.contribution.publication.notification");
    addAsResource("org/silverpeas/core/contribution/publication/service");
    addAsResource("org/silverpeas/publication/publicationSettings.properties");
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
