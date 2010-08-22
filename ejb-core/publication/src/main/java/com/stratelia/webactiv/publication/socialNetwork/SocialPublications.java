/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.publication.socialNetwork;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.rmi.RemoteException;
import java.util.List;


import com.silverpeas.socialNetwork.provider.SocialPublicationsInterface;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;

import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;


public class SocialPublications implements SocialPublicationsInterface {


  /**
   * get the my  SocialInformationPublication  according
   * to number of Item and the first Index
   * @param userId
   * @param limit
   * @param offset
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List getSocialInformationsList(String userId, int limit, int offset) throws
      SilverpeasException {

    List<SocialInformationPublication> publications = null;
    //limit==nbElements
    //offset=firstIndex
    try {
      publications = getEJB().
          getAllPublicationsWithStatusbyUserid(userId, offset, limit);


    } catch (RemoteException ex) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getSocialInformationsList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          ex);
    }
    return publications;
  }

  /**
   * getEJB
   * @return instance of CalendarBmHome
   */
  private PublicationBm getEJB() {
    PublicationBm currentPublicationBm = null;
    try {
      PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      currentPublicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getPublicationHome()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          e);
    }
    return currentPublicationBm;
  }
/**
 * get the   SocialInformationPublication of my contatcs according to number of Item and the first Index
 * @param myId
 * @param myContactsIds
 * @param numberOfElement
 * @param firstIndex
 * @return List
 * @throws SilverpeasException
 */
  @Override
  public List getSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      int numberOfElement,
      int firstIndex) throws SilverpeasException {
    List<String> options = getAvailableComponents(myId, myContactsIds, firstIndex);
    List<SocialInformationPublication> publications = null;
    //limit==nbElements
    //offset=firstIndex
    try {
      publications = getEJB().
          getSocialInformationsListOfMyContacts(myContactsIds, options, numberOfElement, firstIndex);
    } catch (RemoteException ex) {
      throw new PublicationRuntimeException(
          "PublicationBmEJB.getSocialInformationsList()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
          ex);
    }
    return publications;

  }

  /**
   * gets the available component for a given users list
   * @param myId
   * @param myContactsIds
   * @param firstIndex
   * @return List<String>
   */
  public List<String> getAvailableComponents(String myId, List<String> myContactsIds, int firstIndex) {
    try {
      return getEJB().getAvailableComponents(myId, myContactsIds);
    } catch (RemoteException ex) {
      throw new PublicationRuntimeException(
          "SocialPublications.getAvailableComponents",
          SilverpeasRuntimeException.ERROR,
          "publication.GETTING_PUBLICATION_HEADER_FAILED", "", ex);
    }
  }
}
