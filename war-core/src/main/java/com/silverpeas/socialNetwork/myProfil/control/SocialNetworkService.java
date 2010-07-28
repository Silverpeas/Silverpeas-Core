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
package com.silverpeas.socialNetwork.myProfil.control;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.silverpeas.socialNetwork.status.Status;
import com.silverpeas.socialNetwork.status.StatusService;
import com.silverpeas.util.StringUtil;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bensalem Nabil
 */
public class SocialNetworkService {

  private final String userId;

  public SocialNetworkService(String userId) {
    this.userId = userId;
  }

  public Map<Date, List<SocialInformation>> getSocialInformation(SocialInformationType type,
      int limit, int offset) {
    Map<Date, List<SocialInformation>> map = null;
    if (type.equals(SocialInformationType.ALL)) {//because the Itemes was not ordered
      List<SocialInformation> list = new ProviderService().getSocialInformationsList(type, userId,
          null, limit, offset);
      Collections.sort(list);
      map = new socialNetworkUtil().toLinkedHashMapWhenTypeIsALL(list, limit, offset);

    } else {//because the Itemes was already ordered
      List<SocialInformation> list = new ProviderService().getSocialInformationsList(type, userId,
          null, limit, offset);

      map = new socialNetworkUtil().toLinkedHashMap(list);
    }

    return map;
  }

 public String changeStatusService(String textStatus)
 {
   Status status = new Status(Integer.parseInt(userId), new Date(), textStatus);
   return new StatusService().changeStatusService(status);
   
 }
  public String getLastStatusService()
 {
    Status status=new StatusService().getLastStatusService(Integer.parseInt(userId));
    if(StringUtil.isDefined(status.getDescription()))
     return status.getDescription();
   return " ";
 }
 }



