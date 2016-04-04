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

package org.silverpeas.web.socialnetwork.myprofil.control;

import org.silverpeas.core.date.Date;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.ProviderSwitchInterface;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bensalem Nabil;
 */
@Singleton
public class SocialInformationService {

  @Inject
  private ProviderSwitchInterface switchInterface;

  public SocialInformationService() {
  }

  /**
   * get the List of social Information of my according the type of social information and the
   * UserId
   * @return: List<SocialInformation>
   * @param socialInformationType
   * @param userId
   * @param classification
   * @param begin
   * @param end
   */

  public List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, String userId,
      String classification, Date begin, Date end) {
    try {
      return switchInterface.getSocialInformationsList(socialInformationType, userId,
          classification,
          begin, end);
    } catch (Exception ex) {

    }
    return new ArrayList<SocialInformation>();
  }

  /**
   * get the List of social Information of my according the type of social information and the
   * UserId
   * @return: List<SocialInformation>
   * @param socialInformationType
   * @param myId
   * @param myContactIds
   * @param begin
   * @param end
   */

  public List<SocialInformation> getSocialInformationsListOfMyContact(
      SocialInformationType socialInformationType, String myId,
      List<String> myContactIds, Date begin, Date end) {
    try {
      return switchInterface.getSocialInformationsListOfMyContacts(socialInformationType, myId,
          myContactIds, begin, end);
    } catch (Exception ex) {

    }
    return new ArrayList<SocialInformation>();
  }

}