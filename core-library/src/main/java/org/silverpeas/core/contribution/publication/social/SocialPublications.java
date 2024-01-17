/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.publication.social;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialPublicationProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Provider
public class SocialPublications implements SocialPublicationProvider {

  @Inject
  private PublicationService publicationService;

  protected SocialPublications() {
  }


  @Override
  public List<SocialInformation> getSocialInformationList(String userId, Date begin, Date end) {
    return getPublicationService().getAllPublicationsWithStatusbyUserid(userId, begin, end);
  }

  private PublicationService getPublicationService() {
    return publicationService;
  }


  @SuppressWarnings("unchecked")
  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) {
    // getting all components allowed to me
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> instanceIds = new ArrayList<>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "toolbox")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmax")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "quickinfo")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "bookmark")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "webSites")));
    List<SocialInformationPublication> socialPublications =
        publicationService.getSocialInformationsListOfMyContacts(myContactsIds, instanceIds, begin,
            end);

    // Even if the data has been found by filtering on instanceIds that the user can access, it
    // could exists more precise right rules to apply.
    Iterator<SocialInformationPublication> socialPublicationIt = socialPublications.iterator();
    while (socialPublicationIt.hasNext()) {
      SocialInformationPublication socialPublication = socialPublicationIt.next();
      String instanceId = socialPublication.getPublication().getInstanceId();

      // On Kmelia application, if the user has not access right to the publication, then it is
      // removed from the result
      if (!myId.equals(socialPublication.getAuthor()) && instanceId.startsWith("kmelia") &&
          !PublicationAccessControl.get()
              .isUserAuthorized(myId,
                  new PublicationPK(socialPublication.getPublication().getId(), instanceId))) {
        socialPublicationIt.remove();
      }
    }
    return (List) socialPublications;
  }
}