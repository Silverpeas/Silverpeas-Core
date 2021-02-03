/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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

import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.socialnetwork.status.Status;
import org.silverpeas.core.socialnetwork.status.StatusService;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bensalem Nabil
 */
public class SocialNetworkService {

  private String myId;

  public SocialNetworkService(String myId) {
    this.myId = myId;
  }

  /**
   * get my wall : the List of my social information according to the type of social information
   * @param type
   * @param period
   * @return: Map<Date, List<SocialInformation>
   */
  public Map<Date, List<SocialInformation>> getSocialInformation(SocialInformationType type,
      Period period) {

    List<SocialInformation> socialInformationsFull =
        socialInformationService().getSocialInformationsList(type, myId,
        null, period);

    Collections.sort(socialInformationsFull);

    return processResults(socialInformationsFull);
  }

  private Map<Date, List<SocialInformation>> processResults(
      List<SocialInformation> socialInformationsFull) {
    String date = null;
    LinkedHashMap<Date, List<SocialInformation>> hashtable = new LinkedHashMap<>();
    List<SocialInformation> lsi = new ArrayList<>();
    SocialInformation lastInfoAdded = null;

    for (SocialInformation information : socialInformationsFull) {
      if (DateUtil.formatDate(information.getDate()).equals(date)) {
        if (!information.equals(lastInfoAdded)) {
          lsi.add(information);
          lastInfoAdded = information;
        } else {
          if (SocialInformationType.COMMENTPUBLICATION.name().equals(information.getType()) ||
              SocialInformationType.COMMENTPOST.name().equals(information.getType()) ||
              SocialInformationType.COMMENTNEWS.name().equals(information.getType()) ||
              SocialInformationType.COMMENTMEDIA.name().equals(information.getType())) {
            lsi.add(information);
            lastInfoAdded = information;
          } else {
            // change update attribute of last info added (except for comments)
            lsi.get(lsi.size() - 1).setUpdated(false);
          }
        }
      } else {
        date = DateUtil.formatDate(information.getDate());
        lsi = new ArrayList<>();
        lsi.add(information);
        hashtable.put(information.getDate(), lsi);
        lastInfoAdded = information;
      }
    }
    return hashtable;
  }

  /**
   * get my feed : the List of my social information and those of my contacts, according to the type of social information
   * @param type
   * @param period
   * @return: Map<Date, List<SocialInformation>
   */
  public Map<Date, List<SocialInformation>> getSocialInformationOfMyContacts(
      SocialInformationType type, Period period) {

    List<String> myContactIds = getMyContactsIds();
    // add myself
    myContactIds.add(myId);

    List<SocialInformation> socialInformationsFull =
        socialInformationService().getSocialInformationsListOfMyContact(type, myId,
        myContactIds, period);

    Collections.sort(socialInformationsFull);

    return processResults(socialInformationsFull);
  }

  /**
   * get my contact wall : the List of social information of the contacts of user in parameter, according to the type of social information
   * @param myContactId
   * @param type
   * @param period
   * @return: Map<Date, List<SocialInformation>
   */
  public Map<Date, List<SocialInformation>> getSocialInformationOfMyContact(String myContactId,
      SocialInformationType type, Period period) {

    List<String> myContactIds = Collections.singletonList(myContactId);

    List<SocialInformation> socialInformationsFull =
        socialInformationService().getSocialInformationsListOfMyContact(type, myId,
        myContactIds, period);

    Collections.sort(socialInformationsFull);

    return processResults(socialInformationsFull);
  }

  /**
   * update my status
   * @param textStatus
   * @return String
   */
  public String changeStatus(String textStatus) {
    Status status = new Status(Integer.parseInt(myId), new Date(), textStatus);
    return getStatusService().changeStatus(status);

  }

  /**
   * get my last status
   * @return String
   */
  public String getLastStatus() {
    Status status = getStatusService().getLastStatus(Integer.parseInt(myId));
    if (StringUtil.isDefined(status.getDescription())) {
      return status.getDescription();
    }
    return " ";
  }

  public List<String> getMyContactsIds() {
    return getRelationShipService().getMyContactsIds(Integer.parseInt(myId));
  }

  public List<String> getTheContactsIds(String myContactId) {
    return getRelationShipService().getMyContactsIds(Integer.parseInt(myContactId));
  }

  private SocialInformationService socialInformationService() {
    return ServiceProvider.getService(SocialInformationService.class);
  }

  private RelationShipService getRelationShipService() {
    return ServiceProvider.getService(RelationShipService.class);
  }

  private StatusService getStatusService() {
    return ServiceProvider.getService(StatusService.class);
  }

}