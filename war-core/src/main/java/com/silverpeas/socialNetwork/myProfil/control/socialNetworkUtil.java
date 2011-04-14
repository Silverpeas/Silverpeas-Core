/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

import com.stratelia.webactiv.util.DateUtil;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azzedine
 */
public class socialNetworkUtil {

  public static List<SocialInformation> socialInformations = new ArrayList<SocialInformation>();
  private final int FIRST_INDEX = 0;

  public socialNetworkUtil() {
  }
  /**
   * Convert the list of socialInformation to Map
   * limit is the numbre of element
   * offset the last index of element was called
   * @param listEvents
   * @return Map<Date, List<SocialInformation>>
   */

  Map<Date, List<SocialInformation>> toLinkedHashMap(List<SocialInformation> listEvents) {
    String date = null;
    LinkedHashMap<Date, List<SocialInformation>> hashtable =
        new LinkedHashMap<Date, List<SocialInformation>>();
    List<SocialInformation> lsi = new ArrayList<SocialInformation>();

    for (int i = 0; i < listEvents.size(); i++) {
      SocialInformation information = (SocialInformation) listEvents.get(i);
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
   * Convert the list of socialInformation to Map whene the type is ALL
   * limit is the numbre of element
   * offset the last index of element was called
   * @param listEvents
   * @param limit
   * @param offset
   * @return Map<Date, List<SocialInformation>>
   */

  Map<Date, List<SocialInformation>> toLinkedHashMapWhenTypeIsALL(List<SocialInformation> listEvents,
      int limit, int offset) {

    if (offset == 0)// this the first time must initialise the List and the first index start with 0
    {

      socialInformations = new ArrayList<SocialInformation>();
    }

    Collections.sort(listEvents);
    socialInformations.addAll(listEvents);

    limit = Math.min(limit, socialInformations.size());
    listEvents = socialInformations.subList(FIRST_INDEX, limit);
    socialInformations = socialInformations.subList(limit, socialInformations.size());
    String date = null;
    LinkedHashMap<Date, List<SocialInformation>> hashtable =
        new LinkedHashMap<Date, List<SocialInformation>>();
    List<SocialInformation> lsi = new ArrayList<SocialInformation>();

    for (int i = 0; i < listEvents.size(); i++) {
      SocialInformation information = (SocialInformation) listEvents.get(i);
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
}
