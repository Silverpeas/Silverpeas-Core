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

package com.silverpeas.socialnetwork.provider;

import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author Bensalem Nabil
 */
public interface ProviderSwitchInterface {

  /**
   * get my social Informations list according to the social information type and the UserId
   * @param socialInformationType
   * @param userId
   * @param String classification
   * @param limit nb of element
   * @param offset firstIndex
   * @return List<SocialInformation>
   * @exception SilverpeasException
   */
  public List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, String userId,
      String classification, Date begin, Date end) throws SilverpeasException;

  /**
   * get the List of social Informations of my contatcs according to the social information type and
   * the ids of my contacts
   * @param socialInformationType
   * @param myId
   * @param myContactsIds the ids of my contacts
   * @param limit nb of element
   * @param offset firstIndex
   * @return List<SocialInformation>
   * @exception SilverpeasException
   */
  public List<SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType,
      String myId, List<String> myContactsIds, Date begin, Date end) throws SilverpeasException;

  /**
   * return the SocialEvent providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialEventsInterface getSocialEventsInterface();

  /**
   * set SocialEvent providor (by using Inversion of Control Containers )
   * @param socialGalleryInterface
   */

  public void setSocialEventsInterface(SocialEventsInterface socialEventsInterface);

  /**
   * return the SocialGallery providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialGalleryInterface getSocialGalleryInterface();

  /**
   * set SocialGallery providor (by using Inversion of Control Containers )
   * @param socialGalleryInterface
   */
  public void setSocialGalleryInterface(SocialGalleryInterface socialGalleryInterface);

  /**
   * return the SocialPublications providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialPublicationsInterface getSocialPublicationsInterface();

  /**
   * set SocialPublications providor (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  public void setSocialPublicationsInterface(SocialPublicationsInterface socialPublicationsInterface);

  /**
   * return SocialStatus providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialStatusInterface getSocialStatusInterface();

  /**
   * set SocialStatus providor (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  public void setSocialStatusInterface(SocialStatusInterface socialStatusInterface);

  /**
   * return the SocialRelationShips providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialRelationShipsInterface getSocialRelationShipsInterface();

  /**
   * set SocialRelationShips providor (by using Inversion of Control Containers )
   * @param socialPublicationsInterface
   */
  public void setSocialRelationShipsInterface(
      SocialRelationShipsInterface socialRelationShipsInterface);
}
