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

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

public interface SocialEventsInterface {

  public static SocialEventsInterface get() {
    return ServiceProvider.getService(SocialEventsInterface.class);
  }

  /**
   * get list of socialInformation according to number of Item and the first Index
   * @return List <SocialInformation>
   * @param userId
   * @param classification (private or public)
   * @param begin date
   * @param end date
   * @throws SilverpeasException
   */
  public List<SocialInformation> getSocialInformationsList(String userId, String classification,
      Date begin, Date end) throws SilverpeasException;

  /**
   * get the next Events of my contacts according to number of Item and the first Index
   * @return: List <SocialInformation>
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @throws SilverpeasException
   */
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException;

  /**
   * get the Last Events of my contacts according to number of Item and the first Index
   * @return: List <SocialInformation>
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @throws SilverpeasException
   */
  public List<SocialInformation> getLastSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds,
      Date begin, Date end) throws SilverpeasException;

  /**
   * get the my last Events according to number of Item and the first Index
   * @return: List <SocialInformation>
   * @param myId
   * @param begin date
   * @param end date
   * @throws SilverpeasException
   */
  public List<SocialInformation> getMyLastSocialInformationsList(String myId, Date begin, Date end)
      throws SilverpeasException;
}
