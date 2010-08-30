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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bensalem Nabil
 */
public class SocialNetworkService {

  private String myId;
  private final int FACTEUR = 3;//si paginationIndex est un miltiple de BONUS get plus from la base de donnés
  private List<SocialInformation> socialInformationsFull = new ArrayList<SocialInformation>();
  private int socialTypeInformationNumbre = 0;
  private List<String> myContactsIds;
  private int elementPerPage;

  public SocialNetworkService() {
    socialTypeInformationNumbre = 0;
    for (SocialInformationType type : SocialInformationType.values()) {
      if (type.ALL != type && type.EVENT != type) {
        socialTypeInformationNumbre++;
      }
    }
  }

  /**
   * get the List of social Information of my  according the type of social information
   * and the UserId
   * @return: Map<Date, List<SocialInformation>
   * @param:SocialInformationType socialInformationType, String userId,String classification, int limit  ,int offset
   */
  public Map<Date, List<SocialInformation>> getSocialInformation(SocialInformationType type,
      int limit, int paginationIndex) {


    int firstIndex = paginationIndex * limit;
    if ((paginationIndex % FACTEUR) == 0) {
      //si paginationIndex est un miltiple de BONUS: fais la demande de plus de la base de donnés
      fillInTemporaryStorage(type, myId, limit, firstIndex);
    }
    //recupirer la list a partir TemporaryStorage au lieu la base de donnée
    return getFromTemporaryStorage(type, limit, firstIndex);
  }

  /**
   * get the List of social Information of my contatc according the type of social information
   * and the UserId
   * @return: Map<Date, List<SocialInformation>
   * @param:SocialInformationType socialInformationType, String userId,String classification, int limit  ,int offset
   */
  public Map<Date, List<SocialInformation>> getSocialInformationOfMyContacts(
      SocialInformationType type,
      int limit, int paginationIndex) {
    int firstIndex = paginationIndex * limit;
    if ((paginationIndex % FACTEUR) == 0) {
      //si paginationIndex est un miltiple de BONUS: fais la demande de plus de la base de donnés
      fillInTemporaryStorageOfMyContacts(type, limit, firstIndex);
    }
    //recupirer la list a partir TemporaryStorage au lieu la base de donnée
    return getFromTemporaryStorage(type, limit, firstIndex);
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

  /**
   * get  last status of my contact
   * @return String
   */
  public String getLastStatusOfMyContact() {
    int contactId = -1;
    if (StringUtil.isInteger(myContactsIds.get(0))) {
      contactId = Integer.parseInt(myContactsIds.get(0));
    }
    Status status = new StatusService().getLastStatusService(contactId);
    if (StringUtil.isDefined(status.getDescription())) {
      return status.getDescription();
    }
    return " ";
  }
  /**
   * the TemporaryStorage pour eviter le vas et viens a la base de donneés et aussi
   *  eviter de obtenir toutes la resulta  de la base de donnés qui sera une opération couteuse
   *  FACTEUR: c'est un facteur d'optimisation
   * @param type
   * @param userId
   * @param limit
   * @param firstIndex
   */

  private void fillInTemporaryStorage(SocialInformationType type, String userId,
      int limit, int firstIndex) {
    socialInformationsFull = new ProviderService().getSocialInformationsList(type, userId,
        null, limit * FACTEUR, firstIndex * FACTEUR);
    if (SocialInformationType.ALL.equals(type)) {
      Collections.sort(socialInformationsFull);
    }
  }

  /**
   * the TemporaryStorage pour eviter le vas et viens a la base de donneés et aussi
   *  eviter de obtenir toutes la resulta  de la base de donnés qui sera une opération couteuse
   *  FACTEUR: c'est un facteur d'optimisation
   * @param type
   * @param limit
   * @param firstIndex
   */
  private void fillInTemporaryStorageOfMyContacts(SocialInformationType type,
      int limit, int firstIndex) {
    socialInformationsFull = new ProviderService().getSocialInformationsListOfMyContact(type, myId,
        myContactsIds, limit * FACTEUR, firstIndex * FACTEUR);
    if (SocialInformationType.ALL.equals(type)) {
      Collections.sort(socialInformationsFull);
    }
  }
  /**
   * recupirer la resulta à partir de  TemporaryStorage et pas la base de donnes
   * @param type
   * @param limit
   * @param firstIndex
   * @return Map<Date, List<SocialInformation>>
   */

  private Map<Date, List<SocialInformation>> getFromTemporaryStorage(SocialInformationType type,
      int limit, int firstIndex) {
    Map<Date, List<SocialInformation>> map = null;

    if (SocialInformationType.ALL.equals(type)) {// if type equal ALL
      List<SocialInformation> listToDispaly = new ArrayList<SocialInformation>();
      int lastIndex = Math.min(
          firstIndex * socialTypeInformationNumbre + limit * socialTypeInformationNumbre, socialInformationsFull.
          size());
      if (firstIndex * socialTypeInformationNumbre <= lastIndex)//there always more Item of socialnetwork in database or in the socialInformationsFull List
      {
        listToDispaly = socialInformationsFull.subList(firstIndex * socialTypeInformationNumbre,
            lastIndex);
      }
      map = new socialNetworkUtil().toLinkedHashMapWhenTypeIsALL(listToDispaly, limit, firstIndex);

    } else {// if type not equal ALL
      List<SocialInformation> listToDispaly = new ArrayList<SocialInformation>();
      int lastIndex = Math.min(firstIndex + limit, socialInformationsFull.size());
      if (firstIndex <= lastIndex)//there always more Item of socialnetwork in database or in the socialInformationsFull List (TemporaryStorage)
      {
        listToDispaly = socialInformationsFull.subList(firstIndex, lastIndex);
      }
      map = new socialNetworkUtil().toLinkedHashMap(listToDispaly);
    }
    return map;
  }

  /**
   *
   * @param myId
   */
  public void setMyId(String myId) {
    this.myId = myId;
  }

  /**
   *
   * @param myContactsIds
   */
  public void setMyContactsIds(List<String> myContactsIds) {
    this.myContactsIds = myContactsIds;
  }

  /**
   *
   * @param elementPerPage
   */
  public void setElementPerPage(int elementPerPage) {
    this.elementPerPage = elementPerPage;
  }
}



