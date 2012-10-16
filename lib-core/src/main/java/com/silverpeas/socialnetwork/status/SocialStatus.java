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

package com.silverpeas.socialnetwork.status;

import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialStatusInterface;

public class SocialStatus implements SocialStatusInterface {

  private StatusService getStatusService() {
    return new StatusService();
  }

  /**
   * Get list of my socialInformationStatus according to number of Item and the first Index
   * @param userid
   * @param nbElement
   * @param firstIndex
   * @return List
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userid, Date begin, Date end) {

    return getStatusService().getAllStatusService(Integer.parseInt(userid), begin, end);
  }

  /**
   * Get list of socialInformationStatus of my contacts according to number of Item and the first
   * Index
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(
      List<String> myContactsIds, Date begin, Date end) {

    return getStatusService().getSocialInformationsListOfMyContacts(myContactsIds, begin, end);
  }
}
