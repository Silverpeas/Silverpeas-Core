/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.cmis;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * A provider of user contributions in order to be exposed through the Silverpeas implementation of
 * the CMIS objects tree. Each application that has to expose some of its contributions must
 * implements this interface by a CDI managed bean. The bean will be then discovered by the CMIS
 * system in order to get some contributions managed by the application. For doing, the
 * bean has to be annotated with the @{@link javax.inject.Named} qualifier with as value the name
 * of the application following by the suffix {@code ContributionsProvider}. The way the
 * contributions are handled in the application or the concrete type of the contribution is left
 * to the implementation details of the bean implementing this interface.
 * @author mmoquillon
 */
public interface CmisContributionsProvider {

  class Constants {
    private Constants() {
    }

    public static final String NAME_SUFFIX = "ContributionsProviderForCMIS";
  }

  /**
   * Gets a provider of user contributions managed by the specified application.
   * @param appId the unique identifier of an application in Silverpeas.
   * @return a {@link CmisContributionsProvider} instance.
   * @throws IllegalStateException if no such {@link CmisContributionsProvider} instance can be
   * found.
   */
  static CmisContributionsProvider getByAppId(final String appId) {
    return ServiceProvider.getServiceByComponentInstanceAndNameSuffix(appId,
        CmisContributionsProvider.Constants.NAME_SUFFIX);
  }

  /**
   * Gets the contributions that are rooted at the specified application and that are accessible to
   * the given user. For applications using {@link org.silverpeas.core.contribution.model.Folder}s
   * to categorize the content, the root contributions are those that are contained directly in the
   * root folder (in other words, the folder representation of the application).
   * @param appId the unique identifier of a component instance. Should throw
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException} exception
   * if there is no such application.
   * @param user a user in Silverpeas.
   * @return a list with the localized contributions directly accessible at
   * the root level of the application. If there is no contributions, then an empty list is
   * returned.
   */
  List<I18nContribution> getAllowedRootContributions(final ResourceIdentifier appId,
      final User user);

  /**
   * Gets the contributions that are directly in the specified folder and that are accessible to the
   * given user. If the folder or the application doesn't
   * exist, a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException}
   * exception has to be thrown.
   * @param folder a folder in the application.
   * @param user a user in Silverpeas.
   * @return a list with the localized contributions contained in the given folder. If the folder
   * doesn't have any contributions, then an empty list is returned.
   */
  List<I18nContribution> getAllowedContributionsInFolder(final ContributionIdentifier folder,
      final User user);

  /**
   * Gets the specified contribution accessible to the given user. If the contribution doesn't exist
   * then a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException}
   * must be thrown. If the contribution cannot be accessed by the given user then a
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException} must be
   * thrown.
   * @param contributionId the unique identifier of the contribution to get.
   * @return the asked contribution.
   */
  I18nContribution getContribution(final ContributionIdentifier contributionId,
      final User user);

  /**
   * Creates the specified contribution into the specified application.
   * If the application doesn't yet support the creation by CMIS of the specified contribution, then
   * a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException} exception
   * has to be thrown. If the specified application doesn't exist then a
   * {@link org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException} exception
   * has to be thrown.
   * @param contribution the localized contribution to create.
   * @param app the unique identifier of the Silverpeas application in which the contribution has
   * to be created and then managed.
   * @param language the language in which is authored the contribution.
   * @return the instance of the created contribution. It can be different of the one passed as
   * argument.
   */
  I18nContribution createContribution(final I18nContribution contribution,
      final ResourceIdentifier app, final String language);

  /**
   * Creates the specified contribution into the specified folder.
   * If the application doesn't yet support the creation by CMIS of the specified contribution, then
   * a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException} exception
   * has to be thrown. If the specified folder or the application with the given folder doesn't
   * exist, a {@link org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException}
   * has to be thrown.
   * @param contribution the localized contribution to create.
   * @param folder the unique identifier of the folder in which the contribution has to be added
   * once created.
   * @param language the language in which is authored the contribution.
   * @return the instance of the created contribution. It can be different of the one passed as
   * argument.
   */
  I18nContribution createContributionInFolder(final I18nContribution contribution,
      final ContributionIdentifier folder, final String language);
}
