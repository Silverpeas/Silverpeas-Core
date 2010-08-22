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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.socialNetwork.provider;

import com.silverpeas.socialNetwork.SocialNetworkException;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public class ProviderSwitch implements ProviderSwitchInterface {

  private SocialEventsInterface socialEventsInterface;
  private SocialGalleryInterface socialGalleryInterface;
  private SocialPublicationsInterface socialPublicationsInterface;
  private SocialStatusInterface socialStatusInterface;
  private SocialRelationShipsInterface socialRelationShipsInterface;

  /**
   * return the SocialEvent providor  (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialEventsInterface getSocialEventsInterface() {
    return socialEventsInterface;
  }

  /**
   *  set SocialEvent providor  (by using Inversion of Control Containers )
   * @param socialGalleryInterface
   */
  @Override
  public void setSocialEventsInterface(SocialEventsInterface socialEventsInterface) {
    this.socialEventsInterface = socialEventsInterface;
  }

  /**
   * return the SocialGallery providor  (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialGalleryInterface getSocialGalleryInterface() {
    return socialGalleryInterface;
  }

  /**
   *  set SocialGallery providor  (by using Inversion of Control Containers )
   * @param socialGalleryInterface
   */
  @Override
  public void setSocialGalleryInterface(SocialGalleryInterface socialGalleryInterface) {
    this.socialGalleryInterface = socialGalleryInterface;
  }

  /**
   * get my social Informations list  according to the social information type
   * and the UserId
   * @param socialInformationType
   * @param userId
   * @param String classification
   * @param limit nb of element
   * @param offset firstIndex
   * @return List<SocialInformation>
   */
  @Override
  public List getSocialInformationsList(SocialInformationType socialInformationType, String userId,
      String classification, int limit, int offset) throws SocialNetworkException {

    List<SocialInformation> listResult = new ArrayList<SocialInformation>();
    try {
      switch (socialInformationType) {
        case EVENT:
          listResult = getSocialEventsInterface().getSocialInformationsList(userId, classification,
              limit,
              offset);
          break;
        case PHOTO:

          listResult = getSocialGalleryInterface().getSocialInformationsList(userId, limit, offset);
          break;
        case PUBLICATION:
          listResult = getSocialPublicationsInterface().getSocialInformationsList(userId, limit,
              offset);

          break;
        case STATUS:
          listResult = getSocialStatusInterface().getSocialInformationsList(userId, limit, offset);
          break;

        case RELATIONSHIP:
          listResult = getSocialRelationShipsInterface().getSocialInformationsList(userId, limit,
              offset);
          break;

        case LASTEVENT:
          listResult = getSocialEventsInterface().getMyLastSocialInformationsList(userId, limit,
              offset);
          break;


        case ALL:
          for (SocialInformationType type : SocialInformationType.values()) {
            if (socialInformationType.ALL != type && socialInformationType.EVENT != type) {
              List<SocialInformation> listAll = getSocialInformationsList(type, userId,
                  classification,
                  limit, offset);

              if (!(listAll == null)) {
                listResult.addAll(listAll);
              }
            }
          }

          break;
        default:
      }

    } catch (SilverpeasException ex) {
      throw new SocialNetworkException("ProviderSwitch.getSocialInformationsList",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }


    return listResult;

  }

  /**
   * return the SocialPublications providor  (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialPublicationsInterface getSocialPublicationsInterface() {
    return socialPublicationsInterface;
  }

  /**
   * set SocialPublications providor  (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  @Override
  public void setSocialPublicationsInterface(SocialPublicationsInterface socialPublicationsInterface) {
    this.socialPublicationsInterface = socialPublicationsInterface;
  }

  /**
   * return  SocialStatus providor  (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialStatusInterface getSocialStatusInterface() {
    return socialStatusInterface;
  }

  /**
   * set SocialStatus providor  (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  @Override
  public void setSocialStatusInterface(SocialStatusInterface socialStatusInterface) {
    this.socialStatusInterface = socialStatusInterface;
  }

  /**
   * set SocialRelationShips providor  (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  @Override
  public void setSocialRelationShipsInterface(
      SocialRelationShipsInterface socialRelationShipsInterface) {
    this.socialRelationShipsInterface = socialRelationShipsInterface;
  }

  /**
   * return the SocialRelationShips providor  (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialRelationShipsInterface getSocialRelationShipsInterface() {
    return socialRelationShipsInterface;
  }

  /**
   * get the List of social Informations of my contatcs according to the social information type
   * and  the ids of my contacts
   * @param socialInformationType
   * @param myId
   * @param myContactsIds the ids of my contacts
   * @param limit nb of element
   * @param offset firstIndex
   * @return List<SocialInformation>
   */
  @Override
  public List getSocialInformationsListOfMyContacts(SocialInformationType socialInformationType,
      String myId, List<String> myContactsIds, int limit, int offset) throws SilverpeasException {
    List<SocialInformation> list = new ArrayList<SocialInformation>();
    try {
      switch (socialInformationType) {
        case EVENT:
          list = getSocialEventsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, limit,
              offset);
          break;

        case PHOTO:
          list = getSocialGalleryInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, limit, offset);
          break;

        case PUBLICATION:
          list = getSocialPublicationsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, limit, offset);
          break;

        case STATUS:
          list = getSocialStatusInterface().getSocialInformationsListOfMyContacts(myContactsIds,
              limit, offset);
          break;

        case RELATIONSHIP:
          list = getSocialRelationShipsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, limit, offset);
          break;

        case LASTEVENT:
          list = getSocialEventsInterface().getLastSocialInformationsListOfMyContacts(myId,
              myContactsIds, limit, offset);
          break;

        case ALL:
          for (SocialInformationType type : SocialInformationType.values()) {
            if (socialInformationType.ALL != type && socialInformationType.EVENT != type) {
              List<SocialInformation> listAll = getSocialInformationsListOfMyContacts(type, myId,
                  myContactsIds, limit, offset);
              if (!(listAll == null)) {
                list.addAll(listAll);
              }
            }
          }

          break;
        default:
      }

    } catch (SilverpeasException ex) {
      throw new SocialNetworkException("ProviderSwitch.getSocialInformationsListOfMyContacts",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }

    return list;

  }
}
