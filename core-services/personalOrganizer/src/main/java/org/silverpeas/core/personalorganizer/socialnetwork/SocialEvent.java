/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.personalorganizer.socialnetwork;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.personalorganizer.model.JournalHeader;
import org.silverpeas.core.personalorganizer.service.SilverpeasCalendar;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialEventProvider;
import org.silverpeas.core.util.DateUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bensalem Nabil
 */
@Provider
public class SocialEvent implements SocialEventProvider {

  @Inject
  private SilverpeasCalendar calendar;

  private SilverpeasCalendar getCalendar() {
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
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, String classification,
      Date begin, Date end) {
    String now = DateUtil.date2SQLDate(new java.util.Date());
    List<JournalHeader> list =
        getCalendar().getNextEventsForUser(now, userId, classification, begin, end);
    List<SocialInformation> listEvent = new ArrayList<>(list.size());
    for (JournalHeader jh : list) {
      SocialInformationEvent event = new SocialInformationEvent(jh);
      listEvent.add(event);
    }
    return listEvent;
  }

  @Override
  public List<SocialInformation> getSocialInformationList(final String userId, final Date begin,
      final Date end) {
    return getSocialInformationsList(userId, "", begin, end);
  }

  /**
   * get the next socialEvents of my contacts according to number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return
   */
  @Override
  public List getSocialInformationListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    return getCalendar().getNextEventsForMyContacts(day, myId, myContactsIds, begin, end);
  }

  /**
   * get the Last socialEvents of my contacts according to number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return
   */
  @Override
  public List getLastSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    return getCalendar().getLastEventsForMyContacts(day, myId, myContactsIds, begin, end);
  }

  /**
   * get the my last socialEvents according to number of Item and the first Index
   * @param myId
   * @return
   */
  @Override
  public List getMyLastSocialInformationsList(String myId, Date begin, Date end) {
    String day = DateUtil.date2SQLDate(new java.util.Date());
    return getCalendar().getMyLastEvents(day, myId, begin, end);
  }
}
