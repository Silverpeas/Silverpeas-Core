/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.publication.social;

import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialPublicationsInterface;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SocialPublications implements SocialPublicationsInterface {

  /**
   * get my SocialInformationPublication
   *
   * @param userId
   * @param begin
   * @param end
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin,
      Date end) throws SilverpeasException {
    return getEJB().getAllPublicationsWithStatusbyUserid(userId, begin, end);
  }

  private PublicationBm getEJB() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBm.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException("SocialPublications.getEJB()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * get the SocialInformationPublication of my contacts
   *
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List
   * @throws SilverpeasException
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    // getting all components allowed to me
    OrganisationController oc = OrganisationControllerFactory.getOrganisationController();
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "toolbox")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmax")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "quickinfo")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "bookmark")));
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "webSites")));
    List<SocialInformationPublication> socialPublications =
        getEJB().getSocialInformationsListOfMyContacts(myContactsIds, instanceIds, begin, end);

    // Even if the data has been found by filtering on instanceIds that the user can access, it
    // could exists more precise right rules to apply.
    Iterator<SocialInformationPublication> socialPublicationIt = socialPublications.iterator();
    while (socialPublicationIt.hasNext()) {
      SocialInformationPublication socialPublication = socialPublicationIt.next();
      String instanceId = socialPublication.getPublication().getComponentInstanceId();

      if (!myId.equals(socialPublication.getAuthor()) && instanceId.startsWith("kmelia")) {

        // On Kmelia application, if the user has not right access to the publication, then it is
        // removed from the result

        AccessController<PublicationPK> publicationAccessController =
            AccessControllerProvider.getAccessController("publicationAccessController");
        if (!publicationAccessController.isUserAuthorized(myId,
            new PublicationPK(socialPublication.getPublication().getId(), instanceId))) {
          socialPublicationIt.remove();
        }
      }
    }
    return (List) socialPublications;
  }
}