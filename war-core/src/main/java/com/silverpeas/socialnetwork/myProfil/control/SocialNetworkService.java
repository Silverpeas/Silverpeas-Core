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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.socialnetwork.myProfil.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.silverpeas.socialnetwork.status.Status;
import com.silverpeas.socialnetwork.status.StatusService;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author Bensalem Nabil
 */
public class SocialNetworkService {

  private String myId;

  public SocialNetworkService(String myId) {
    this.myId = myId;
  }

  /**
   * get the List of social Information of my according the type of social information and the
   * UserId
   * @return: Map<Date, List<SocialInformation>
   * @param:SocialInformationType socialInformationType, String userId,String classification, int
   * limit ,int offset
   */
  public Map<Date, List<SocialInformation>> getSocialInformation(SocialInformationType type,
      Date begin, Date end) {

    com.silverpeas.calendar.Date dBegin = new com.silverpeas.calendar.Date(begin);
    com.silverpeas.calendar.Date dEnd = new com.silverpeas.calendar.Date(end);

    List<SocialInformation> socialInformationsFull =
        new ProviderService().getSocialInformationsList(type, myId,
        null, dEnd, dBegin);

    if (SocialInformationType.ALL.equals(type)) {
      Collections.sort(socialInformationsFull);
    }

    return processResults(socialInformationsFull);
  }

  private Map<Date, List<SocialInformation>> processResults(
      List<SocialInformation> socialInformationsFull) {
    String date = null;
    LinkedHashMap<Date, List<SocialInformation>> hashtable =
        new LinkedHashMap<Date, List<SocialInformation>>();
    List<SocialInformation> lsi = new ArrayList<SocialInformation>();

    for (SocialInformation information : socialInformationsFull) {
      if (DateUtil.formatDate(information.getDate()).equals(date)) {
        lsi.add(information);
      } else {
        date = DateUtil.formatDate(information.getDate());
        lsi = new ArrayList<SocialInformation>();
        lsi.add(information);
        hashtable.put(information.getDate(), lsi);
      }
    }
    return hashtable;
  }

  /**
   * get the List of social Information of my contatc according the type of social information and
   * the UserId
   * @return: Map<Date, List<SocialInformation>
   * @param:SocialInformationType socialInformationType, String userId,String classification, int
   * limit ,int offset
   */
  public Map<Date, List<SocialInformation>> getSocialInformationOfMyContacts(
      SocialInformationType type, Date begin, Date end) {

    com.silverpeas.calendar.Date dBegin = new com.silverpeas.calendar.Date(begin);
    com.silverpeas.calendar.Date dEnd = new com.silverpeas.calendar.Date(end);

    List<String> myContactIds = getMyContactsIds();
    myContactIds.add(myId); // add myself

    List<SocialInformation> socialInformationsFull =
        new ProviderService().getSocialInformationsListOfMyContact(type, myId,
        myContactIds, dEnd, dBegin);

    if (SocialInformationType.ALL.equals(type)) {
      Collections.sort(socialInformationsFull);
    }

    return processResults(socialInformationsFull);
  }

  public Map<Date, List<SocialInformation>> getSocialInformationOfMyContact(String myContactId,
      SocialInformationType type, Date begin, Date end) {

    com.silverpeas.calendar.Date dBegin = new com.silverpeas.calendar.Date(begin);
    com.silverpeas.calendar.Date dEnd = new com.silverpeas.calendar.Date(end);

    List<String> myContactIds = new ArrayList<String>();
    myContactIds.add(myContactId);

    List<SocialInformation> socialInformationsFull =
        new ProviderService().getSocialInformationsListOfMyContact(type, myId,
        myContactIds, dEnd, dBegin);

    if (SocialInformationType.ALL.equals(type)) {
      Collections.sort(socialInformationsFull);
    }

    return processResults(socialInformationsFull);
  }

  /**
   * update my status
   * @param textStatus
   * @return String
   */
  public String changeStatusService(String textStatus) {
    Status status = new Status(Integer.parseInt(myId), new Date(), textStatus);
    return new StatusService().changeStatusService(status);

  }

  /**
   * get my last status
   * @return String
   */
  public String getLastStatusService() {
    Status status = new StatusService().getLastStatusService(Integer.parseInt(myId));
    if (StringUtil.isDefined(status.getDescription())) {
      return status.getDescription();
    }
    return " ";
  }

  public List<String> getMyContactsIds() {
    try {
      return new RelationShipService().getMyContactsIds(Integer.parseInt(myId));
    } catch (SQLException ex) {
      SilverTrace.error("socialNetworkService", "SocialNetworkService.getMyContactsIds", "", ex);
    }
    return new ArrayList<String>();
  }

}