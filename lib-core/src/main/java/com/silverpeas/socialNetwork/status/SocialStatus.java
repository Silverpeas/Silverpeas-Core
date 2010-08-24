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
package com.silverpeas.socialNetwork.status;

import com.silverpeas.socialNetwork.model.SocialInformation;
import java.util.List;

import com.silverpeas.socialNetwork.provider.SocialStatusInterface;

public class SocialStatus implements SocialStatusInterface {

  static private StatusService statusService;

  private StatusService getStatusService() {
    statusService = new StatusService();
    return statusService;
  }

  @Override
  public List getSocialInformationsList(String userid, int nbElement, int firstIndex) {

    List<SocialInformationStatus> list_status =
        this.getStatusService().getAllStatusService(Integer.
            parseInt(userid),
            nbElement, firstIndex);

    return list_status;
  }

  @Override
  public List<SocialInformationStatus> getSocialInformationsListOfMyContacts(
      List<String> myContactsIds,
      int numberOfElement, int firstIndex) {
    List<SocialInformationStatus> list_status =
        this.getStatusService().getSocialInformationsListOfMyContacts(myContactsIds,
            numberOfElement, firstIndex);

    return list_status;
  }
}
