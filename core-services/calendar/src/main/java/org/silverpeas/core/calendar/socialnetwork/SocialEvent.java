/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.calendar.socialnetwork;

import org.silverpeas.core.date.Date;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialEventsInterface;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.calendar.service.CalendarException;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.JournalHeader;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
@Singleton
public class SocialEvent implements SocialEventsInterface {

  @Inject
  private SilverpeasCalendar calendar;

  private SilverpeasCalendar getCalendar() throws CalendarException {
    if (calendar == null) {
      throw new CalendarException(SocialEvent.class.getSimpleName(), SilverTrace.TRACE_LEVEL_ERROR,
          "IoC error to get SilverpeasCalendar implementation");
    }
    return calendar;
  }

  protected SocialEvent() {

  }

  /**
   * @param userId the user identifier
   * @param classification
   * @param begin date
   * @param end date
   * @return
   * @throws CalendarException
   * @throws UtilException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, String classification,
      Date begin, Date end) throws CalendarException {
    try {
      String now = DateUtil.date2SQLDate(new java.util.Date());
      List<JournalHeader> list =
          getCalendar().getNextEventsForUser(now, userId, classification, begin, end);
      List<SocialInformation> listEvent = new ArrayList<>(list.size());
      for (JournalHeader jh : list) {
        SocialInformationEvent event = new SocialInformationEvent(jh);
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
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) throws SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getCalendar().getNextEventsForMyContacts(day, myId, myContactsIds, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }

  /**
   * get the Last socialEvents of my contacts according to number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getLastSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) throws SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getCalendar().getLastEventsForMyContacts(day, myId, myContactsIds, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }

  /**
   * get the my last socialEvents according to number of Item and the first Index
   * @param myId
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List getMyLastSocialInformationsList(String myId, Date begin, Date end)
      throws SilverpeasException {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    try {
      return getCalendar().getMyLastEvents(day, myId, begin, end);
    } catch (Exception ex) {
      throw new CalendarException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }
}
