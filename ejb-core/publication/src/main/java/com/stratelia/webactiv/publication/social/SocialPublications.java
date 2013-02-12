/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.publication.social;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialPublicationsInterface;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

public class SocialPublications implements SocialPublicationsInterface {

  /**
   * get the my SocialInformationPublication according to number of Item and the first Index
   * @param userId
   * @param limit
   * @param offset
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin,
      Date end) throws SilverpeasException {

    try {
      return getEJB().getAllPublicationsWithStatusbyUserid(userId, begin, end);
    } catch (RemoteException ex) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getSocialInformationsList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          ex);
    }
  }

  /**
   * getEJB
   * @return instance of CalendarBmHome
   */
  private PublicationBm getEJB() {
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      return publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationHome()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          e);
    }
  }

  /**
   * get the SocialInformationPublication of my contatcs according to number of Item and the first
   * Index
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    // getting all components allowed to me and my contacts
    OrganisationController oc = OrganisationControllerFactory.getOrganizationController();
    List<String> options = new ArrayList<String>();
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmelia")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "toolbox")));
    options.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "kmax")));
    try {
      return getEJB().getSocialInformationsListOfMyContacts(myContactsIds, options, begin, end);
    } catch (RemoteException ex) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getSocialInformationsListOfMyContacts()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          ex);
    }
  }
}