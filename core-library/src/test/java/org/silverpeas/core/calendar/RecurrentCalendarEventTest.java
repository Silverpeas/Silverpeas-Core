/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.ical4j.ICal4JCalendarEventOccurrenceGenerator;
import org.silverpeas.core.calendar.ical4j.ICal4JDateCodec;
import org.silverpeas.core.calendar.ical4j.ICal4JRecurrenceCodec;
import org.silverpeas.core.calendar.notification.CalendarEventLifeCycleEventNotifier;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.calendar.repository.CalendarRepository;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedBean;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.test.unit.extention.TestManagedMock;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.date.TimeUnit.MONTH;
import static org.silverpeas.core.date.TimeUnit.WEEK;

/**
 * @author mmoquillon
 */
@EnableSilverTestEnv
@TestManagedBeans({Transaction.class, JpaPersistOperation.class, JpaUpdateOperation.class})
class RecurrentCalendarEventTest {

  private static final String CALENDAR_ID = "CAL_ID_1";
  private static final String EVENT_TITLE = "an event";
  private static final String EVENT_DESCRIPTION = "a description";
  private static final String USER_ID = "0";

  @SuppressWarnings("unused")
  @TestManagedBean
  private final CalendarEventOccurrenceGenerator generator =
      new ICal4JCalendarEventOccurrenceGenerator(new ICal4JDateCodec(),
          new ICal4JRecurrenceCodec(new ICal4JDateCodec()));

  @TestManagedMock
  CalendarEventRepository eventRepository;

  @TestManagedMock
  CalendarEventLifeCycleEventNotifier notifier;

  @BeforeEach
  public void mockRepositories(
      @TestManagedMock CalendarEventOccurrenceRepository eventOccurRepository,
      @TestManagedMock CalendarRepository calendarRepository,
      @TestManagedMock OrganizationController organizationController,
      @TestManagedMock UserProvider userProvider) {
    Answer<? extends User> userAnswer = a -> {
      String id = a.getArgument(0);
      UserDetail user = new UserDetail();
      user.setId(id);
      return user;
    };
    when(userProvider.getUser(anyString())).thenAnswer(userAnswer);
    when(organizationController.getUserDetail(anyString())).thenAnswer(userAnswer);
    when(eventOccurRepository.getAll(anyCollection(), any(Period.class))).thenReturn(
        Collections.emptyList());
    when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(i -> i.getArgument(0));
    doNothing().when(notifier)
        .notifyEventOn(any(ResourceEvent.Type.class), any(CalendarEvent.class));
    when(calendarRepository.getById(CALENDAR_ID)).thenReturn(
        new Calendar("calendar32", "A shared calendar"));
    OperationContext.fromUser(USER_ID);
  }

  @Test
  void planAWeeklyTimelyEventOnFirstSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = aTimelyEvent()
        .recur(Recurrence.every(2, WEEK).on(MONDAY, FRIDAY))
        .planOn(calendar);
    verify(eventRepository).save(event);
    verify(notifier).notifyEventOn(ResourceEvent.Type.CREATION, event);
  }

  @Test
  void planAMonthlyTimelyEventOnSomeSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = aTimelyEvent()
        .recur(Recurrence.every(1, MONTH)
            .on(DayOfWeekOccurrence.nth(1, MONDAY), DayOfWeekOccurrence.nth(3, FRIDAY)))
        .planOn(calendar);
    verify(eventRepository).save(event);
    verify(notifier).notifyEventOn(ResourceEvent.Type.CREATION, event);
  }

  @Test
  void planAWeeklyAllDayEventOnFirstSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = anAllDayEvent()
        .recur(Recurrence.every(2, WEEK).on(MONDAY, FRIDAY))
        .planOn(calendar);
    verify(eventRepository).save(event);
    verify(notifier).notifyEventOn(ResourceEvent.Type.CREATION, event);
  }

  @Test
  void planAMonthlyAllDayEventOnSomeSpecificDays() {
    Calendar calendar = Calendar.getById(CALENDAR_ID);
    CalendarEvent event = anAllDayEvent()
        .recur(Recurrence.every(1, MONTH)
            .on(DayOfWeekOccurrence.nth(1, MONDAY), DayOfWeekOccurrence.nth(3, FRIDAY)))
        .planOn(calendar);
    verify(eventRepository).save(event);
    verify(notifier).notifyEventOn(ResourceEvent.Type.CREATION, event);
  }

  private CalendarEvent aTimelyEvent() {
    OffsetDateTime now = OffsetDateTime.now();
    return CalendarEvent.on(Period.between(now, now.plusHours(2)))
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
  }

  private CalendarEvent anAllDayEvent() {
    LocalDate today = LocalDate.now();
    return CalendarEvent.on(today)
        .createdBy(USER_ID)
        .withTitle(EVENT_TITLE)
        .withDescription(EVENT_DESCRIPTION);
  }
}
  