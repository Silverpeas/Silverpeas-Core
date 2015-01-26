/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.socialnetwork.provider;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.SocialNetworkException;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
public class ProviderSwitch implements ProviderSwitchInterface {

  private SocialEventsInterface socialEventsInterface;
  private SocialGalleryInterface socialGalleryInterface;
  private SocialCommentGalleryInterface socialCommentGalleryInterface;
  private SocialPublicationsInterface socialPublicationsInterface;
  private SocialCommentPublicationsInterface socialCommentPublicationsInterface;
  private SocialCommentQuickInfosInterface socialCommentQuickInfosInterface;
  private SocialStatusInterface socialStatusInterface;
  private SocialRelationShipsInterface socialRelationShipsInterface;

  /**
   * return the SocialEvent providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  @Override
  public SocialEventsInterface getSocialEventsInterface() {
    return socialEventsInterface;
  }

  /**
   * set SocialEvent providor (by using Inversion of Control Containers )
   * @param socialEventsInterface
   */
  @Override
  public void setSocialEventsInterface(SocialEventsInterface socialEventsInterface) {
    this.socialEventsInterface = socialEventsInterface;
  }

  /**
   * return the SocialGallery providor (by using Inversion of Control Containers )
   * @return SocialGalleryInterface
   */
  @Override
  public SocialGalleryInterface getSocialGalleryInterface() {
    return socialGalleryInterface;
  }

  /**
   * set SocialGallery providor (by using Inversion of Control Containers )
   * @param socialGalleryInterface
   */
  @Override
  public void setSocialGalleryInterface(SocialGalleryInterface socialGalleryInterface) {
    this.socialGalleryInterface = socialGalleryInterface;
  }

  /**
   * return the SocialCommentGallery providor (by using Inversion of Control Containers )
   * @return SocialCommentGalleryInterface
   */
  @Override
  public SocialCommentGalleryInterface getSocialCommentGalleryInterface() {
    return socialCommentGalleryInterface;
  }

  /**
   * set SocialCommentGallery providor (by using Inversion of Control Containers )
   * @param socialCommentGalleryInterface
   */
  @Override
  public void setSocialCommentGalleryInterface(SocialCommentGalleryInterface socialCommentGalleryInterface) {
    this.socialCommentGalleryInterface = socialCommentGalleryInterface;
  }

  /**
   * get my social Informations list according to the social information type and the UserId
   * @param socialInformationType
   * @param userId
   * @param classification
   * @param begin
   * @param end
   * @return List<SocialInformation>
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, String userId,
      String classification, Date begin, Date end) throws SocialNetworkException {

    List<SocialInformation> listResult = new ArrayList<SocialInformation>();
    try {
      switch (socialInformationType) {
        case EVENT:
          listResult =
              getSocialEventsInterface().getSocialInformationsList(userId, classification, begin,
              end);
          break;

        case MEDIA:
          listResult = getSocialGalleryInterface().getSocialInformationsList(userId, begin, end);

          //MEDIA filter displays also comments on medias
          List<SocialInformation> listCommentMedia =
              getSocialInformationsList(SocialInformationType.COMMENTMEDIA, userId,
                  classification, begin, end);

          if (listCommentMedia != null) {
            listResult.addAll(listCommentMedia);
          }
          break;

        case COMMENTMEDIA:
          //Comments on gallery components
          listResult =
              getSocialCommentGalleryInterface().getSocialInformationsList(userId, begin, end);
          break;

        case PUBLICATION:
          listResult =
              getSocialPublicationsInterface().getSocialInformationsList(userId, begin, end);

          //PUBLICATION filter displays also comments on publications
          List<SocialInformation> listCommentPublication =
              getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, userId,
                  classification, begin, end);

          if (listCommentPublication != null) {
            listResult.addAll(listCommentPublication);
          }
          break;

        case COMMENTPUBLICATION:
          //Comments on kmelia and blog components
          listResult =
              getSocialCommentPublicationsInterface().getSocialInformationsList(userId, begin, end);

          List<SocialInformation> listCommentNews =
              getSocialInformationsList(SocialInformationType.COMMENTNEWS, userId, classification,
                  begin, end);

          if (listCommentNews != null) {
            listResult.addAll(listCommentNews);
          }
          break;

        case COMMENTNEWS:
          //Comments on quickinfo components
          listResult =
              getSocialCommentQuickInfosInterface().getSocialInformationsList(userId, begin, end);
          break;

        case COMMENT:
          List<SocialInformation> listCommentKmeliaBlogQuickInfos =
              getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, userId,
                  classification, begin, end);

          if (listCommentKmeliaBlogQuickInfos != null) {
            listResult.addAll(listCommentKmeliaBlogQuickInfos);
          }

          List<SocialInformation> listCommentGallery =
              getSocialInformationsList(SocialInformationType.COMMENTMEDIA, userId, classification,
                  begin, end);

          if (listCommentGallery != null) {
            listResult.addAll(listCommentGallery);
          }
          break;

        case STATUS:
          listResult = getSocialStatusInterface().getSocialInformationsList(userId, begin, end);
          break;

        case RELATIONSHIP:
          listResult =
              getSocialRelationShipsInterface().getSocialInformationsList(userId, begin, end);
          break;

        case LASTEVENT:
          listResult =
              getSocialEventsInterface().getMyLastSocialInformationsList(userId, begin, end);
          break;

        case ALL:
          for (SocialInformationType type : SocialInformationType.values()) {
            if (SocialInformationType.ALL != type &&
                SocialInformationType.EVENT != type &&
                SocialInformationType.COMMENT != type &&
                SocialInformationType.COMMENTPUBLICATION != type &&
                SocialInformationType.COMMENTNEWS != type &&
                SocialInformationType.COMMENTMEDIA != type) {
              List<SocialInformation> listAll = getSocialInformationsList(type, userId,
                  classification, begin, end);

              if (listAll != null) {
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
   * return the SocialPublications providor (by using Inversion of Control Containers )
   * @return SocialPublicationsInterface
   */
  @Override
  public SocialPublicationsInterface getSocialPublicationsInterface() {
    return socialPublicationsInterface;
  }

  /**
   * set SocialPublications providor (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  @Override
  public void setSocialPublicationsInterface(SocialPublicationsInterface socialPublicationsInterface) {
    this.socialPublicationsInterface = socialPublicationsInterface;
  }

  /**
   * return the SocialCommentPublications providor (by using Inversion of Control Containers )
   * @return SocialCommentPublicationsInterface
   */
  @Override
  public SocialCommentPublicationsInterface getSocialCommentPublicationsInterface() {
    return socialCommentPublicationsInterface;
  }

  /**
   * set SocialCommentPublications providor (by using Inversion of Control Containers )
   * @param socialCommentPublicationsInterface
   */
  @Override
  public void setSocialCommentPublicationsInterface(SocialCommentPublicationsInterface socialCommentPublicationsInterface) {
    this.socialCommentPublicationsInterface = socialCommentPublicationsInterface;
  }

  /**
   * return the SocialCommentQuickInfos providor (by using Inversion of Control Containers )
   * @return SocialCommentQuickInfosInterface
   */
  @Override
  public SocialCommentQuickInfosInterface getSocialCommentQuickInfosInterface() {
    return socialCommentQuickInfosInterface;
  }

  /**
   * set SocialCommentQuickInfos providor (by using Inversion of Control Containers )
   * @param socialCommentQuickInfosInterface
   */
  @Override
  public void setSocialCommentQuickInfosInterface(SocialCommentQuickInfosInterface socialCommentQuickInfosInterface) {
    this.socialCommentQuickInfosInterface = socialCommentQuickInfosInterface;
  }

  /**
   * return SocialStatus providor (by using Inversion of Control Containers )
   * @return SocialStatusInterface
   */
  @Override
  public SocialStatusInterface getSocialStatusInterface() {
    return socialStatusInterface;
  }

  /**
   * set SocialStatus providor (by using Inversion of Control Containers )
   * @param socialStatusInterface
   */
  @Override
  public void setSocialStatusInterface(SocialStatusInterface socialStatusInterface) {
    this.socialStatusInterface = socialStatusInterface;
  }

  /**
   * set SocialRelationShips providor (by using Inversion of Control Containers )
   * @param socialRelationShipsInterface
   */
  @Override
  public void setSocialRelationShipsInterface(
      SocialRelationShipsInterface socialRelationShipsInterface) {
    this.socialRelationShipsInterface = socialRelationShipsInterface;
  }

  /**
   * return the SocialRelationShips providor (by using Inversion of Control Containers )
   * @return SocialRelationShipsInterface
   */
  @Override
  public SocialRelationShipsInterface getSocialRelationShipsInterface() {
    return socialRelationShipsInterface;
  }

  /**
   * get the List of social Informations of my contatcs according to the social information type and
   * my UserId , ids of my contacts ,limit and index
   * @param socialInformationType
   * @param myId
   * @param myContactsIds the ids of my contacts
   * @param begin
   * @param end
   * @return List<SocialInformation>
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType,
      String myId, List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    List<SocialInformation> list = new ArrayList<SocialInformation>();
    try {
      switch (socialInformationType) {
        case EVENT:
          list = getSocialEventsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, begin, end);
          break;

        case MEDIA:
          list = getSocialGalleryInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, begin, end);

          //MEDIA filter displays also comments on medias
          List<SocialInformation> listCommentMedia =
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA, myId,
                  myContactsIds, begin, end);

          if (listCommentMedia != null) {
            list.addAll(listCommentMedia);
          }
          break;

        case COMMENTMEDIA:
          //Comments on gallery components
          list =
              getSocialCommentGalleryInterface().getSocialInformationsListOfMyContacts(myId, myContactsIds, begin,
                  end);
          break;

        case PUBLICATION:
          list = getSocialPublicationsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, begin, end);

          //PUBLICATION filter displays publications and comments on publications
          List<SocialInformation> listCommentPublication =
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION, myId,
                  myContactsIds, begin, end);

          if (listCommentPublication != null) {
            list.addAll(listCommentPublication);
          }
          break;

        case COMMENTPUBLICATION:
          //Comments on kmelia and blog components
          list = getSocialCommentPublicationsInterface()
              .getSocialInformationsListOfMyContacts(myId, myContactsIds, begin, end);

          List<SocialInformation> listCommentNews =
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTNEWS, myId,
                  myContactsIds, begin, end);

          if (listCommentNews != null) {
            list.addAll(listCommentNews);
          }
          break;

        case COMMENTNEWS:
          //Comments on quickinfo components
          list =
              getSocialCommentQuickInfosInterface().getSocialInformationsListOfMyContacts(myId,
                  myContactsIds, begin, end);
          break;

        case COMMENT:
          List<SocialInformation> listCommentKmeliaBlogQuickInfos =
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION, myId,
                  myContactsIds, begin, end);

          if (listCommentKmeliaBlogQuickInfos != null) {
            list.addAll(listCommentKmeliaBlogQuickInfos);
          }

          List<SocialInformation> listCommentGallery =
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA, myId,
                  myContactsIds, begin, end);

          if (listCommentGallery != null) {
            list.addAll(listCommentGallery);
          }
          break;

        case STATUS:
          list =
              getSocialStatusInterface().getSocialInformationsListOfMyContacts(myContactsIds,
              begin, end);
          break;

        case RELATIONSHIP:
          list = getSocialRelationShipsInterface().getSocialInformationsListOfMyContacts(myId,
              myContactsIds, begin, end);
          break;

        case LASTEVENT:
          list = getSocialEventsInterface().getLastSocialInformationsListOfMyContacts(myId,
              myContactsIds, begin, end);
          break;

        case ALL:
          for (SocialInformationType type : SocialInformationType.values()) {
            if (SocialInformationType.ALL != type &&
                SocialInformationType.EVENT != type &&
                SocialInformationType.COMMENT != type &&
                SocialInformationType.COMMENTPUBLICATION != type &&
                SocialInformationType.COMMENTNEWS != type &&
                SocialInformationType.COMMENTMEDIA != type) {
              List<SocialInformation> listAll = getSocialInformationsListOfMyContacts(type, myId,
                  myContactsIds, begin, end);

              if (listAll != null) {
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
