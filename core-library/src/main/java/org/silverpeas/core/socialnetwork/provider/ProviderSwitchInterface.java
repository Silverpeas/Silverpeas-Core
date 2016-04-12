/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.socialnetwork.provider;

import java.util.List;

import org.silverpeas.core.date.Date;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.exception.SilverpeasException;

/**
 * @author Bensalem Nabil
 */
public interface ProviderSwitchInterface {

  /**
   * get my social Informations list according to the social information type and the UserId
   * @param socialInformationType
   * @param userId
   * @param classification
   * @param begin date
   * @param end date
   * @return List<SocialInformation>
   * @exception SilverpeasException
   */
  List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, String userId,
      String classification, Date begin, Date end) throws SilverpeasException;

  /**
   * get the List of social Informations of my contatcs according to the social information type and
   * the ids of my contacts
   * @param socialInformationType
   * @param myId
   * @param myContactsIds the ids of my contacts
   * @param begin date
   * @param end date
   * @return List<SocialInformation>
   * @exception SilverpeasException
   */
  List<SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType,
      String myId, List<String> myContactsIds, Date begin, Date end) throws SilverpeasException;

  /**
   * return the SocialEvent providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  SocialEventsInterface getSocialEventsInterface();

  /**
   * return the SocialGallery providor (by using Inversion of Control Containers )
   * @return SocialGalleryInterface
   */
  SocialGalleryInterface getSocialGalleryInterface();

  /**
   * return the SocialCommentGallery providor (by using Inversion of Control Containers )
   * @return SocialCommentGalleryInterface
   */
  SocialCommentGalleryInterface getSocialCommentGalleryInterface();

  /**
   * return the SocialPublications providor (by using Inversion of Control Containers )
   * @return SocialPublicationsInterface
   */
  SocialPublicationsInterface getSocialPublicationsInterface();

  /**
   * return the SocialCommentPublications providor (by using Inversion of Control Containers )
   * @return SocialCommentPublicationsInterface
   */
  SocialCommentPublicationsInterface getSocialCommentPublicationsInterface();

  /**
   * return the SocialCommentQuickInfos providor (by using Inversion of Control Containers )
   * @return SocialCommentQuickInfosInterface
   */
  SocialCommentQuickInfosInterface getSocialCommentQuickInfosInterface();

  /**
   * return SocialStatus providor (by using Inversion of Control Containers )
   * @return SocialStatusInterface
   */
  SocialStatusInterface getSocialStatusInterface();

  /**
   * return the SocialRelationShips providor (by using Inversion of Control Containers )
   * @return SocialRelationShipsInterface
   */
  SocialRelationShipsInterface getSocialRelationShipsInterface();

}
