/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.calendar.socialnetwork;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialEventsInterface;

import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.calendar.control.SilverpeasCalendar;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * @author Bensalem Nabil
 */
public class SocialEvent implements SocialEventsInterface {

  static private SilverpeasCalendar calendarBm = null;

  private static synchronized SilverpeasCalendar getEJB() throws CalendarException {
    if (calendarBm == null) {
      try {
        calendarBm = (EJBUtilitaire.getEJBObjectRef(JNDINames.CALENDARBM_EJBHOME,
            SilverpeasCalendar.class));
      } catch (Exception e) {
        throw new CalendarException("SocialEvent.getEJB()", SilverpeasException.ERROR,
            "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return calendarBm;
  }

  /**
   *
   * @param userId
   * @param classification
   * @param begin
   * @param end
   * @return
   * @throws CalendarException
   * @throws UtilException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, String classification,
      Date begin, Date end) throws CalendarException {
    try {
      String now = DateUtil.date2SQLDate(new java.util.Date());
      List<JournalHeader> list = getEJB().getNextEventsForUser(now, userId, classification, begin,
          end);
      List<SocialInformation> listEvent = new ArrayList<SocialInformation>(list.size());
      for (JournalHeader jh : list) {
        SocialInformationEvent event = new SocialInformationEvent((Schedulable) jh);
        listEvent.add(event);
      }
      return listEvent;
    } catch (CalendarException ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }

  }

  /**
   * get the next socialEvents of my contacts according to number of Item and the first Index
   *
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getEJB().getNextEventsForMyContacts(day, myId, myContactsIds, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }

  /**
   * get the Last socialEvents of my contacts according to number of Item and the first Index
   *
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getLastSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) throws SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getEJB().getLastEventsForMyContacts(day, myId, myContactsIds, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }

  /**
   * get the my last socialEvents according to number of Item and the first Index
   *
   * @param myId
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getMyLastSocialInformationsList(String myId, Date begin, Date end) throws
      SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getEJB().getMyLastEvents(day, myId, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }
}
