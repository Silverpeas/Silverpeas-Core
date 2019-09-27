/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.calendar.notification;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.CalendarEventOccurrenceBuilder;
import org.silverpeas.core.calendar.CalendarEventStubBuilder;
import org.silverpeas.core.calendar.CalendarMockBuilder;
import org.silverpeas.core.calendar.repository.CalendarEventOccurrenceRepository;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.reminder.DurationReminder;
import org.silverpeas.core.reminder.Reminder;
import org.silverpeas.core.reminder.ReminderProcessName;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMap;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;

import javax.ws.rs.core.UriBuilder;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
@TestManagedBeans({JpaUpdateOperation.class, JpaPersistOperation.class,
    CalendarEventUserNotificationReminder.class})
public class CalendarEventUserNotificationReminderTest {
  private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

  private static final ReminderProcessName PROCESS_NAME = () -> "TestReminderProcess";
  private static final String INSTANCE_ID = "instance26";
  private static final String LOCAL_ID = "localId";
  private static final String FR = "fr";
  private static final String EN = "en";
  private static final String DE = "de";
  private static final String COMPONENT_NAME = "componentName";

  private static final LocalDate DATE_0F_21 = LocalDate.parse("2018-02-21");
  private static final OffsetDateTime DATE_0F_21_AT_21H = OffsetDateTime.parse("2018-02-21T21:00:00Z");
  private static final OffsetDateTime DATE_0F_21_AT_23H = OffsetDateTime.parse("2018-02-21T23:00:00Z");
  private static final LocalDate DATE_0F_22 = LocalDate.parse("2018-02-22");
  private static final OffsetDateTime DATE_0F_22_AT_01H = OffsetDateTime.parse("2018-02-22T01:00:00Z");

  @RegisterExtension
  public FieldMocker reflectionRule = new FieldMocker();

  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private ComponentAccessControl componentAccessControl;
  @TestManagedMock
  private Administration administration;
  @TestManagedMock
  private ContributionManager contributionManager;
  @TestManagedMock
  private CalendarEventOccurrenceRepository calendarEventOccurrenceRepository;
  private User receiver;
  @TestManagedMock
  private ComponentInstanceRoutingMap componentInstanceRoutingMap;

  private Period currentPeriod;

  @SuppressWarnings({"unchecked", "serial"})
  @BeforeEach
  public void setup(@TestManagedMock UserProvider userProvider, @TestManagedMock
      ComponentInstanceRoutingMapProviderByInstance componentInstanceRoutingMapProviderByInstance,
      @TestManagedMock ComponentInstanceRoutingMapProvider componentInstanceRoutingMapProvider,
      @TestManagedMock SilverpeasComponentInstanceProvider silverpeasComponentInstanceProvider,
      @TestManagedMock SilverpeasComponentInstance componentInstance) {
    when(componentInstance.getName()).thenReturn(COMPONENT_NAME);
    when(silverpeasComponentInstanceProvider.getById(INSTANCE_ID))
        .thenReturn(Optional.of(componentInstance));
    when(silverpeasComponentInstanceProvider.getComponentName(INSTANCE_ID))
        .thenReturn("componentNameTest");

    when(componentInstanceRoutingMapProviderByInstance.getByInstanceId(INSTANCE_ID))
        .thenReturn(componentInstanceRoutingMapProvider);
    when(componentInstanceRoutingMapProvider.absolute()).thenReturn(componentInstanceRoutingMap);

    UserPreferences userPreferences = new UserPreferences("26", "fr", ZoneId.systemDefault(), null,
        null, false, false, false, UserMenuDisplay.DEFAULT);
    receiver = mock(User.class);
    when(receiver.getId()).thenReturn("26");
    when(receiver.getUserPreferences()).thenReturn(userPreferences);

    when(userProvider.getUser(receiver.getId())).thenReturn(receiver);
    reflectionRule
        .setField(DisplayI18NHelper.class, Locale.getDefault().getLanguage(), "defaultLanguage");
  }

  @Test
  public void durationReminderOf0MinuteOnCalendarContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOnAllDay())
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    assertContentsOnSimpleEventOnAllDay(durationReminder);
  }

  @Test
  public void durationReminderOf1MinuteOnCalendarContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOnAllDay())
        .triggerBefore(1, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    assertContentsOnSimpleEventOnAllDay(durationReminder);
  }

  @Test
  public void durationReminderOf5MinutesOnCalendarContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOnAllDay())
        .triggerBefore(5, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    assertContentsOnSimpleEventOnAllDay(durationReminder);
  }

  @Test
  public void durationReminderOf0HourAndWithAnotherUserZoneIdOnCalendarContributionShouldWork()
      throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOnAllDay())
        .triggerBefore(0, TimeUnit.HOUR, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018 (UTC)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018 (UTC)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018 (UTC)."));
  }

  private void assertContentsOnSimpleEventOnAllDay(final DurationReminder durationReminder) {
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018."));
  }

  @Test
  public void durationReminderOnSeveralDaysEventOnAllDayShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSeveralDaysEventOnAllDay())
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 - 22.02.2018"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 - 02/22/2018"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 - 22/02/2018"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 to 22.02.2018."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 to 02/22/2018."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 au 22/02/2018."));
  }

  @Test
  public void durationReminderWithAnotherUserZoneIdOnSeveralDaysEventOnAllDayShouldWork() throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(setupSeveralDaysEventOnAllDay())
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 - 22.02.2018 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 - 02/22/2018 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 - 22/02/2018 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 to 22.02.2018 (UTC)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 to 02/22/2018 (UTC)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 au 22/02/2018 (UTC)."));
  }

  @Test
  public void durationReminderWithAnotherBeforeCalendarZoneIdOnSeveralDaysEventOnAllDayShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSeveralDaysEventOnAllDay();
    when(calendarEvent.getCalendar().getZoneId()).thenReturn(ZoneId.of("America/Cancun"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 - 22.02.2018 (America/Cancun)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 - 02/22/2018 (America/Cancun)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 - 22/02/2018 (America/Cancun)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 to 22.02.2018 (America/Cancun)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 to 02/22/2018 (America/Cancun)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 au 22/02/2018 (America/Cancun)."));
  }

  @Test
  public void durationReminderWithAnotherAfterCalendarZoneIdOnSeveralDaysEventOnAllDayShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSeveralDaysEventOnAllDay();
    when(calendarEvent.getCalendar().getZoneId()).thenReturn(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 - 22.02.2018 (Asia/Muscat)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 - 02/22/2018 (Asia/Muscat)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 - 22/02/2018 (Asia/Muscat)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 to 22.02.2018 (Asia/Muscat)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 to 02/22/2018 (Asia/Muscat)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 au 22/02/2018 (Asia/Muscat)."));
  }

  @Test
  public void durationReminderOnSimpleEventOf2HoursShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOn2Hours(ZoneId.systemDefault()))
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 21:00 - 23:00"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 21:00 - 23:00"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 21:00 - 23:00"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018 from 21:00 to 23:00."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018 from 21:00 to 23:00."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018 de 21:00 à 23:00."));
  }

  @Test
  public void durationReminderWithAnotherUserZoneIdOnSimpleEventOf2HoursShouldWork() throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(setupSimpleEventOn2Hours(ZoneId.systemDefault()))
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 21:00 - 23:00 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 21:00 - 23:00 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 21:00 - 23:00 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018 from 21:00 to 23:00 (UTC)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018 from 21:00 to 23:00 (UTC)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018 de 21:00 à 23:00 (UTC)."));
  }

  @Test
  public void durationReminderWithAnotherBeforeCalendarZoneIdOnSimpleEventOf2HoursShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSimpleEventOn2Hours(ZoneId.of("America/Cancun"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 21:00 - 23:00 (America/Cancun)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 21:00 - 23:00 (America/Cancun)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 21:00 - 23:00 (America/Cancun)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018 from 21:00 to 23:00 (America/Cancun)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018 from 21:00 to 23:00 (America/Cancun)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018 de 21:00 à 23:00 (America/Cancun)."));
  }

  @Test
  public void durationReminderWithAnotherAfterCalendarZoneIdOnSimpleEventOf2HoursShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSimpleEventOn2Hours(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 21:00 - 23:00 (Asia/Muscat)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 21:00 - 23:00 (Asia/Muscat)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 21:00 - 23:00 (Asia/Muscat)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be on 21.02.2018 from 21:00 to 23:00 (Asia/Muscat)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be on 02/21/2018 from 21:00 to 23:00 (Asia/Muscat)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu le 21/02/2018 de 21:00 à 23:00 (Asia/Muscat)."));
  }

  @Test
  public void durationReminderOnSeveralDaysEventOf2HoursShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder(setupSeveralDaysEventOn2Hours(ZoneId.systemDefault()))
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 23:00 - 22.02.2018 01:00"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 23:00 - 02/22/2018 01:00"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 23:00 - 22/02/2018 01:00"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 at 23:00 to 22.02.2018 at 01:00."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 at 23:00 to 02/22/2018 at 01:00."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 à 23:00 au 22/02/2018 à 01:00."));
  }

  @Test
  public void durationReminderWithAnotherBeforeUserZoneIdOnSeveralDaysEventOf2HoursShouldWork()
      throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("America/Cancun"));
    final DurationReminder durationReminder = initReminderBuilder(setupSeveralDaysEventOn2Hours(ZoneId.systemDefault()))
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 23:00 - 22.02.2018 01:00 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 23:00 - 02/22/2018 01:00 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 23:00 - 22/02/2018 01:00 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 at 23:00 to 22.02.2018 at 01:00 (UTC)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 at 23:00 to 02/22/2018 at 01:00 (UTC)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 à 23:00 au 22/02/2018 à 01:00 (UTC)."));
  }

  @Test
  public void durationReminderWithAnotherAfterUserZoneIdOnSeveralDaysEventOf2HoursShouldWork()
      throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(setupSeveralDaysEventOn2Hours(ZoneId.systemDefault()))
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 23:00 - 22.02.2018 01:00 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 23:00 - 02/22/2018 01:00 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 23:00 - 22/02/2018 01:00 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 at 23:00 to 22.02.2018 at 01:00 (UTC)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 at 23:00 to 02/22/2018 at 01:00 (UTC)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 à 23:00 au 22/02/2018 à 01:00 (UTC)."));
  }

  @Test
  public void durationReminderWithAnotherBeforeCalendarZoneIdOnSeveralDaysEventOf2HoursShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSeveralDaysEventOn2Hours(ZoneId.of("America/Cancun"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 23:00 - 22.02.2018 01:00 (America/Cancun)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 23:00 - 02/22/2018 01:00 (America/Cancun)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 23:00 - 22/02/2018 01:00 (America/Cancun)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 at 23:00 to 22.02.2018 at 01:00 (America/Cancun)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 at 23:00 to 02/22/2018 at 01:00 (America/Cancun)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 à 23:00 au 22/02/2018 à 01:00 (America/Cancun)."));
  }

  @Test
  public void durationReminderWithAnotherAfterCalendarZoneIdOnSeveralDaysEventOf2HoursShouldWork() throws Exception {
    final CalendarEvent calendarEvent = setupSeveralDaysEventOn2Hours(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder(calendarEvent)
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder);
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the event super test - 21.02.2018 23:00 - 22.02.2018 01:00 (Asia/Muscat)"));
    assertThat(titles.get(EN), is("Reminder about the event super test - 02/21/2018 23:00 - 02/22/2018 01:00 (Asia/Muscat)"));
    assertThat(titles.get(FR), is("Rappel sur l'événement super test - 21/02/2018 23:00 - 22/02/2018 01:00 (Asia/Muscat)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("REMINDER: The event <b>super test</b> will be from 21.02.2018 at 23:00 to 22.02.2018 at 01:00 (Asia/Muscat)."));
    assertThat(contents.get(EN), is("REMINDER: The event <b>super test</b> will be from 02/21/2018 at 23:00 to 02/22/2018 at 01:00 (Asia/Muscat)."));
    assertThat(contents.get(FR), is("RAPPEL : L'événement <b>super test</b> aura lieu du 21/02/2018 à 23:00 au 22/02/2018 à 01:00 (Asia/Muscat)."));
  }

  private Contribution setupSimpleEventOnAllDay() {
    return commonSetup(Period.between(DATE_0F_21, DATE_0F_21.plusDays(1)));
  }

  private CalendarEvent setupSeveralDaysEventOnAllDay() {
    return commonSetup(Period.between(DATE_0F_21, DATE_0F_22.plusDays(1)));
  }

  private CalendarEvent setupSimpleEventOn2Hours(ZoneId zoneId) {
    CalendarEvent calendarEvent = commonSetup(Period.between(
        DATE_0F_21_AT_21H.toZonedDateTime().withZoneSameLocal(zoneId).toOffsetDateTime(),
        DATE_0F_21_AT_23H.toZonedDateTime().withZoneSameLocal(zoneId).toOffsetDateTime()));
    when(calendarEvent.getCalendar().getZoneId()).thenReturn(zoneId);
    return calendarEvent;
  }

  private CalendarEvent setupSeveralDaysEventOn2Hours(ZoneId zoneId) {
    CalendarEvent calendarEvent =  commonSetup(Period.between(
        DATE_0F_21_AT_23H.toZonedDateTime().withZoneSameLocal(zoneId).toOffsetDateTime(),
        DATE_0F_22_AT_01H.toZonedDateTime().withZoneSameLocal(zoneId).toOffsetDateTime()));
    when(calendarEvent.getCalendar().getZoneId()).thenReturn(zoneId);
    return calendarEvent;
  }

  private CalendarEvent commonSetup(final Period period) {
    currentPeriod = period;
    final Calendar calendar = CalendarMockBuilder.from(INSTANCE_ID).withId("calendarUuid")
        .atZoneId(UTC_ZONE_ID).build();
    final CalendarEvent event = CalendarEventStubBuilder
        .from(period)
        .plannedOn(calendar)
        .withId(LOCAL_ID)
        .withTitle("super test")
        .build();
    final CalendarEventOccurrence occurrence = CalendarEventOccurrenceBuilder.forEvent(event)
        .startingAt(event.getStartDate())
        .endingAt(event.getEndDate())
        .build();
    when(contributionManager.getById(event.getContributionId()))
        .thenReturn(Optional.of(event));
    when(componentInstanceRoutingMap.getPermalink(occurrence.getContributionId()))
        .thenReturn(UriBuilder.fromPath("").build());
    when(calendarEventOccurrenceRepository.getById(occurrence.getId())).thenReturn(occurrence);
    return event;
  }

  private Reminder.ReminderBuilder initReminderBuilder(Contribution contribution) {
    return new Reminder.ReminderBuilder().about(contribution.getContributionId())
        .withText("Dummy test").forUser(receiver);
  }

  private void triggerDateTime(DurationReminder reminder)
      throws IllegalAccessException {
    Optional<Contribution> contribution = contributionManager.getById(reminder.getContributionId());
    if (contribution.isPresent()) {
      final CalendarEvent event = (CalendarEvent) contribution.get();
      final OffsetDateTime occStartDate;
      if (event.isOnAllDay()) {
        occStartDate = ((LocalDate) currentPeriod.getStartDate())
            .atStartOfDay(event.getCalendar().getZoneId()).toOffsetDateTime();
      } else {
        occStartDate = (OffsetDateTime) currentPeriod.getStartDate();
      }
      final OffsetDateTime finalDateTime = occStartDate
          .minus(reminder.getDuration(), reminder.getTimeUnit().toChronoUnit())
          .toZonedDateTime()
          .withZoneSameInstant(ZoneId.systemDefault())
          .toOffsetDateTime();
      FieldUtils.writeField(reminder, "triggerDateTime", finalDateTime, true);
    }
  }

  private Map<String, String> computeNotificationContents(Reminder reminder) {
    final UserNotification userNotification = new CalendarEventUserNotificationReminder
        .UserNotification(reminder).build();
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getContent(userNotification, FR));
    result.put(EN, getContent(userNotification, EN));
    result.put(DE, getContent(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private Map<String, String> computeNotificationTitles(Reminder reminder) {
    final UserNotification userNotification = new CalendarEventUserNotificationReminder
        .UserNotification(reminder).build();
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getTitle(userNotification, FR));
    result.put(EN, getTitle(userNotification, EN));
    result.put(DE, getTitle(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private String getContent(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getContent(language)
        .replaceAll("<!--BEFORE_MESSAGE_FOOTER--><!--AFTER_MESSAGE_FOOTER-->", "");
  }

  private String getTitle(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getTitle(language);
  }

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(UTC_ZONE_ID));
  }
}