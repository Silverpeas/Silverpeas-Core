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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialPublicationsInterface;

import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.publication.control.PublicationBm;
import com.stratelia.webactiv.publication.model.PublicationRuntimeException;

import javax.inject.Singleton;

@Singleton
public class SocialPublications implements SocialPublicationsInterface {

  protected SocialPublications() {

  }

  /**
   * get my SocialInformationPublication
   * @param userId
   * @param begin
   * @param end
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end)
      throws SilverpeasException {
    return getPublicationService().getAllPublicationsWithStatusbyUserid(userId, begin, end);
  }

  private PublicationBm getPublicationService() {
    try {
      return ServiceProvider.getService(PublicationBm.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException("SocialPublications.getPublicationService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * get the SocialInformationPublication of my contacts
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    // getting all components allowed to me
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> options = new ArrayList<>();
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "toolbox")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmax")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "blog")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "quickinfo")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "bookmark")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "webSites")));
    return getPublicationService()
        .getSocialInformationsListOfMyContacts(myContactsIds, options, begin, end);
  }
}