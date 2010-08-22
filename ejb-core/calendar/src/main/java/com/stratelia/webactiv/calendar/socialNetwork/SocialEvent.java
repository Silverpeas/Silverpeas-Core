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
package com.stratelia.webactiv.calendar.socialNetwork;

import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.provider.SocialEventsInterface;

import com.stratelia.webactiv.calendar.control.CalendarBm;

import com.stratelia.webactiv.calendar.control.CalendarBmHome;
import com.stratelia.webactiv.calendar.control.CalendarException;

import com.stratelia.webactiv.calendar.model.JournalDAO;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.Schedulable;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import java.lang.String;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public class SocialEvent implements SocialEventsInterface {

  static private CalendarBm calendarBm = null;
  private JournalDAO dao = new JournalDAO();

  /**
   * getEJB
   * @return instance of CalendarBmHome
   */
 private static  CalendarBm getEJB() throws CalendarException {
    if (calendarBm == null) {
      try {
        calendarBm = ((CalendarBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.CALENDARBM_EJBHOME, CalendarBmHome.class)).create();
      } catch (Exception e) {
        throw new CalendarException("SocialEvent.getEJB()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }

  /**
   * get list of socialEvents according to
   *  number of Item and the first Index
   * @param userId
   * @param classification
   * @param limit
   * @param offset
   * @return List<SocialInformation>
   * @throws CalendarException
   * @throws UtilException
   */
  @Override
  public List getSocialInformationsList(String userId, String classification, int limit, int offset)
      throws CalendarException, UtilException {
    List<JournalHeader> list = null;
    List listEvent = new ArrayList();
    Calendar calendar = Calendar.getInstance();
    try {
      list = getEJB().getNextEventsForUser(DateUtil.date2SQLDate(calendar.getTime()), userId,
          classification, limit, offset);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
    for (JournalHeader jh : list) {
      SocialInformationEvent event = new SocialInformationEvent((Schedulable) jh);
      listEvent.add(event);
    }
    return listEvent;
  }

  /**
   * get the next socialEvents of my contacts according to
   * number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws SilverpeasException {
    List<SocialInformationEvent> list = null;
    Calendar calendar = Calendar.getInstance();
    String day = DateUtil.date2SQLDate(calendar.getTime());
    try {
      list = getEJB().getNextEventsForMyContacts(day, myId, myContactsIds, numberOfElement,
          firstIndex);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
System.out.println("listEvent size="+list.size());
    return list;
  }

  /**
   * get the Last socialEvents of my contacts according to number of Item and the first Index
   * @return: List <SocialInformation>
   * @param :String myId , List<String> myContactsIds , int numberOfElement, int firstIndex
   */
  @Override
  public List getLastSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, int numberOfElement, int firstIndex) throws SilverpeasException {
    List<SocialInformationEvent> list = null;
    Calendar calendar = Calendar.getInstance();
    String day = DateUtil.date2SQLDate(calendar.getTime());
    try {
      list = getEJB().getLastEventsForMyContacts(day, myId, myContactsIds, numberOfElement,
          firstIndex);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }

    return list;
  }

  /**
   * get the my last socialEvents  according to number of Item and the first Index
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getMyLastSocialInformationsList(String myId, int numberOfElement, int firstIndex)
      throws SilverpeasException {
   List<SocialInformationEvent> list = null;
    Calendar calendar = Calendar.getInstance();
    String day = DateUtil.date2SQLDate(calendar.getTime());
    try {
      list = getEJB().getMyLastEvents(day, myId, numberOfElement,
          firstIndex);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }

    return list;
  }
}
